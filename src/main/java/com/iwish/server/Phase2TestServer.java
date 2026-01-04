package com.iwish.server;

import com.iwish.database.DatabaseManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Phase 2 Test Server - Tests protocol commands
 * Run this before running Phase2Test client
 */
public class Phase2TestServer {

    public static void main(String[] args) {
        System.out.println("=== PHASE 2 TEST SERVER ===");
        System.out.println("Starting server on port 5000...\n");

        DatabaseManager dbManager = new DatabaseManager();
        if (!dbManager.connect()) {
            System.err.println("Failed to connect to database!");
            return;
        }

        try (ServerSocket serverSocket = new ServerSocket(5000)) {
            System.out.println("✅ Server started successfully");
            System.out.println("✅ Database connected");
            System.out.println("Waiting for client connections...\n");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());

                // Handle client in new thread
                ClientHandler handler = new ClientHandler(clientSocket, dbManager);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
