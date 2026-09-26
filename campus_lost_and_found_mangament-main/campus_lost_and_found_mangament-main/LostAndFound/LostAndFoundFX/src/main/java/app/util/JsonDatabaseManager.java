package app.util;

import app.config.DatabaseConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.File;
import java.io.IOException;

/**
 * Low-level JSON file storage helper. Reads and writes plain JSON arrays
 * (items.json / users.json) using Jackson's tree model (JsonNode), rather than
 * mapping directly to the abstract Item/User classes - this avoids needing
 * polymorphic type annotations on the model classes.
 *
 * JsonUserDAO builds/reads app.model.User objects from the nodes returned
 * here. (items.json / loadItemsArray / saveItemsArray are kept for reference
 * in case items are ever moved to JSON storage too.)
 */
public class JsonDatabaseManager {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final Object itemsLock = new Object();
    private static final Object usersLock = new Object();

    public static File getItemsFile() {
        return new File(DatabaseConfig.getItemsFile());
    }

    public static File getUsersFile() {
        return new File(DatabaseConfig.getUsersFile());
    }

    /**
     * Initializes users.json with default seed data if it does not exist.
     * (Items are not seeded here - items live in SQLite, see SqliteItemDAO.)
     */
    public static void initializeJsonStorage() {
        synchronized (usersLock) {
            File usersFile = getUsersFile();
            if (!usersFile.exists() || usersFile.length() == 0) {
                ArrayNode users = objectMapper.createArrayNode();
                users.add(userNode(1, "admin", "admin", "ADMIN"));
                users.add(userNode(2, "user", "user", "USER"));
                writeQuietly(usersFile, users, "users.json");
            }
        }
    }

    /** Loads the raw items array, initializing seed data on first run. */
    public static ArrayNode loadItemsArray() throws IOException {
        synchronized (itemsLock) {
            File itemsFile = getItemsFile();
            if (!itemsFile.exists() || itemsFile.length() == 0) {
                initializeJsonStorage();
            }
            JsonNode node = objectMapper.readTree(itemsFile);
            return (node != null && node.isArray()) ? (ArrayNode) node : objectMapper.createArrayNode();
        }
    }

    /** Overwrites items.json with the given array. */
    public static void saveItemsArray(ArrayNode items) throws IOException {
        synchronized (itemsLock) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(getItemsFile(), items);
        }
    }

    /** Loads the raw users array, initializing seed data on first run. */
    public static ArrayNode loadUsersArray() throws IOException {
        synchronized (usersLock) {
            File usersFile = getUsersFile();
            if (!usersFile.exists() || usersFile.length() == 0) {
                initializeJsonStorage();
            }
            JsonNode node = objectMapper.readTree(usersFile);
            return (node != null && node.isArray()) ? (ArrayNode) node : objectMapper.createArrayNode();
        }
    }

    /** Overwrites users.json with the given array. */
    public static void saveUsersArray(ArrayNode users) throws IOException {
        synchronized (usersLock) {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(getUsersFile(), users);
        }
    }

    /** Tests if JSON storage is working properly. */
    public static boolean testStorage() {
        try {
            initializeJsonStorage();
            return getItemsFile().exists() && getUsersFile().exists();
        } catch (Exception e) {
            System.err.println("JSON storage test failed: " + e.getMessage());
            return false;
        }
    }

    private static ObjectNode userNode(int id, String username, String password, String role) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("id", id);
        node.put("username", username);
        node.put("password", password);
        node.put("role", role);
        return node;
    }

    private static ObjectNode itemNode(int id, String type, String title, String category, String location,
                                        String description, String imagePath, String reporterName,
                                        String phone, String email, String status) {
        ObjectNode node = objectMapper.createObjectNode();
        node.put("id", id);
        node.put("type", type);
        node.put("title", title);
        node.put("category", category);
        node.put("location", location);
        node.put("description", description);
        node.put("image_path", imagePath);
        node.put("reporter_name", reporterName);
        node.put("phone", phone);
        node.put("email", email);
        node.put("status", status);
        return node;
    }

    private static void writeQuietly(File file, ArrayNode data, String label) {
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
        } catch (IOException e) {
            System.err.println("Error initializing " + label + ": " + e.getMessage());
        }
    }
}
