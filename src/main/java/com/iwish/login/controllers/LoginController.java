package com.iwish.login.controllers;

import com.iwish.client.NetworkManager;
import java.io.IOException;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;

public class LoginController {

    // --- Login Fields ---
    @FXML
    private VBox loginForm;
    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordVisibleField;
    @FXML
    private Label loginMessageLabel; // Inline Message

    // --- Register Fields ---
    @FXML
    private VBox registerForm;
    @FXML
    private TextField regUsernameField;
    @FXML
    private TextField regEmailField;
    @FXML
    private PasswordField regPasswordField;
    @FXML
    private TextField regPasswordVisibleField;
    @FXML
    private PasswordField regConfirmPasswordField;
    @FXML
    private TextField regConfirmPasswordVisibleField;
    @FXML
    private Label registerMessageLabel; // Inline Message

    private boolean isPasswordVisible = false;
    private boolean isRegPasswordVisible = false;
    private boolean isRegConfirmPasswordVisible = false;

    @FXML
    public void initialize() {
        if (loginForm != null && registerForm != null) {
            loginForm.setVisible(true);
            registerForm.setVisible(false);
        }

        setupPasswordReveal(passwordField, passwordVisibleField);
        setupPasswordReveal(regPasswordField, regPasswordVisibleField);
        setupPasswordReveal(regConfirmPasswordField, regConfirmPasswordVisibleField);

        // Initial Connection Check
        if (!NetworkManager.getInstance().connect()) {
            showLoginMessage("Server Unavailable: Check Connection", true);
        }
    }

    private void setupPasswordReveal(PasswordField pf, TextField tf) {
        if (tf != null && pf != null) {
            tf.managedProperty().bind(tf.visibleProperty());
            tf.visibleProperty().bind(pf.visibleProperty().not());
            tf.textProperty().bindBidirectional(pf.textProperty());
        }
    }

    // --- Navigation & State Reset ---
    @FXML
    private void showRegister() {
        loginForm.setVisible(false);
        registerForm.setVisible(true);
        clearMessages();
    }

    @FXML
    private void showLogin() {
        registerForm.setVisible(false);
        loginForm.setVisible(true);
        clearMessages();
    }

    private void clearMessages() {
        if (loginMessageLabel != null) {
            loginMessageLabel.setVisible(false);
            loginMessageLabel.setManaged(false);
        }
        if (registerMessageLabel != null) {
            registerMessageLabel.setVisible(false);
            registerMessageLabel.setManaged(false);
        }
    }

    // --- Helper for Inline Messages ---
    private void showLoginMessage(String message, boolean isError) {
        updateLabel(loginMessageLabel, message, isError);
    }

    private void showRegisterMessage(String message, boolean isError) {
        updateLabel(registerMessageLabel, message, isError);
    }

    private void updateLabel(Label label, String message, boolean isError) {
        if (label == null)
            return;
        label.setText(message);
        label.getStyleClass().removeAll("message-error", "message-success");
        label.getStyleClass().add(isError ? "message-error" : "message-success");
        label.setVisible(true);
        label.setManaged(true);
    }

    // --- Password Toggles ---
    @FXML
    private void togglePassword() {
        isPasswordVisible = !isPasswordVisible;
        passwordField.setVisible(!isPasswordVisible);
        passwordVisibleField.setVisible(isPasswordVisible);
    }

    @FXML
    private void toggleRegPassword() {
        isRegPasswordVisible = !isRegPasswordVisible;
        regPasswordField.setVisible(!isRegPasswordVisible);
        regPasswordVisibleField.setVisible(isRegPasswordVisible);
    }

    @FXML
    private void toggleRegConfirmPassword() {
        isRegConfirmPasswordVisible = !isRegConfirmPasswordVisible;
        regConfirmPasswordField.setVisible(!isRegConfirmPasswordVisible);
        regConfirmPasswordVisibleField.setVisible(isRegConfirmPasswordVisible);
    }

    // --- Actions ---
    @FXML
    private void handleSignIn() {
        clearMessages();
        String username = usernameField.getText();
        String password = isPasswordVisible ? passwordVisibleField.getText() : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showLoginMessage("Please enter username and password.", true);
            return;
        }

        // Use NetworkManager for login
        com.iwish.client.NetworkManager nm = com.iwish.client.NetworkManager.getInstance();

        // Run in background thread to avoid freezing UI
        new Thread(() -> {
            int userId = nm.login(username, password);

            javafx.application.Platform.runLater(() -> {
                if (userId != -1) {
                    showLoginMessage("Login Successful! Redirecting...", false);

                    // Navigate to Home page
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                                getClass().getResource("/com/iwish/login/fxml/home_view.fxml"));
                        javafx.scene.Parent root = loader.load();

                        javafx.scene.Scene scene = new javafx.scene.Scene(root, 1024, 768);

                        javafx.stage.Stage stage = (javafx.stage.Stage) usernameField.getScene().getWindow();
                        stage.setScene(scene);
                        stage.setTitle("i-Wish - Home");
                        stage.centerOnScreen();
                    } catch (java.io.IOException ex) {
                        ex.printStackTrace();
                        showLoginMessage("Error loading home page", true);
                    }
                } else {
                    showLoginMessage("Invalid username or password", true);
                }
            });
        }).start();
    }

    @FXML
    private void handleRegister() {
        clearMessages();
        String username = regUsernameField.getText();
        String email = regEmailField.getText();
        String pass = isRegPasswordVisible ? regPasswordVisibleField.getText() : regPasswordField.getText();
        String confirm = isRegConfirmPasswordVisible ? regConfirmPasswordVisibleField.getText()
                : regConfirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            showRegisterMessage("All fields are required.", true);
            return;
        }

        if (!pass.equals(confirm)) {
            showRegisterMessage("Passwords do not match!", true);
            return;
        }

        // Use NetworkManager for registration
        com.iwish.client.NetworkManager nm = com.iwish.client.NetworkManager.getInstance();

        new Thread(() -> {
            int userId = nm.register(username, pass, email);

            javafx.application.Platform.runLater(() -> {
                if (userId != -1) {
                    showRegisterMessage("Registration Successful! Please Sign In.", false);
                    // Optional: Switch back to login form
                    showLogin();
                    showLoginMessage("Registration Successful! Please Sign In.", false);
                } else {
                    showRegisterMessage("Registration failed (username/email may be taken)", true);
                }
            });
        }).start();
    }
}
