package com.iwish.login.controllers;

import com.iwish.client.NetworkManager;
import com.iwish.models.Item;
import com.iwish.util.ImageCache;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.Cursor;

import java.util.List;

public class FriendProfileController {

    @FXML
    private StackPane rootStackPane;
    @FXML
    private Label headerTitleLabel;
    @FXML
    private VBox itemsContainer;

    private NetworkManager networkManager;
    private int friendId;
    private String friendName;
    private Runnable onBackAction;

    @FXML
    public void initialize() {
        networkManager = NetworkManager.getInstance();
    }

    public void setFriendData(int friendId, String friendName) {
        this.friendId = friendId;
        this.friendName = friendName;

        headerTitleLabel.setText(friendName + "'s Wishlist");

        loadWishlist();
    }

    public void setOnBackAction(Runnable action) {
        this.onBackAction = action;
    }

    @FXML
    private void handleBack() {
        if (onBackAction != null) {
            onBackAction.run();
        }
    }

    private void loadWishlist() {
        new Thread(() -> {
            List<Item> wishlist = networkManager.getUserWishlist(friendId);

            Platform.runLater(() -> {
                itemsContainer.getChildren().clear();
                if (wishlist.isEmpty()) {
                    Label empty = new Label("No items in wishlist yet.");
                    empty.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
                    itemsContainer.getChildren().add(empty);
                } else {
                    for (Item item : wishlist) {
                        double goal = item.getPrice();
                        double funded = item.getFunded();

                        itemsContainer.getChildren().add(createWishlistItem(item, funded, goal));
                    }
                }
            });
        }).start();
    }

    private HBox createWishlistItem(Item item, double funded, double goal) {
        HBox row = new HBox(20);
        row.getStyleClass().add("wishlist-item-card");

        // Image - make it clickable
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("item-image-container");
        imageContainer.setCursor(Cursor.HAND);
        
        // Use image cache for faster loading
        Image loadedImage = ImageCache.getInstance().getImage(item.getImgSrc());
        
        if (loadedImage != null) {
            ImageView iv = new ImageView(loadedImage);
            iv.setFitWidth(90);
            iv.setFitHeight(90);
            iv.setPreserveRatio(true);
            imageContainer.getChildren().add(iv);
            
            // Handle errors
            loadedImage.errorProperty().addListener((obs, oldV, newV) -> {
                if (newV) {
                    Platform.runLater(() -> {
                        imageContainer.getChildren().clear();
                        Label placeholder = new Label("📷");
                        placeholder.getStyleClass().add("placeholder-icon");
                        imageContainer.getChildren().add(placeholder);
                    });
                }
            });
        } else {
            Label placeholder = new Label("📷");
            placeholder.getStyleClass().add("placeholder-icon");
            imageContainer.getChildren().add(placeholder);
        }
        
        // Add click handler to show details
        final Image finalImage = loadedImage;
        imageContainer.setOnMouseClicked(e -> showItemDetails(item, finalImage));
        
        // Add hover effect
        imageContainer.setOnMouseEntered(e -> {
            imageContainer.setStyle("-fx-background-color: rgba(0,0,0,0.1); -fx-background-radius: 5;");
        });
        imageContainer.setOnMouseExited(e -> {
            imageContainer.setStyle("");
        });

        // Details
        VBox details = new VBox(10);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("item-name");

        Label priceLabel = new Label("Price: E£ " + String.format("%,.0f", item.getPrice()));
        priceLabel.getStyleClass().add("item-price");

        // Progress
        double progress = (goal > 0) ? Math.min(funded / goal, 1.0) : 0;
        int progressPercent = (int) (progress * 100);

        HBox progressContainer = new HBox(15);
        progressContainer.setAlignment(Pos.CENTER_LEFT);

        StackPane progressStack = new StackPane();
        HBox.setHgrow(progressStack, Priority.ALWAYS);

        ProgressBar bar = new ProgressBar(progress);
        bar.getStyleClass().add("custom-progress-bar");
        bar.setMaxWidth(Double.MAX_VALUE);
        bar.setPrefHeight(35);

        Label progressLabel = new Label(progressPercent + "% funded");
        progressLabel.getStyleClass().add("progress-label");
        progressLabel.setPadding(new Insets(0, 0, 0, 15));
        StackPane.setAlignment(progressLabel, Pos.CENTER_LEFT);

        progressStack.getChildren().addAll(bar, progressLabel);

        Label fundingLabel = new Label(
                "E£ " + String.format("%,.0f", funded) + " of E£ " + String.format("%,.0f", goal));
        fundingLabel.getStyleClass().add("funding-label");
        fundingLabel.setMinWidth(120);
        fundingLabel.setAlignment(Pos.CENTER_RIGHT);

        progressContainer.getChildren().addAll(progressStack, fundingLabel);
        details.getChildren().addAll(nameLabel, priceLabel, progressContainer);

        // Contribute Button
        Button contributeBtn = new Button("Contribute");
        contributeBtn.getStyleClass().add("contribute-button");
        contributeBtn.setOnAction(e -> showContributionModal(item.getName(), item.getId(), funded, goal));

        row.getChildren().addAll(imageContainer, details, contributeBtn);
        return row;
    }

    private void showContributionModal(String itemName, int itemId, double funded, double goal) {
        StackPane modalOverlay = new StackPane();
        modalOverlay.getStyleClass().add("modal-overlay");

        VBox modalContent = new VBox(20);
        modalContent.getStyleClass().add("modal-content");
        modalContent.setOnMouseClicked(e -> e.consume()); // Prevent close when clicking content

        Label title = new Label("Contribute to " + itemName);
        title.getStyleClass().add("modal-title");
        title.setWrapText(true);

        TextField amountField = new TextField();
        amountField.setPromptText("Amount (e.g. 50)");
        amountField.getStyleClass().add("amount-field");

        double remaining = Math.max(goal - funded, 0);
        Label remainingLabel = new Label("Remaining needed: E£ " + String.format("%,.0f", remaining));
        remainingLabel.getStyleClass().add("remaining-label");

        Button confirmBtn = new Button("Confirm Payment");
        confirmBtn.getStyleClass().add("confirm-button");
        confirmBtn.setOnAction(e -> {
            String amountStr = amountField.getText();
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0) {
                    System.out.println("Contributing " + amount + " to item " + itemId);
                    boolean success = networkManager.addContribution(friendId, itemId, amount);
                    if (success) {
                        rootStackPane.getChildren().remove(modalOverlay);
                        loadWishlist(); // Refresh to show updated progress bar
                    } else {
                        remainingLabel.setText("Payment failed. Please try again.");
                        remainingLabel.setStyle("-fx-text-fill: red;");
                    }
                }
            } catch (NumberFormatException ex) {
                // ignore invalid
            }
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("cancel-button");
        cancelBtn.setOnAction(e -> rootStackPane.getChildren().remove(modalOverlay));

        modalContent.getChildren().addAll(title, amountField, remainingLabel, confirmBtn, cancelBtn);
        modalOverlay.getChildren().add(modalContent);

        // Close on background click
        modalOverlay.setOnMouseClicked(e -> rootStackPane.getChildren().remove(modalOverlay));

        rootStackPane.getChildren().add(modalOverlay);
    }
    
    private void showItemDetails(Item item, Image image) {
        // Get the scene
        javafx.scene.Scene scene = itemsContainer.getScene();
        if (scene == null) return;
        
        javafx.scene.Parent root = scene.getRoot();
        
        // Get or create a StackPane container for the modal overlay
        final StackPane finalContainer;
        
        if (root instanceof StackPane) {
            finalContainer = (StackPane) root;
        } else {
            // Wrap the root in a StackPane to allow overlay
            StackPane wrapper = new StackPane();
            wrapper.getChildren().add(root);
            scene.setRoot(wrapper);
            finalContainer = wrapper;
        }
        
        // Create modal overlay - covers entire window
        StackPane modalOverlay = new StackPane();
        modalOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        modalOverlay.setPrefSize(scene.getWidth(), scene.getHeight());
        modalOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        modalOverlay.setOnMouseClicked(e -> {
            finalContainer.getChildren().remove(modalOverlay);
        });
        
        // Update size when window resizes
        scene.widthProperty().addListener((obs, oldV, newV) -> {
            modalOverlay.setPrefWidth(newV.doubleValue());
        });
        scene.heightProperty().addListener((obs, oldV, newV) -> {
            modalOverlay.setPrefHeight(newV.doubleValue());
        });

        // Create modal content
        VBox modalContent = new VBox(20);
        modalContent.setAlignment(Pos.CENTER);
        modalContent.setPadding(new Insets(30));
        modalContent.setMaxWidth(600);
        modalContent.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");

        // Prevent closing when clicking inside the modal
        modalContent.setOnMouseClicked(e -> e.consume());

        // Header with close button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_RIGHT);
        headerBox.setPadding(new Insets(0, 0, 10, 0));
        Button closeBtn = new Button("✖");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #999; " +
                "-fx-font-size: 24px; -fx-cursor: hand; -fx-border-width: 0;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #333; " +
                "-fx-font-size: 24px; -fx-cursor: hand; -fx-border-width: 0;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: #999; " +
                "-fx-font-size: 24px; -fx-cursor: hand; -fx-border-width: 0;"));
        closeBtn.setOnAction(e -> finalContainer.getChildren().remove(modalOverlay));
        headerBox.getChildren().add(closeBtn);

        // Large image - exactly 500x350px
        StackPane imageBox = new StackPane();
        imageBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        imageBox.setPrefSize(500, 350);
        imageBox.setMinSize(500, 350);
        imageBox.setMaxSize(500, 350);
        imageBox.setAlignment(Pos.CENTER);

        if (image != null && !image.isError()) {
            ImageView largeImageView = new ImageView(image);
            largeImageView.setFitWidth(500);
            largeImageView.setFitHeight(350);
            largeImageView.setPreserveRatio(true);
            largeImageView.setSmooth(true);
            imageBox.getChildren().add(largeImageView);
        } else {
            Label placeholderLabel = new Label("📷");
            placeholderLabel.setStyle("-fx-font-size: 80px; -fx-text-fill: #ccc;");
            imageBox.getChildren().add(placeholderLabel);
        }

        // Product details
        VBox detailsBox = new VBox(12);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        detailsBox.setPadding(new Insets(15, 0, 0, 0));

        // Product Name - Bold and prominent
        Label nameLabel = new Label(item.getName());
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        nameLabel.setWrapText(true);

        // Total Price - Clear pricing information
        Label priceLabel = new Label("Total Price: E£ " + String.format("%,.2f", item.getPrice()));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");

        // Funding Progress - Shows funded amount, goal, and percentage
        double progress = (item.getPrice() > 0) ? Math.min(item.getFunded() / item.getPrice(), 1.0) : 0;
        int progressPercent = (int) (progress * 100);

        Label fundingLabel = new Label("Funding: E£ " + String.format("%,.2f", item.getFunded()) +
                " of E£ " + String.format("%,.2f", item.getPrice()) +
                " (" + progressPercent + "%)");
        fundingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");

        // Remaining Amount - How much more is needed
        double remaining = Math.max(item.getPrice() - item.getFunded(), 0);
        Label remainingLabel = new Label("Remaining: E£ " + String.format("%,.2f", remaining));
        remainingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");

        // Progress Bar - Visual representation of funding progress
        ProgressBar detailProgressBar = new ProgressBar(progress);
        detailProgressBar.setPrefWidth(500);
        detailProgressBar.setPrefHeight(25);
        detailProgressBar.setStyle("-fx-accent: #2196F3; -fx-background-radius: 5;");

        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button contributeBtn = new Button("Contribute");
        contributeBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        contributeBtn.setOnMouseEntered(e -> contributeBtn.setStyle(
                "-fx-background-color: #2980b9; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;"));
        contributeBtn.setOnMouseExited(e -> contributeBtn.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;"));
        contributeBtn.setOnAction(e -> {
            showContributionModal(item.getName(), item.getId(), item.getFunded(), item.getPrice());
            finalContainer.getChildren().remove(modalOverlay);
        });
        
        buttonBox.getChildren().add(contributeBtn);

        detailsBox.getChildren().addAll(nameLabel, priceLabel, fundingLabel, remainingLabel, detailProgressBar, buttonBox);

        modalContent.getChildren().addAll(headerBox, imageBox, detailsBox);
        modalOverlay.getChildren().add(modalContent);
        StackPane.setAlignment(modalContent, Pos.CENTER);

        // Add modal overlay to root container
        finalContainer.getChildren().add(modalOverlay);
    }
}
