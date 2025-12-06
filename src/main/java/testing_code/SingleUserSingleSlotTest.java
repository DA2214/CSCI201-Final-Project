// Test Case 11
public class SingleUserSingleSlotTest extends BaseServletTestSupport {

    @Test
    void userCannotReserveTwoMachinesSameTime() throws Exception {
        ReservationServlet servlet = new ReservationServlet();

        String start = "2025-01-01T09:00:00";
        String end   = "2025-01-01T10:00:00";

        // first reservation should succeed
        HttpServletRequest r1 = mock(HttpServletRequest.class);
        HttpServletResponse s1 = mock(HttpServletResponse.class);
        StringWriter w1 = new StringWriter();
        when(s1.getWriter()).thenReturn(new PrintWriter(w1));
        when(r1.getPathInfo()).thenReturn("/reserve");
        when(r1.getParameter("userId")).thenReturn("10");
        when(r1.getParameter("machineId")).thenReturn("1");
        when(r1.getParameter("startTime")).thenReturn(start);
        when(r1.getParameter("endTime")).thenReturn(end);
        servlet.doPost(r1, s1);

        // second reservation same user/time different machine
        HttpServletRequest r2 = mock(HttpServletRequest.class);
        HttpServletResponse s2 = mock(HttpServletResponse.class);
        StringWriter w2 = new StringWriter();
        when(s2.getWriter()).thenReturn(new PrintWriter(w2));
        when(r2.getPathInfo()).thenReturn("/reserve");
        when(r2.getParameter("userId")).thenReturn("10");
        when(r2.getParameter("machineId")).thenReturn("2");
        when(r2.getParameter("startTime")).thenReturn(start);
        when(r2.getParameter("endTime")).thenReturn(end);
        servlet.doPost(r2, s2);

        assertTrue(w1.toString().contains("\"success\":true"));
        assertTrue(w2.toString().contains("\"success\":false") 
                   || w2.toString().contains("already has a reservation"));
    }
}
