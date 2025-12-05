import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/AccountRegistrationServlet")
public class AccountRegistrationServlet extends HttpServlet {

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
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        if (!FieldValidationUtil.IsUsernameValid(username)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid username");
        }
        if (!FieldValidationUtil.IsEmailValid(email)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid email");
        }

        DatabaseAccessor.getLock().lock();
        try {
            if (!DatabaseAccessor.CheckEmailExists(email) && !DatabaseAccessor.CheckUserExists(username)) {
                int userID = DatabaseAccessor.RegisterUser(username, email, password);

                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                resp.setContentType("application/json");
                resp.getWriter().print(gson.toJson(new UserIdResponse(userID)));
            } else {
                resp.setStatus(HttpServletResponse.SC_NOT_ACCEPTABLE);
            }
        } finally {
            DatabaseAccessor.getLock().unlock();
        }
    }
}
