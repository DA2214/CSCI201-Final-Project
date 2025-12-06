// Test Case 2
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.*;

public class NotifyUserTest {

    @Test
    void notifyUser_nullOrEmptyMessage_isRejected() throws Exception {
        NotificationDAO dao = mock(NotificationDAO.class);
        SafeNotificationService service = new SafeNotificationService(dao);

        assertFalse(service.notifyUser(1, null));
        assertFalse(service.notifyUser(1, ""));
        assertFalse(service.notifyUser(1, "   "));

        verify(dao, never()).addNotification(any());
    }

    @Test
    void notifyUser_validMessage_isStored() throws Exception {
        NotificationDAO dao = mock(NotificationDAO.class);
        SafeNotificationService service = new SafeNotificationService(dao);

        boolean result = service.notifyUser(5, "Workout starting soon");

        assertTrue(result);
        verify(dao, times(1)).addNotification(any(Notification.class));
    }
}
