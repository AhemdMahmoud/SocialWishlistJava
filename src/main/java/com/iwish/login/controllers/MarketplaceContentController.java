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
    // We create this programmatically to ensure it overrides any stale FXML layout
    private javafx.scene.layout.TilePane itemsContainer; 

    private List<Item> allItems;

    @FXML
    public void initialize() {
        System.out.println("DEBUG: MarketplaceContentController initialized with Programmatic TilePane");
        setupLayout();
        loadItems();
    }
    
    private void setupLayout() {
        itemsContainer = new javafx.scene.layout.TilePane();
        itemsContainer.setHgap(25);
        itemsContainer.setVgap(25);
        itemsContainer.setPrefTileWidth(220);
        itemsContainer.setPrefTileHeight(320);
        itemsContainer.setAlignment(Pos.TOP_LEFT);
        itemsContainer.setStyle("-fx-padding: 20;");
        
        // Ensure wrapping happens based on available width
        itemsContainer.setPrefColumns(4); 
        
        if (scrollPane != null) {
            scrollPane.setContent(itemsContainer);
            scrollPane.setFitToWidth(true);
        } else {
            System.err.println("ERROR: ScrollPane not injected!");
        }
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
        if (itemsContainer == null) return;
        itemsContainer.getChildren().clear();

        if (allItems == null) return;

        System.out.println("--- DEBUG: Printing Fetched Items ---");
        for (Item item : allItems) {
            System.out.println("Item: " + item.getName() + " | Price: " + item.getPrice() + " | Img: " + item.getImgSrc());
            VBox card = createItemCard(item);
            itemsContainer.getChildren().add(card);
        }
        System.out.println("--- DEBUG: End of Items ---");
    }

    private VBox createItemCard(Item item) {
        // Main Card Container
        VBox card = new VBox();
        card.getStyleClass().add("item-card");
        
        // FORCE the card size to match the TilePane slot
        card.setPrefWidth(220);
        card.setPrefHeight(320);

        // Image Container
        javafx.scene.layout.StackPane imageContainer = new javafx.scene.layout.StackPane();
        imageContainer.getStyleClass().add("card-image-placeholder");

        // Load Image
        String imageUrl = item.getImgSrc();
        ImageView imageView = new ImageView();
        
        // Define standard size
        double imgWidth = 220;
        double imgHeight = 160;
        
        imageView.setFitWidth(imgWidth);
        imageView.setFitHeight(imgHeight);
        imageView.setPreserveRatio(false); // Fill the area
        
        // Clip for rounded top corners
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(imgWidth, imgHeight);
        clip.setArcWidth(20);
        clip.setArcHeight(20);
        imageView.setClip(clip);

        if (imageUrl != null && !imageUrl.isEmpty()) {
            try {
                // Background loading
                Image img = new Image(imageUrl, true); // background loading
                imageView.setImage(img);
                
                // Show standard placeholder until loaded or if error
                img.errorProperty().addListener((obs, oldV, newV) -> {
                    if (newV) {
                        imageView.setImage(null); // Clear broken image
                        Label imgLabel = new Label("No Image");
                        imgLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold;");
                        imageContainer.getChildren().add(imgLabel);
                    }
                });
            } catch (Exception e) {
                // Exception handling
            }
        }
        
        imageContainer.getChildren().add(imageView);
        imageContainer.setAlignment(Pos.CENTER);

        // Content Container
        VBox content = new VBox();
        content.getStyleClass().add("card-content");
        content.setAlignment(Pos.CENTER_LEFT);
        content.setSpacing(8);

        // Name
        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("item-name");
        nameLabel.setWrapText(true);
        nameLabel.setMaxHeight(40); // Limit height

        // Price
        Label priceLabel = new Label(String.format("$%.2f", item.getPrice()));
        priceLabel.getStyleClass().add("item-price");

        // Spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, javafx.scene.layout.Priority.ALWAYS);

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
