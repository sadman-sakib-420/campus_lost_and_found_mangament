package app.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Initializes and manages SQLite database schema, table structures,
 * and foreign key relationships (Users 1 -> N Items).
 */
public class DatabaseInitializer {

    public static void initializeDatabase() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            // 1. Enable Foreign Key Constraints in SQLite
            stmt.execute("PRAGMA foreign_keys = ON;");

            // 2. Create Users Table
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT NOT NULL UNIQUE, " +
                    "password TEXT NOT NULL, " +
                    "role TEXT NOT NULL DEFAULT 'USER', " +
                    "full_name TEXT, " +
                    "email TEXT, " +
                    "phone TEXT" +
                    ");";
            stmt.execute(createUsersTable);

            // 3. Create Items Table with Foreign Key Relationship to users(id)
            String createItemsTable = "CREATE TABLE IF NOT EXISTS items (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "user_id INTEGER, " +
                    "type TEXT NOT NULL, " +
                    "title TEXT NOT NULL, " +
                    "category TEXT, " +
                    "location TEXT, " +
                    "description TEXT, " +
                    "image_path TEXT, " +
                    "reporter_name TEXT, " +
                    "phone TEXT, " +
                    "email TEXT, " +
                    "status TEXT NOT NULL DEFAULT 'OPEN', " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL" +
                    ");";
            stmt.execute(createItemsTable);

            // 4. Migration Check: Ensure 'user_id' column exists if items table was created earlier
            ensureUserIdColumn(conn);

            // 5. Seed default demonstration users if empty
            seedUsersIfEmpty(conn);

            // 6. Seed sample items if items table is empty
            seedItemsIfEmpty(conn);

        } catch (SQLException e) {
            System.err.println("Database initialization error: " + e.getMessage());
        }
    }

    private static void ensureUserIdColumn(Connection conn) {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("PRAGMA table_info(items)")) {

            boolean hasUserId = false;
            while (rs.next()) {
                String colName = rs.getString("name");
                if ("user_id".equalsIgnoreCase(colName)) {
                    hasUserId = true;
                    break;
                }
            }

            if (!hasUserId) {
                stmt.execute("ALTER TABLE items ADD COLUMN user_id INTEGER REFERENCES users(id);");
            }
        } catch (SQLException e) {
            System.err.println("Column migration notice: " + e.getMessage());
        }
    }

    private static void seedUsersIfEmpty(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertUsers = "INSERT INTO users (id, username, password, role, full_name, email, phone) VALUES " +
                        "(1, 'admin', 'admin', 'ADMIN', 'Campus Administrator', 'admin@campus.edu', '01710000001'), " +
                        "(2, 'user', 'user', 'USER', 'Student User', 'student@campus.edu', '01710000002');";
                stmt.execute(insertUsers);
            }
        }
    }

    private static void seedItemsIfEmpty(Connection conn) throws SQLException {
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM items")) {
            if (rs.next() && rs.getInt(1) == 0) {
                String insertItems = "INSERT INTO items (user_id, type, title, category, location, description, image_path, reporter_name, phone, email, status) VALUES " +
                        "(2, 'LOST', 'Blue Hydro Flask Water Bottle', 'Bottles', 'Library 2nd Floor', '32oz navy blue Hydro Flask with stickers.', NULL, 'Student User', '01710000002', 'student@campus.edu', 'OPEN'), " +
                        "(1, 'FOUND', 'Blue Hydro Flask Bottle', 'Bottles', 'Library Reading Room', 'Found a navy blue water bottle near desk 5.', NULL, 'Campus Admin', '01710000001', 'admin@campus.edu', 'OPEN'), " +
                        "(2, 'LOST', 'Dell XPS 13 Laptop Charger', 'Electronics', 'Computer Lab 3', 'Black 65W USB-C Dell charger.', NULL, 'Student User', '01710000002', 'student@campus.edu', 'OPEN'), " +
                        "(1, 'FOUND', 'USB-C Laptop Power Adapter', 'Electronics', 'Computer Lab 3', 'Dell Type-C black charger plugged in desk.', NULL, 'Campus Security', '01710000001', 'security@campus.edu', 'OPEN');";
                stmt.execute(insertItems);
            }
        }
    }
}
