// Test Case 5
public class WorkoutLifecycleTest extends BaseServletTestSupport {

    private ReservationServlet servlet;

    @BeforeEach
    void setupWorkout() throws Exception {
        super.baseSetUp();
        servlet = new ReservationServlet();

        Connection conn = DatabaseAccessor.GetDatabaseConnection();
        // Insert a machine and reservation
        try (PreparedStatement m = conn.prepareStatement(
                "INSERT INTO Machines(machineId, name, type, status) VALUES (1, 'Treadmill #1', 'Cardio', 'AVAILABLE')")) {
            m.executeUpdate();
        }
        try (PreparedStatement r = conn.prepareStatement(
                "INSERT INTO Reservations(reservationId, userId, machineId, status) VALUES (100, 1, 1, 'CONFIRMED')")) {
            r.executeUpdate();
        }
    }

    @Test
    void startThenEndWorkout_createsUsageAndResetsStatus() throws Exception {
        // Start workout
        when(request.getPathInfo()).thenReturn("/startWorkout");
        when(request.getParameter("reservationId")).thenReturn("100");
        servlet.doPost(request, response);

        // Verify DB flags
        Connection conn = DatabaseAccessor.GetDatabaseConnection();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT status FROM Machines WHERE machineId = 1")) {
            ResultSet rs = ps.executeQuery();
            assertTrue(rs.next());
            assertEquals("IN_USE", rs.getString(1));
        }

        // End workout
        baseSetUp(); // reset mocks
        when(request.getPathInfo()).thenReturn("/endWorkout");
        when(request.getParameter("reservationId")).thenReturn("100");
        servlet.doPost(request, response);

        // Machine status back to AVAILABLE and usage row exists
        try (PreparedStatement ms = conn.prepareStatement(
                "SELECT status FROM Machines WHERE machineId = 1")) {
            ResultSet rs = ms.executeQuery();
            assertTrue(rs.next());
            assertEquals("AVAILABLE", rs.getString(1));
        }
        try (PreparedStatement us = conn.prepareStatement(
                "SELECT COUNT(*) FROM Usage WHERE reservationId = 100")) {
            ResultSet rs = us.executeQuery();
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void endWorkoutWithoutStart_returnsNoActiveSession() throws Exception {
        baseSetUp();
        when(request.getPathInfo()).thenReturn("/endWorkout");
        when(request.getParameter("reservationId")).thenReturn("999");

        servlet.doPost(request, response);
        String json = responseWriter.toString();
        assertTrue(json.contains("no active session"));
    }
}
