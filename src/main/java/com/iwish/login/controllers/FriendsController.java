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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

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
    private VBox friendsHeader;

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

    @FXML
    private Label requestsBadge;

    private ToggleGroup tabGroup;
    private NetworkManager networkManager;
    private List<Friend> currentFriends;
    private List<FriendRequest> currentRequests;

    private List<User> allUsers;

    private Timer refreshTimer;
    private Set<String> sentRequestUsernames = new HashSet<>();

    @FXML
    public void initialize() {
        networkManager = NetworkManager.getInstance();
        setupTabs();

        // Initial load
        loadMyFriends();
        loadFriendRequests();

        // Start auto-refresh for real-time updates
        startAutoRefresh();
    }

    private void updateTabBadge() {
        if (requestsBadge != null) {
            boolean hasRequests = currentRequests != null && !currentRequests.isEmpty();
            boolean isSelected = friendRequestsTab.isSelected();
            // Show badge if there are requests AND the tab is NOT currently selected
            requestsBadge.setVisible(hasRequests && !isSelected);
        }
    }

    // ... existing methods ...
    private HBox createSearchResultRow(User user) {
        // ... (unchanged)
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

        // CHECK PERSISTENT STATE
        if (sentRequestUsernames.contains(user.getUsername())) {
            sendRequestBtn.setText("Request Sent");
            sendRequestBtn.setDisable(true);
            sendRequestBtn.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white;");
        } else {
            sendRequestBtn.setOnAction(e -> handleSendRequest(user, sendRequestBtn));
        }

        row.getChildren().addAll(avatar, nameLabel, spacer, sendRequestBtn);
        return row;
    }

    private void handleSendRequest(User user, Button button) {
        new Thread(() -> {
            int userId = networkManager.getCurrentUserId();
            boolean success = networkManager.addFriend(userId, user.getUsername());

            Platform.runLater(() -> {
                if (success) {
                    // Update UI state immediately and persistently
                    button.setText("Request Sent");
                    button.setDisable(true);
                    button.setStyle("-fx-background-color: #bdc3c7; -fx-text-fill: white;");

                    // Track this user so the button stays disabled on refresh
                    sentRequestUsernames.add(user.getUsername());

                    // Removed "Success" alert as per user request
                } else {
                    showAlert("Error", "Failed to send friend request", Alert.AlertType.ERROR);
                }
            });
        }).start();
    }

    private void startAutoRefresh() {
        if (refreshTimer != null)
            return;

        refreshTimer = new Timer(true); // Daemon timer
        refreshTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                // Safety check: if view is no longer part of scene (user navigated away), stop
                // timer
                if (friendsList.getScene() == null && friendsList.getParent() == null) {
                    this.cancel();
                    return;
                }

                Platform.runLater(() -> {
                    if (searchField.getText().isEmpty()) {
                        // Background fetch
                        new Thread(() -> {
                            int userId = networkManager.getCurrentUserId();
                            // Fetch new lists
                            List<FriendRequest> newRequests = networkManager.getFriendRequests(userId);
                            List<Friend> newFriends = networkManager.getFriends(userId);

                            Platform.runLater(() -> {
                                // Update local data refs
                                currentRequests = newRequests;
                                currentFriends = newFriends;

                                // Update Badge Logic
                                updateTabBadge();

                                // Refresh active view
                                if (friendRequestsTab.isSelected()) {
                                    filterAllUsers(""); // Refresh requests list
                                } else if (myFriendsTab.isSelected()) {
                                    filterFriends(""); // Refresh friends list
                                }
                            });
                        }).start();
                    }
                });
            }
        }, 3000, 3000); // Check every 3 seconds
    }

    private void setupTabs() {
        tabGroup = new ToggleGroup();
        myFriendsTab.setToggleGroup(tabGroup);
        friendRequestsTab.setToggleGroup(tabGroup);

        // When switching to Requests tab, refresh to ensure we have latest All Users +
        // Requests
        friendRequestsTab.selectedProperty().addListener((obs, oldVal, newVal) -> {
            updateTabBadge(); // Update badge on selection change

            if (newVal) {
                searchField.setPromptText("Search to add friends...");
                if (allUsers != null) {
                    filterAllUsers(searchField.getText().trim());
                }
            } else {
                searchField.setPromptText("Search your friends...");
            }
        });
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

        searchField.setPromptText("Search to add friends...");
        searchField.clear();

        // Initial load/filter
        if (allUsers != null) {
            filterAllUsers("");
        } else {
            loadFriendRequests();
        }
    }

    private void loadMyFriends() {
        new Thread(() -> {
            int userId = networkManager.getCurrentUserId();
            // Friends are now sorted by ID DESC (newest first) from server
            currentFriends = networkManager.getFriends(userId);

            // Fetch notifications to identify "New" friends (unread accepted requests)
            List<com.iwish.models.Notification> notifications = networkManager.getNotifications(userId);
            Set<String> newFriendUsernames = new HashSet<>();
            for (com.iwish.models.Notification n : notifications) {
                if (!n.isRead() &&
                        (n.getType() != null && n.getType().equals("FRIEND_ACCEPTED") ||
                                (n.getMessage() != null && n.getMessage().toLowerCase().contains("accepted")))) {

                    // Parse username from message or logic
                    // Message format: "User [Target] accepted..."
                    // Simple matching:
                    for (Friend f : currentFriends) {
                        if (n.getMessage().contains(f.getUsername())) {
                            newFriendUsernames.add(f.getUsername());
                        }
                    }
                }
            }

            Platform.runLater(() -> {
                friendsList.getChildren().clear();
                if (currentFriends.isEmpty()) {
                    Label emptyLabel = new Label("No friends yet. Start adding friends!");
                    emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16px;");
                    friendsList.getChildren().add(emptyLabel);
                } else {
                    List<Friend> newFriends = new java.util.ArrayList<>();
                    List<Friend> oldFriends = new java.util.ArrayList<>();

                    for (Friend f : currentFriends) {
                        if (newFriendUsernames.contains(f.getUsername())) {
                            newFriends.add(f);
                        } else {
                            oldFriends.add(f);
                        }
                    }

                    // Render "New Friends" Section
                    if (!newFriends.isEmpty()) {
                        Label newLabel = new Label("New Friends");
                        newLabel.setStyle(
                                "-fx-font-weight: bold; -fx-text-fill: #2ecc71; -fx-font-size: 14px; -fx-padding: 0 0 5 0;");
                        friendsList.getChildren().add(newLabel);

                        for (Friend f : newFriends) {
                            friendsList.getChildren().add(createFriendRow(f));
                        }

                        // Separator
                        if (!oldFriends.isEmpty()) {
                            Separator sep = new Separator();
                            sep.setPadding(new javafx.geometry.Insets(10, 0, 10, 0));
                            friendsList.getChildren().add(sep);
                        }
                    }

                    // Render "Old Friends"
                    for (Friend f : oldFriends) {
                        friendsList.getChildren().add(createFriendRow(f));
                    }
                }
            });
        }).start();
    }

    private void loadFriendRequests() {
        new Thread(() -> {
            int userId = networkManager.getCurrentUserId();
            currentRequests = networkManager.getFriendRequests(userId);

            // Also load ALL users for "Discover" mode (Client side filtering)
            allUsers = networkManager.searchUsers("");

            Platform.runLater(() -> {
                // Update tab badge logic REMOVED as per user request -> NOW RE-ADDED (Red Dot)
                updateTabBadge();

                // Initial render: Show Requests + All Users (filtered by empty string = all)
                filterAllUsers("");
            });
        }).start();
    }

    private void filterAllUsers(String query) {
        requestsList.getChildren().clear();
        String lowerQuery = query.toLowerCase();

        // 1. Pending Requests Section
        if (!currentRequests.isEmpty()) {
            boolean hasMatch = false;
            VBox pendingBox = new VBox(10);

            for (FriendRequest request : currentRequests) {
                if (request.getUsername().toLowerCase().contains(lowerQuery)) {
                    pendingBox.getChildren().add(createRequestRow(request));
                    hasMatch = true;
                }
            }

            if (hasMatch) {
                Label header = new Label("Friend Requests");
                header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
                requestsList.getChildren().add(header);
                requestsList.getChildren().add(pendingBox);

                Region div = new Region();
                div.setMinHeight(1);
                div.setStyle("-fx-background-color: #ecf0f1;");
                requestsList.getChildren().add(div);
            }
        }

        // 2. Discover/All Users Section
        if (allUsers != null && !allUsers.isEmpty()) {
            Label header = new Label("All Users");
            header.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #2c3e50;");
            requestsList.getChildren().add(header);

            int userId = networkManager.getCurrentUserId();
            boolean foundUser = false;

            for (User user : allUsers) {
                // EXCLUDE self and existing friends (basic filter) logic could go here
                // For now, just exclude self
                if (user.getUserId() == userId)
                    continue;

                // Exclude existing friends from "Discover" list?
                // Checks currentFriends list
                boolean isFriend = false;
                if (currentFriends != null) {
                    for (Friend f : currentFriends) {
                        if (f.getUserId() == user.getUserId()) {
                            isFriend = true;
                            break;
                        }
                    }
                }
                if (isFriend)
                    continue; // Don't show already friends in "Add" list

                if (user.getUsername().toLowerCase().contains(lowerQuery)) {
                    // System.out.println("DEBUG: Adding user row: " + user.getUsername());
                    requestsList.getChildren().add(createSearchResultRow(user));
                    foundUser = true;
                }
            }

            if (!foundUser) {
                Label noResults = new Label("No users found matching '" + query + "'");
                noResults.setStyle("-fx-text-fill: #7f8c8d;");
                requestsList.getChildren().add(noResults);
            }
        }
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
        Button viewProfileBtn = new Button("View Wishlist");
        viewProfileBtn.getStyleClass().add("action-button-blue");
        viewProfileBtn.setOnAction(e -> handleViewProfile(friend));

        // Delete/Unfriend Button
        Button unfriendBtn = new Button("Remove");
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

    @FXML
    private void handleSearchInput() {
        // Local filtering only on key release
        String query = searchField.getText().trim();
        if (myFriendsTab.isSelected()) {
            filterFriends(query);
        } else if (friendRequestsTab.isSelected()) {
            filterAllUsers(query);
        }
    }

    @FXML
    private void handleGlobalSearch() {
        // Global network search on Enter or Search Button
        String query = searchField.getText().trim();
        // If query is empty or we are in Requests tab, we rely on local filter
        if (friendRequestsTab.isSelected()) {
            filterAllUsers(query);
            return;
        }

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

                // Hide Main Friends Views
                myFriendsView.setVisible(false);
                myFriendsView.setManaged(false);
                friendRequestsView.setVisible(false);
                friendRequestsView.setManaged(false);
                searchResultsView.setVisible(false);
                searchResultsView.setManaged(false);

                // Hide Header (Full Screen Mode)
                if (friendsHeader != null) {
                    friendsHeader.setVisible(false);
                    friendsHeader.setManaged(false);
                }

                parentStack.getChildren().add(profileRoot);

                controller.setOnBackAction(() -> {
                    parentStack.getChildren().remove(profileRoot);
                    showMyFriends();

                    // Restore Header
                    if (friendsHeader != null) {
                        friendsHeader.setVisible(true);
                        friendsHeader.setManaged(true);
                    }
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
                            // Silent success as requested
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
                    // Silent success, no redirect
                    // Just refresh the lists
                    loadFriendRequests();
                    loadMyFriends();

                    // REMOVED: showMyFriends() and tab switch
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
                    // Silent success (checking if user wanted this too, assuming consistency)
                    // "remove the notification thing for any users regarding friends accepatance or
                    // sending"
                    // implies silent ops generally. Keeping consistency.
                    loadFriendRequests();
                } else {
                    showAlert("Error", "Failed to decline request", Alert.AlertType.ERROR);
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
