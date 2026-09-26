package app.ui;

import app.model.Admin;
import app.model.Item;
import app.service.ItemService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class AdminManageItemsView {

    private Scene scene;
    private VBox cardsContainer;
    private String currentFilter = "ALL";

    public AdminManageItemsView(Stage stage, Admin admin) {

        Label title = new Label("Manage Lost & Found Items");
        title.getStyleClass().add("title");

        /* =========================
           AUTO-MATCH BUTTON
        ========================= */
        Button matchBtn = new Button("Show Possible Matches");
        matchBtn.getStyleClass().add("primary-btn");

        matchBtn.setOnAction(e -> {
            List<String> matches = ItemService.findPossibleMatches();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Auto-Match Suggestions");
            alert.setHeaderText("Possible Lost and Found Matches");

            if (matches.isEmpty()) {
                alert.setContentText("No matching items found.");
            } else {
                StringBuilder sb = new StringBuilder();
                for (String m : matches) {
                    sb.append(m).append("\n");
                }
                alert.setContentText(sb.toString());
            }
            alert.showAndWait();
        });

        /* =========================
           SEARCH + FILTERS
        ========================= */
        TextField searchField = new TextField();
        searchField.setPromptText("Search by title or location...");
        searchField.setMaxWidth(300);

        Button allBtn = new Button("All");
        Button lostBtn = new Button("Lost");
        Button foundBtn = new Button("Found");
        Button returnedBtn = new Button("Returned");

        for (Button b : new Button[]{allBtn, lostBtn, foundBtn, returnedBtn}) {
            b.getStyleClass().add("filter-btn");
        }

        HBox filters = new HBox(10, allBtn, lostBtn, foundBtn, returnedBtn);
        filters.setAlignment(Pos.CENTER_LEFT);

        HBox topControls = new HBox(20, searchField, filters);
        VBox topArea = new VBox(10, matchBtn, topControls);

        cardsContainer = new VBox(15);
        cardsContainer.setPadding(new Insets(20));

        ScrollPane scrollPane = new ScrollPane(cardsContainer);
        scrollPane.setFitToWidth(true);

        /* =========================
           FILTER LOGIC
        ========================= */
        allBtn.setOnAction(e -> { currentFilter = "ALL"; refreshCards(searchField.getText()); });
        lostBtn.setOnAction(e -> { currentFilter = "LOST"; refreshCards(searchField.getText()); });
        foundBtn.setOnAction(e -> { currentFilter = "FOUND"; refreshCards(searchField.getText()); });
        returnedBtn.setOnAction(e -> { currentFilter = "RETURNED"; refreshCards(searchField.getText()); });

        searchField.textProperty().addListener((obs, o, n) -> refreshCards(n));

        refreshCards("");

        Button backBtn = new Button("Back");
        backBtn.setOnAction(e -> {
            AdminDashboardLayout dashboard =
                    new AdminDashboardLayout(stage, admin);
            stage.setScene(dashboard.getScene());
        });

        VBox root = new VBox(15, title, topArea, scrollPane, backBtn);
        root.setPadding(new Insets(20));

        // Layout Responsiveness: Property Constraints relative to window size
        searchField.prefWidthProperty().bind(root.widthProperty().multiply(0.35));
        scrollPane.prefHeightProperty().bind(root.heightProperty().subtract(topArea.heightProperty()).subtract(130));
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        scene = new Scene(root, 1100, 650);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm()
        );
    }

    /* =========================
       CREATE ITEM CARD
    ========================= */
    private HBox createItemCard(Item item) {

        Label name = new Label(item.getTitle());
        name.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        Label details = new Label(
                "Type: " + item.getType() +
                        " | Category: " + item.getCategory() +
                        " | Location: " + item.getLocation()
        );

        Label status = new Label("Status: " + item.getStatus());
        status.getStyleClass().add(
                item.getStatus().equals("RETURNED")
                        ? "status-returned"
                        : "status-open"
        );

        VBox infoBox = new VBox(6, name, details, status);

        Button returnBtn = new Button("Mark Returned");
        Button deleteBtn = new Button("Delete");

        returnBtn.getStyleClass().add("primary-btn");
        deleteBtn.getStyleClass().add("danger-btn");

        /* ===== FIXED: PERSIST STATUS ===== */
        returnBtn.setOnAction(e -> {
            ItemService.markAsReturned(item);
            refreshCards("");
        });

        deleteBtn.setOnAction(e -> {

            if (!item.getStatus().equals("RETURNED")) {
                showInfo("Only returned items can be deleted.");
                return;
            }

            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
            confirm.setHeaderText("Confirm Deletion");
            confirm.setContentText("Delete this item permanently?");

            Optional<ButtonType> result = confirm.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                ItemService.deleteItem(item);
                refreshCards("");
            }
        });

        VBox actions = new VBox(8, returnBtn, deleteBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);

        HBox card = new HBox(20, infoBox, actions);
        card.setPadding(new Insets(15));
        card.setStyle(
                "-fx-background-color: white;" +
                        "-fx-background-radius: 10;" +
                        "-fx-border-color: #e5e7eb;" +
                        "-fx-border-radius: 10;"
        );

        HBox.setHgrow(infoBox, Priority.ALWAYS);
        return card;
    }

    /* =========================
       REFRESH CARDS
    ========================= */
    private void refreshCards(String searchText) {

        cardsContainer.getChildren().clear();

        for (Item item : ItemService.getAllItems()) {

            boolean matchesFilter =
                    currentFilter.equals("ALL") ||
                            item.getType().equalsIgnoreCase(currentFilter) ||
                            item.getStatus().equalsIgnoreCase(currentFilter);

            boolean matchesSearch =
                    searchText == null || searchText.isEmpty() ||
                            item.getTitle().toLowerCase().contains(searchText.toLowerCase()) ||
                            item.getLocation().toLowerCase().contains(searchText.toLowerCase());

            if (matchesFilter && matchesSearch) {
                cardsContainer.getChildren().add(createItemCard(item));
            }
        }

        if (cardsContainer.getChildren().isEmpty()) {
            Label empty = new Label("No items match the current filter.");
            empty.setStyle("-fx-text-fill: #6b7280;");
            cardsContainer.getChildren().add(empty);
        }
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    public Scene getScene() {
        return scene;
    }
}
