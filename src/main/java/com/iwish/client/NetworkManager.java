package com.iwish.client;

import com.iwish.models.Item;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class NetworkManager {
    private static NetworkManager instance;
    private Socket socket;
    private DataInputStream dis;
    private DataOutputStream dos;
    private final String SERVER_ADDRESS = "localhost";
    private final int SERVER_PORT = 5000;

    private NetworkManager() {
    }

    public static NetworkManager getInstance() {
        if (instance == null) {
            instance = new NetworkManager();
        }
        return instance;
    }

    public boolean connect() {
        try {
            socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<Item> getAllItems() {
        List<Item> items = new ArrayList<>();
        try {
            dos.writeUTF("GET_ITEMS");
            String response = dis.readUTF();
            
            String[] parts = response.split("##");
            if ("SUCCESS".equals(parts[0])) {
                int count = Integer.parseInt(parts[1]);
                // Parts structure: SUCCESS, Count, Item1, Item2...
                // Item structure: ID:Name:Price:Desc
                for (int i = 2; i < parts.length; i++) {
                    String itemStr = parts[i];
                    String[] itemParts = itemStr.split(":");
                // Expecting at least: ID:Name:Price
                if (itemParts.length >= 3) {
                    try {
                        int id = Integer.parseInt(itemParts[0]);
                        String name = itemParts[1];
                        double price = Double.parseDouble(itemParts[2]);
                        
                        String imgSrc = "";
                        if (itemParts.length >= 4) {
                            imgSrc = itemParts[3];
                        }
                        
                        items.add(new Item(id, name, price, imgSrc));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing number for item: " + itemStr + " - " + e.getMessage());
                    }
                } else {
                    System.err.println("Skipping malformed item string (less than 3 parts): " + itemStr);
                }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return items;
    }
    
    public void close() {
        try {
            if (socket != null) socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
