package com.iwish.login.controllers;

import com.iwish.client.NetworkManager;
import com.iwish.models.Item;
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
import javafx.scene.layout.Region;

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
            double price = item.getPrice();
            double goal = price;
            double funded = item.getFunded();

            HBox row = createWishlistItem(item.getImgSrc(), item.getName(), price, funded, goal, item.getId());
            itemsContainer.getChildren().add(row);
        }
    }

    private HBox createWishlistItem(String imagePath, String name, double price, double funded, double goal,
            int itemId) {
        HBox item = new HBox(20);
        item.getStyleClass().add("wishlist-item");
        item.setAlignment(Pos.CENTER_LEFT);

        // Product image
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("item-image-container");
        imageContainer.setPrefSize(100, 100);
        imageContainer.setMinSize(100, 100);
        imageContainer.setMaxSize(100, 100);

        try {
            String validUrl = imagePath;
            if (imagePath != null && (imagePath.startsWith("C:") || imagePath.startsWith("G:"))) {
                validUrl = "file:///" + imagePath.replace("\\", "/");
            }

            if (validUrl != null && !validUrl.isEmpty()) {
                // Use standard Image (background loading = true)
                Image img = new Image(validUrl, true);
                ImageView imageView = new ImageView(img);
                imageView.setFitWidth(90);
                imageView.setFitHeight(90);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                imageContainer.getChildren().add(imageView);
            } else {
                throw new Exception("No image URL");
            }
        } catch (Exception e) {
            Label placeholderLabel = new Label("📷");
            placeholderLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #ccc;");
            imageContainer.getChildren().add(placeholderLabel);
        }

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
                }
            });
        }).start();
    }
}
