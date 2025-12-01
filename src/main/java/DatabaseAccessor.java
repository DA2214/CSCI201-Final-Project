

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseAccessor {

    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/TrojanTracker";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASS = "root";

    private static Connection connection = null;

    static {
        try {
            connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASS);
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
}