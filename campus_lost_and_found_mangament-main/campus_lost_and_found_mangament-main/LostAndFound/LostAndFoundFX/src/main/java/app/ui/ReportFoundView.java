package app.ui;

import app.model.FoundItem;
import app.model.User;
import app.service.ItemService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class ReportFoundView {

    private Scene scene;
    private File selectedImageFile;

    public ReportFoundView(Stage stage, User user) {

        /*
         * =========================
         * ROOT LAYOUT
         * =========================
         */
        VBox root = new VBox();
        root.setAlignment(Pos.TOP_CENTER);
        root.setStyle("-fx-background-color: #f3f4f6;");

        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        /*
         * =========================
         * CENTER CARD FORM
         * =========================
         */
        VBox formCard = new VBox(20);
        formCard.setMaxWidth(600);
        formCard.setPadding(new Insets(40));
        formCard.getStyleClass().add("item-card");
        formCard.setStyle(
                "-fx-background-color: white; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 0);");

        // Header
        Label title = new Label("Report a Found Item");
        title.getStyleClass().add("title");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #111827;");

        Text subtitle = new Text("Thank you for helping return a lost item. Please provide details below.");
        subtitle.setStyle("-fx-fill: #6b7280; -fx-font-size: 14px;");

        VBox headerBox = new VBox(5, title, subtitle);
        headerBox.setAlignment(Pos.CENTER);
        headerBox.setPadding(new Insets(0, 0, 20, 0));

        /*
         * =========================
         * FORM FIELDS
         * =========================
         */

        // Section: Item Details
        Label section1 = new Label("Item Details");
        section1.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #374151;");

        TextField itemName = createStyledTextField("Item Name (e.g., Red Umbrella)");
        TextField category = createStyledTextField("Category");
        TextField location = createStyledTextField("Location Found");

        TextArea description = new TextArea();
        description.setPromptText("Description of the item...");
        description.setPrefRowCount(3);
        description.setWrapText(true);
        description.getStyleClass().add("text-area");

        // Section: Contact Info
        Label section2 = new Label("Your Contact Information");
        section2.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #374151;");
        section2.setPadding(new Insets(10, 0, 0, 0));

        TextField reporterName = createStyledTextField("Your Full Name");

        HBox contactRow = new HBox(15);
        TextField phone = createStyledTextField("Phone Number");
        TextField email = createStyledTextField("Email Address");
        phone.setPrefWidth(300);
        email.setPrefWidth(300);
        contactRow.getChildren().addAll(phone, email);

        // Upload Image Section
        Label imageLabel = new Label("Item Image (Optional)");
        imageLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #374151;");

        Button uploadBtn = new Button("Choose Image File...");
        uploadBtn.getStyleClass().add("secondary-btn");
        uploadBtn.setMaxWidth(Double.MAX_VALUE);

        Label fileStatus = new Label("No file selected");
        fileStatus.setStyle("-fx-text-fill: #6b7280; -fx-font-size: 12px; -fx-font-style: italic;");

        uploadBtn.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select Item Image");
            chooser.getExtensionFilters()
                    .add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
            File file = chooser.showOpenDialog(stage);
            if (file != null) {
                selectedImageFile = file;
                fileStatus.setText("Selected: " + file.getName());
                fileStatus.setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;"); // Green text
            }
        });

        /*
         * =========================
         * ACTIONS
         * =========================
         */
        Button submitBtn = new Button("Submit Report");
        submitBtn.getStyleClass().add("primary-btn");
        submitBtn.setPrefWidth(200);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("button");
        cancelBtn.setStyle("-fx-text-fill: #6b7280; -fx-cursor: hand;");

        HBox actions = new HBox(20, cancelBtn, submitBtn);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setPadding(new Insets(20, 0, 0, 0));

        Label message = new Label();
        message.setWrapText(true);

        /*
         * =========================
         * LOGIC
         * =========================
         */
        submitBtn.setOnAction(e -> {
            if (itemName.getText().isEmpty() || location.getText().isEmpty()) {
                message.setText("Please fill in at least Item Name and Location.");
                message.setStyle("-fx-text-fill: #dc2626;");
                return;
            }

            String imgPath = (selectedImageFile != null) ? selectedImageFile.toURI().toString() : null;

            FoundItem item = new FoundItem(
                    itemName.getText(),
                    category.getText(),
                    location.getText(),
                    description.getText(),
                    imgPath,
                    reporterName.getText(),
                    phone.getText(),
                    email.getText());

            boolean success = ItemService.addItem(item);
            if (!success) {
                Alert errorAlert = new Alert(Alert.AlertType.ERROR, "Failed to save found item to database. Please check the SQLite database file.", ButtonType.OK);
                errorAlert.showAndWait();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Found item reported successfully!", ButtonType.OK);
            alert.showAndWait();

            stage.setScene(new UserDashboardLayout(stage, user).getScene());
        });

        cancelBtn.setOnAction(e -> stage.setScene(new UserDashboardLayout(stage, user).getScene()));

        /*
         * =========================
         * ASSEMBLE
         * =========================
         */
        formCard.getChildren().addAll(
                headerBox,
                section1, itemName, category, location, description,
                section2, reporterName, contactRow,
                imageLabel, uploadBtn, fileStatus,
                message,
                actions);

        VBox container = new VBox(formCard);
        container.setAlignment(Pos.CENTER);
        container.setPadding(new Insets(40));

        scrollPane.setContent(container);
        root.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        scene = new Scene(root, 1000, 700);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
    }

    private TextField createStyledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        return tf;
    }

    public Scene getScene() {
        return scene;
    }
}
