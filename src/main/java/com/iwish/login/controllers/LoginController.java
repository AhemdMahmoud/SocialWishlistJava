package com.iwish.login.controllers;

import com.iwish.client.NetworkManager;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class LoginController {

    @FXML
    private VBox loginForm;
    @FXML
    private VBox registerForm;

    @FXML
    private TextField usernameField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordVisibleField;
    @FXML
    private Label loginMessageLabel;

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
    private Label registerMessageLabel;

    private boolean isPasswordVisible = false;
    private boolean isRegPasswordVisible = false;
    private boolean isRegConfirmPasswordVisible = false;

    @FXML
    public void initialize() {
        // Connect to server on startup
        NetworkManager nm = NetworkManager.getInstance();
        if (!nm.isConnected()) {
            if (!nm.connect()) {
                showError(loginMessageLabel, "Could not connect to server. Check if server is running.");
            }
        }
    }

    @FXML
    private void handleSignIn() {
        String username = usernameField.getText().trim();
        String password = isPasswordVisible ? passwordVisibleField.getText() : passwordField.getText();

        if (username.isEmpty() || password.isEmpty()) {
            showError(loginMessageLabel, "Please enter username and password");
            return;
        }

        int userId = NetworkManager.getInstance().login(username, password);
        if (userId != -1) {
            System.out.println("Login successful! User ID: " + userId);
            navigateToHome();
        } else {
            showError(loginMessageLabel, "Invalid username or password");
        }
    }

    @FXML
    private void handleRegister() {
        String username = regUsernameField.getText().trim();
        String email = regEmailField.getText().trim();
        String password = isRegPasswordVisible ? regPasswordVisibleField.getText() : regPasswordField.getText();
        String confirm = isRegConfirmPasswordVisible ? regConfirmPasswordVisibleField.getText()
                : regConfirmPasswordField.getText();

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError(registerMessageLabel, "All fields are required");
            return;
        }

        if (!password.equals(confirm)) {
            showError(registerMessageLabel, "Passwords do not match");
            return;
        }

        int userId = NetworkManager.getInstance().register(username, password, email);
        if (userId != -1) {
            System.out.println("Registration successful! User ID: " + userId);
            navigateToHome();
        } else {
            showError(registerMessageLabel, "Registration failed");
        }
    }

    private void navigateToHome() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iwish/login/fxml/home_view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginForm.getScene().getWindow();
            Scene scene = new Scene(root, 1024, 768);
            scene.getStylesheets().add(getClass().getResource("/com/iwish/login/styles/friends.css").toExternalForm()); // Reusing
                                                                                                                        // friends
                                                                                                                        // css
                                                                                                                        // or
                                                                                                                        // creates
                                                                                                                        // generic
                                                                                                                        // main
                                                                                                                        // css
                                                                                                                        // later
                                                                                                                        // if
                                                                                                                        // needed
            // Ideally we should have home.css or styles loaded in FXML.
            // home_view.fxml does not have stylesheet attached in source, so we might want
            // to attach one or rely on specific view styles

            stage.setScene(scene);
            stage.setTitle("i-Wish - Home");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showError(loginMessageLabel, "Error loading application");
        }
    }

    @FXML
    private void showRegister() {
        loginForm.setVisible(false);
        registerForm.setVisible(true);
        clearFields();
    }

    @FXML
    private void showLogin() {
        registerForm.setVisible(false);
        loginForm.setVisible(true);
        clearFields();
    }

    @FXML
    private void togglePassword() {
        isPasswordVisible = !isPasswordVisible;
        toggleFieldVisibility(passwordField, passwordVisibleField, isPasswordVisible);
    }

    @FXML
    private void toggleRegPassword() {
        isRegPasswordVisible = !isRegPasswordVisible;
        toggleFieldVisibility(regPasswordField, regPasswordVisibleField, isRegPasswordVisible);
    }

    @FXML
    private void toggleRegConfirmPassword() {
        isRegConfirmPasswordVisible = !isRegConfirmPasswordVisible;
        toggleFieldVisibility(regConfirmPasswordField, regConfirmPasswordVisibleField, isRegConfirmPasswordVisible);
    }

    private void toggleFieldVisibility(PasswordField pf, TextField tf, boolean visible) {
        pf.setVisible(!visible);
        tf.setVisible(visible);
        if (visible) {
            tf.setText(pf.getText());
        } else {
            pf.setText(tf.getText());
        }
    }

    private void clearFields() {
        usernameField.clear();
        passwordField.clear();
        passwordVisibleField.clear();
        regUsernameField.clear();
        regEmailField.clear();
        regPasswordField.clear();
        regPasswordVisibleField.clear();
        regConfirmPasswordField.clear();
        regConfirmPasswordVisibleField.clear();
        loginMessageLabel.setVisible(false);
        registerMessageLabel.setVisible(false);
    }

    private void showError(Label label, String message) {
        label.setText(message);
        label.setVisible(true);
        label.setManaged(true);
        label.setStyle("-fx-text-fill: red;");
    }
}
