package com.iwish.client;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.io.File;

public class FriendProfilePage {
    
    private Stage stage;
    private String friendName;
    
    public FriendProfilePage(Stage stage, String friendName) {
        this.stage = stage;
        this.friendName = friendName;
    }
    
    public Scene createFriendProfileScene() {
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
        logoBox.setStyle("-fx-background-color: #1565C0; -fx-padding: 15; -fx-background-radius: 5;");
        Label logoLabel = new Label("W");
        logoLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: white;");
        logoBox.getChildren().add(logoLabel);
        
        // Navigation buttons
        VBox homeBtn = createNavButton("🏠", "Home", false);
        VBox wishlistBtn = createNavButton("♥", "My Wishlist", false);
        VBox friendsBtn = createNavButton("👥", "Friends", true);
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
            // TODO: Add navigation logic here
        });
        
        return btn;
    }
    
    private VBox createNotificationButton() {
        VBox btn = new VBox(5);
        btn.setAlignment(Pos.CENTER);
        btn.setPadding(new Insets(15, 10, 15, 10));
        btn.setStyle("-fx-background-color: transparent;");
        btn.setCursor(javafx.scene.Cursor.HAND);
        
        StackPane iconContainer = new StackPane();
        
        Label iconLabel = new Label("🔔");
        iconLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");
        
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
        VBox content = new VBox(30);
        content.setPadding(new Insets(30, 40, 30, 40));
        
        // Header with title and user icon
        HBox header = new HBox();
        header.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(header, Priority.ALWAYS);
        
        Label title = new Label("Friend's Profile");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
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
        
        // Friend profile section
        HBox profileSection = new HBox(20);
        profileSection.setAlignment(Pos.CENTER_LEFT);
        profileSection.setPadding(new Insets(20));
        
        // Friend avatar
        Circle avatar = new Circle(50);
        avatar.setFill(Color.web("#1976D2"));
        
        StackPane avatarContainer = new StackPane();
        avatarContainer.getChildren().add(avatar);
        
        Label avatarIcon = new Label("😉");
        avatarIcon.setStyle("-fx-font-size: 50px; -fx-text-fill: #5a4a4a;");
        avatarContainer.getChildren().add(avatarIcon);
        
        Label friendNameLabel = new Label(friendName);
        friendNameLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #f0f4f8;");
        
        profileSection.getChildren().addAll(avatarContainer, friendNameLabel);
        
        // Wishlist title
        Label wishlistTitle = new Label(friendName + "'s Wishlist");
        wishlistTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        wishlistTitle.setPadding(new Insets(10, 0, 10, 0));
        
        // Wishlist items in scroll pane
        VBox itemsContainer = new VBox(20);
        itemsContainer.setPadding(new Insets(10));
        
        itemsContainer.getChildren().addAll(
            createFriendWishlistItem("C:\\Users\\11\\Downloads\\SocialWishlistJava\\src\\main\\java\\resources\\images\\51JNhjr4McL._AC_SY300_SX300_QL70_ML2_.jpg", "Wireless Headphones", 35.00, 60, 100),
            createFriendWishlistItem("C:\\Users\\11\\Downloads\\SocialWishlistJava\\src\\main\\java\\resources\\images\\71FwYuL1FDL._AC_SY300_SX300_QL70_ML2_.jpg", "Amazon Smartwatch", 150.00, 100, 150),
            createFriendWishlistItem("C:\\Users\\11\\Downloads\\SocialWishlistJava\\src\\main\\java\\resources\\images\\5158rDvSz+L._AC_SY300_SX300_QL70_ML2_.jpg", "Wireless Sernyton", 100.00, 60, 100),
            createFriendWishlistItem("C:\\Users\\11\\Downloads\\SocialWishlistJava\\src\\main\\java\\resources\\images\\download.jpg", "Wireless headphone", 120.00, 40, 120)
        );
        
        ScrollPane scrollPane = new ScrollPane(itemsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPannable(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);
        
        content.getChildren().addAll(header, profileSection, wishlistTitle, scrollPane);
        
        return content;
    }
    
    private HBox createFriendWishlistItem(String imagePath, String name, double price, 
                                          double funded, double goal) {
        HBox item = new HBox(20);
        item.setPadding(new Insets(20));
        item.setAlignment(Pos.CENTER_LEFT);
        item.setStyle("-fx-background-color: white; -fx-background-radius: 10; " +
                      "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 10, 0, 0, 2);");
        
        // Product image
        StackPane imageContainer = new StackPane();
        imageContainer.setStyle("-fx-background-color: #f5f5f5; -fx-background-radius: 8;");
        imageContainer.setPrefSize(100, 100);
        imageContainer.setMinSize(100, 100);
        imageContainer.setMaxSize(100, 100);
        
        try {
            Image productImage = null;
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                productImage = new Image(imgFile.toURI().toString());
            } else {
                productImage = new Image(getClass().getResourceAsStream("/" + imagePath));
            }
            
            ImageView imageView = new ImageView(productImage);
            imageView.setFitWidth(90);
            imageView.setFitHeight(90);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            
            imageContainer.getChildren().add(imageView);
        } catch (Exception e) {
            Label placeholderLabel = new Label("📷");
            placeholderLabel.setStyle("-fx-font-size: 48px;");
            imageContainer.getChildren().add(placeholderLabel);
        }
        
        // Product details
        VBox details = new VBox(10);
        HBox.setHgrow(details, Priority.ALWAYS);
        
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        
        Label priceLabel = new Label("Total price: $" + String.format("%.2f", price));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");
        
        double progress = Math.min(funded / goal, 1.0);
        int progressPercent = (int)(progress * 100);
        
        // Progress bar container
        HBox progressContainer = new HBox(15);
        progressContainer.setAlignment(Pos.CENTER_LEFT);
        
        StackPane progressStack = new StackPane();
        HBox.setHgrow(progressStack, Priority.ALWAYS);
        progressStack.setMaxWidth(Double.MAX_VALUE);
        
        ProgressBar bgBar = new ProgressBar(1.0);
        bgBar.setPrefHeight(35);
        bgBar.setMaxWidth(Double.MAX_VALUE);
        bgBar.setStyle("-fx-accent: #e0e0e0; -fx-background-radius: 5;");
        
        ProgressBar progressBar = new ProgressBar(progress);
        progressBar.setPrefHeight(35);
        progressBar.setMaxWidth(Double.MAX_VALUE);
        progressBar.setStyle("-fx-accent: #1976D2; -fx-background-radius: 5;");
        
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
        
        // Contribute button
        Button contributeBtn = new Button("Contribute");
        contributeBtn.setPrefSize(120, 45);
        contributeBtn.setStyle("-fx-background-color: #d97a34; -fx-text-fill: white; " +
                              "-fx-font-size: 14px; -fx-font-weight: bold; " +
                              "-fx-background-radius: 8; -fx-cursor: hand; -fx-border-width: 0;");
        
        contributeBtn.setOnMouseEntered(e -> contributeBtn.setStyle(
            "-fx-background-color: #c46a24; -fx-text-fill: white; " +
            "-fx-font-size: 14px; -fx-font-weight: bold; " +
            "-fx-background-radius: 8; -fx-cursor: hand; -fx-border-width: 0;"));
        
        contributeBtn.setOnMouseExited(e -> contributeBtn.setStyle(
            "-fx-background-color: #d97a34; -fx-text-fill: white; " +
            "-fx-font-size: 14px; -fx-font-weight: bold; " +
            "-fx-background-radius: 8; -fx-cursor: hand; -fx-border-width: 0;"));
        
        contributeBtn.setOnAction(e -> showContributionModal(name, price, funded, goal));
        
        item.getChildren().addAll(imageContainer, details, contributeBtn);
        
        return item;
    }
    
    private void showContributionModal(String itemName, double price, double funded, double goal) {
        StackPane modalOverlay = new StackPane();
        modalOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.5);");
        
        VBox modalContent = new VBox(20);
        modalContent.setAlignment(Pos.CENTER);
        modalContent.setPadding(new Insets(30));
        modalContent.setMaxWidth(450);
        modalContent.setStyle("-fx-background-color: white; -fx-background-radius: 15; " +
                             "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 20, 0, 0, 5);");
        
        modalContent.setOnMouseClicked(e -> e.consume());
        
        // Modal title
        Label titleLabel = new Label("Contribute to " + itemName);
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");
        titleLabel.setWrapText(true);
        titleLabel.setMaxWidth(400);
        
        // Amount input field
        TextField amountField = new TextField();
        amountField.setPromptText("Amount (e.g., 50)");
        amountField.setStyle("-fx-font-size: 16px; -fx-padding: 12; " +
                            "-fx-border-color: #3498db; -fx-border-width: 2; " +
                            "-fx-border-radius: 8; -fx-background-radius: 8;");
        amountField.setPrefHeight(50);
        
        // Remaining needed label
        double remaining = Math.max(goal - funded, 0);
        Label remainingLabel = new Label("Remaining needed: $" + String.format("%.0f", remaining));
        remainingLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #2c3e50; -fx-font-weight: bold;");
        
        // Confirm button
        Button confirmBtn = new Button("Confirm Payment");
        confirmBtn.setPrefWidth(380);
        confirmBtn.setPrefHeight(50);
        confirmBtn.setStyle("-fx-background-color: #ff6b1a; -fx-text-fill: white; " +
                           "-fx-font-size: 16px; -fx-font-weight: bold; " +
                           "-fx-background-radius: 8; -fx-cursor: hand;");
        
        confirmBtn.setOnMouseEntered(e -> confirmBtn.setStyle(
            "-fx-background-color: #e55a0a; -fx-text-fill: white; " +
            "-fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-background-radius: 8; -fx-cursor: hand;"));
        
        confirmBtn.setOnMouseExited(e -> confirmBtn.setStyle(
            "-fx-background-color: #ff6b1a; -fx-text-fill: white; " +
            "-fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-background-radius: 8; -fx-cursor: hand;"));
        
        confirmBtn.setOnAction(e -> {
            String amount = amountField.getText();
            if (!amount.isEmpty()) {
                System.out.println("Contributing $" + amount + " to " + itemName);
                // TODO: Process payment
                ((StackPane)stage.getScene().getRoot()).getChildren().remove(modalOverlay);
            }
        });
        
        // Cancel button
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setPrefWidth(380);
        cancelBtn.setPrefHeight(50);
        cancelBtn.setStyle("-fx-background-color: #d0d0d0; -fx-text-fill: #2c3e50; " +
                          "-fx-font-size: 16px; -fx-font-weight: bold; " +
                          "-fx-background-radius: 8; -fx-cursor: hand;");
        
        cancelBtn.setOnMouseEntered(e -> cancelBtn.setStyle(
            "-fx-background-color: #c0c0c0; -fx-text-fill: #2c3e50; " +
            "-fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-background-radius: 8; -fx-cursor: hand;"));
        
        cancelBtn.setOnMouseExited(e -> cancelBtn.setStyle(
            "-fx-background-color: #d0d0d0; -fx-text-fill: #2c3e50; " +
            "-fx-font-size: 16px; -fx-font-weight: bold; " +
            "-fx-background-radius: 8; -fx-cursor: hand;"));
        
        cancelBtn.setOnAction(e -> 
            ((StackPane)stage.getScene().getRoot()).getChildren().remove(modalOverlay));
        
        modalContent.getChildren().addAll(titleLabel, amountField, remainingLabel, confirmBtn, cancelBtn);
        modalOverlay.getChildren().add(modalContent);
        
        modalOverlay.setOnMouseClicked(e -> 
            ((StackPane)stage.getScene().getRoot()).getChildren().remove(modalOverlay));
        
        ((StackPane)stage.getScene().getRoot()).getChildren().add(modalOverlay);
    }
}
