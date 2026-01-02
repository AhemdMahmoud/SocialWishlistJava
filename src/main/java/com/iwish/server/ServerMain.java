package com.iwish.server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ServerMain extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/com/iwish/server/server_view.fxml"));
        primaryStage.setTitle("i-Wish Server Admin");
        primaryStage.setScene(new Scene(root));
        primaryStage.setOnCloseRequest(e -> System.exit(0)); // Ensure full shutdown
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
