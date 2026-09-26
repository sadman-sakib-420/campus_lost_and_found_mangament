package app.ui;

import app.ui.AdminManageItemsView;
import app.model.Admin;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class AdminDashboard {

    private Scene scene;

    public AdminDashboard(Stage stage, Admin admin) {

        Label title = new Label("Admin Dashboard");
        title.getStyleClass().add("title");

        Label welcome = new Label("Welcome Admin");

        Button viewAllItemsBtn = new Button("View All Items");
        Button manageItemsBtn = new Button("Manage Lost & Found");
        Button logoutBtn = new Button("Logout");

        viewAllItemsBtn.getStyleClass().add("primary-btn");
        manageItemsBtn.getStyleClass().add("primary-btn");

        logoutBtn.setOnAction(e -> {
            LoginView loginView = new LoginView(stage);
            stage.setScene(loginView.getScene());
        });

        manageItemsBtn.setOnAction(e -> {
            AdminManageItemsView view = new AdminManageItemsView(stage, admin);
            stage.setScene(view.getScene());
        });

        CampusWeatherWidget weatherWidget = new CampusWeatherWidget();

        VBox root = new VBox(20,
                title,
                welcome,
                weatherWidget,
                viewAllItemsBtn,
                manageItemsBtn,
                logoutBtn
        );
        weatherWidget.prefWidthProperty().bind(root.widthProperty().multiply(0.7));

        root.setAlignment(Pos.CENTER);

        scene = new Scene(root, 900, 550);
        scene.getStylesheets().add(
                getClass().getResource("/styles.css").toExternalForm()
        );
    }

    public Scene getScene() {
        return scene;
    }
}
