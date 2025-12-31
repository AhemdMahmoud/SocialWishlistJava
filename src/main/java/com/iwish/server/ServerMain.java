package com.iwish.server;

import com.iwish.database.DatabaseManager;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerMain {
    private static final int PORT = 5000;

    public static void main(String[] args) {
        System.out.println("Starting iWish Server...");

        try {
            // Initialize Database
            DatabaseManager dbManager = new DatabaseManager();
            if (dbManager.connect()) {
                System.out.println("Database connected successfully.");
            } else {
                System.err.println("Database connection failed. Exiting.");
                return;
            }

            // Start Server Socket
            try (ServerSocket serverSocket = new ServerSocket(PORT)) {
                System.out.println("Server listening on port " + PORT);

                while (true) {
                    Socket socket = serverSocket.accept();
                    System.out.println("New client connected: " + socket.getInetAddress());
                    new Thread(new ClientHandler(socket, dbManager)).start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
