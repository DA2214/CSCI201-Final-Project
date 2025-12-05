

import java.sql.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DatabaseAccessor {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/TrojanTracker";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASS = "root";

    private static Connection connection = null;
    private static PreparedStatement getUserFromUsernameStatement = null;
    private static PreparedStatement getUserFromEmailStatement = null;
    private static PreparedStatement insertUserStatement = null;

    private static final ReentrantLock lock = new ReentrantLock();

    static {
        InitializeConnection();
    }

    public static Connection GetDatabaseConnection() throws SQLException {
        CheckLock();
        if (connection != null && !connection.isClosed()) {
            return connection;
        } else {
            InitializeConnection();
            if (connection != null && !connection.isClosed()) return connection;
            throw new SQLException("Could not initialize database connection, please check server");
        }
    }

    public static void InitializeConnection() {
        try {
            // remove old connection and statements first if they exist
            if (connection != null && connection.isClosed()) {
                if (getUserFromUsernameStatement != null) {
                    getUserFromUsernameStatement.close();
                }
                if (getUserFromEmailStatement != null) {
                    getUserFromEmailStatement.close();
                }
                if (insertUserStatement != null) {
                    insertUserStatement.close();
                }
            }

            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);

            getUserFromUsernameStatement = connection.prepareStatement("SELECT * FROM users WHERE username = ?");
            getUserFromEmailStatement = connection.prepareStatement("SELECT * FROM users WHERE email = ?");
            insertUserStatement = connection.prepareStatement("INSERT INTO users (username, email, password) VALUES (?, ?, ?)");
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Could not initialize database connection, please check server: " + e.getMessage());
        }
    }

    public static boolean LoginUser(String username, String password) {
        CheckLock();
        if (username == null || password == null) { return false; }

        try {
            getUserFromUsernameStatement.setString(1, username);
            try (ResultSet rs = getUserFromUsernameStatement.executeQuery()) {
                if (rs.next() && rs.getString("password").equals(password)) {
                    return true;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean CheckUserExists(String username) {
        CheckLock();
        try {
            getUserFromUsernameStatement.setString(1, username);
            try (ResultSet rs = getUserFromUsernameStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static boolean CheckEmailExists(String email) {
        CheckLock();
        try {
            getUserFromEmailStatement.setString(1, email);
            try (ResultSet rs = getUserFromEmailStatement.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {}
        return false;
    }

    public static boolean RegisterUser(String username, String password, String email) {
        CheckLock();
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

    public static Lock getLock() {
        return lock;
    }

    private static void CheckLock() {
        if (!lock.isHeldByCurrentThread()) {
            throw new IllegalStateException("Please acquire the lock on the class before running methods on the class");
        }
        try {
            GetDatabaseConnection();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
