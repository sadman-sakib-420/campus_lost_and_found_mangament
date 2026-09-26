package app.ui;

import app.model.Admin;
import app.model.Item;
import app.service.ItemService;
import java.util.List;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class AdminDashboardLayout {

        private Scene scene;
        private BorderPane root;

        public AdminDashboardLayout(Stage stage, Admin admin) {

                root = new BorderPane();

                /*
                 * =========================
                 * SIDEBAR (NAVIGATION ONLY)
                 * =========================
                 */
                VBox sidebar = new VBox(15);
                sidebar.getStyleClass().add("sidebar");
                sidebar.setPadding(new Insets(30, 20, 20, 20));
                sidebar.setPrefWidth(260);

                Label sidebarTitle = new Label("Admin Panel");
                sidebarTitle.getStyleClass().add("sidebar-title");
                sidebarTitle.setMaxWidth(Double.MAX_VALUE);

                Button dashboardBtn = new Button("Dashboard");
                Button viewItemsBtn = new Button("View All Items");
                Button manageItemsBtn = new Button("Manage Lost & Found");
                Button analyticsBtn = new Button("Analytics");
                Button logoutBtn = new Button("Logout");

                // Apply style class to sidebar buttons
                for (Button b : new Button[] {
                                dashboardBtn, viewItemsBtn,
                                manageItemsBtn, analyticsBtn, logoutBtn
                }) {
                        b.setMaxWidth(Double.MAX_VALUE);
                        b.setGraphicTextGap(10);
                        // Icons could be added here
                }

                sidebar.getChildren().addAll(
                                sidebarTitle,
                                dashboardBtn,
                                viewItemsBtn,
                                manageItemsBtn,
                                analyticsBtn,
                                new Region(), // Spacer
                                logoutBtn);
                VBox.setVgrow(sidebar.getChildren().get(5), javafx.scene.layout.Priority.ALWAYS);

                root.setLeft(sidebar);

                /*
                 * =========================
                 * DASHBOARD CONTENT
                 * =========================
                 */
                VBox home = createDashboardContent();
                ScrollPane scrollPane = new ScrollPane(home);
                scrollPane.setFitToWidth(true);
                scrollPane.setStyle("-fx-background-color: transparent;");
                root.setCenter(scrollPane);

                /*
                 * =========================
                 * NAVIGATION ACTIONS
                 * =========================
                 */
                dashboardBtn.setOnAction(e -> root.setCenter(scrollPane));

                viewItemsBtn.setOnAction(e -> root.setCenter(new ViewItemsView(stage, admin)
                                .getScene().getRoot()));

                manageItemsBtn.setOnAction(e -> root.setCenter(new AdminManageItemsView(stage, admin)
                                .getScene().getRoot()));

                analyticsBtn.setOnAction(e -> showAnalytics());

                logoutBtn.setOnAction(e -> stage.setScene(new LoginView(stage).getScene()));

                scene = new Scene(root, 1200, 750);
                scene.getStylesheets().add(
                                getClass().getResource("/styles.css").toExternalForm());
        }

        /*
         * =========================
         * ADMIN DASHBOARD CONTENT
         * =========================
         */
        private VBox createDashboardContent() {

                VBox container = new VBox(35);
                container.setPadding(new Insets(50));

                // Welcome Banner
                VBox banner = new VBox(10);
                banner.setPadding(new Insets(30));
                banner.setStyle("-fx-background-color: linear-gradient(to right, #4f46e5, #818cf8); -fx-background-radius: 15;");

                Label heading = new Label("Lost and Found Administration");
                heading.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

                Label subHeading = new Label(
                                "Manage, verify, and track lost items across Khulna University of Engineering & Technology.");
                subHeading.setStyle("-fx-font-size: 16px; -fx-text-fill: #e0e7ff;");

                banner.getChildren().addAll(heading, subHeading);

                // Info Section
                Label sectionTitle = new Label("Quick Overview");
                sectionTitle.getStyleClass().add("title");

                HBox infoCards = new HBox(20);
                VBox card1 = createInfoCard("System Overview", "The administrative module manages all lost and found records reported within the campus. It ensures proper verification.");
                VBox card2 = createInfoCard("Administrator Responsibilities", "Administrators verify reported items, manage item status, coordinate returns, and ensure data accuracy.");
                VBox card3 = createInfoCard("Security and Access Control", "Only administrators can view personal contact details of users. This maintains privacy across the platform.");

                infoCards.getChildren().addAll(card1, card2, card3);

                Label flowTitle = new Label("Workflow");
                flowTitle.getStyleClass().add("title");

                HBox flowCards = new HBox(20);
                VBox flow1 = createInfoCard("Review Reports", "All submissions are reviewed for authenticity.");
                VBox flow2 = createInfoCard("Match and Verify", "Items are cross-checked for matches.");
                VBox flow3 = createInfoCard("Approve Return", "Items are approved for return.");

                flowCards.getChildren().addAll(flow1, flow2, flow3);

                // Live Campus Weather Widget (HTTP REST API / Jackson JSON / Concurrency)
                CampusWeatherWidget weatherWidget = new CampusWeatherWidget();

                // Dynamic & Responsive Property Constraints relative to window/container width
                weatherWidget.prefWidthProperty().bind(container.widthProperty().subtract(20));
                banner.prefWidthProperty().bind(container.widthProperty().subtract(20));

                for (VBox c : new VBox[]{card1, card2, card3, flow1, flow2, flow3}) {
                    c.prefWidthProperty().bind(container.widthProperty().subtract(80).divide(3));
                }

                container.getChildren().addAll(
                                banner,
                                weatherWidget,
                                sectionTitle,
                                infoCards,
                                flowTitle,
                                flowCards);

                return container;
        }

        /*
         * =========================
         * INFO CARD WITH SVG ICON
         * =========================
         */
        private VBox createInfoCard(String titleText, String contentText) {

                SVGPath icon = new SVGPath();
                icon.setContent(getIconPath(titleText));
                icon.setFill(Color.web("#4f46e5"));
                icon.setScaleX(1.1);
                icon.setScaleY(1.1);

                Label title = new Label(titleText);
                title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #111827;");

                HBox titleBox = new HBox(15, icon, title);
                titleBox.setAlignment(Pos.CENTER_LEFT);

                Label content = new Label(contentText);
                content.setWrapText(true);
                content.setStyle("-fx-font-size: 14px; -fx-text-fill: #6b7280;");

                VBox card = new VBox(15, titleBox, content);
                card.getStyleClass().add("item-card");
                card.setPrefWidth(320);
                card.setMinHeight(160);
                card.setAlignment(Pos.TOP_LEFT);

                return card;
        }

        /*
         * =========================
         * SVG ICON PATHS
         * =========================
         */
        private String getIconPath(String title) {

                if (title.contains("Overview")) {
                        return "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 "
                                        + "10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z";
                }

                if (title.contains("Responsibilities")) {
                        return "M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4"
                                        + "-4 1.79-4 4 1.79 4 4 4z";
                }

                if (title.contains("Security") || title.contains("Privacy")) {
                        return "M12 2l8 4v6c0 5.55-3.84 10.74-8 12"
                                        + "-4.16-1.26-8-6.45-8-12V6l8-4z";
                }

                return "M4 6h4v4H4V6zm6 0h4v4h-4V6zm6 0h4v4h-4V6z";
        }

        /*
         * =========================
         * ANALYTICS VIEW
         * =========================
         */
        private void showAnalytics() {

                List<Item> allItems = ItemService.getAllItems();
                int total = allItems.size();
                long lost = allItems.stream().filter(i -> "LOST".equalsIgnoreCase(i.getType())).count();
                long found = allItems.stream().filter(i -> "FOUND".equalsIgnoreCase(i.getType())).count();
                long returned = allItems.stream().filter(i -> "RETURNED".equalsIgnoreCase(i.getStatus())).count();

                Label title = new Label("System Analytics");
                title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold;");

                Label stats = new Label(
                                "Total Items Reported : " + total + "\n" +
                                                "Lost Items           : " + lost + "\n" +
                                                "Found Items          : " + found + "\n" +
                                                "Returned Items       : " + returned);

                PieChart chart = new PieChart();
                chart.getData().addAll(
                                new PieChart.Data("Lost", lost),
                                new PieChart.Data("Found", found),
                                new PieChart.Data("Returned", returned));

                VBox analytics = new VBox(20, title, stats, chart);
                analytics.setPadding(new Insets(40));

                root.setCenter(analytics);
        }

        public Scene getScene() {
                return scene;
        }
}
