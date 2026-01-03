package com.iwish.client;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class ClientMain extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        System.out.println("Client UI starting...");
        
        // TODO: Initialize your Login Controller or Dashboard here.
        // NetworkManager.getInstance().connect();
        
//        // Create and show the Wishlist page
        WishlistPage wishlistPage = new WishlistPage(primaryStage);
        Scene scene     = wishlistPage.createWishlistScene();
//        
        // To show Friend's Profile page:
//        FriendProfilePage friendPage = new FriendProfilePage(primaryStage, "Ahmed");
//        Scene scene = friendPage.createFriendProfileScene();
        
        
        primaryStage.setTitle("i-Wish Client");
        primaryStage.setScene(scene);
        primaryStage.show();

           
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}