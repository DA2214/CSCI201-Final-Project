import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import com.google.gson.Gson;

@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("username") == null) {
            out.print("[]");
            return;
        }

        String username = (String) session.getAttribute("username");

        ArrayList<Notification> notes = NotificationDAO.getNotifications(username);

        Gson gson = new Gson();
        out.print(gson.toJson(notes));
    }
}
