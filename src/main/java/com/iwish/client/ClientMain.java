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
        primaryStage.setTitle("i-Wish - Login");
        // Loading login css if needed, usually done in FXML or here
        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/iwish/login/styles/login.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}