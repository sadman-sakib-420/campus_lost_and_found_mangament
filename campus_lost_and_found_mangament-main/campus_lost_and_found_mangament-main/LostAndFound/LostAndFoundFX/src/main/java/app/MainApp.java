package app;

import app.service.ItemService;
import app.ui.LoginView;
import javafx.application.Application;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) {

        // ✅ LOAD SAVED DATA (IMPORTANT)
        ItemService.init();

        // Launch directly to Login View
        LoginView loginView = new LoginView(stage);

        stage.setTitle("Campus Lost & Found System");
        stage.setScene(loginView.getScene());
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}
