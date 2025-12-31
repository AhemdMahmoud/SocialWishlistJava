package com.iwish.client;

import javafx.application.Application;
import javafx.stage.Stage;

public class ClientMain extends Application {

    @Override
    public void start(Stage primaryStage) {
        System.out.println("Client UI starting...");

        // TODO: Initialize your Login Controller or Dashboard here.
        // NetworkManager.getInstance().connect();

        primaryStage.setTitle("i-Wish Client");
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
