package com.iwish.login.controllers;

import com.iwish.client.NetworkManager;
import com.iwish.models.Friend;
import com.iwish.models.FriendRequest;
import com.iwish.models.User;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

import java.util.List;

public class FriendsController {

    @FXML
    private ToggleButton myFriendsTab;
    @FXML
    private ToggleButton friendRequestsTab;
    @FXML
    private TextField searchField;
    @FXML
    private Button searchButton;

    @FXML
    private VBox myFriendsView;
    @FXML
    private VBox friendRequestsView;
    @FXML
    private VBox searchResultsView;

    @FXML
    private VBox friendsList;
    @FXML
    private VBox requestsList;
    @FXML
    private VBox searchResultsList;

    private ToggleGroup tabGroup;
    private NetworkManager networkManager;
    private List<Friend> currentFriends;
    private List<FriendRequest> currentRequests;

    @FXML
    public void initialize() {
        networkManager = NetworkManager.getInstance();
        setupTabs();
        loadMyFriends();
        loadFriendRequests();
    }

    private void setupTabs() {
        tabGroup = new ToggleGroup();
        myFriendsTab.setToggleGroup(tabGroup);
        friendRequestsTab.setToggleGroup(tabGroup);

        // Allow deselection for search mode
        // tabGroup.selectedToggleProperty().addListener((obs, old, newVal) -> {
        // if (newVal == null) {
        // old.setSelected(true);
        // }
        // });
    }

    @FXML
    private void showMyFriends() {
        myFriendsView.setVisible(true);
        myFriendsView.setManaged(true);
        friendRequestsView.setVisible(false);
        friendRequestsView.setManaged(false);
        searchResultsView.setVisible(false);
        searchResultsView.setManaged(false);

        searchField.setPromptText("Search your friends...");
        searchField.clear();
    }

    @FXML
    private void showFriendRequests() {
        myFriendsView.setVisible(false);
        myFriendsView.setManaged(false);
        friendRequestsView.setVisible(true);
        friendRequestsView.setManaged(true);
        searchResultsView.setVisible(false);
        searchResultsView.setManaged(false);

        searchField.setPromptText("Search for users to add...");
        searchField.clear();
    }

    private void loadMyFriends() {
        new Thread(() -> {
            int userId = networkManager.getCurrentUserId();
            currentFriends = networkManager.getFriends(userId);

            Platform.runLater(() -> {
                friendsList.getChildren().clear();
                if (currentFriends.isEmpty()) {
                    Label emptyLabel = new Label("No friends yet. Start adding friends!");
                    emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16px;");
                    friendsList.getChildren().add(emptyLabel);
                } else {
                    for (Friend friend : currentFriends) {
                        friendsList.getChildren().add(createFriendRow(friend));
                    }
                }
            });
        }).start();
    }

    private void loadFriendRequests() {
        new Thread(() -> {
            int userId = networkManager.getCurrentUserId();
            currentRequests = networkManager.getFriendRequests(userId);

            Platform.runLater(() -> {
                // Update tab badge
                String tabText = "Friend Requests";
                if (!currentRequests.isEmpty()) {
                    tabText += " ➌".replace("3", String.valueOf(currentRequests.size()));
                }
                friendRequestsTab.setText(tabText);

                requestsList.getChildren().clear();
                if (currentRequests.isEmpty()) {
                    Label emptyLabel = new Label("No pending friend requests");
                    emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16px;");
                    requestsList.getChildren().add(emptyLabel);
                } else {
                    for (FriendRequest request : currentRequests) {
                        requestsList.getChildren().add(createRequestRow(request));
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
        nameLabel.getStyleClass().add("friend-name");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // View Profile Button
        Button viewProfileBtn = new Button("View Profile");
        viewProfileBtn.getStyleClass().add("action-button-blue");
        viewProfileBtn.setOnAction(e -> handleViewProfile(friend));

        // Delete/Unfriend Button
        Button unfriendBtn = new Button("Unfriend");
        unfriendBtn.getStyleClass().add("action-button-red"); // Reuse red button style
        unfriendBtn.setOnAction(e -> handleRemoveFriend(friend));

        row.getChildren().addAll(avatar, nameLabel, spacer, viewProfileBtn, unfriendBtn);
        return row;
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
        Label nameLabel = new Label(request.getUsername());
        nameLabel.getStyleClass().add("friend-name");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Action Buttons
        HBox actions = new HBox(10);
        Button acceptBtn = new Button("Accept");
        acceptBtn.getStyleClass().add("action-button-green");
        acceptBtn.setOnAction(e -> handleAcceptRequest(request));

        Button declineBtn = new Button("Decline");
        declineBtn.getStyleClass().add("action-button-red");
        declineBtn.setOnAction(e -> handleDeclineRequest(request));

        actions.getChildren().addAll(acceptBtn, declineBtn);
        row.getChildren().addAll(avatar, nameLabel, spacer, actions);

        return row;
    }

    private HBox createSearchResultRow(User user) {
        HBox row = new HBox(15);
        row.getStyleClass().add("friend-row");
        row.setAlignment(Pos.CENTER_LEFT);
        row.setMaxWidth(Double.MAX_VALUE);

        // Avatar
        Circle avatar = new Circle(25);
        avatar.getStyleClass().add("avatar-circle");

        // Name
        Label nameLabel = new Label(user.getUsername());
        nameLabel.getStyleClass().add("friend-name");

        // Spacer
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        // Send Request Button
        Button sendRequestBtn = new Button("Send Request");
        sendRequestBtn.getStyleClass().add("action-button-blue");
        sendRequestBtn.setOnAction(e -> handleSendRequest(user, sendRequestBtn));

        row.getChildren().addAll(avatar, nameLabel, spacer, sendRequestBtn);
        return row;
    }

    @FXML
    private void handleSearchInput() {
        // Local filtering only on key release
        String query = searchField.getText().trim();
        if (myFriendsTab.isSelected()) {
            filterFriends(query);
        }
    }

    @FXML
    private void handleGlobalSearch() {
        // Global network search on Enter or Search Button
        String query = searchField.getText().trim();
        if (!query.isEmpty()) {
            searchUsers(query);
            // Optionally update UI to indicate search mode
        }
    }

    private void filterFriends(String query) {
        friendsList.getChildren().clear();

        if (query.isEmpty()) {
            // Show all friends
            if (currentFriends != null) {
                for (Friend friend : currentFriends) {
                    friendsList.getChildren().add(createFriendRow(friend));
                }
            }
        } else {
            // Filter friends
            boolean found = false;
            if (currentFriends != null) {
                for (Friend friend : currentFriends) {
                    if (friend.getUsername().toLowerCase().contains(query.toLowerCase())) {
                        friendsList.getChildren().add(createFriendRow(friend));
                        found = true;
                    }
                }
            }

            if (!found) {
                Label noResults = new Label("No friends found matching '" + query + "'");
                noResults.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16px;");
                friendsList.getChildren().add(noResults);
            }
        }
    }

    private void searchUsers(String query) {
        new Thread(() -> {
            List<User> results = networkManager.searchUsers(query);

            Platform.runLater(() -> {
                // Show search results view
                myFriendsView.setVisible(false);
                myFriendsView.setManaged(false);
                friendRequestsView.setVisible(false);
                friendRequestsView.setManaged(false);
                searchResultsView.setVisible(true);
                searchResultsView.setManaged(true);

                // Deselect tabs to indicate distinct mode
                if (tabGroup.getSelectedToggle() != null) {
                    tabGroup.getSelectedToggle().setSelected(false);
                }

                searchResultsList.getChildren().clear();

                // Add explicit Close/Back button
                Button closeSearchBtn = new Button("Close Search & Return to My Friends");
                closeSearchBtn.getStyleClass().add("action-button-red");
                closeSearchBtn.setOnAction(e -> {
                    showMyFriends();
                    myFriendsTab.setSelected(true);
                });
                searchResultsList.getChildren().add(closeSearchBtn);

                if (results.isEmpty()) {
                    Label noResults = new Label("No users found matching '" + query + "'");
                    noResults.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16px;");
                    searchResultsList.getChildren().add(noResults);
                } else {
                    for (User user : results) {
                        searchResultsList.getChildren().add(createSearchResultRow(user));
                    }
                }
            });
        }).start();
    }

    private void handleViewProfile(Friend friend) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/com/iwish/login/fxml/friend_profile_content.fxml"));
            javafx.scene.Parent profileRoot = loader.load();

            FriendProfileController controller = loader.getController();
            controller.setFriendData(friend.getUserId(), friend.getUsername());

            // Handle Back Button: Restore the main Friends view visibility
            // We assume FriendsController's root view is inside a container or we can just
            // hide/show
            // But here, we can replace the center content of the BorderPane if we have
            // access,
            // OR simpler: Hide current "My Friends" VBox and show the Profile Root in the
            // StackPane.

            // Check if profileRoot is already added to stack? No.
            // We need to add it to the StackPane in friends_view.fxml

            if (myFriendsView.getParent() instanceof StackPane) {
                StackPane parentStack = (StackPane) myFriendsView.getParent();

                // Hide all others
                myFriendsView.setVisible(false);
                myFriendsView.setManaged(false);
                friendRequestsView.setVisible(false);
                friendRequestsView.setManaged(false);
                searchResultsView.setVisible(false);
                searchResultsView.setManaged(false);

                parentStack.getChildren().add(profileRoot);

                controller.setOnBackAction(() -> {
                    parentStack.getChildren().remove(profileRoot);
                    showMyFriends();
                });
            } else {
                System.err.println("Parent is not StackPane, cannot swap views.");
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Error", "Could not load profile", Alert.AlertType.ERROR);
        }
    }

    private void handleRemoveFriend(Friend friend) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Remove Friend");
        confirm.setHeaderText("Remove " + friend.getUsername() + "?");
        confirm.setContentText("Are you sure you want to remove this friend?");

        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                new Thread(() -> {
                    int userId = networkManager.getCurrentUserId();
                    boolean success = networkManager.removeFriend(userId, friend.getUserId());

                    Platform.runLater(() -> {
                        if (success) {
                            showAlert("Success", "Friend removed successfully", Alert.AlertType.INFORMATION);
                            loadMyFriends();
                        } else {
                            showAlert("Error", "Failed to remove friend", Alert.AlertType.ERROR);
                        }
                    });
                }).start();
            }
        });
    }

    private void handleAcceptRequest(FriendRequest request) {
        new Thread(() -> {
            boolean success = networkManager.acceptFriendRequest(request.getFriendshipId());

            Platform.runLater(() -> {
                if (success) {
                    showAlert("Success", "Friend request accepted!", Alert.AlertType.INFORMATION);
                    // Immediate refresh of both lists
                    loadFriendRequests();
                    loadMyFriends();

                    // Optional: Switch to "My Friends" tab to show the new friend immediately?
                    // user said: "make new accepted friend apear in my friends page ...
                    // immediately"
                    // So we can switch the view for them:
                    showMyFriends();
                    myFriendsTab.setSelected(true);

                } else {
                    showAlert("Error", "Failed to accept request", Alert.AlertType.ERROR);
                }
            });
        }).start();
    }

    private void handleDeclineRequest(FriendRequest request) {
        new Thread(() -> {
            boolean success = networkManager.declineFriendRequest(request.getFriendshipId());

            Platform.runLater(() -> {
                if (success) {
                    showAlert("Success", "Friend request declined", Alert.AlertType.INFORMATION);
                    loadFriendRequests();
                } else {
                    showAlert("Error", "Failed to decline request", Alert.AlertType.ERROR);
                }
            });
        }).start();
    }

    private void handleSendRequest(User user, Button button) {
        new Thread(() -> {
            int userId = networkManager.getCurrentUserId();
            boolean success = networkManager.addFriend(userId, user.getUsername());

            Platform.runLater(() -> {
                if (success) {
                    showAlert("Success", "Friend request sent to " + user.getUsername(), Alert.AlertType.INFORMATION);
                    button.setDisable(true);
                    button.setText("Request Sent");
                } else {
                    showAlert("Error", "Failed to send friend request", Alert.AlertType.ERROR);
                }
            });
        }).start();
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
