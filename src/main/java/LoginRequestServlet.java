import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet("/LoginRequestServlet")
public class LoginRequestServlet extends HttpServlet {
    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");

        DatabaseAccessor.getLock().lock();
        try {
            if (DatabaseAccessor.LoginUser(username, password)) {
                resp.setStatus(HttpServletResponse.SC_OK);
                resp.setContentType("application/json");

                // TODO write json response
            } else {
                resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        } finally {
            DatabaseAccessor.getLock().unlock();
        }
    }
}
