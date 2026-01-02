package com.iwish.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class ServerMain {
    private static final int PORT = 5005;
    private static Vector<ClientHandler> activeClients = new Vector<>();

    public static void main(String[] args) {
        System.out.println("=== i-Wish Server (Auth Only) ===");
        System.out.println("Starting server on port " + PORT + "...");

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started successfully!");
            System.out.println("Waiting for client connections...\n");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket);
                activeClients.add(handler);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("Server error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void removeClient(ClientHandler client) {
        activeClients.remove(client);
        System.out.println("Client disconnected. Active clients: " + activeClients.size());
    }
}
