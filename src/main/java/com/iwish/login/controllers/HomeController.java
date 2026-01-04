package com.iwish.login.controllers;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeController {

    @FXML
    private Button homeNavButton;
    @FXML
    private Button wishlistNavButton;
    @FXML
    private Button friendsNavButton;
    @FXML
    private Button logoutButton;

    @FXML
    private StackPane contentArea;
    // Removed @FXML homeView as it's not in FXML
    private Parent friendsView;

    @FXML
    public void initialize() {
        // Default state: Home active
        setActiveButton(homeNavButton);
        // Maybe load a welcome label or keep empty
    }

    @FXML
    private void handleHomeNav() {
        setActiveButton(homeNavButton);
        contentArea.getChildren().clear();
        // contentArea.getChildren().add(homeView); // Removed until homeView is defined
    }

    @FXML
    private void handleWishlistNav() {
        // Disabled in UI, but safe guard
        setActiveButton(wishlistNavButton);
        // TODO: Load Wishlist View
    }

    @FXML
    private void handleFriendsNav() {
        setActiveButton(friendsNavButton);

        try {
            if (friendsView == null) {
                // Lazy loading Friends View
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iwish/login/fxml/friends_view.fxml"));
                friendsView = loader.load();
            }
            contentArea.getChildren().clear();
            contentArea.getChildren().add(friendsView);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Friends view: " + e.getMessage());
            // Optionally show error in UI
        }
    }

    @FXML
    private void handleLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iwish/login/fxml/login_view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root, 1024, 768);
            scene.getStylesheets().add(getClass().getResource("/com/iwish/login/styles/login.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("i-Wish - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeButton) {
        // Reset all buttons style
        homeNavButton.getStyleClass().remove("sidebar-button-active");
        wishlistNavButton.getStyleClass().remove("sidebar-button-active");
        friendsNavButton.getStyleClass().remove("sidebar-button-active");

        // Set active
        activeButton.getStyleClass().add("sidebar-button-active");
    }
}
