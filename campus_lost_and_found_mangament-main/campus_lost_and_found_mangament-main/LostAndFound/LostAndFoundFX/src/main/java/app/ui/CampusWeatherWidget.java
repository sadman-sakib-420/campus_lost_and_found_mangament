package app.ui;

import app.service.CampusWeatherService;
import app.service.CampusWeatherService.CampusWeather;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Reusable Live Weather & Campus Environment Widget.
 * Demonstrates:
 * 1. Networking & Data Parsing: HTTP REST API calls & Jackson JSON parsing
 * 2. Concurrency: Background multi-threading using AppThreadPool
 * 3. JavaFX UI & Panes: StackPane, HBox, VBox
 * 4. Layout Responsiveness: Width property binding
 */
public class CampusWeatherWidget extends StackPane {

    public CampusWeatherWidget() {
        setAlignment(Pos.CENTER);
        setPadding(new Insets(15, 20, 15, 20));
        setStyle("-fx-background-color: white; -fx-background-radius: 12; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.06), 10, 0, 0, 4); " +
                "-fx-border-color: #e5e7eb; -fx-border-radius: 12;");

        // Loading state
        HBox loadingBox = new HBox(12);
        loadingBox.setAlignment(Pos.CENTER_LEFT);
        ProgressIndicator spinner = new ProgressIndicator();
        spinner.setMaxSize(24, 24);
        Label loadingLabel = new Label("Connecting to Campus Weather API (HTTP REST / JSON)...");
        loadingLabel.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 13px;");
        loadingBox.getChildren().addAll(spinner, loadingLabel);

        getChildren().add(loadingBox);

        // Fetch live JSON data asynchronously using Concurrency Thread Pool
        CampusWeatherService.fetchWeatherAsync(this::populateWeatherData);
    }

    private void populateWeatherData(CampusWeather weather) {
        getChildren().clear();

        HBox contentBox = new HBox(20);
        contentBox.setAlignment(Pos.CENTER_LEFT);

        // Left: Temperature & Icon
        VBox tempBox = new VBox(2);
        tempBox.setAlignment(Pos.CENTER_LEFT);

        Label tempLabel = new Label(String.format("%.1f°C", weather.getTemperature()));
        tempLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #1f2937;");

        Label condLabel = new Label(weather.getConditionText());
        condLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: 600; -fx-text-fill: #4b5563;");

        tempBox.getChildren().addAll(tempLabel, condLabel);

        // Middle: Location & Outdoor Search Advisory
        VBox infoBox = new VBox(4);
        infoBox.setAlignment(Pos.CENTER_LEFT);

        Label titleLabel = new Label("Campus Weather & Outdoor Search Advisory");
        titleLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2563eb;");

        Label advisoryLabel = new Label(weather.getAdvisory() +
                String.format(" (Wind: %.1f km/h)", weather.getWindSpeed()));
        advisoryLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #4b5563;");
        advisoryLabel.setWrapText(true);

        infoBox.getChildren().addAll(titleLabel, advisoryLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        // Right: Live API Badge
        VBox badgeBox = new VBox();
        badgeBox.setAlignment(Pos.CENTER_RIGHT);
        Label apiBadge = new Label(weather.isLive() ? "● LIVE REST API" : "○ CACHED API");
        apiBadge.setStyle(weather.isLive() ?
                "-fx-background-color: #ecfdf5; -fx-text-fill: #059669; -fx-padding: 4 10 4 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold; -fx-border-color: #a7f3d0; -fx-border-radius: 20;" :
                "-fx-background-color: #f3f4f6; -fx-text-fill: #6b7280; -fx-padding: 4 10 4 10; -fx-background-radius: 20; -fx-font-size: 11px; -fx-font-weight: bold; -fx-border-color: #d1d5db; -fx-border-radius: 20;");

        badgeBox.getChildren().add(apiBadge);

        contentBox.getChildren().addAll(tempBox, infoBox, badgeBox);
        getChildren().add(contentBox);
    }
}
