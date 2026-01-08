package com.iwish.login.controllers;

import com.iwish.client.NetworkManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class HomeController {

    @FXML
    private Button homeNavButton;
    @FXML
    private Button wishlistNavButton;
    @FXML
    private Button friendsNavButton;
    @FXML
    private Button notificationsNavButton;
    @FXML
    private Button logoutButton;

    @FXML
    private Label welcomeLabel;
    @FXML
    private Label notificationsBadgeLabel;
    @FXML
    private Label notificationsBadgeSidebar;

    @FXML
    private StackPane contentArea;

    private Timer notificationsTimer;
    // private Parent homeView; // TODO: Implement Home View separate FXML or Keep
    // current logic

    @FXML
    public void initialize() {
        // Set welcome message
        NetworkManager nm = NetworkManager.getInstance();
        if (welcomeLabel != null) {
            welcomeLabel.setText("Welcome, " + nm.getCurrentUsername());
        }

        // Default state: Home active
        handleHomeNav();

        startNotificationBadgeTimer();
    }

    @FXML
    private void handleHomeNav() {
        // Mark friend notifications as read when leaving Friends view
        markFriendNotificationsAsRead();
        setActiveButton(homeNavButton);
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/iwish/login/fxml/marketplace_content.fxml"));
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
        markFriendNotificationsAsRead();
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
            // Reload friends view every time to refresh data
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iwish/login/fxml/friends_view.fxml"));
            Parent friendsView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(friendsView);

            // NOTE: We DO NOT mark notifications as read here anymore.
            // They remain unread so the "New Friends" section persists.

        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Friends view: " + e.getMessage());
            showError("Error loading Friends View");
        }
    }

    @FXML
    private void handleNotificationsNav() {
        markFriendNotificationsAsRead();
        setActiveButton(notificationsNavButton);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iwish/client/notifications_view.fxml"));
            Parent notificationsView = loader.load();
            contentArea.getChildren().clear();
            contentArea.getChildren().add(notificationsView);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Error loading Notifications view: " + e.getMessage());
            showError("Error loading Notifications View");
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
            stopNotificationBadgeTimer();
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
        if (homeNavButton != null)
            homeNavButton.getStyleClass().remove("sidebar-button-active");
        if (wishlistNavButton != null)
            wishlistNavButton.getStyleClass().remove("sidebar-button-active");
        if (friendsNavButton != null)
            friendsNavButton.getStyleClass().remove("sidebar-button-active");
        if (notificationsNavButton != null)
            notificationsNavButton.getStyleClass().remove("sidebar-button-active");

        if (activeButton != null)
            activeButton.getStyleClass().add("sidebar-button-active");
    }

    private void startNotificationBadgeTimer() {
        if (notificationsTimer != null) {
            return;
        }
        notificationsTimer = new Timer(true);
        notificationsTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                refreshNotificationBadge();
            }
        }, 0, 1000);
    }

    private void stopNotificationBadgeTimer() {
        if (notificationsTimer != null) {
            notificationsTimer.cancel();
            notificationsTimer = null;
        }
    }

    @FXML
    private Label friendsBadgeSidebar;

    // ... existing fields ...

    private void refreshNotificationBadge() {
        NetworkManager nm = NetworkManager.getInstance();
        if (!nm.isConnected()) {
            return;
        }
        int userId = nm.getCurrentUserId();

        // 1. Check for Pending Friend Requests
        java.util.List<com.iwish.models.FriendRequest> requests = nm.getFriendRequests(userId);
        boolean hasPendingRequests = !requests.isEmpty();

        // 2. Check for "Accepted" notifications (Your request to X was accepted)
        java.util.List<com.iwish.models.Notification> list = nm.getNotifications(userId);

        // Filter out "Accepted" notifications from the general count
        // User requested: "no need for notification regard ccepted friend request ...
        // on notification button"
        long unread = list.stream()
                .filter(n -> !n.isRead())
                .filter(n -> n.getMessage() == null || !n.getMessage().toLowerCase().contains("accepted"))
                .count();

        boolean hasAcceptedNotifications = list.stream()
                .anyMatch(n -> !n.isRead() && n.getMessage() != null
                        && n.getMessage().toLowerCase().contains("accepted"));

        Platform.runLater(() -> {
            // Update General Notifications Header Badge
            if (notificationsBadgeLabel != null) {
                if (unread > 0) {
                    notificationsBadgeLabel.setText(String.valueOf(unread));
                    notificationsBadgeLabel.setVisible(true);
                } else {
                    notificationsBadgeLabel.setText("");
                    notificationsBadgeLabel.setVisible(false);
                }
            }
            // Update Sidebar Notifications Badge
            if (notificationsBadgeSidebar != null) {
                if (unread > 0) {
                    notificationsBadgeSidebar.setText(String.valueOf(unread));
                    notificationsBadgeSidebar.setVisible(true);
                } else {
                    notificationsBadgeSidebar.setText("");
                    notificationsBadgeSidebar.setVisible(false);
                }
            }

            // Update Friends Sidebar Badge (Red Dot)
            if (friendsBadgeSidebar != null) {
                if (hasPendingRequests || hasAcceptedNotifications) {
                    friendsBadgeSidebar.setVisible(true);
                } else {
                    friendsBadgeSidebar.setVisible(false);
                }
            }
        });
    }

    private void markFriendNotificationsAsRead() {
        NetworkManager nm = NetworkManager.getInstance();
        if (!nm.isConnected())
            return;

        new Thread(() -> {
            int userId = nm.getCurrentUserId();
            java.util.List<com.iwish.models.Notification> list = nm.getNotifications(userId);

            for (com.iwish.models.Notification n : list) {
                // If notification is about accepted friend request (the condition for the
                // badge)
                if (!n.isRead() && n.getMessage() != null && n.getMessage().toLowerCase().contains("accepted")) {
                    nm.markNotificationRead(n.getNotificationId());
                }
            }
            // Force immediate badge refresh
            refreshNotificationBadge();
        }).start();
    }
}
