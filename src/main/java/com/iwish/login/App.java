package com.iwish.login;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class App extends Application {

    private static Scene scene;

    @Override
    public void start(Stage stage) throws IOException {
        scene = new Scene(loadFXML("login_view"), 1024, 768);
        // Make the scene background transparent for the floating card effect
        scene.setFill(Color.TRANSPARENT);

        // Optional: Load CSS
        scene.getStylesheets().add(App.class.getResource("/com/iwish/login/styles/login.css").toExternalForm());

        stage.setScene(scene);
        stage.setTitle("i-Wish Login");
        stage.show();
    }

    private static Parent loadFXML(String fxml) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/com/iwish/login/fxml/" + fxml + ".fxml"));
        return fxmlLoader.load();
    }

    public static void main(String[] args) {
        launch();
    }
}
