package app.dao;

import app.model.Item;

import java.util.List;

/**
 * Contract for item persistence. Implemented by SqliteItemDAO — in this app,
 * items are always stored in SQLite (lost_found_db.db). See DAOFactory.
 */
public interface ItemDAO {

    /**
     * Inserts a new lost or found item into storage.
     * Sets the auto-generated ID on the item object.
     *
     * @param item The item to insert
     * @return true if insertion succeeded, false otherwise
     * @throws Exception if a storage access error occurs
     */
    boolean addItem(Item item) throws Exception;

    /**
     * Retrieves all items from storage, reconstructed as LostItem or FoundItem.
     *
     * @return List of all Item objects
     * @throws Exception if a storage access error occurs
     */
    List<Item> getAllItems() throws Exception;

    /**
     * Updates the status of an item (e.g., 'OPEN' or 'RETURNED') by its ID.
     *
     * @param itemId primary key of item
     * @param status new status string
     * @return true if updated, false otherwise
     * @throws Exception if a storage access error occurs
     */
    boolean updateItemStatus(int itemId, String status) throws Exception;

    /**
     * Deletes an item from storage by its ID.
     *
     * @param itemId primary key of item
     * @return true if deleted, false otherwise
     * @throws Exception if a storage access error occurs
     */
    boolean deleteItem(int itemId) throws Exception;
}
