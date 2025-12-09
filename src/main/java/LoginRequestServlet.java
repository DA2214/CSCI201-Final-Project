import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet("/LoginRequestServlet")
public class LoginRequestServlet extends HttpServlet {

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private static class UserIdResponse {
        private int userId;

        private UserIdResponse(int userId) {
            this.userId = userId;
        }
    }


    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        if (!FieldValidationUtil.IsUsernameValid(username)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid username");
        }

        DatabaseAccessor.getLock().lock();
        try {
            int userId = DatabaseAccessor.LoginUser(username, password);
            if (userId != -1) {
            	
            	// ADDED: Create session
                HttpSession session = req.getSession(true);

                // ADDED: Store username for NotificationServlet
                session.setAttribute("username", username);

                // ADDED: Store userId for reservation actions
                session.setAttribute("userId", userId);
            	
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                resp.setContentType("application/json");

                resp.getWriter().print(gson.toJson(new UserIdResponse(userId)));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } finally {
            DatabaseAccessor.getLock().unlock();
        }
    }
}
