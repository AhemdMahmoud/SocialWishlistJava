package com.iwish.login.controllers;

import com.iwish.client.NetworkManager;
import com.iwish.models.Item;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;

public class MarketplaceContentController {

    @FXML
    private TextField searchField;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private GridPane gridPane;

    private List<Item> allItems;

    @FXML
    public void initialize() {
        loadItems();
    }

    private void loadItems() {
        new Thread(() -> {
            NetworkManager network = NetworkManager.getInstance();
            if (network.isConnected()) {
                allItems = network.getAllItems();
                Platform.runLater(this::populateGrid);
            }
        }).start();
    }

    private void populateGrid() {
        gridPane.getChildren().clear();
        int column = 0;
        int row = 0;

        if (allItems == null)
            return;

        for (Item item : allItems) {
            VBox card = createItemCard(item);
            gridPane.add(card, column++, row);

            // Grid width 3 or 4 depending on resize, let's stick to 3 for safe fit or 4 if
            // wide
            if (column == 3) {
                column = 0;
                row++;
            }
        }
    }

    private VBox createItemCard(Item item) {
        // Main Card Container
        VBox card = new VBox();
        card.getStyleClass().add("item-card");

        // Image Placeholder
        VBox imageContainer = new VBox();
        imageContainer.getStyleClass().add("card-image-placeholder");

        // Load Image
        String imageUrl = item.getImgSrc();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                // Handle local paths if needed, or URLs
                // For simplified logic, assuming URL or valid resource path
                // Check if it's absolute file path (windows)
                String validUrl = imageUrl;
                if (imageUrl.startsWith("C:") || imageUrl.startsWith("G:")) {
                    validUrl = "file:///" + imageUrl.replace("\\", "/");
                }

                ImageView imageView = new ImageView(new Image(validUrl, true));
                imageView.setFitHeight(140);
                imageView.setFitWidth(220);
                imageView.setPreserveRatio(true);
                imageContainer.getChildren().add(imageView);
            } catch (Exception e) {
                Label imgLabel = new Label("IMG");
                imgLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-weight: bold;");
                imageContainer.getChildren().add(imgLabel);
            }
        } else {
            Label imgLabel = new Label("IMG");
            imgLabel.setStyle("-fx-text-fill: #bdc3c7; -fx-font-weight: bold;");
            imageContainer.getChildren().add(imgLabel);
        }

        imageContainer.setAlignment(Pos.CENTER);

        // Content Container
        VBox content = new VBox();
        content.getStyleClass().add("card-content");
        content.setAlignment(Pos.CENTER_LEFT);

        // Name
        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("item-name");
        nameLabel.setWrapText(true);

        // Price
        Label priceLabel = new Label("$" + item.getPrice());
        priceLabel.getStyleClass().add("item-price");

        // Spacer
        Region spacer = new Region();
        spacer.setPrefHeight(10);

        // Button
        Button wishButton = new Button("Add to Wishlist");
        wishButton.getStyleClass().add("add-btn");
        wishButton.setMaxWidth(Double.MAX_VALUE);
        wishButton.setOnAction(e -> handleAddToWishlist(item, wishButton));

        content.getChildren().addAll(nameLabel, priceLabel, spacer, wishButton);
        card.getChildren().addAll(imageContainer, content);

        return card;
    }

    private void handleAddToWishlist(Item item, Button btn) {
        new Thread(() -> {
            boolean success = NetworkManager.getInstance()
                    .addToWishlist(NetworkManager.getInstance().getCurrentUserId(), item.getId());
            Platform.runLater(() -> {
                if (success) {
                    btn.setText("Added!");
                    btn.setDisable(true);
                }
            });
        }).start();
    }
}
