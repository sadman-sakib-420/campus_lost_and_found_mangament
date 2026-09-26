package app.util;

import app.config.DatabaseConfig;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConnection {

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            System.err.println("SQLite JDBC Driver not found: " + e.getMessage());
        }
    }

    /**
     * Obtains a new database connection using the URL from DatabaseConfig.
     * Callers must close the returned connection, preferably using try-with-resources.
     * SQLite does not use a username/password, so only the URL is used.
     *
     * @return Connection to SQLite database
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(DatabaseConfig.getUrl());
        try (Statement stmt = conn.createStatement()) {
            // Ensures foreign key constraints are respected (off by default in SQLite)
            stmt.execute("PRAGMA foreign_keys = ON");
        }
        return conn;
    }

    /**
     * Tests if the database connection can be established successfully.
     *
     * @return true if connected, false otherwise
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (SQLException e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            return false;
        }
    }
}
