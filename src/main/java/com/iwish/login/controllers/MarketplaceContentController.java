package com.iwish.login.controllers;

import com.iwish.client.NetworkManager;
import com.iwish.models.Item;
import com.iwish.util.ImageCache;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.Cursor;
import javafx.geometry.Insets;

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
        setupSearch();
    }
    
    private void setupSearch() {
        if (searchField != null) {
            // Search on text change
            searchField.textProperty().addListener((obs, oldVal, newVal) -> {
                filterItems(newVal);
            });
        }
    }
    
    private void filterItems(String query) {
        if (itemsContainer == null || allItems == null) return;
        
        String lowerQuery = query == null ? "" : query.toLowerCase().trim();
        
        itemsContainer.getChildren().clear();
        
        if (lowerQuery.isEmpty()) {
            // Show all items
            for (Item item : allItems) {
                VBox card = createItemCard(item);
                itemsContainer.getChildren().add(card);
            }
        } else {
            // Filter items by name or description
            boolean found = false;
            for (Item item : allItems) {
                boolean matches = false;
                
                // Check name
                if (item.getName() != null && item.getName().toLowerCase().contains(lowerQuery)) {
                    matches = true;
                }
                
                // Check description
                if (!matches && item.getDescription() != null && 
                    item.getDescription().toLowerCase().contains(lowerQuery)) {
                    matches = true;
                }
                
                if (matches) {
                    VBox card = createItemCard(item);
                    itemsContainer.getChildren().add(card);
                    found = true;
                }
            }
            
            if (!found) {
                Label noResults = new Label("No items found matching '" + query + "'");
                noResults.setStyle("-fx-text-fill: #7f8c8d; -fx-font-size: 16px; -fx-padding: 20;");
                itemsContainer.getChildren().add(noResults);
            }
        }
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
        if (itemsContainer == null)
            return;
        itemsContainer.getChildren().clear();

        if (allItems == null)
            return;

        System.out.println("--- DEBUG: Printing Fetched Items ---");
        for (Item item : allItems) {
            System.out.println(
                    "Item: " + item.getName() + " | Price: " + item.getPrice() + " | Img: " + item.getImgSrc());
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

        // Image Container - make it clickable
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("card-image-placeholder");
        imageContainer.setCursor(Cursor.HAND);

        // Load Image
        String imageUrl = item.getImgSrc();
        ImageView imageView = new ImageView();
        Image loadedImage = null;

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

        // Use image cache for faster loading
        loadedImage = ImageCache.getInstance().getImage(imageUrl);
        
        if (loadedImage != null) {
            imageView.setImage(loadedImage);
            
            // Show placeholder if image has error
            loadedImage.errorProperty().addListener((obs, oldV, newV) -> {
                if (newV) {
                    Platform.runLater(() -> {
                        imageView.setImage(null);
                        Label imgLabel = new Label("No Image");
                        imgLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-weight: bold;");
                        imageContainer.getChildren().add(imgLabel);
                    });
                }
            });
            
            // Show placeholder while loading
            if (!loadedImage.isBackgroundLoading() || loadedImage.getProgress() < 1.0) {
                Label loadingLabel = new Label("Loading...");
                loadingLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 12px;");
                imageContainer.getChildren().add(loadingLabel);
                
                // Remove loading label when image is ready
                loadedImage.progressProperty().addListener((obs, oldV, newV) -> {
                    if (newV.doubleValue() >= 1.0) {
                        Platform.runLater(() -> {
                            imageContainer.getChildren().remove(loadingLabel);
                        });
                    }
                });
            }
        } else {
            // No image or failed to load
            Label imgLabel = new Label("📷");
            imgLabel.setStyle("-fx-font-size: 48px; -fx-text-fill: #ccc;");
            imageContainer.getChildren().add(imgLabel);
        }

        imageContainer.getChildren().add(imageView);
        imageContainer.setAlignment(Pos.CENTER);
        
        // Add click handler to show details
        final Image finalImage = loadedImage;
        imageContainer.setOnMouseClicked(e -> showItemDetails(item, finalImage));
        
        // Add hover effect
        imageContainer.setOnMouseEntered(e -> {
            imageContainer.setStyle("-fx-background-color: rgba(0,0,0,0.1); -fx-background-radius: 10 10 0 0;");
        });
        imageContainer.setOnMouseExited(e -> {
            imageContainer.setStyle("");
        });

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
        Label priceLabel = new Label(String.format("E£ %,.0f", item.getPrice()));
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

        // Description
        Label descriptionLabel = null;
        if (item.getDescription() != null && !item.getDescription().isEmpty()) {
            descriptionLabel = new Label(item.getDescription());
            descriptionLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-wrap-text: true;");
            descriptionLabel.setWrapText(true);
        }

        // Action buttons
        HBox buttonBox = new HBox(15);
        buttonBox.setAlignment(Pos.CENTER);
        buttonBox.setPadding(new Insets(20, 0, 0, 0));
        
        Button addBtn = new Button("Add to Wishlist");
        addBtn.setStyle("-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;");
        addBtn.setOnMouseEntered(e -> addBtn.setStyle(
                "-fx-background-color: #2980b9; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;"));
        addBtn.setOnMouseExited(e -> addBtn.setStyle(
                "-fx-background-color: #3498db; -fx-text-fill: white; " +
                "-fx-font-size: 16px; -fx-padding: 10 20; -fx-background-radius: 5; -fx-cursor: hand;"));
        addBtn.setOnAction(e -> {
            handleAddToWishlist(item, addBtn);
            // Don't close modal, just update button
        });
        
        buttonBox.getChildren().add(addBtn);

        detailsBox.getChildren().add(nameLabel);
        if (descriptionLabel != null) {
            detailsBox.getChildren().add(descriptionLabel);
        }
        detailsBox.getChildren().addAll(priceLabel, buttonBox);

        headerBox.getChildren().add(closeBtn);
        modalContent.getChildren().addAll(headerBox, imageBox, detailsBox);
        
        modalOverlay.getChildren().add(modalContent);
        StackPane.setAlignment(modalContent, Pos.CENTER);

        // Add modal overlay to root container (covers entire window including sidebar)
        finalContainer.getChildren().add(modalOverlay);
    }
    
}
