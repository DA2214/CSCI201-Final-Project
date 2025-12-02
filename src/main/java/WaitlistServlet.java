
import java.io.IOException;
import java.sql.*;
import java.util.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@WebServlet("/WaitlistServlet/*")
public class WaitlistServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/TrojanTracker"; // edit for person who uses mySQLWorkbench
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASS = "root";

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    private Connection getConn() throws SQLException {
        return DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
    }


    // JSON RESPONSE OBJECT
    class JsonResponse {
        boolean success;
        String message;

        JsonResponse(boolean s, String m) {
            success = s;
            message = m;
        }
    }

    // POST HANDLER (join / claim / decline)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo(); // e.g. "/join"
        String body = req.getReader().lines().reduce("", (a, b) -> a + b);
        Map<String, String> data = gson.fromJson(body, Map.class);

        try {
            if (path == null) {
                send(resp, false, "No endpoint is given.");
                return;
            }
          
            switch (path) {
                case "/join": // join waitlist
                    handleJoin(resp, data);
                    break;

                case "/claim": // claim rservation
                    handleClaim(resp, data);
                    break;

                case "/decline": // decline reservatioon 
                    handleDecline(resp, data);
                    break;

                default:
                    send(resp, false, "Invalid waitlist endpoint: " + path);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            send(resp, false, "Database error: " + e.getMessage());
        }
    }

    
    // 1. JOIN WAITLIST
    private void handleJoin(HttpServletResponse resp, Map<String, String> data)
            throws SQLException, IOException {

        int userID = Integer.parseInt(data.get("userID"));
        String machineID = data.get("machineID");

        try (Connection conn = getConn()) {

            PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO waitlist (userID, machineID, notified) VALUES (?, ?, 0)"
            );
            ps.setInt(1, userID);
            ps.setString(2, machineID);
            ps.executeUpdate();

            send(resp, true, "User added to waitlist.");
        }
    }

    // 2. CLAIM  (user accepts)
    private void handleClaim(HttpServletResponse resp, Map<String, String> data)
            throws SQLException, IOException {

        int userID = Integer.parseInt(data.get("userID"));
        String machineID = data.get("machineID");

        try (Connection conn = getConn()) {

            PreparedStatement del = conn.prepareStatement(
                "DELETE FROM waitlist " +
                "WHERE userID = ? AND machineID = ? " +
                "LIMIT 1"
            );
            del.setInt(1, userID);
            del.setString(2, machineID);
            del.executeUpdate();

            send(resp, true, "Slot successfully claimed.");
        }
    }

    
    // 3. DECLINE  → Notifies next user in line
    private void handleDecline(HttpServletResponse resp, Map<String, String> data)
            throws SQLException, IOException {

        int userID = Integer.parseInt(data.get("userID"));
        String machineID = data.get("machineID");

        try (Connection conn = getConn()) {

            // Remove declining user
            PreparedStatement del = conn.prepareStatement(
                "DELETE FROM waitlist WHERE userID = ? AND machineID = ? LIMIT 1"
            );
            del.setInt(1, userID);
            del.setString(2, machineID);
            del.executeUpdate();

            // Get next in line
            PreparedStatement next = conn.prepareStatement(
                "SELECT userID FROM waitlist " +
                "WHERE machineID = ? AND notified = 0 " +
                "ORDER BY waitID ASC LIMIT 1"
            );
            next.setString(1, machineID);
            ResultSet rs = next.executeQuery();

            if (!rs.next()) {
                send(resp, true, "User declined. No one else is waiting.");
                return;
            }

            int nextUserID = rs.getInt("userID");

            // Insert notification
            PreparedStatement notify = conn.prepareStatement(
                "INSERT INTO notifications (userID, message) VALUES (?, ?)"
            );
            notify.setInt(1, nextUserID);
            notify.setString(2, "A slot has opened for machine: " + machineID);
            notify.executeUpdate();

            // Mark notified
            PreparedStatement update = conn.prepareStatement(
                "UPDATE waitlist SET notified = 1 WHERE userID = ? AND machineID = ?"
            );
            update.setInt(1, nextUserID);
            update.setString(2, machineID);
            update.executeUpdate();

            send(resp, true, "User declined. Next user has been notified.");
        }
    }

    
    // SEND JSON RESPONSE
    private void send(HttpServletResponse resp, boolean success, String msg)
            throws IOException {

        JsonResponse r = new JsonResponse(success, msg);
        resp.getWriter().write(gson.toJson(r));
    }
}


