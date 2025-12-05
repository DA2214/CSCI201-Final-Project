import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/UsernameExistsServlet")
public class UsernameExistsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        if (username == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Username is required");
        }
        DatabaseAccessor.getLock().lock();
        try {
            resp.setStatus(DatabaseAccessor.CheckUserExists(username) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND);
        } finally {
            DatabaseAccessor.getLock().unlock();
        }
    }
}
