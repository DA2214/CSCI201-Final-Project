
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

    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();


    // JSON response object
    class JsonResponse {
        boolean success;
        String message;

        JsonResponse(boolean s, String m) {
            success = s;
            message = m;
        }
    }

    // Handle GET requests - Get user's active waitlists
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String userIdParam = req.getParameter("userID");
        if (userIdParam == null || userIdParam.isEmpty()) {
            // Return empty array instead of error response for consistency
            resp.getWriter().write(gson.toJson(new ArrayList<WaitlistEntry>()));
            return;
        }

        try {
            int userID = Integer.parseInt(userIdParam);
            List<WaitlistEntry> waitlists = getUserWaitlists(userID);
            resp.getWriter().write(gson.toJson(waitlists));
        } catch (NumberFormatException e) {
            // Return empty array on invalid format
            resp.getWriter().write(gson.toJson(new ArrayList<WaitlistEntry>()));
        } catch (SQLException e) {
            e.printStackTrace();
            // Return empty array on database error
            resp.getWriter().write(gson.toJson(new ArrayList<WaitlistEntry>()));
        }
    }

    // Get all waitlist entries for a user
    private List<WaitlistEntry> getUserWaitlists(int userID) throws SQLException {
        DatabaseAccessor.getLock().lock();
        try {
            Connection conn = DatabaseAccessor.GetDatabaseConnection();
            
            String waitlistQuery = "SELECT machineID FROM waitlist WHERE userID = ?";
            List<WaitlistEntry> entries = new ArrayList<>();
            
            try (PreparedStatement waitlistPs = conn.prepareStatement(waitlistQuery)) {
                waitlistPs.setInt(1, userID);
                try (ResultSet waitlistRs = waitlistPs.executeQuery()) {
                    while (waitlistRs.next()) {
                        String machineName = waitlistRs.getString("machineID");
                        
                        String machineQuery = "SELECT machineId, name FROM Machines WHERE name = ?";
                        try (PreparedStatement machinePs = conn.prepareStatement(machineQuery)) {
                            machinePs.setString(1, machineName);
                            try (ResultSet machineRs = machinePs.executeQuery()) {
                                if (machineRs.next()) {
                                    WaitlistEntry entry = new WaitlistEntry();
                                    entry.machineId = machineRs.getInt("machineId");
                                    entry.machineName = machineRs.getString("name");
                                    entries.add(entry);
                                }
                            }
                        }
                    }
                }
            }
            
            return entries;
        } finally {
            DatabaseAccessor.getLock().unlock();
        }
    }

    // Handle POST requests
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String path = req.getPathInfo();
        String body = req.getReader().lines().reduce("", (a, b) -> a + b);
        Map<String, Object> data = gson.fromJson(body, Map.class);

        try {
            if (path == null) {
                send(resp, false, "No endpoint provided.");
                return;
            }

            switch (path) {
                case "/join":
                    handleJoin(resp, data);
                    break;
                case "/claim":
                    handleClaim(resp, data);
                    break;
                case "/decline":
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

    // JOIN waitlist
    private void handleJoin(HttpServletResponse resp, Map<String, Object> data)
            throws SQLException, IOException {

        int userID = getIntValue(data, "userID");

        DatabaseAccessor.getLock().lock();
        try {
            Connection conn = DatabaseAccessor.GetDatabaseConnection();
            
            String machineID = getMachineName(data, "machineID", conn);

            PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO waitlist (userID, machineID, notified) VALUES (?, ?, 0)"
            );
            ps.setInt(1, userID);
            ps.setString(2, machineID);
            ps.executeUpdate();
            
         // notify user they joined a waitlist
            NotificationDAO.createNotification(
                userID,
                "You joined the waitlist for machine " + machineID + "."
            );

            send(resp, true, "User added to waitlist.");
        } finally {
            DatabaseAccessor.getLock().unlock();
        }
    }

    // CLAIM reservation
    private void handleClaim(HttpServletResponse resp, Map<String, Object> data)
            throws SQLException, IOException {

        int userID = getIntValue(data, "userID");
        
        DatabaseAccessor.getLock().lock();
        try {
            Connection conn = DatabaseAccessor.GetDatabaseConnection();
            String machineID = getMachineName(data, "machineID", conn);

            PreparedStatement del = conn.prepareStatement(
                "DELETE FROM waitlist WHERE userID = ? AND machineID = ? LIMIT 1"
            );
            del.setInt(1, userID);
            del.setString(2, machineID);
            del.executeUpdate();
            
         // Notification on successful claim
            NotificationDAO.createNotification(
                userID,
                "You have successfully claimed an open spot for machine " + machineID + "."
            );

            send(resp, true, "Slot successfully claimed.");
        } finally {
            DatabaseAccessor.getLock().unlock();
        }
    }

    // DECLINE -> notify next user
    private void handleDecline(HttpServletResponse resp, Map<String, Object> data)
            throws SQLException, IOException {

        int userID = getIntValue(data, "userID");
        
        DatabaseAccessor.getLock().lock();
        try {
            Connection conn = DatabaseAccessor.GetDatabaseConnection();
            String machineID = getMachineName(data, "machineID", conn);

            // Remove declining user
            PreparedStatement del = conn.prepareStatement(
                "DELETE FROM waitlist WHERE userID = ? AND machineID = ? LIMIT 1"
            );
            del.setInt(1, userID);
            del.setString(2, machineID);
            del.executeUpdate();

            // Get next user
            PreparedStatement next = conn.prepareStatement(
                "SELECT userID FROM waitlist WHERE machineID = ? AND notified = 0 ORDER BY waitID ASC LIMIT 1"
            );
            next.setString(1, machineID);
            ResultSet rs = next.executeQuery();

            if (!rs.next()) {
                send(resp, true, "User declined. No one else is waiting.");
                return;
            }

            int nextUserID = rs.getInt("userID");

            // Create notification
            // Create notification for next user
            NotificationDAO.createNotification(
                nextUserID,
                "A spot has opened on machine " + machineID + "! You may reserve it now."
            );

            // Mark notified
            PreparedStatement update = conn.prepareStatement(
                "UPDATE waitlist SET notified = 1 WHERE userID = ? AND machineID = ?"
            );
            update.setInt(1, nextUserID);
            update.setString(2, machineID);
            update.executeUpdate();

            send(resp, true, "User declined. Next user has been notified.");
        } finally {
            DatabaseAccessor.getLock().unlock();
        }
    }

    // Helper methods to safely extract values from Map<String, Object>
    private int getIntValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required parameter: " + key);
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value instanceof String) {
            return Integer.parseInt((String) value);
        }
        throw new IllegalArgumentException("Invalid type for " + key + ": expected number or string");
    }

    private String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required parameter: " + key);
        }
        return value.toString();
    }

    // Get machine name from machineID (handles both integer machineId and string machine name)
    private String getMachineName(Map<String, Object> data, String key, Connection conn) throws SQLException {
        Object value = data.get(key);
        if (value == null) {
            throw new IllegalArgumentException("Missing required parameter: " + key);
        }
        
        if (value instanceof String) {
            return (String) value;
        }
        
        if (value instanceof Number) {
            int machineId = ((Number) value).intValue();
            String query = "SELECT name FROM Machines WHERE machineId = ?";
            try (PreparedStatement ps = conn.prepareStatement(query)) {
                ps.setInt(1, machineId);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("name");
                    } else {
                        throw new IllegalArgumentException("Machine not found: " + machineId);
                    }
                }
            }
        }
        
        return value.toString();
    }

    // Response class for waitlist entries
    class WaitlistEntry {
        int machineId;
        String machineName;
    }

    // Send JSON response
    private void send(HttpServletResponse resp, boolean success, String msg)
            throws IOException {
        JsonResponse r = new JsonResponse(success, msg);
        resp.getWriter().write(gson.toJson(r));
    }
}
