package app.ui;

import app.dao.DAOFactory;
import app.dao.UserDAO;
import app.model.Admin;
import app.model.User;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.stage.Stage;

public class LoginView {

    private Scene scene;

    public LoginView(Stage stage) {

        /*
         * =========================
         * LEFT MENU (BRANDING)
         * =========================
         */
        VBox brandingPanel = new VBox(20);
        brandingPanel.setPadding(new Insets(40));
        brandingPanel.setAlignment(Pos.CENTER_LEFT);
        brandingPanel.setStyle("-fx-background-color: linear-gradient(to bottom right, #1e1b4b, #312e81);");
        brandingPanel.setPrefWidth(400);

        SVGPath logoIcon = new SVGPath();
        logoIcon.setContent(
                "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm-1 17.93c-3.95-.49-7-3.85-7-7.93 0-.62.08-1.21.21-1.79L9 15v1c0 1.1.9 2 2 2v1.93zm6.9-2.54c-.26-.81-1-1.39-1.9-1.39h-1v-3c0-.55-.45-1-1-1H8v-2h2c.55 0 1-.45 1-1V7h2c1.1 0 2-.9 2-2v-.41c2.93 1.19 5 4.06 5 7.41 0 2.08-.8 3.97-2.1 5.39z");
        logoIcon.setFill(Color.WHITE);
        logoIcon.setScaleX(3);
        logoIcon.setScaleY(3);

        Label brandTitle = new Label("Campus\nLost & Found");
        brandTitle.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");

        Label brandSubtitle = new Label("Streamline lost item recovery with\nour digital campus solution.");
        brandSubtitle.setStyle("-fx-font-size: 16px; -fx-text-fill: #a5b4fc; -fx-wrap-text: true;");

        brandingPanel.getChildren().addAll(logoIcon, new Region(), brandTitle, brandSubtitle);
        VBox.setVgrow(brandingPanel.getChildren().get(1), Priority.ALWAYS); // Spacer
        ((Region) brandingPanel.getChildren().get(1)).setMaxHeight(50); // limit spacer

        /*
         * =========================
         * RIGHT MENU (LOGIN FORM)
         * =========================
         */
        VBox loginForm = new VBox(20);
        loginForm.setAlignment(Pos.CENTER);
        loginForm.setPadding(new Insets(50));
        loginForm.setStyle("-fx-background-color: white;");
        HBox.setHgrow(loginForm, Priority.ALWAYS);

        Label loginHeader = new Label("Welcome Back");
        loginHeader.getStyleClass().add("title");

        Label loginSub = new Label("Please enter your details to sign in.");
        loginSub.getStyleClass().add("subtitle");

        /* -- Fields -- */
        VBox fieldsBox = new VBox(15);
        fieldsBox.setMaxWidth(350);

        Label userLabel = new Label("Username");
        userLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");
        TextField usernameField = new TextField();
        usernameField.setPromptText("Enter your username");

        Label passLabel = new Label("Password");
        passLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter your password");

        Button loginBtn = new Button("Sign In");
        loginBtn.getStyleClass().add("primary-btn");
        loginBtn.setMaxWidth(Double.MAX_VALUE);

        Label message = new Label();
        message.setStyle("-fx-text-fill: #dc2626; -fx-font-size: 13px;");

        fieldsBox.getChildren().addAll(
                userLabel, usernameField,
                passLabel, passwordField,
                new Region(), // spacer
                loginBtn, message);
        ((Region) fieldsBox.getChildren().get(4)).setMinHeight(10);

        loginForm.getChildren().addAll(loginHeader, loginSub, fieldsBox);

        /*
         * =========================
         * MAIN LAYOUT
         * =========================
         */
        HBox root = new HBox(brandingPanel, loginForm);
        root.setPrefSize(1000, 600); // Default window size

        /*
         * =========================
         * LOGIN LOGIC
         * =========================
         */
        UserDAO userDAO = DAOFactory.getUserDAO();

        loginBtn.setOnAction(e -> {
            String user = usernameField.getText();
            String pass = passwordField.getText();

            if (user.trim().isEmpty() || pass.isEmpty()) {
                message.setText("Please enter username and password.");
                usernameField.setStyle("-fx-border-color: #dc2626;");
                passwordField.setStyle("-fx-border-color: #dc2626;");
                return;
            }

            try {
                User authenticatedUser = userDAO.authenticate(user.trim(), pass);

                if (authenticatedUser instanceof Admin) {
                    Admin admin = (Admin) authenticatedUser;
                    AdminDashboardLayout layout = new AdminDashboardLayout(stage, admin);
                    stage.setScene(layout.getScene());
                    centerStage(stage);

                } else if (authenticatedUser != null) {
                    UserDashboardLayout layout = new UserDashboardLayout(stage, authenticatedUser);
                    stage.setScene(layout.getScene());
                    centerStage(stage);

                } else {
                    message.setText("Invalid username or password.");
                    usernameField.setStyle("-fx-border-color: #dc2626;");
                    passwordField.setStyle("-fx-border-color: #dc2626;");
                }
            } catch (Exception ex) {
                message.setText("Database connection error.");
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Database Error");
                alert.setHeaderText("Database Connection Failed");
                alert.setContentText("Could not access the data storage.\n\nPlease ensure:\n1. 'lost_found_db.db' exists and db.url in db.properties is correct (for items).\n2. users.json is writable in the project root (for user accounts).\n\nDetails: " + ex.getMessage());
                alert.showAndWait();
            }
        });

        // Reset borders on type
        usernameField.setOnKeyTyped(e -> usernameField.setStyle(""));
        passwordField.setOnKeyTyped(e -> passwordField.setStyle(""));

        scene = new Scene(root, 1000, 600);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        stage.setTitle("Lost & Found - Login");
    }

    private void centerStage(Stage stage) {
        // Helper to re-center if size changes significantly
        javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
        stage.setX((screenBounds.getWidth() - stage.getWidth()) / 2);
        stage.setY((screenBounds.getHeight() - stage.getHeight()) / 2);
    }

    public Scene getScene() {
        return scene;
    }
}
