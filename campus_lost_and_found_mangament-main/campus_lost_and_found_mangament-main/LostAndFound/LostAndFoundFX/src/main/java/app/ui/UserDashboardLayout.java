package app.ui;

import app.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class UserDashboardLayout {

        private Scene scene;
        private BorderPane root;

        public UserDashboardLayout(Stage stage, User user) {

                root = new BorderPane();

                /*
                 * =========================
                 * SIDEBAR (NAVIGATION ONLY)
                 * =========================
                 */
                VBox sidebar = new VBox(15);
                sidebar.getStyleClass().add("sidebar");
                sidebar.setPadding(new Insets(30, 20, 20, 20));
                sidebar.setPrefWidth(240);

                Label title = new Label("Lost & Found");
                title.getStyleClass().add("sidebar-title");
                title.setMaxWidth(Double.MAX_VALUE);

                Button dashboardBtn = new Button("Dashboard");
                Button reportLostBtn = new Button("Report Lost Item");
                Button reportFoundBtn = new Button("Report Found Item");
                Button viewItemsBtn = new Button("View Items");
                Button logoutBtn = new Button("Logout");

                for (Button b : new Button[] {
                                dashboardBtn, reportLostBtn,
                                reportFoundBtn, viewItemsBtn, logoutBtn
                }) {
                        b.setMaxWidth(Double.MAX_VALUE);
                        b.setGraphicTextGap(10);
                }

                sidebar.getChildren().addAll(
                                title,
                                dashboardBtn,
                                reportLostBtn,
                                reportFoundBtn,
                                viewItemsBtn,
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

                reportLostBtn.setOnAction(e -> root.setCenter(new ReportLostView(stage, user)
                                .getScene().getRoot()));

                reportFoundBtn.setOnAction(e -> root.setCenter(new ReportFoundView(stage, user)
                                .getScene().getRoot()));

                viewItemsBtn.setOnAction(e -> root.setCenter(new ViewItemsView(stage, user)
                                .getScene().getRoot()));

                logoutBtn.setOnAction(e -> stage.setScene(new LoginView(stage).getScene()));

                scene = new Scene(root, 1100, 650);
                scene.getStylesheets().add(
                                getClass().getResource("/styles.css").toExternalForm());
        }

        /*
         * =========================
         * DASHBOARD INFORMATION UI
         * =========================
         */
        private VBox createDashboardContent() {

                VBox container = new VBox(35);
                container.setPadding(new Insets(50));

                // Welcome Banner
                VBox banner = new VBox(10);
                banner.setPadding(new Insets(30));
                banner.setStyle("-fx-background-color: linear-gradient(to right, #2563eb, #60a5fa); -fx-background-radius: 15;");

                Label heading = new Label("Campus Lost and Found System");
                heading.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: white;");

                Label subHeading = new Label(
                                "Report, track, and recover your lost belongings at Khulna University of Engineering & Technology.");
                subHeading.setStyle("-fx-font-size: 16px; -fx-text-fill: #eff6ff;");

                banner.getChildren().addAll(heading, subHeading);

                /* ===== TOP INFO CARDS ===== */
                Label sectionTitle = new Label("How it Works");
                sectionTitle.getStyleClass().add("title");

                HBox infoCards = new HBox(20);
                VBox card1 = createInfoCard("System Overview", "This application provides a centralized platform for reporting and tracking lost and found items within the campus.");
                VBox card2 = createInfoCard("How the System Works", "Users report lost or found items with relevant details and images. The administrator verifies the information.");
                VBox card3 = createInfoCard("User Responsibilities", "Users must provide accurate item information and regularly check item status through the system.");

                infoCards.getChildren().addAll(card1, card2, card3);

                /* ===== FLOW SECTION ===== */
                Label flowTitle = new Label("How Items Are Returned");
                flowTitle.getStyleClass().add("title");

                HBox flowCards = new HBox(20);
                VBox flow1 = createInfoCard("Report Item", "A user reports a lost or found item by submitting item details and an image.");
                VBox flow2 = createInfoCard("Verification", "The administrator verifies the reported details and checks for possible matches.");
                VBox flow3 = createInfoCard("Return Process", "Once verified, the item is safely returned to the rightful owner.");

                flowCards.getChildren().addAll(flow1, flow2, flow3);

                VBox privacyCard = createInfoCard(
                                "Privacy and Security",
                                "Personal contact details are visible only to the administrator. This ensures user privacy and prevents misuse of information.");

                // Live Campus Weather Widget (HTTP REST API / Jackson JSON / Concurrency)
                CampusWeatherWidget weatherWidget = new CampusWeatherWidget();

                // Dynamic & Responsive Property Constraints relative to window/container width
                weatherWidget.prefWidthProperty().bind(container.widthProperty().subtract(20));
                banner.prefWidthProperty().bind(container.widthProperty().subtract(20));
                privacyCard.prefWidthProperty().bind(container.widthProperty().subtract(20));

                for (VBox c : new VBox[]{card1, card2, card3, flow1, flow2, flow3}) {
                    c.prefWidthProperty().bind(container.widthProperty().subtract(80).divide(3));
                }

                container.getChildren().addAll(
                                banner,
                                weatherWidget,
                                sectionTitle,
                                infoCards,
                                flowTitle,
                                flowCards,
                                privacyCard);

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
                icon.setFill(Color.web("#1e40af"));
                icon.setScaleX(0.8);
                icon.setScaleY(0.8);

                Label title = new Label(titleText);
                title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

                HBox titleBox = new HBox(10, icon, title);
                titleBox.setAlignment(Pos.CENTER_LEFT);

                Label content = new Label(contentText);
                content.setWrapText(true);
                content.setStyle("-fx-font-size: 13px;");

                VBox card = new VBox(12, titleBox, content);
                card.setPadding(new Insets(20));
                card.setPrefWidth(300);
                card.getStyleClass().add("item-card");
                card.setAlignment(Pos.TOP_LEFT);

                return card;
        }

        /*
         * =========================
         * SVG ICON PATHS
         * =========================
         */
        private String getIconPath(String title) {

                if (title.contains("System Overview")) {
                        return "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 "
                                        + "10-4.48 10-10S17.52 2 12 2zm1 15h-2v-6h2v6zm0-8h-2V7h2v2z";
                }

                if (title.contains("How the System Works")) {
                        return "M4 6h4v4H4V6zm6 0h4v4h-4V6zm6 0h4v4h-4V6z"
                                        + "M4 14h4v4H4v-4zm6 0h4v4h-4v-4zm6 0h4v4h-4v-4z";
                }

                if (title.contains("Privacy")) {
                        return "M12 2l8 4v6c0 5.55-3.84 10.74-8 12"
                                        + "-4.16-1.26-8-6.45-8-12V6l8-4z";
                }

                return "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10";
        }

        public Scene getScene() {
                return scene;
        }
}
