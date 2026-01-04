package com.iwish.login.controllers;

import com.iwish.client.NetworkManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
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
    private Label welcomeLabel;

    @FXML
    private StackPane contentArea;
    
    private Parent friendsView;
    // private Parent homeView; // TODO: Implement Home View separate FXML or Keep current logic

    @FXML
    public void initialize() {
        // Set welcome message
        NetworkManager nm = NetworkManager.getInstance();
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + nm.getCurrentUsername());
        }
        
        // Default state: Home active
        handleHomeNav();
    }

    @FXML
    private void handleHomeNav() {
        setActiveButton(homeNavButton);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iwish/login/fxml/marketplace_content.fxml"));
            Parent marketplaceView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(marketplaceView);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading Marketplace");
        }
    }

    @FXML
    private void handleWishlistNav() {
        setActiveButton(wishlistNavButton);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iwish/login/fxml/wishlist_content.fxml"));
            Parent wishlistView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(wishlistView);
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error loading Wishlist");
        }
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
            showError("Error loading Friends View");
        }
    }
    
    private void showError(String message) {
        Label errorLabel = new Label(message);
        errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
        contentArea.getChildren().clear();
        contentArea.getChildren().add(errorLabel);
    }

    @FXML
    private void handleLogout() {
        try {
            // Close connection
            NetworkManager.getInstance().close();
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iwish/login/fxml/login_view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) logoutButton.getScene().getWindow();
            Scene scene = new Scene(root, 900, 600);
            scene.getStylesheets().add(getClass().getResource("/com/iwish/login/styles/login.css").toExternalForm());

            stage.setScene(scene);
            stage.setTitle("i-Wish - Login");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeButton) {
        // Reset all buttons style
        if (homeNavButton != null) homeNavButton.getStyleClass().remove("sidebar-button-active");
        if (wishlistNavButton != null) wishlistNavButton.getStyleClass().remove("sidebar-button-active");
        if (friendsNavButton != null) friendsNavButton.getStyleClass().remove("sidebar-button-active");

        // Set active
        if (activeButton != null) activeButton.getStyleClass().add("sidebar-button-active");
    }
}
