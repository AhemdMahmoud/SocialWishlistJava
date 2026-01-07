package com.iwish.client;

import com.iwish.models.Notification;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.format.DateTimeFormatter;

public class NotificationsController {

    @FXML
    private ListView<Notification> notificationsList;
    @FXML
    private Button refreshButton;
    @FXML
    private Label statusLabel;

    private final NetworkManager networkManager = NetworkManager.getInstance();
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    public void initialize() {
        notificationsList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(Notification n, boolean empty) {
                super.updateItem(n, empty);
                if (empty || n == null) {
                    setText(null);
                } else {
                    String dateText = n.getNotificationDate() != null
                            ? n.getNotificationDate().toLocalDateTime().format(formatter)
                            : "";
                    String prefix = n.isRead() ? "" : "[NEW] ";
                    setText(prefix + dateText + " - " + n.getMessage());
                }
            }
        });
        loadNotifications();
    }

    @FXML
    private void handleRefresh() {
        loadNotifications();
    }

    @FXML
    private void handleMarkSelectedAsRead() {
        Notification selected = notificationsList.getSelectionModel().getSelectedItem();
        if (selected == null || selected.isRead()) {
            return;
        }
        new Thread(() -> {
            boolean ok = networkManager.markNotificationRead(selected.getNotificationId());
            Platform.runLater(() -> {
                if (ok) {
                    selected.setRead(true);
                    notificationsList.refresh();
                    if (statusLabel != null) {
                        statusLabel.setText("Marked as read");
                    }
                } else if (statusLabel != null) {
                    statusLabel.setText("Failed to mark as read");
                }
            });
        }).start();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/com/iwish/login/fxml/home_view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) notificationsList.getScene().getWindow();
            Scene scene = new Scene(root, 1024, 768);
            scene.getStylesheets().add(
                getClass().getResource("/com/iwish/login/styles/friends.css").toExternalForm());
            stage.setScene(scene);
            stage.setTitle("i-Wish - Home");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadNotifications() {
        if (!networkManager.isConnected()) {
            if (statusLabel != null) {
                statusLabel.setText("Not connected");
            }
            return;
        }
        int userId = networkManager.getCurrentUserId();
        new Thread(() -> {
            var list = networkManager.getNotifications(userId);
            ObservableList<Notification> obs = FXCollections.observableArrayList(list);
            Platform.runLater(() -> {
                notificationsList.setItems(obs);
                if (statusLabel != null) {
                    statusLabel.setText("Loaded " + obs.size() + " notifications");
                }
            });
        }).start();
    }
}