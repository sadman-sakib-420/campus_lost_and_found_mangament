package app.dao;

import app.model.User;

/**
 * Contract for user authentication. Implemented by JsonUserDAO — in this app,
 * users are always stored in users.json. See DAOFactory.
 */
public interface UserDAO {

    /**
     * Authenticates a user by username and password.
     *
     * @param username user entered username
     * @param password user entered password
     * @return User or Admin object if authenticated, null otherwise
     * @throws Exception if a storage access error occurs
     */
    User authenticate(String username, String password) throws Exception;

    /**
     * Finds a user by their username.
     *
     * @param username username to search for
     * @return User or Admin object if found, null otherwise
     * @throws Exception if a storage access error occurs
     */
    User findUserByUsername(String username) throws Exception;
}
