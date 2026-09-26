package app.dao;

import app.model.Admin;
import app.model.User;
import app.util.JsonDatabaseManager;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * UserDAO implementation backed by a plain JSON file (users.json).
 * Used for user accounts - see DAOFactory (fixed split: users -> JSON, items -> SQLite).
 */
public class JsonUserDAO implements UserDAO {

    @Override
    public User authenticate(String username, String password) throws Exception {
        ArrayNode users = JsonDatabaseManager.loadUsersArray();

        for (JsonNode node : users) {
            String uname = text(node, "username");
            String pass = text(node, "password");
            if (uname != null && uname.equals(username) && pass != null && pass.equals(password)) {
                return toUser(node);
            }
        }
        return null;
    }

    @Override
    public User findUserByUsername(String username) throws Exception {
        ArrayNode users = JsonDatabaseManager.loadUsersArray();

        for (JsonNode node : users) {
            String uname = text(node, "username");
            if (uname != null && uname.equals(username)) {
                return toUser(node);
            }
        }
        return null;
    }

    private User toUser(JsonNode node) {
        int id = node.path("id").asInt(0);
        String uname = text(node, "username");
        String pass = text(node, "password");
        String role = text(node, "role");

        if ("ADMIN".equalsIgnoreCase(role)) {
            return new Admin(id, uname, pass);
        }
        return new User(id, uname, pass, role);
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        return (value == null || value.isNull()) ? null : value.asText();
    }
}
