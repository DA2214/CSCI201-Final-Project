// Test Case 10
import com.google.gson.*;

public class WaitlistPromotionTest extends BaseServletTestSupport {

    private WaitlistServlet servlet;
    private Gson gson = new Gson();

    @BeforeEach
    void setupWaitlist() throws Exception {
        super.baseSetUp();
        servlet = new WaitlistServlet();

        Connection conn = DatabaseAccessor.GetDatabaseConnection();
        // clear tables
        conn.createStatement().executeUpdate("DELETE FROM waitlist");
        conn.createStatement().executeUpdate("DELETE FROM notifications");
    }

    private void join(int userId, int machineId) throws Exception {
        HttpServletRequest r = mock(HttpServletRequest.class);
        HttpServletResponse s = mock(HttpServletResponse.class);
        StringWriter w = new StringWriter();
        when(s.getWriter()).thenReturn(new PrintWriter(w));
        when(r.getPathInfo()).thenReturn("/join");

        String body = "{\"userID\":" + userId + ",\"machineID\":" + machineId + "}";
        BufferedReader br = new BufferedReader(new StringReader(body));
        when(r.getReader()).thenReturn(br);

        servlet.doPost(r, s);
    }

    @Test
    void declinePromotesNextUserAndCreatesNotification() throws Exception {
        int machineId = 1;
        join(1, machineId); // A
        join(2, machineId); // B
        join(3, machineId); // C

        // Simulate user A decline -> should notify B
        HttpServletRequest r = mock(HttpServletRequest.class);
        HttpServletResponse s = mock(HttpServletResponse.class);
        StringWriter w = new StringWriter();
        when(s.getWriter()).thenReturn(new PrintWriter(w));
        when(r.getPathInfo()).thenReturn("/decline");

        String body = "{\"userID\":1,\"machineID\":" + machineId + "}";
        when(r.getReader()).thenReturn(new BufferedReader(new StringReader(body)));

        servlet.doPost(r, s);

        Connection conn = DatabaseAccessor.GetDatabaseConnection();
        // Exactly one notification for user 2
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT userID, message FROM notifications")) {
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("userID"));
            assertTrue(rs.getString("message").contains("machine"));
            assertFalse(rs.next());
        }

        // B now has notified = 1
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT userID, notified FROM waitlist WHERE machineID = ? ORDER BY waitID ASC")) {
            ps.setString(1, "Treadmill #1"); // if machineID stored as name, adjust accordingly
            ResultSet rs = ps.executeQuery();
            // A removed, B and C remain
            assertTrue(rs.next());
            assertEquals(2, rs.getInt("userID"));
            assertEquals(1, rs.getInt("notified"));
        }
    }
}
