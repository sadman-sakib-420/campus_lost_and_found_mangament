package app.ui;

import app.ui.ReportLostView;
import app.ui.ReportFoundView;
import app.ui.ViewItemsView;
import app.model.User;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class UserDashboard {

    private Scene scene;

    public UserDashboard(Stage stage, User user) {

        Label title = new Label("User Dashboard");
        title.getStyleClass().add("title");

        Label welcome = new Label("Welcome, " + user.getUsername());

        Button reportLostBtn = new Button("Report Lost Item");
        Button reportFoundBtn = new Button("Report Found Item");
        Button logoutBtn = new Button("Logout");
        Button viewItemsBtn = new Button("View Items");

        reportLostBtn.getStyleClass().add("primary-btn");
        reportFoundBtn.getStyleClass().add("primary-btn");
        viewItemsBtn.getStyleClass().add("primary-btn");

        logoutBtn.setOnAction(e -> {
            LoginView loginView = new LoginView(stage);
            stage.setScene(loginView.getScene());
        });

        reportLostBtn.setOnAction(e -> {
            ReportLostView lostView = new ReportLostView(stage, user);
            stage.setScene(lostView.getScene());
        });

        reportFoundBtn.setOnAction(e -> {
            ReportFoundView foundView = new ReportFoundView(stage, user);
            stage.setScene(foundView.getScene());
        });

        viewItemsBtn.setOnAction(e -> {
            ViewItemsView view = new ViewItemsView(stage, user);
            stage.setScene(view.getScene());
        });

        CampusWeatherWidget weatherWidget = new CampusWeatherWidget();

        VBox root = new VBox(20,
                title,
                welcome,
                weatherWidget,
                reportLostBtn,
                reportFoundBtn,
                viewItemsBtn,
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

