package app.config;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class DatabaseConfig {

    private static final String CONFIG_FILE = "db.properties";
    private static final Properties properties = new Properties();
    private static boolean isLoaded = false;

    static {
        loadProperties();
    }

    private static synchronized void loadProperties() {
        if (isLoaded) {
            return;
        }

        // 1. Try loading from classpath
        InputStream is = DatabaseConfig.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
        if (is == null) {
            is = DatabaseConfig.class.getResourceAsStream("/" + CONFIG_FILE);
        }

        if (is != null) {
            try (InputStream input = is) {
                properties.load(input);
                isLoaded = true;
                return;
            } catch (IOException e) {
                System.err.println("Failed to read " + CONFIG_FILE + " from classpath: " + e.getMessage());
            }
        }

        // 2. Fallback: Try loading from local working directory
        File localFile = new File(CONFIG_FILE);
        if (localFile.exists()) {
            try (InputStream fis = new FileInputStream(localFile)) {
                properties.load(fis);
                isLoaded = true;
                return;
            } catch (IOException e) {
                System.err.println("Failed to read " + CONFIG_FILE + " from current directory: " + e.getMessage());
            }
        }

        // 3. Fallback defaults if file cannot be read
        System.err.println("Warning: db.properties not found. Using fallback database defaults.");
        properties.setProperty("db.url", "jdbc:sqlite:lost_found_db.db");
        properties.setProperty("users.file", "users.json");
        isLoaded = true;
    }

    /** SQLite connection URL - used for items storage. */
    public static String getUrl() {
        loadProperties();
        return properties.getProperty("db.url", "jdbc:sqlite:lost_found_db.db");
    }

    /** JSON file path - used for users storage. */
    public static String getUsersFile() {
        loadProperties();
        return properties.getProperty("users.file", "users.json");
    }

    /** JSON file path for items - kept for reference only; items are not stored here. */
    public static String getItemsFile() {
        loadProperties();
        return properties.getProperty("items.file", "items.json");
    }
}
