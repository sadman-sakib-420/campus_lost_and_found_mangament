package app.service;

import app.dao.DAOFactory;
import app.dao.ItemDAO;
import app.model.Item;
import app.util.DatabaseConnection;
import app.util.JsonDatabaseManager;

import java.util.ArrayList;
import java.util.List;

public class ItemService {

    private static final ItemDAO itemDAO = DAOFactory.getItemDAO();

    /* =========================
       INITIALIZE STORAGE
       Items and Users live in SQLite with Relational Foreign Keys.
       JSON fallback is also maintained.
    ========================= */
    public static void init() {
        // 1. Initialize SQLite Database Schema & Relationships (users 1 -> N items)
        app.util.DatabaseInitializer.initializeDatabase();

        // 2. Initialize JSON fallback storage
        JsonDatabaseManager.initializeJsonStorage();
    }

    /* =========================
       ADD ITEM
    ========================= */
    public static boolean addItem(Item item) {
        try {
            return itemDAO.addItem(item);
        } catch (Exception e) {
            System.err.println("Storage error adding item: " + e.getMessage());
            return false;
        }
    }

    /* =========================
       GET ALL ITEMS (Synchronous)
    ========================= */
    public static List<Item> getAllItems() {
        try {
            return itemDAO.getAllItems();
        } catch (Exception e) {
            System.err.println("Storage error retrieving items: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /* =========================
       GET ALL ITEMS (Asynchronous via Concurrency Thread Pool)
    ========================= */
    public static void getAllItemsAsync(java.util.function.Consumer<List<Item>> onSuccess, java.util.function.Consumer<Throwable> onError) {
        javafx.concurrent.Task<List<Item>> task = new javafx.concurrent.Task<>() {
            @Override
            protected List<Item> call() throws Exception {
                return itemDAO.getAllItems();
            }
        };

        task.setOnSucceeded(e -> {
            if (onSuccess != null) {
                onSuccess.accept(task.getValue());
            }
        });

        task.setOnFailed(e -> {
            if (onError != null) {
                onError.accept(task.getException());
            }
        });

        app.util.AppThreadPool.runTask(task);
    }

    /* =========================
       UPDATE ITEM STATUS
    ========================= */
    public static void markAsReturned(Item item) {
        if (item == null) return;
        try {
            itemDAO.updateItemStatus(item.getId(), "RETURNED");
            item.setStatus("RETURNED");
        } catch (Exception e) {
            System.err.println("Storage error marking item returned: " + e.getMessage());
        }
    }

    public static void markAsReturned(int itemId) {
        try {
            itemDAO.updateItemStatus(itemId, "RETURNED");
        } catch (Exception e) {
            System.err.println("Storage error marking item returned: " + e.getMessage());
        }
    }

    /* =========================
       DELETE ITEM
    ========================= */
    public static void deleteItem(Item item) {
        if (item == null) return;
        try {
            itemDAO.deleteItem(item.getId());
        } catch (Exception e) {
            System.err.println("Storage error deleting item: " + e.getMessage());
        }
    }

    public static void deleteItem(int itemId) {
        try {
            itemDAO.deleteItem(itemId);
        } catch (Exception e) {
            System.err.println("Storage error deleting item: " + e.getMessage());
        }
    }

    /* =========================
       AUTO-MATCH LOGIC
    ========================= */
    public static List<String> findPossibleMatches() {
        List<Item> items = getAllItems();
        List<String> matches = new ArrayList<>();

        for (Item lost : items) {
            if (!lost.getType().equalsIgnoreCase("LOST") ||
                    lost.getStatus().equalsIgnoreCase("RETURNED")) {
                continue;
            }

            for (Item found : items) {
                if (!found.getType().equalsIgnoreCase("FOUND") ||
                        found.getStatus().equalsIgnoreCase("RETURNED")) {
                    continue;
                }

                int score = 0;

                if (lost.getCategory() != null &&
                        found.getCategory() != null &&
                        lost.getCategory().equalsIgnoreCase(found.getCategory())) {
                    score += 3;
                }

                if (lost.getLocation() != null &&
                        found.getLocation() != null) {
                    String lostLoc = lost.getLocation().toLowerCase();
                    String foundLoc = found.getLocation().toLowerCase();

                    if (lostLoc.contains(foundLoc) ||
                            foundLoc.contains(lostLoc)) {
                        score += 2;
                    }
                }

                if (lost.getTitle() != null &&
                        found.getTitle() != null) {
                    String[] lostWords =
                            lost.getTitle().toLowerCase().split("\\s+");

                    for (String word : lostWords) {
                        if (found.getTitle()
                                .toLowerCase()
                                .contains(word)) {
                            score += 2;
                            break;
                        }
                    }
                }

                if (lost.getDescription() != null &&
                        found.getDescription() != null &&
                        found.getDescription()
                                .toLowerCase()
                                .contains(lost.getDescription().toLowerCase())) {
                    score += 1;
                }

                if (score >= 5) {
                    matches.add(
                            "Possible match (" + score + "): " +
                                    "LOST [" + lost.getTitle() + "] <-> FOUND [" +
                                    found.getTitle() + "]"
                    );
                }
            }
        }

        return matches;
    }

    public static ItemDAO getItemDAO() {
        return itemDAO;
    }
}
