package com.iwish.client;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load Login View
        Parent root = FXMLLoader.load(getClass().getResource("/com/iwish/login/fxml/login_view.fxml"));

        Scene scene = new Scene(root, 1024, 768);
        scene.getStylesheets().add(getClass().getResource("/com/iwish/login/styles/login.css").toExternalForm());

        primaryStage.setTitle("i-Wish - Login");
        primaryStage.setScene(scene);
        primaryStage.centerOnScreen();
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}