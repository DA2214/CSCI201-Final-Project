import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/markNotificationRead")
public class NotificationReadServlet extends HttpServlet {

    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String idParam = request.getParameter("notifyID");

        if (idParam != null) {
            try {
                int notifyID = Integer.parseInt(idParam);
                NotificationDAO.markAsRead(notifyID);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
