package app.dao;

import app.model.Admin;
import app.model.User;
import app.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * UserDAO implementation backed by SQLite database (users table).
 * Enables relational integrity with the items table via user_id foreign key.
 */
public class SqliteUserDAO implements UserDAO {

    private final JsonUserDAO jsonFallback = new JsonUserDAO();

    @Override
    public User authenticate(String username, String password) throws Exception {
        String sql = "SELECT id, username, password, role FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            stmt.setString(2, password);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQLite user authentication error, checking JSON fallback: " + e.getMessage());
        }

        // Fallback to JSON if user is stored in users.json
        return jsonFallback.authenticate(username, password);
    }

    @Override
    public User findUserByUsername(String username) throws Exception {
        String sql = "SELECT id, username, password, role FROM users WHERE username = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapUser(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("SQLite find user error, checking JSON fallback: " + e.getMessage());
        }

        return jsonFallback.findUserByUsername(username);
    }

    private User mapUser(ResultSet rs) throws SQLException {
        int id = rs.getInt("id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        String role = rs.getString("role");

        if ("ADMIN".equalsIgnoreCase(role)) {
            return new Admin(id, username, password);
        }
        return new User(id, username, password, role);
    }
}
