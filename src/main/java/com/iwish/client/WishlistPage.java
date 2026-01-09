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
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import com.iwish.models.Notification;
import com.iwish.models.Item;

public class WishlistPage {

    private Stage stage;
    private VBox itemsContainer;
    private Label totalValueLabel;
    private double currentTotal = 0.0;

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
            ImageView logoView = new ImageView(
                    new Image(getClass().getResourceAsStream("/images/Logo.png")));
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

        sidebar.getChildren().addAll(logoBox, homeBtn, wishlistBtn, friendsBtn, notificationBtn, spacer,
                logoutBtn);

        return sidebar;
    }

    private VBox createNavButton(String icon, String text, boolean selected) {
        VBox btn = new VBox(5);
        btn.setAlignment(Pos.CENTER);
        btn.setPadding(new Insets(15, 10, 15, 10));
        btn.setStyle(selected ? "-fx-background-color: #1565C0; -fx-background-radius: 5;"
                : "-fx-background-color: transparent;");
        btn.setCursor(javafx.scene.Cursor.HAND);

        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24px; -fx-text-fill: white;");

        Label textLabel = new Label(text);
        textLabel.setStyle("-fx-font-size: 10px; -fx-text-fill: white;");

        btn.getChildren().addAll(iconLabel, textLabel);

        btn.setOnMouseEntered(e -> {
            if (!selected)
                btn.setStyle("-fx-background-color: #1565C0; -fx-background-radius: 5;");
        });

        btn.setOnMouseExited(e -> {
            if (!selected)
                btn.setStyle("-fx-background-color: transparent;");
        });

        btn.setOnMouseClicked(e -> {
            System.out.println(text + " clicked");
            try {
                if (text.equals("Home")) {
                    javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                            getClass().getResource("/com/iwish/client/marketplace.fxml"));
                    javafx.scene.Parent root = loader.load();
                    stage.setScene(new Scene(root));
                } else if (text.equals("Friends")) {
                    FriendProfilePage friendPage = new FriendProfilePage(stage, "Ahmed", 2);
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

        // Notification badge - will be updated dynamically
        Circle badge = new Circle(8);
        badge.setFill(Color.web("#e74c3c"));
        badge.setStroke(Color.WHITE);
        badge.setStrokeWidth(2);
        badge.setVisible(false); // Initially hidden

        Label badgeLabel = new Label();
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

        // Add click handler to show notifications
        btn.setOnMouseClicked(e -> {
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                        getClass().getResource("/com/iwish/client/notifications_view.fxml"));
                javafx.scene.Parent root = loader.load();
                Stage stage = (Stage) btn.getScene().getWindow();
                stage.setScene(new Scene(root));
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });

        // Load notifications count
        loadNotificationCount(badge, badgeLabel);

        return btn;
    }

    private void loadNotificationCount(Circle badge, Label badgeLabel) {
        new Thread(() -> {
            try {
                NetworkManager network = NetworkManager.getInstance();
                if (network.connect()) {
                    int userId = network.getCurrentUserId();
                    List<Notification> notifications = network.getNotifications(userId);
                    long unreadCount = notifications.stream().filter(n -> !n.isRead()).count();

                    javafx.application.Platform.runLater(() -> {
                        if (unreadCount > 0) {
                            badge.setVisible(true);
                            badgeLabel.setText(String.valueOf(unreadCount));
                        } else {
                            badge.setVisible(false);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
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

        // Total Value Label
        totalValueLabel = new Label("Total: $0.00");
        totalValueLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #27ae60;");
        totalValueLabel.setPadding(new Insets(0, 20, 0, 0));

        header.getChildren().addAll(title, spacer, totalValueLabel, userIconContainer);

        // Wishlist items container
        itemsContainer = new VBox(20);
        itemsContainer.setPadding(new Insets(10));

        ScrollPane scrollPane = new ScrollPane(itemsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        scrollPane.setPannable(true);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        content.getChildren().addAll(header, scrollPane);

        // Load items dynamically
        loadItems();

        return content;
    }

    private void loadItems() {
        new Thread(() -> {
            try {
                NetworkManager network = NetworkManager.getInstance();
                if (network.connect()) {
                    List<Item> items = network.getUserWishlist(network.getCurrentUserId());
                    Platform.runLater(() -> populateList(items));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void populateList(List<Item> items) {
        itemsContainer.getChildren().clear();
        currentTotal = 0.0;

        if (items.isEmpty()) {
            Label emptyLabel = new Label("Your wishlist is empty. Go to Marketplace to add items!");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #7f8c8d;");
            itemsContainer.getChildren().add(emptyLabel);
        } else {
            // Calculate total value
            for (Item item : items) {
                currentTotal += item.getPrice();
            }

            // Group items by ID
            Map<Integer, List<Item>> groupedItems = items.stream()
                    .collect(Collectors.groupingBy(Item::getId));

            // Create controls for each group
            for (List<Item> group : groupedItems.values()) {
                if (group.isEmpty())
                    continue;
                Item representative = group.get(0); // Use the first item for details
                int quantity = group.size();
                itemsContainer.getChildren().add(createWishlistItem(representative, quantity));
            }
        }
        updateTotalLabel();
    }

    private void updateTotalLabel() {
        if (totalValueLabel != null) {
            totalValueLabel.setText("Total: $" + String.format("%.2f", currentTotal));
        }
    }

    private HBox createWishlistItem(Item itemData, int quantity) {
        String imagePath = itemData.getImgSrc();
        String name = itemData.getName();
        double unitPrice = itemData.getPrice();
        double totalPrice = unitPrice * quantity; // Show summed price
        double funded = itemData.getFunded() * quantity; // Assuming funded scales (or just show unit funded? user asked
                                                         // for sum price)
        double goal = totalPrice;

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
            if (imagePath != null && !imagePath.isEmpty()) {
                if (imagePath.startsWith("http")) {
                    productImage = new Image(imagePath);
                } else {
                    File imgFile = new File(imagePath);
                    if (imgFile.exists()) {
                        productImage = new Image(imgFile.toURI().toString());
                    } else {
                        productImage = new Image(getClass().getResourceAsStream("/" + imagePath));
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }

        if (productImage != null && !productImage.isError()) {
            ImageView imageView = new ImageView(productImage);
            imageView.setFitWidth(90);
            imageView.setFitHeight(90);
            imageView.setPreserveRatio(true);
            imageView.setSmooth(true);
            imageContainer.getChildren().add(imageView);
        } else {
            Label placeholderLabel = new Label("📷");
            placeholderLabel.setStyle("-fx-font-size: 48px;");
            imageContainer.getChildren().add(placeholderLabel);
        }

        // Add click event to show details
        final Image finalImage = productImage;
        imageContainer.setOnMouseClicked(
                e -> showItemDetails(finalImage, imagePath, name, totalPrice, funded, goal));

        // Product details
        VBox details = new VBox(10);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;");

        // Price Label showing Unit or Total? User asked "added agin it 80" -> Total
        Label priceLabel = new Label("Total Value: $" + String.format("%.2f", totalPrice));
        priceLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #7f8c8d;");

        // Quantity Control Container (Pill shape)
        HBox qtyControl = new HBox(15);
        qtyControl.setAlignment(Pos.CENTER);
        qtyControl.setStyle("-fx-background-color: #1976D2; -fx-background-radius: 20; -fx-padding: 5 15 5 15;");
        qtyControl.setPrefHeight(40);
        qtyControl.setMaxWidth(150);

        // Delete (Trash) Button
        Button trashBtn = new Button("🗑");
        trashBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold; -fx-cursor: hand;");
        trashBtn.setPadding(Insets.EMPTY);
        trashBtn.setOnAction(e -> {
            new Thread(() -> {
                NetworkManager network = NetworkManager.getInstance();
                if (network.removeFromWishlist(network.getCurrentUserId(), itemData.getId())) {
                    Platform.runLater(this::loadItems); // Refresh full list to handle grouping/price updates cleanly
                }
            }).start();
        });

        // Quantity Label
        Label qtyLabel = new Label(String.valueOf(quantity));
        qtyLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");

        // Plus Button
        Button plusBtn = new Button("+");
        plusBtn.setStyle(
                "-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-cursor: hand;");
        plusBtn.setPadding(Insets.EMPTY);
        plusBtn.setOnAction(e -> {
            new Thread(() -> {
                NetworkManager network = NetworkManager.getInstance();
                if (network.addToWishlist(network.getCurrentUserId(), itemData.getId())) {
                    Platform.runLater(this::loadItems); // Refresh full list
                }
            }).start();
        });

        qtyControl.getChildren().addAll(trashBtn, qtyLabel, plusBtn);

        details.getChildren().addAll(nameLabel, priceLabel);

        // Right side container for control
        HBox rightContainer = new HBox(qtyControl);
        rightContainer.setAlignment(Pos.CENTER_RIGHT);

        item.getChildren().addAll(imageContainer, details, rightContainer);

        return item;
    }

    private void showItemDetails(Image image, String imagePath, String name, double price,
            double funded, double goal) {
        // Create modal overlay
        StackPane modalOverlay = new StackPane();
        modalOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.7);");
        modalOverlay.setOnMouseClicked(e -> {
            // Close modal when clicking on overlay
            ((StackPane) stage.getScene().getRoot()).getChildren().remove(modalOverlay);
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
        closeBtn.setOnAction(e -> ((StackPane) stage.getScene().getRoot()).getChildren().remove(modalOverlay));
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
        int progressPercent = (int) (progress * 100);

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
        ((StackPane) stage.getScene().getRoot()).getChildren().add(modalOverlay);
    }
}