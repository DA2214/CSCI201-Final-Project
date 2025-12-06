// Test Case 4
import java.util.concurrent.*;

public class ReserveMachineConcurrencyTest extends BaseServletTestSupport {

    @Test
    void reserveMachine_onlyOneSucceeds() throws Exception {
        ReservationServlet servlet = new ReservationServlet();

        ExecutorService executor = Executors.newFixedThreadPool(2);
        Callable<String> task = () -> {
            HttpServletRequest req = mock(HttpServletRequest.class);
            HttpServletResponse resp = mock(HttpServletResponse.class);
            StringWriter w = new StringWriter();
            when(resp.getWriter()).thenReturn(new PrintWriter(w));
            when(req.getPathInfo()).thenReturn("/reserve");
            when(req.getParameter("machineId")).thenReturn("1");
            when(req.getParameter("startTime")).thenReturn("2025-01-01T10:00:00");
            when(req.getParameter("endTime")).thenReturn("2025-01-01T11:00:00");
            servlet.doPost(req, resp);
            return w.toString();
        };

        Future<String> f1 = executor.submit(task);
        Future<String> f2 = executor.submit(task);

        String r1 = f1.get();
        String r2 = f2.get();

        int successCount = 0;
        if (r1.contains("\"success\":true")) successCount++;
        if (r2.contains("\"success\":true")) successCount++;

        assertEquals(1, successCount);
        executor.shutdown();
    }
}
