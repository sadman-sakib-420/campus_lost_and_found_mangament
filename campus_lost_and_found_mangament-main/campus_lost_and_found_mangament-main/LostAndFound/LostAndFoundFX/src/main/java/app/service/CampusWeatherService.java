package app.service;

import app.util.AppThreadPool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.application.Platform;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.function.Consumer;

/**
 * Service that demonstrates Networking & Data Parsing (HTTP requests & JSON parsing)
 * and Concurrency (Asynchronous background fetch using Thread Pool).
 *
 * Fetches real-time weather & environment data for the campus area via Open-Meteo REST API,
 * parses the JSON payload with Jackson, and provides lost & found outdoor search advisories.
 */
public class CampusWeatherService {

    private static final String API_URL =
            "https://api.open-meteo.com/v1/forecast?latitude=22.8998&longitude=89.5024&current_weather=true";

    private static final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(6))
            .build();

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static class CampusWeather {
        private final double temperature;
        private final double windSpeed;
        private final int weatherCode;
        private final String conditionText;
        private final String advisory;
        private final boolean live;

        public CampusWeather(double temperature, double windSpeed, int weatherCode,
                             String conditionText, String advisory, boolean live) {
            this.temperature = temperature;
            this.windSpeed = windSpeed;
            this.weatherCode = weatherCode;
            this.conditionText = conditionText;
            this.advisory = advisory;
            this.live = live;
        }

        public double getTemperature() { return temperature; }
        public double getWindSpeed() { return windSpeed; }
        public int getWeatherCode() { return weatherCode; }
        public String getConditionText() { return conditionText; }
        public String getAdvisory() { return advisory; }
        public boolean isLive() { return live; }
    }

    /**
     * Synchronously sends HTTP GET request, fetches JSON from the internet, and parses it.
     */
    public static CampusWeather fetchWeather() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .timeout(Duration.ofSeconds(6))
                    .header("Accept", "application/json")
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                return parseWeatherJson(response.body(), true);
            }
        } catch (Exception e) {
            System.err.println("Notice: HTTP weather fetch offline or timed out: " + e.getMessage());
        }

        // Offline fallback
        return getOfflineFallback();
    }

    /**
     * Asynchronously fetches JSON data over HTTP using AppThreadPool and notifies on JavaFX Application Thread.
     */
    public static void fetchWeatherAsync(Consumer<CampusWeather> callback) {
        AppThreadPool.execute(() -> {
            CampusWeather result = fetchWeather();
            if (callback != null) {
                Platform.runLater(() -> callback.accept(result));
            }
        });
    }

    /**
     * Parses the JSON response body using Jackson ObjectMapper.
     */
    public static CampusWeather parseWeatherJson(String jsonString, boolean isLive) {
        try {
            JsonNode root = objectMapper.readTree(jsonString);
            JsonNode current = root.path("current_weather");

            double temp = current.path("temperature").asDouble(27.0);
            double wind = current.path("windspeed").asDouble(8.0);
            int code = current.path("weathercode").asInt(0);

            String condition = mapWeatherCode(code);
            String advisory = generateAdvisory(code, temp);

            return new CampusWeather(temp, wind, code, condition, advisory, isLive);
        } catch (Exception e) {
            System.err.println("JSON Parsing error: " + e.getMessage());
            return getOfflineFallback();
        }
    }

    private static String mapWeatherCode(int code) {
        return switch (code) {
            case 0 -> "Clear Sky ☀️";
            case 1, 2 -> "Partly Cloudy ⛅";
            case 3 -> "Overcast ☁️";
            case 45, 48 -> "Foggy 🌫️";
            case 51, 53, 55 -> "Light Drizzle 🌦️";
            case 61, 63, 65 -> "Rain 🌧️";
            case 80, 81, 82 -> "Rain Showers 🌧️";
            case 95, 96, 99 -> "Thunderstorm ⛈️";
            default -> "Fair Conditions 🌤️";
        };
    }

    private static String generateAdvisory(int code, double temp) {
        if (code >= 51 && code <= 99) {
            return "Rain detected. Outdoor lost items may be at risk of water damage; search covered areas.";
        } else if (temp > 34.0) {
            return "High temperature today. Remember hydration while searching outdoor campus grounds.";
        } else {
            return "Pleasant campus weather. Great conditions for outdoor lost & found item search.";
        }
    }

    private static CampusWeather getOfflineFallback() {
        return new CampusWeather(26.0, 7.5, 0, "Clear Sky (Cached) ☀️",
                "Campus lost & found desk operating normally.", false);
    }
}
