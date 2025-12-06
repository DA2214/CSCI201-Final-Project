// Test Case 1
import org.junit.jupiter.api.Test;

public class ReservationCancelTest extends BaseServletTestSupport {

    @Test
    void cancelReservation_invalidId_returnsNotFoundNoException() throws Exception {
        ReservationServlet servlet = new ReservationServlet();

        when(request.getPathInfo()).thenReturn("/cancel");
        when(request.getParameter("reservationId")).thenReturn("999999"); // non-existing

        assertDoesNotThrow(() -> servlet.doPost(request, response));

        String json = responseWriter.toString();
        assertTrue(json.contains("Reservation not found"));
        assertTrue(json.contains("\"success\":false") || json.contains("\"success\": false"));
    }
}
