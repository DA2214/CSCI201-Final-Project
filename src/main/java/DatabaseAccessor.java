

import java.sql.*;

public class DatabaseAccessor {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/TrojanTracker";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASS = "root";

    private static Connection connection = null;
    private static PreparedStatement getUserFromUsernameStatement = null;
    private static PreparedStatement getUserFromEmailStatement = null;
    private static PreparedStatement insertUserStatement = null;

    static {
        try {
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);

            getUserFromUsernameStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            getUserFromEmailStatement = connection.prepareStatement("SELECT * FROM users WHERE email = ?");
            insertUserStatement = connection.prepareStatement("INSERT INTO users (username, email, password) VALUES (?, ?, ?)");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not initialize database connection, please check server: " + e.getMessage());
        }
    }

    public static Connection GetDatabaseConnection() throws SQLException {
        if (connection != null) {
            return connection;
        } else {
            throw new SQLException("Could not initialize database connection, please check server");
        }
    }

    public static boolean LoginUser(String username, String password) {
        if (username == null || password == null) { return false; }

        try {
            getUserFromUsernameStatement.setString(1, username);
            ResultSet rs = getUserFromUsernameStatement.executeQuery();
            if (rs.next() && rs.getString("password").equals(password)) {
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean CheckUserExists(String username) {
        try {
            getUserFromUsernameStatement.setString(1, username);
            ResultSet rs = getUserFromUsernameStatement.executeQuery();
            return rs.next();
        } catch (SQLException e) {}
        return false;
    }

    public static boolean CheckEmailExists(String email) {
        try {
            getUserFromEmailStatement.setString(1, email);
            ResultSet rs = getUserFromEmailStatement.executeQuery();
            return rs.next();
        } catch (SQLException e) {}
        return false;
    }

    public static boolean RegisterUser(String username, String password, String email) {
        if (CheckUserExists(username) || CheckEmailExists(email)) return false;

        try {
            insertUserStatement.setString(1, username);
            insertUserStatement.setString(2, email);
            insertUserStatement.setString(3, password);
            insertUserStatement.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }
}
