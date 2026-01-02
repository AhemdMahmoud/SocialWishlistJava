package com.iwish.client;

import com.iwish.models.Item;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class MarketplaceController implements Initializable {

    @FXML
    private TextField searchField;
    @FXML
    private ScrollPane scrollPane;
    @FXML
    private GridPane gridPane;

    private List<Item> allItems;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadItems();
    }

    private void loadItems() {
        // Run in background to avoid blocking UI
        new Thread(() -> {
            NetworkManager network = NetworkManager.getInstance();
            if (network.connect()) {
                allItems = network.getAllItems();
                Platform.runLater(this::populateGrid);
            } else {
                Platform.runLater(() -> System.err.println("Failed to connect to server."));
            }
        }).start();
    }

    private void populateGrid() {
        gridPane.getChildren().clear();
        int column = 0;
        int row = 0; // Start at 0

        if (allItems == null) return;

        for (Item item : allItems) {
            VBox card = createItemCard(item);
            gridPane.add(card, column++, row);

            // Grid width 4 for wider screen
            if (column == 4) {
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
                javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(new javafx.scene.image.Image(imageUrl, true));
                imageView.setFitHeight(140);
                imageView.setFitWidth(220);
                imageView.setPreserveRatio(true);
                imageContainer.getChildren().add(imageView);
            } catch (Exception e) {
                // Fallback if image fails to load
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
        Button wishButton = new Button("Add to Wishlist"); // Heart icon could go here
        wishButton.getStyleClass().add("add-btn");
        wishButton.setMaxWidth(Double.MAX_VALUE); // Full width
        wishButton.setOnAction(e -> handleAddToWishlist(item));

        content.getChildren().addAll(nameLabel, priceLabel, spacer, wishButton);
        card.getChildren().addAll(imageContainer, content);
        
        return card;
    }

    private void handleAddToWishlist(Item item) {
        System.out.println("Added to wishlist: " + item.getName());
        // TODO: distinct logic for adding to personal wishlist (Server request)
    }
}
