package com.iwish.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.File;

public class WishlistPage {
    
    private Stage stage;
    
    public WishlistPage(Stage stage) {
        this.stage = stage;
    }
    
    public Scene createWishlistScene() {
        StackPane root = new StackPane();
        
        BorderPane mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: #f0f4f8;");
        
        // Left sidebar
        VBox sidebar = createSidebar();
        mainLayout.setLeft(sidebar);
        
        // Main content area
        VBox mainContent = createMainContent();
        mainLayout.setCenter(mainContent);
        
        root.getChildren().add(mainLayout);
        
        return new Scene(root, 1500, 900);
    }
    
    private VBox createSidebar() {
        VBox sidebar = new VBox(20);
        sidebar.setPrefWidth(104);
        sidebar.setStyle("-fx-background-color: #1976D2;");
        sidebar.setPadding(new Insets(20, 0, 20, 0));
        sidebar.setAlignment(Pos.TOP_CENTER);
        
        // Logo
        VBox logoBox = new VBox();
        logoBox.setAlignment(Pos.CENTER);
        logoBox.setStyle("-fx-background-color: #1565C0; -fx-padding: 10; -fx-background-radius: 5;");
        
        try {
            ImageView logoView = new ImageView(new Image(getClass().getResourceAsStream("/images/Logo.png")));
            logoView.setFitHeight(50);
            logoView.setFitWidth(50);
            logoView.setPreserveRatio(true);
            logoBox.getChildren().add(logoView);
        } catch (Exception e) {
            Label logoLabel = new Label("W");
            logoLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");
            logoBox.getChildren().add(logoLabel);
        }
        
        // Navigation buttons
        VBox homeBtn = createNavButton("🏠", "Home", false);
        VBox wishlistBtn = createNavButton("♥", "My Wishlist", true);
        VBox friendsBtn = createNavButton("👥", "Friends", false);
        VBox notificationBtn = createNotificationButton();
        
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        
        VBox logoutBtn = createNavButton("➜", "Logout", false);
        
        sidebar.getChildren().addAll(logoBox, homeBtn, wishlistBtn, friendsBtn, notificationBtn, spacer, logoutBtn);
        
        return sidebar;
    }
    
    private VBox createNavButton(String icon, String text, boolean selected) {
        VBox btn = new VBox(5);
        btn.setAlignment(Pos.CENTER);
        btn.setPadding(new Insets(15, 10, 15, 10));
        btn.setStyle(selected ? "-fx-background-color: #1565C0; -fx-background-radius: 5;" : 
                                "-fx-background-color: transparent;");
        btn.setCursor(javafx.scene.Cursor.HAND);
        
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");
        
        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: white;");
        
        btn.getChildren().addAll(iconLabel, textLabel);
        
        btn.setOnMouseEntered(e -> {
            if (!selected) btn.setStyle("-fx-background-color: #1565C0; -fx-background-radius: 5;");
        });
        
        btn.setOnMouseExited(e -> {
            if (!selected) btn.setStyle("-fx-background-color: transparent;");
        });
        
        btn.setOnMouseClicked(e -> {
            System.out.println(text + " clicked");
            try {
                if (text.equals("Home")) {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/com/iwish/client/marketplace.fxml"));
                    javafx.scene.Parent root = loader.load();
                    stage.setScene(new Scene(root));
                } else if (text.equals("Friends")) {
                    FriendProfilePage friendPage = new FriendProfilePage(stage, "Ahmed");
                    stage.setScene(friendPage.createFriendProfileScene());
                } else if (text.equals("My Wishlist")) {
                    // Already here
                } else if (text.equals("Logout")) {
                    // TODO: Logout logic
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        
        return btn;
    }
    
    private VBox createNotificationButton() {
        VBox btn = new VBox(5);
        btn.setAlignment(Pos.CENTER);
        btn.setPadding(new Insets(15, 10, 15, 10));
        btn.setStyle("-fx-background-color: transparent;");
        btn.setCursor(javafx.scene.Cursor.HAND);
        
        // Notification icon with badge
        StackPane iconContainer = new StackPane();
        
        Label iconLabel = new Label("🔔");
        iconLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");
        
        // Notification badge
        Circle badge = new Circle(8);
        badge.setFill(Color.web("#e74c3c"));
        badge.setStroke(Color.WHITE);
        badge.setStrokeWidth(2);
        
        Label badgeLabel = new Label("3");
        badgeLabel.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        StackPane badgeContainer = new StackPane(badge, badgeLabel);
        StackPane.setAlignment(badgeContainer, Pos.TOP_RIGHT);
        badgeContainer.setTranslateX(8);
        badgeContainer.setTranslateY(-8);
        
        iconContainer.getChildren().addAll(iconLabel, badgeContainer);
        
        Label textLabel = new Label("Notifications");
        textLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: white;");
        
        btn.getChildren().addAll(iconContainer, textLabel);
        
        btn.setOnMouseEntered(e -> btn.setStyle("-fx-background-color: #1565C0; -fx-background-radius: 5;"));
        btn.setOnMouseExited(e -> btn.setStyle("-fx-background-color: transparent;"));
        btn.setOnMouseClicked(e -> System.out.println("Notifications clicked"));
        
        return btn;
    }
    
    private VBox createMainContent() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30, 40, 30, 40));
        
        // Header
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(header, Priority.ALWAYS);
        
        Label title = new Label("My Wishlist");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        // User icon
        StackPane userIconContainer = new StackPane();
        Circle userIcon = new Circle(20);
        userIcon.setFill(Color.web("#e0e0e0"));
        userIcon.setStroke(Color.web("#666"));
        userIcon.setStrokeWidth(2);
        
        Label userLabel = new Label("👤");
        userLabel.setStyle("-fx-font-size: 20px;");
        
        userIconContainer.getChildren().addAll(userIcon, userLabel);
        userIconContainer.setCursor(javafx.scene.Cursor.HAND);
        
        header.getChildren().addAll(title, spacer, userIconContainer);
        
        // Wishlist items in scroll pane
        VBox itemsContainer = new VBox(20);
        itemsContainer.setPadding(new Insets(10));
        
        // Add wishlist items
        itemsContainer.getChildren().addAll(
            createWishlistItem("C:\\Users\\11\\Downloads\\SocialWishlistJava\\src\\main\\java\\resources\\images\\51JNhjr4McL._AC_SY300_SX300_QL70_ML2_.jpg", "Wireless Headphones", 35.00, 60, 100),
            createWishlistItem("C:\\Users\\11\\Downloads\\SocialWishlistJava\\src\\main\\java\\resources\\images\\71FwYuL1FDL._AC_SY300_SX300_QL70_ML2_.jpg", "Amazon Smartwatch", 150.00, 100, 150),
            createWishlistItem("C:\\Users\\11\\Downloads\\SocialWishlistJava\\src\\main\\java\\resources\\images\\5158rDvSz+L._AC_SY300_SX300_QL70_ML2_.jpg", "Wireless Sernyton", 100.00, 60, 100),
            createWishlistItem("C:\\Users\\11\\Downloads\\SocialWishlistJava\\src\\main\\java\\resources\\images\\download.jpg", "Wireless headphone", 120.00, 40, 120),
            createWishlistItem("C:\\Users\\11\\Downloads\\SocialWishlistJava\\src\\main\\java\\resources\\images\\images (1).jpg", "Wireles headphone", 190.00, 170, 190)
        );
        
        ScrollPane scrollPane = new ScrollPane(itemsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPannable(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        content.getChildren().addAll(header, scrollPane);
        
        return content;
    }
    
    private HBox createWishlistItem(String imagePath, String name, double price, 
                                    double funded, double goal) {
        HBox item = new HBox(20);
        item.setPadding(new Insets(20));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        // Product image
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: #f5f5f5; " +
                               "-fx-background-radius: 8; -fx-cursor: hand;");
        imageContainer.setPrefSize(100, 100);
        imageContainer.setMinSize(100, 100);
        imageContainer.setMaxSize(100, 100);
        
        Image productImage = null;
        
        try {
            // Try to load the image
            // Try loading from file system first
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                productImage = new Image(imgFile.toURI().toString());
            } else {
                // Try loading from resources
                productImage = new Image(getClass().getResourceAsStream("/" + imagePath));
            }
            
            ImageView imageView = new ImageView(productImage);
            imageView.setFitWidth(90);
            imageView.setFitHeight(90);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            
            imageContainer.getChildren().add(imageView);
        } catch (Exception e) {
            // If image loading fails, show a placeholder
            Label placeholderLabel = new Label("📷");
            placeholderLabel.setStyle("-fx-font-size: 48px;");
            imageContainer.getChildren().add(placeholderLabel);
            System.out.println("Could not load image: " + imagePath);
        }
        
        // Add click event to show details
        final Image finalImage = productImage;
        imageContainer.setOnMouseClicked(e -> showItemDetails(finalImage, imagePath, name, price, funded, goal));
        
        // Add hover effect
        imageContainer.setOnMouseEntered(e -> 
            imageContainer.setStyle("-fx-background-color: #e0e0e0; " +
                                   "-fx-background-radius: 8; -fx-cursor: hand;"));
        imageContainer.setOnMouseExited(e -> 
            imageContainer.setStyle("-fx-background-color: #f5f5f5; " +
                                   "-fx-background-radius: 8; -fx-cursor: hand;"));
        
        // Product details
        VBox details = new VBox(10);
        HBox.setHgrow(details, Priority.ALWAYS);
        
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label priceLabel = new Label("Total price: $" + String.format("%.2f", price));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        // Calculate progress percentage correctly
        double progress = Math.min(funded / goal, 1.0); // Ensure it doesn't exceed 100%
        int progressPercent = (int)(progress * 100);
        
        // Progress bar container
        HBox progressContainer = new HBox(15);
        progressContainer.setAlignment(Pos.CENTER_LEFT);
        
        StackPane progressStack = new StackPane();
        HBox.setHgrow(progressStack, Priority.ALWAYS);
        progressStack.setMaxWidth(Double.MAX_VALUE);
        
        // Background bar (gray)
        ProgressBar bgBar = new ProgressBar(1.0);
        bgBar.setPrefHeight(35);
        bgBar.setMaxWidth(Double.MAX_VALUE);
        bgBar.getStyleClass().add("background-bar");
        bgBar.setStyle("-fx-accent: #e0e0e0; -fx-background-radius: 5;");
        
        // Actual progress bar (blue)
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setPrefHeight(35);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.getStyleClass().add("progress-bar");
        progressBar.setStyle("-fx-accent: #2196F3; -fx-background-radius: 5;");
        
        // Progress label
        Label progressLabel = new Label(progressPercent + "% funded");
        progressLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: white;");
        StackPane.setAlignment(progressLabel, Pos.CENTER_LEFT);
        progressLabel.setPadding(new Insets(0, 0, 0, 15));
        
        progressStack.getChildren().addAll(bgBar, progressBar, progressLabel);
        
        Label fundingLabel = new Label("$" + String.format("%.0f", funded) + " of $" + String.format("%.0f", goal));
        fundingLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d; -fx-font-weight: bold;");
        fundingLabel.setPrefWidth(120);
        fundingLabel.setAlignment(Pos.CENTER_RIGHT);
        
        progressContainer.getChildren().addAll(progressStack, fundingLabel);
        
        details.getChildren().addAll(nameLabel, priceLabel, progressContainer);
        
        // Delete button
        Button deleteBtn = new Button("🗑");
        deleteBtn.setPrefSize(50, 50);
        deleteBtn.setStyle("-fx-background-color: #e74c3c; -fx-text-fill: white; " +
                          "-fx-font-size: 18px; -fx-background-radius: 8; " +
                          "-fx-cursor: hand; -fx-border-width: 0;");
        
        deleteBtn.setOnMouseEntered(e -> deleteBtn.setStyle(
            "-fx-background-color: #c0392b; -fx-text-fill: white; " +
            "-fx-font-size: 18px; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-width: 0;"));
        
        deleteBtn.setOnMouseExited(e -> deleteBtn.setStyle(
            "-fx-background-color: #e74c3c; -fx-text-fill: white; " +
            "-fx-font-size: 18px; -fx-background-radius: 8; -fx-cursor: hand; -fx-border-width: 0;"));
        
        deleteBtn.setOnAction(e -> {
            System.out.println("Delete clicked for: " + name);
            // TODO: Implement delete functionality
            // Remove item from parent container
            ((VBox)item.getParent()).getChildren().remove(item);
        });
        
        item.getChildren().addAll(imageContainer, details, deleteBtn);
        
        return item;
    }
    
    private void showItemDetails(Image image, String imagePath, String name, double price, 
                                 double funded, double goal) {
        // Create modal overlay
        StackPane modalOverlay = new StackPane();
        modalOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        modalOverlay.setOnMouseClicked(e -> {
            // Close modal when clicking on overlay
            ((StackPane)stage.getScene().getRoot()).getChildren().remove(modalOverlay);
        });
        
        // Create modal content
        VBox modalContent = new VBox(20);
        modalContent.setAlignment(Pos.CENTER);
        modalContent.setPadding(new Insets(40));
        modalContent.setMaxWidth(600);
        modalContent.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");
        
        // Prevent closing when clicking inside the modal
        modalContent.setOnMouseClicked(e -> e.consume());
        
        // Close button
        HBox headerBox = new HBox();
        headerBox.setAlignment(Pos.CENTER_RIGHT);
        Button closeBtn = new Button("✖");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #999; " +
                         "-fx-font-size: 20px; -fx-cursor: hand; -fx-border-width: 0;");
        closeBtn.setOnMouseEntered(e -> closeBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #333; " +
            "-fx-font-size: 20px; -fx-cursor: hand; -fx-border-width: 0;"));
        closeBtn.setOnMouseExited(e -> closeBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #999; " +
            "-fx-font-size: 20px; -fx-cursor: hand; -fx-border-width: 0;"));
        closeBtn.setOnAction(e -> 
            ((StackPane)stage.getScene().getRoot()).getChildren().remove(modalOverlay));
        headerBox.getChildren().add(closeBtn);
        
        // Large image
        StackPane imageBox = new StackPane();
        imageBox.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 10;");
        imageBox.setPrefSize(500, 350);
        imageBox.setMaxSize(500, 350);
        
        if (image != null) {
            ImageView largeImageView = new ImageView(image);
            largeImageView.setFitWidth(480);
            largeImageView.setFitHeight(330);
            largeImageView.setPreserveRatio(true);
            largeImageView.setSmooth(true);
            imageBox.getChildren().add(largeImageView);
        } else {
            Label placeholderLabel = new Label("📷");
            placeholderLabel.setStyle("-fx-font-size: 100px;");
            imageBox.getChildren().add(placeholderLabel);
        }
        
        // Product details
        VBox detailsBox = new VBox(15);
        detailsBox.setAlignment(Pos.CENTER_LEFT);
        detailsBox.setPadding(new Insets(20, 30, 20, 30));
        
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        nameLabel.setWrapText(true);
        
        Label priceLabel = new Label("Total Price: $" + String.format("%.2f", price));
        priceLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #7f8c8d;");
        
        double progress = Math.min(funded / goal, 1.0);
        int progressPercent = (int)(progress * 100);
        
        Label fundingLabel = new Label("Funding: $" + String.format("%.0f", funded) + 
                                      " of $" + String.format("%.0f", goal) + 
                                      " (" + progressPercent + "%)");
        fundingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #27ae60; -fx-font-weight: bold;");
        
        double remaining = Math.max(goal - funded, 0);
        Label remainingLabel = new Label("Remaining: $" + String.format("%.0f", remaining));
        remainingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #e74c3c;");
        
        // Progress bar
        ProgressBar detailProgressBar = new ProgressBar(progress);
        detailProgressBar.setPrefWidth(500);
        detailProgressBar.setPrefHeight(25);
        detailProgressBar.setStyle("-fx-accent: #2196F3;");
        
        detailsBox.getChildren().addAll(nameLabel, priceLabel, fundingLabel, 
                                        remainingLabel, detailProgressBar);
        
        modalContent.getChildren().addAll(headerBox, imageBox, detailsBox);
        modalOverlay.getChildren().add(modalContent);
        
        // Add modal to scene
        ((StackPane)stage.getScene().getRoot()).getChildren().add(modalOverlay);
    }
}