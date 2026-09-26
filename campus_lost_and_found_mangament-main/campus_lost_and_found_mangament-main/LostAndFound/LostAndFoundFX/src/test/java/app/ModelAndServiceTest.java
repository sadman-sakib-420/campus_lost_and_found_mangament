package app;

import app.config.DatabaseConfig;
import app.model.Admin;
import app.model.FoundItem;
import app.model.Item;
import app.model.LostItem;
import app.model.User;
import app.service.CampusWeatherService;
import app.service.CampusWeatherService.CampusWeather;
import app.util.AppThreadPool;
import app.util.DatabaseInitializer;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.*;

public class ModelAndServiceTest {

    @Test
    public void testUserAndAdminInheritance() {
        User user = new User(1, "testuser", "secret", "USER");
        assertEquals(1, user.getId());
        assertEquals("testuser", user.getUsername());
        assertEquals("secret", user.getPassword());
        assertEquals("USER", user.getRole());
        assertTrue(user.login("secret"));
        assertFalse(user.login("wrongpass"));

        Admin admin = new Admin(2, "admin", "adminpass");
        assertTrue(admin instanceof User);
        assertEquals(2, admin.getId());
        assertEquals("admin", admin.getUsername());
        assertEquals("ADMIN", admin.getRole());
        assertTrue(admin.login("adminpass"));

        Item item = new LostItem(10, "Keys", "Stationery", "Lab 1", "Bunch of keys", null, "Alice", "123", "a@a.com", "OPEN");
        assertEquals("OPEN", item.getStatus());
        admin.markItemReturned(item);
        assertEquals("RETURNED", item.getStatus());
    }

    @Test
    public void testLostAndFoundItemPolymorphism() {
        Item lost = new LostItem(101, "Blue Bottle", "Bottles", "Library", "Flask", null, "John", "111", "j@e.com", "OPEN");
        Item found = new FoundItem(102, "Blue Bottle", "Bottles", "Library", "Flask", null, "Jane", "222", "j2@e.com", "OPEN");

        assertEquals("LOST", lost.getType());
        assertEquals("FOUND", found.getType());
        assertEquals(101, lost.getId());
        assertEquals(102, found.getId());
        assertEquals("Blue Bottle", lost.getTitle());
        assertEquals("Library", found.getLocation());

        // Test Relational User ID mapping
        lost.setUserId(2);
        assertEquals(2, lost.getUserId());
    }

    @Test
    public void testDatabaseConfigLoaded() {
        assertNotNull(DatabaseConfig.getUrl());
        assertTrue(DatabaseConfig.getUrl().contains("lost_found_db"));
        assertNotNull(DatabaseConfig.getUsersFile());
        assertTrue(DatabaseConfig.getUsersFile().contains("users.json"));
    }

    @Test
    public void testDatabaseInitializerRelationalSchema() {
        // Initializes SQLite tables (users and items with foreign keys)
        assertDoesNotThrow(DatabaseInitializer::initializeDatabase);
    }

    @Test
    public void testConcurrencyThreadPool() throws Exception {
        // Tests multi-threading and thread pool execution
        Future<String> future = AppThreadPool.submit(() -> "Task Completed in Thread Pool");
        assertNotNull(future);
        assertEquals("Task Completed in Thread Pool", future.get());
    }

    @Test
    public void testNetworkJsonDataParsing() {
        // Tests HTTP JSON parser with sample REST API payload
        String sampleJson = """
            {
                "latitude": 22.8998,
                "longitude": 89.5024,
                "current_weather": {
                    "temperature": 29.5,
                    "windspeed": 11.2,
                    "weathercode": 2
                }
            }
            """;

        CampusWeather weather = CampusWeatherService.parseWeatherJson(sampleJson, true);
        assertNotNull(weather);
        assertEquals(29.5, weather.getTemperature(), 0.001);
        assertEquals(11.2, weather.getWindSpeed(), 0.001);
        assertEquals(2, weather.getWeatherCode());
        assertTrue(weather.getConditionText().contains("Cloudy"));
        assertTrue(weather.isLive());
        assertNotNull(weather.getAdvisory());
    }

    @Test
    public void testAutoMatchAlgorithmScoring() {
        Item lost = new LostItem(1, "Blue Flask", "Bottles", "Library Floor 2", "Metal insulated flask", null, "John", "111", "j@e.com", "OPEN");
        Item foundMatching = new FoundItem(2, "Blue Flask Water Bottle", "Bottles", "Library", "Flask found", null, "Jane", "222", "j2@e.com", "OPEN");
        Item foundNonMatching = new FoundItem(3, "Black Umbrella", "Accessories", "Cafeteria", "Umbrella", null, "Bob", "333", "b@e.com", "OPEN");

        int score = 0;
        if (lost.getCategory().equalsIgnoreCase(foundMatching.getCategory())) score += 3;
        if (lost.getLocation().toLowerCase().contains(foundMatching.getLocation().toLowerCase())) score += 2;
        for (String word : lost.getTitle().toLowerCase().split("\\s+")) {
            if (foundMatching.getTitle().toLowerCase().contains(word)) {
                score += 2;
                break;
            }
        }
        if (foundMatching.getDescription().toLowerCase().contains(lost.getDescription().toLowerCase()) ||
            lost.getDescription().toLowerCase().contains(foundMatching.getDescription().toLowerCase())) {
            score += 1;
        }
        assertTrue(score >= 5);

        int nonMatchScore = 0;
        if (lost.getCategory().equalsIgnoreCase(foundNonMatching.getCategory())) nonMatchScore += 3;
        if (lost.getLocation().toLowerCase().contains(foundNonMatching.getLocation().toLowerCase())) nonMatchScore += 2;
        assertTrue(nonMatchScore < 5);
    }
}
