package com.iwish.login.controllers;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;
import com.iwish.client.NetworkManager;
import com.iwish.models.Friend;
import com.iwish.models.FriendRequest;

public class FriendsController {

    @FXML
    private ToggleButton myFriendsTab;
    @FXML
    private ToggleButton friendRequestsTab;
    @FXML
    private TextField searchField;
    @FXML
    private VBox myFriendsView;
    @FXML
    private VBox friendRequestsView;
    @FXML
    private VBox friendsList;
    @FXML
    private VBox requestsList;

    private ToggleGroup tabGroup;

    @FXML
    public void initialize() {
        setupTabs();
        loadFriends();
        loadRequests();
    }

    private void setupTabs() {
        tabGroup = new ToggleGroup();
        myFriendsTab.setToggleGroup(tabGroup);
        friendRequestsTab.setToggleGroup(tabGroup);

        tabGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if (newVal == null) {
                old.setSelected(true);
            }
        });
    }

    @FXML
    private void showMyFriends() {
        myFriendsView.setVisible(true);
        myFriendsView.setManaged(true);
        friendRequestsView.setVisible(false);
        friendRequestsView.setManaged(false);
        loadFriends(); // Refresh
    }

    @FXML
    private void showFriendRequests() {
        myFriendsView.setVisible(false);
        myFriendsView.setManaged(false);
        friendRequestsView.setVisible(true);
        friendRequestsView.setManaged(true);
        loadRequests(); // Refresh
    }

    @FXML
    private void handleSendRequest() {
        String username = searchField.getText().trim();
        if (!username.isEmpty()) {
            NetworkManager nm = NetworkManager.getInstance();
            int userId = nm.getCurrentUserId();

            new Thread(() -> {
                boolean success = nm.addFriend(userId, username);
                javafx.application.Platform.runLater(() -> {
                    if (success) {
                        searchField.clear();
                        showAlert(Alert.AlertType.INFORMATION, "Success", "Friend request sent to " + username);
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Error",
                                "Failed to send request. User may not exist or request already pending.");
                    }
                });
            }).start();
        }
    }

    private void loadFriends() {
        friendsList.getChildren().clear();
        NetworkManager nm = NetworkManager.getInstance();
        int userId = nm.getCurrentUserId();

        new Thread(() -> {
            java.util.List<Friend> friends = nm.getFriends(userId);
            javafx.application.Platform.runLater(() -> {
                if (friends.isEmpty()) {
                    Label emptyFunc = new Label("No friends yet. Search to add some!");
                    emptyFunc.setStyle("-fx-text-fill: #95a5a6; -fx-padding: 20;");
                    friendsList.getChildren().add(emptyFunc);
                } else {
                    for (Friend friend : friends) {
                        friendsList.getChildren().add(createFriendRow(friend));
                    }
                }
            });
        }).start();
    }

    private HBox createFriendRow(Friend friend) {
        HBox row = new HBox(15);
        row.getStyleClass().add("friend-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);

        // Avatar
        Circle avatar = new Circle(25);
        avatar.getStyleClass().add("avatar-circle");

        // Name
        Label nameLabel = new Label(friend.getUsername());
        nameLabel.getStyleClass().add("card-title");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Buttons
        Button viewProfileBtn = new Button("View Profile");
        viewProfileBtn.getStyleClass().add("action-button-blue");
        // TODO: Implement View Profile

        Button deleteBtn = new Button("🗑");
        deleteBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #95a5a6; -fx-font-size: 16px;");
        deleteBtn.setOnAction(e -> handleDeleteFriend(friend));

        row.getChildren().addAll(avatar, nameLabel, spacer, viewProfileBtn, deleteBtn);
        return row;
    }

    private void handleDeleteFriend(Friend friend) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Remove Friend");
        alert.setHeaderText("Remove " + friend.getUsername() + "?");
        alert.setContentText("Are you sure you want to remove this friend?");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                NetworkManager nm = NetworkManager.getInstance();
                int userId = nm.getCurrentUserId();

                new Thread(() -> {
                    boolean success = nm.removeFriend(userId, friend.getUserId());
                    javafx.application.Platform.runLater(() -> {
                        if (success) {
                            loadFriends(); // Refresh
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Error", "Failed to remove friend.");
                        }
                    });
                }).start();
            }
        });
    }

    private void loadRequests() {
        requestsList.getChildren().clear();
        NetworkManager nm = NetworkManager.getInstance();
        int userId = nm.getCurrentUserId();

        new Thread(() -> {
            java.util.List<FriendRequest> requests = nm.getFriendRequests(userId);
            javafx.application.Platform.runLater(() -> {
                if (requests.isEmpty()) {
                    Label emptyLabel = new Label("No pending requests.");
                    emptyLabel.setStyle("-fx-text-fill: #95a5a6; -fx-padding: 20;");
                    requestsList.getChildren().add(emptyLabel);
                } else {
                    friendRequestsTab.setText("Friend Requests (" + requests.size() + ")"); // Update count
                    for (FriendRequest request : requests) {
                        requestsList.getChildren().add(createRequestRow(request));
                    }
                }
            });
        }).start();
    }

    private HBox createRequestRow(FriendRequest request) {
        HBox row = new HBox(15);
        row.getStyleClass().add("friend-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);

        // Avatar
        Circle avatar = new Circle(25);
        avatar.getStyleClass().add("avatar-circle");

        // Name
        Label nameLabel = new Label(request.getRequesterUsername());
        nameLabel.getStyleClass().add("card-title");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Buttons
        HBox actions = new HBox(10);
        Button acceptBtn = new Button("Accept");
        acceptBtn.getStyleClass().add("action-button-green");
        acceptBtn.setOnAction(e -> handleAcceptRequest(request.getFriendshipId()));

        Button declineBtn = new Button("Decline");
        declineBtn.getStyleClass().add("action-button-red");
        declineBtn.setOnAction(e -> handleDeclineRequest(request.getFriendshipId()));

        actions.getChildren().addAll(acceptBtn, declineBtn);
        row.getChildren().addAll(avatar, nameLabel, spacer, actions);

        return row;
    }

    private void handleAcceptRequest(int friendshipId) {
        NetworkManager nm = NetworkManager.getInstance();
        new Thread(() -> {
            boolean success = nm.acceptFriendRequest(friendshipId);
            javafx.application.Platform.runLater(() -> {
                if (success) {
                    loadRequests(); // Refresh
                    loadFriends(); // Also refresh friends list if visible
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to accept request.");
                }
            });
        }).start();
    }

    private void handleDeclineRequest(int friendshipId) {
        NetworkManager nm = NetworkManager.getInstance();
        new Thread(() -> {
            boolean success = nm.declineFriendRequest(friendshipId);
            javafx.application.Platform.runLater(() -> {
                if (success) {
                    loadRequests(); // Refresh
                } else {
                    showAlert(Alert.AlertType.ERROR, "Error", "Failed to decline request.");
                }
            });
        }).start();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
