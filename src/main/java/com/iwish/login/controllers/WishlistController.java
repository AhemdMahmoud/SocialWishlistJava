package com.iwish.login.controllers;

import com.iwish.client.NetworkManager;
import com.iwish.models.Item;
import com.iwish.util.ImageCache;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.Cursor;

import java.util.List;

public class WishlistController {

    @FXML
    private VBox itemsContainer;

    @FXML
    public void initialize() {
        loadWishlist();
    }

    private void loadWishlist() {
        new Thread(() -> {
            NetworkManager network = NetworkManager.getInstance();
            if (network.isConnected()) {
                int userId = network.getCurrentUserId();
                List<Item> items = network.getUserWishlist(userId);
                Platform.runLater(() -> populateList(items));
            }
        }).start();
    }

    private void populateList(List<Item> items) {
        itemsContainer.getChildren().clear();
        if (items == null || items.isEmpty()) {
            Label emptyLabel = new Label("Your wishlist is empty. Go to Home to add items!");
            emptyLabel.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16px;");
            itemsContainer.getChildren().add(emptyLabel);
            return;
        }

        for (Item item : items) {
            HBox row = createWishlistItem(item);
            itemsContainer.getChildren().add(row);
        }
    }

    private HBox createWishlistItem(Item itemObj) {
        HBox item = new HBox(20);
        item.getStyleClass().add("wishlist-item");
        item.setAlignment(Pos.CENTER_LEFT);

        String imagePath = itemObj.getImgSrc();
        String name = itemObj.getName();
        double price = itemObj.getPrice();
        double funded = itemObj.getFunded();
        double goal = price;
        int itemId = itemObj.getId();

        // Product image - make it clickable
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("item-image-container");
        imageContainer.setPrefSize(100, 100);
        imageContainer.setMinSize(100, 100);
        imageContainer.setMaxSize(100, 100);
        imageContainer.setCursor(Cursor.HAND);
        
        // Use image cache for faster loading
        Image loadedImage = ImageCache.getInstance().getImage(imagePath);
        
        if (loadedImage != null) {
            ImageView imageView = new ImageView(loadedImage);
            imageView.setFitWidth(90);
            imageView.setFitHeight(90);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageContainer.getChildren().add(imageView);
            
            // Handle errors
            loadedImage.errorProperty().addListener((obs, oldV, newV) -> {
                if (newV) {
                    Platform.runLater(() -> {
                        imageContainer.getChildren().remove(imageView);
                        Label placeholderLabel = new Label("📷");
                        placeholderLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #ccc;");
                        imageContainer.getChildren().add(placeholderLabel);
                    });
                }
            });
        } else {
            Label placeholderLabel = new Label("📷");
            placeholderLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #ccc;");
            imageContainer.getChildren().add(placeholderLabel);
        }
        
        // Add click handler to show details
        final Image finalImage = loadedImage;
        imageContainer.setOnMouseClicked(e -> showItemDetails(itemObj, finalImage));
        
        // Add hover effect
        imageContainer.setOnMouseEntered(e -> {
            if (imageContainer.getStyleClass().contains("item-image-container")) {
                imageContainer.setStyle("-fx-background-color: rgba(0,0,0,0.1); -fx-background-radius: 5;");
            }
        });
        imageContainer.setOnMouseExited(e -> {
            imageContainer.setStyle("");
        });

        // Product details
        VBox details = new VBox(10);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("item-name");
        nameLabel.setWrapText(true);

        Label priceLabel = new Label("Total price: E£ " + String.format("%,.0f", price));
        priceLabel.getStyleClass().add("item-price-label");

        // Progress bar container
        HBox progressContainer = new HBox(15);
        progressContainer.setAlignment(Pos.CENTER_LEFT);

        StackPane progressStack = new StackPane();
        HBox.setHgrow(progressStack, Priority.ALWAYS);
        progressStack.setMaxWidth(Double.MAX_VALUE);

        double progress = (goal > 0) ? Math.min(funded / goal, 1.0) : 0;
        int progressPercent = (int) (progress * 100);

        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setPrefHeight(35);
        progressBar.getStyleClass().add("custom-progress-bar");

        Label progressLabel = new Label(progressPercent + "% funded");
        progressLabel.getStyleClass().add("progress-label");
        StackPane.setAlignment(progressLabel, Pos.CENTER_LEFT);
        progressLabel.setPadding(new Insets(0, 0, 0, 15));

        progressStack.getChildren().addAll(progressBar, progressLabel);

        Label fundingLabel = new Label(
                "E£ " + String.format("%,.0f ", funded) + " of E£ " + String.format("%,.0f ", goal));
        fundingLabel.getStyleClass().add("funding-label");

        progressContainer.getChildren().addAll(progressStack, fundingLabel);
        details.getChildren().addAll(nameLabel, priceLabel, progressContainer);

        // Delete button
        Button deleteBtn = new Button("🗑");
        deleteBtn.getStyleClass().add("delete-btn");
        deleteBtn.setOnAction(e -> handleDelete(itemId, item));

        item.getChildren().addAll(imageContainer, details, deleteBtn);
        return item;
    }

    private void handleDelete(int itemId, HBox row) {
        new Thread(() -> {
            boolean success = NetworkManager.getInstance()
                    .removeFromWishlist(NetworkManager.getInstance().getCurrentUserId(), itemId);
            Platform.runLater(() -> {
                if (success) {
                    itemsContainer.getChildren().remove(row);
                    // Reload wishlist to refresh data from database
                    loadWishlist();
                }
            });
        }).start();
    }
    
    private void showItemDetails(Item item, Image image) {
        // Get the scene and find the root BorderPane
        javafx.scene.Scene scene = itemsContainer.getScene();
        if (scene == null) return;
        
        javafx.scene.Parent root = scene.getRoot();
        
        // Get or create a StackPane container for the modal overlay
        // This ensures the modal covers the entire window including sidebar
        final StackPane finalContainer;
        
        if (root instanceof StackPane) {
            // Already a StackPane, use it
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
        // Make overlay fill entire window
        modalOverlay.setPrefSize(scene.getWidth(), scene.getHeight());
        modalOverlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
        
        modalOverlay.setOnMouseClicked(e -> {
            // Close modal when clicking on overlay
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
        
        Button removeBtn = new Button("Remove from Wishlist");
        removeBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        removeBtn.setOnMouseEntered(e -> removeBtn.setStyle(
                "-fx-background-color: #c0392b; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;"));
        removeBtn.setOnMouseExited(e -> removeBtn.setStyle(
                "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;"));
        removeBtn.setOnAction(e -> {
            handleDelete(item.getId(), null);
            finalContainer.getChildren().remove(modalOverlay);
        });
        
        buttonBox.getChildren().add(removeBtn);

        detailsBox.getChildren().addAll(nameLabel, priceLabel, fundingLabel, remainingLabel, detailProgressBar, buttonBox);

        modalContent.getChildren().addAll(headerBox, imageBox, detailsBox);
        
        modalOverlay.getChildren().add(modalContent);
        StackPane.setAlignment(modalContent, Pos.CENTER);

        // Add modal overlay to root container (covers entire window including sidebar)
        finalContainer.getChildren().add(modalOverlay);
    }
    
}
