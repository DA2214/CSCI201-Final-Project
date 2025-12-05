import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/EmailExistsServlet")
public class EmailExistsServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String email = req.getParameter("email");
        if (email == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email is required");
        }
        DatabaseAccessor.getLock().lock();
        try {
            resp.setStatus(DatabaseAccessor.CheckEmailExists(email) ? HttpServletResponse.SC_OK : HttpServletResponse.SC_NOT_FOUND);
        } finally {
            DatabaseAccessor.getLock().unlock();
        }
    }
}
