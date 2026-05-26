package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static Connection connection = null;

    // Private constructor to prevent instantiation
    private DatabaseConnection() {}

    public static synchronized Connection getInstance() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                // Explicitly register driver (important for some environments)
                Class.forName("org.postgresql.Driver");
            } catch (ClassNotFoundException e) {
                throw new SQLException("PostgreSQL JDBC Driver not found", e);
            }
            
            String url = EnvLoader.get("DB_URL");
            String user = EnvLoader.get("DB_USER");
            String password = EnvLoader.get("DB_PASSWORD");
            
            if (url == null || user == null) {
                throw new SQLException("Database credentials not found in env configuration.");
            }
            
            connection = DriverManager.getConnection(url, user, password);
        }
        return connection;
    }
}
