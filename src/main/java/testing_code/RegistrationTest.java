// Test Case 8
public class RegistrationTest extends BaseServletTestSupport {

    private RegisterServlet servlet;

    @BeforeEach
    void setupRegistration() throws Exception {
        super.baseSetUp();
        servlet = new RegisterServlet();
    }

    @Test
    void registerNewUser_isPersisted() throws Exception {
        when(request.getParameter("username")).thenReturn("newuser1");
        when(request.getParameter("password")).thenReturn("password1");

        servlet.doPost(request, response);

        Connection conn = DatabaseAccessor.GetDatabaseConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT COUNT(*) FROM Users WHERE username = ?")) {
            ps.setString(1, "newuser1");
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void registerDuplicateUser_reportsError() throws Exception {
        Connection conn = DatabaseAccessor.GetDatabaseConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Users(username, passwordHash) VALUES (?, ?)")) {
            ps.setString(1, "dupe");
            ps.setString(2, "x");
            ps.executeUpdate();
        }

        when(request.getParameter("username")).thenReturn("dupe");
        when(request.getParameter("password")).thenReturn("something");

        servlet.doPost(request, response);
        String json = responseWriter.toString();
        assertTrue(json.contains("already exists"));
    }
}
