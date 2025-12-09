import java.sql.*;
import java.util.ArrayList;

public class NotificationDAO {

    // helper: username -> uid
    private static Integer getUserIDFromUsername(String username) {
        String sql = "SELECT uid FROM users WHERE username = ?";

        try (Connection conn = DatabaseAccessor.GetDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("uid");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; // not found
    }

    // create a notification for a given username
    public static void createNotification(int userID, String message) {
        System.out.println("DEBUG: Creating notification for userID=" + userID + ", message=" + message);

        String sql = "INSERT INTO notifications (userID, message) VALUES (?, ?)";

        try {
            Connection conn = DatabaseAccessor.GetDatabaseConnection();
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setInt(1, userID);
                stmt.setString(2, message);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // fetch all notifications for a username, newest first
    public static ArrayList<Notification> getNotifications(String username) {
        ArrayList<Notification> list = new ArrayList<>();

        Integer userID = getUserIDFromUsername(username);
        if (userID == null) return list;

        String sql = "SELECT * FROM notifications WHERE userID = ? ORDER BY createdAt DESC";

        try (Connection conn = DatabaseAccessor.GetDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, userID);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                list.add(new Notification(
                    rs.getInt("notifyID"),
                    rs.getInt("userID"),
                    rs.getString("message"),
                    rs.getString("createdAt"),
                    rs.getInt("readStatus")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return list;
    }

    // mark one notification as read
    public static void markAsRead(int notifyID) {
        String sql = "UPDATE notifications SET readStatus = 1 WHERE notifyID = ?";

        try (Connection conn = DatabaseAccessor.GetDatabaseConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, notifyID);
            stmt.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
