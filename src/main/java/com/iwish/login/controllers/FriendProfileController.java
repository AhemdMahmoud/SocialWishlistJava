package com.iwish.login.controllers;

import com.iwish.client.NetworkManager;
import com.iwish.models.Item;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.File;
import java.util.List;

public class FriendProfileController {

    @FXML
    private StackPane rootStackPane;
    @FXML
    private Label friendNameLabel;
    @FXML
    private Label wishlistTitleLabel;
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

        friendNameLabel.setText(friendName);
        wishlistTitleLabel.setText(friendName + "'s Wishlist");

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
                        // For demo purposes, randomizing funded amount if not in model
                        // Assuming Item model might not have 'funded' field properly populated
                        // matching the legacy UI which hardcoded values.
                        // We will default to 0 funded for now or random for visual test.
                        double goal = item.getPrice();
                        double funded = 0; // In real app, this comes from DB

                        itemsContainer.getChildren().add(createWishlistItem(item, funded, goal));
                    }
                }
            });
        }).start();
    }

    private HBox createWishlistItem(Item item, double funded, double goal) {
        HBox row = new HBox(20);
        row.getStyleClass().add("wishlist-item-card");

        // Image
        StackPane imageContainer = new StackPane();
        imageContainer.getStyleClass().add("item-image-container");

        try {
            Image img = null;
            if (item.getImgSrc() != null && !item.getImgSrc().isEmpty()) {
                // Try checking if it's a file path
                File f = new File(item.getImgSrc());
                if (f.exists()) {
                    img = new Image(f.toURI().toString());
                } else {
                    // Try resource
                    img = new Image(getClass().getResourceAsStream(item.getImgSrc()));
                }
            }

            if (img != null && !img.isError()) {
                ImageView iv = new ImageView(img);
                iv.setFitWidth(90);
                iv.setFitHeight(90);
                iv.setPreserveRatio(true);
                imageContainer.getChildren().add(iv);
            } else {
                throw new Exception("Image invalid");
            }
        } catch (Exception e) {
            Label placeholder = new Label("📷");
            placeholder.getStyleClass().add("placeholder-icon");
            imageContainer.getChildren().add(placeholder);
        }

        // Details
        VBox details = new VBox(10);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label nameLabel = new Label(item.getName());
        nameLabel.getStyleClass().add("item-name");

        Label priceLabel = new Label("Price: $" + String.format("%.2f", item.getPrice()));
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
                "$" + String.format("%.0f", funded) + " of $" + String.format("%.0f", goal));
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
        Label remainingLabel = new Label("Remaining needed: $" + String.format("%.0f", remaining));
        remainingLabel.getStyleClass().add("remaining-label");

        Button confirmBtn = new Button("Confirm Payment");
        confirmBtn.getStyleClass().add("confirm-button");
        confirmBtn.setOnAction(e -> {
            String amountStr = amountField.getText();
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount > 0) {
                    System.out.println("Contributing " + amount + " to item " + itemId);
                    // TODO: Implement network call for payment
                    rootStackPane.getChildren().remove(modalOverlay);
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
}
