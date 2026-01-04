package com.iwish.server;

import com.iwish.database.DatabaseManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;

public class ServerController {

    @FXML
    private TextArea logArea;
    @FXML
    private Label statusLabel;
    @FXML
    private Label connectionCountLabel;
    @FXML
    private Label uptimeLabel;
    @FXML
    private ToggleButton serverToggle;

    private ServerSocket serverSocket;
    private boolean isRunning = false;
    private ExecutorService executor = Executors.newCachedThreadPool();
    private DatabaseManager dbManager;
    private int clientCount = 0;

    // Uptime tracking
    private Timer uptimeTimer;
    private int uptimeSeconds = 0;

    @FXML
    public void initialize() {
        log("Initializing Server Controller...");
        dbManager = new DatabaseManager();
        updateStatus("Stopped");
    }

    @FXML
    private void handleServerToggle() {
        if (serverToggle.isSelected()) {
            startServer();
            serverToggle.setText("Stop Server");
        } else {
            stopServer();
            serverToggle.setText("Start Server");
        }
    }

    private void startServer() {
        if (isRunning)
            return;

        displayStatus("Starting...", "Stopped"); // Temporary state

        executor.submit(() -> {
            try {
                log("Connecting to Database...");
                if (!dbManager.connect()) {
                    Platform.runLater(() -> {
                        log("Database connection failed!");
                        serverToggle.setSelected(false);
                        serverToggle.setText("Start Server");
                        updateStatus("Stopped");
                    });
                    return;
                }
                Platform.runLater(() -> log("Database connected."));

                serverSocket = new ServerSocket(5000);
                isRunning = true;
                Platform.runLater(() -> {
                    updateStatus("Running");
                    startUptimeCounter();
                });
                Platform.runLater(() -> log("Server listening on port 5000"));

                while (isRunning && !serverSocket.isClosed()) {
                    try {
                        Socket clientSocket = serverSocket.accept();
                        clientCount++;
                        Platform.runLater(() -> {
                            log("New client connected: " + clientSocket.getInetAddress());
                            updateClientCount();
                        });

                        // Handle client in new thread
                        executor.submit(new ClientHandler(clientSocket, dbManager));

                    } catch (IOException e) {
                        if (isRunning) {
                            Platform.runLater(() -> log("Error accepting client: " + e.getMessage()));
                        }
                    }
                }
            } catch (java.net.BindException e) {
                // Port already in use
                Platform.runLater(() -> {
                    log("ERROR: Port 5000 is already in use!");
                    log("Another server instance may be running.");
                    log("Solution: Stop other server or use different port.");
                    serverToggle.setSelected(false);
                    serverToggle.setText("Start Server");
                    updateStatus("Stopped");
                });
            } catch (IOException e) {
                Platform.runLater(() -> {
                    log("Server error: " + e.getMessage());
                    serverToggle.setSelected(false);
                    serverToggle.setText("Start Server");
                    updateStatus("Stopped");
                });
            }
        });
    }

    private void stopServer() {
        isRunning = false;
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
            dbManager.shutdown();
        } catch (IOException e) {
            log("Error closing server: " + e.getMessage());
        }
        stopUptimeCounter();
        updateStatus("Stopped");
        log("Server stopped.");
        clientCount = 0;
        updateClientCount();
    }

    private void updateStatus(String status) {
        if ("Running".equals(status)) {
            displayStatus("● Running", "label-status-running");
        } else {
            displayStatus("● Stopped", "label-status-stopped");
        }
    }

    private void displayStatus(String text, String styleClass) {
        statusLabel.setText(text);
        statusLabel.getStyleClass().clear();
        statusLabel.getStyleClass().add(styleClass);
    }

    private void updateClientCount() {
        connectionCountLabel.setText(String.valueOf(clientCount));
    }

    private void startUptimeCounter() {
        uptimeSeconds = 0;
        uptimeTimer = new Timer(true);
        uptimeTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                uptimeSeconds++;
                Platform.runLater(() -> updateUptimeLabel());
            }
        }, 1000, 1000);
    }

    private void stopUptimeCounter() {
        if (uptimeTimer != null) {
            uptimeTimer.cancel();
            uptimeTimer = null;
        }
        uptimeSeconds = 0;
        Platform.runLater(() -> updateUptimeLabel()); // or leave it at last value? decided to reset
    }

    private void updateUptimeLabel() {
        if (uptimeLabel == null)
            return;
        int hours = uptimeSeconds / 3600;
        int minutes = (uptimeSeconds % 3600) / 60;
        int seconds = uptimeSeconds % 60;
        uptimeLabel.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
    }

    public void log(String message) {
        Platform.runLater(() -> logArea.appendText(message + "\n"));
    }
}
