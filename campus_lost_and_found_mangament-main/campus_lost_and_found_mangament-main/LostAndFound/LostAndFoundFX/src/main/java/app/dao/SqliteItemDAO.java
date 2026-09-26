package app.dao;

import app.model.FoundItem;
import app.model.Item;
import app.model.LostItem;
import app.util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SqliteItemDAO implements ItemDAO {

    /**
     * Inserts a new lost or found item into SQLite.
     * Sets the auto-generated database ID on the item object.
     *
     * @param item The item to insert
     * @return true if insertion succeeded, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean addItem(Item item) throws SQLException {
        String sql = "INSERT INTO items (user_id, type, title, category, location, description, image_path, reporter_name, phone, email, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            if (item.getUserId() != null) {
                stmt.setInt(1, item.getUserId());
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setString(2, item.getType());
            stmt.setString(3, item.getTitle());
            stmt.setString(4, item.getCategory());
            stmt.setString(5, item.getLocation());
            stmt.setString(6, item.getDescription());
            stmt.setString(7, item.getImagePath());
            stmt.setString(8, item.getReporterName());
            stmt.setString(9, item.getPhone());
            stmt.setString(10, item.getEmail());
            stmt.setString(11, item.getStatus() != null ? item.getStatus() : "OPEN");

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        item.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves all items from the database with relational JOIN to the users table,
     * reconstructed as LostItem or FoundItem.
     *
     * @return List of all Item objects
     * @throws SQLException if a database access error occurs
     */
    public List<Item> getAllItems() throws SQLException {
        List<Item> items = new ArrayList<>();
        // Relational query joining items with users
        String sql = "SELECT items.id, items.user_id, items.type, items.title, items.category, " +
                     "items.location, items.description, items.image_path, items.reporter_name, " +
                     "items.phone, items.email, items.status, users.username AS linked_user " +
                     "FROM items " +
                     "LEFT JOIN users ON items.user_id = users.id " +
                     "ORDER BY items.id DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("id");
                int userId = rs.getInt("user_id");
                String type = rs.getString("type");
                String title = rs.getString("title");
                String category = rs.getString("category");
                String location = rs.getString("location");
                String description = rs.getString("description");
                String imagePath = rs.getString("image_path");
                String reporterName = rs.getString("reporter_name");
                String phone = rs.getString("phone");
                String email = rs.getString("email");
                String status = rs.getString("status");

                Item item;
                if ("LOST".equalsIgnoreCase(type)) {
                    item = new LostItem(id, title, category, location, description, imagePath, reporterName, phone, email, status);
                } else {
                    item = new FoundItem(id, title, category, location, description, imagePath, reporterName, phone, email, status);
                }
                if (!rs.wasNull()) {
                    item.setUserId(userId);
                }

                items.add(item);
            }
        }
        return items;
    }

    /**
     * Updates the status of an item (e.g., 'OPEN' or 'RETURNED') by its database ID.
     *
     * @param itemId database primary key of item
     * @param status new status string
     * @return true if updated, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean updateItemStatus(int itemId, String status) throws SQLException {
        String sql = "UPDATE items SET status = ? WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, status);
            stmt.setInt(2, itemId);

            return stmt.executeUpdate() > 0;
        }
    }

    /**
     * Deletes an item from the database by its ID.
     *
     * @param itemId database primary key of item
     * @return true if deleted, false otherwise
     * @throws SQLException if a database access error occurs
     */
    public boolean deleteItem(int itemId) throws SQLException {
        String sql = "DELETE FROM items WHERE id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, itemId);

            return stmt.executeUpdate() > 0;
        }
    }
}
