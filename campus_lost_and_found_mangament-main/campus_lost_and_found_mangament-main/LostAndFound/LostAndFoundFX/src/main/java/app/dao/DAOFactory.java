package app.dao;

/**
 * Fixed storage split for this app:
 *   - Items -> SQLite  (SqliteItemDAO)
 *   - Users -> JSON     (JsonUserDAO)
 */
public class DAOFactory {

    public static ItemDAO getItemDAO() {
        return new SqliteItemDAO();
    }

    public static UserDAO getUserDAO() {
        return new SqliteUserDAO();
    }
}
