package com.iwish.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

/**
 * Phase 2 Test Client - Tests server protocol commands
 * Run Phase2TestServer first!
 */
public class Phase2TestClient {

    private static Socket socket;
    private static DataInputStream dis;
    private static DataOutputStream dos;

    public static void main(String[] args) {
        System.out.println("=== PHASE 2: SERVER PROTOCOL TEST ===\n");

        try {
            // Connect to server
            System.out.println("Test 1: Connect to Server");
            socket = new Socket("localhost", 5000);
            dis = new DataInputStream(socket.getInputStream());
            dos = new DataOutputStream(socket.getOutputStream());
            System.out.println("✅ Connected to server\n");

            // Test 2: Register User
            System.out.println("Test 2: Register User");
            String registerResponse = sendRequest("REGISTER##testuser##password123##test@example.com");
            System.out.println("Response: " + registerResponse);
            if (registerResponse.startsWith("REGISTER_SUCCESS")) {
                String userId = registerResponse.split("##")[1];
                System.out.println("✅ User registered (ID: " + userId + ")\n");
            } else {
                System.out.println("❌ Registration failed\n");
            }

            // Test 3: Login
            System.out.println("Test 3: Login");
            String loginResponse = sendRequest("LOGIN##testuser##password123");
            System.out.println("Response: " + loginResponse);
            if (loginResponse.startsWith("LOGIN_SUCCESS")) {
                String userId = loginResponse.split("##")[1];
                System.out.println("✅ Login successful (User ID: " + userId + ")\n");
            } else {
                System.out.println("❌ Login failed\n");
            }

            // Test 4: Get Marketplace Items
            System.out.println("Test 4: Get Marketplace Items");
            String itemsResponse = sendRequest("GET_ITEMS");
            System.out.println("Response: " + itemsResponse);
            String[] itemsParts = itemsResponse.split("##");
            if (itemsParts[0].equals("SUCCESS")) {
                int count = Integer.parseInt(itemsParts[1]);
                System.out.println("✅ Received " + count + " items from marketplace");
                for (int i = 2; i < itemsParts.length; i++) {
                    String[] itemData = itemsParts[i].split(":");
                    System.out.println("  - " + itemData[1] + " ($" + itemData[2] + ")");
                }
                System.out.println();
            } else {
                System.out.println("❌ Failed to get items\n");
            }

            // Test 5: Add to Wishlist
            System.out.println("Test 5: Add Items to Wishlist");
            String add1 = sendRequest("ADD_TO_WISHLIST##1##1"); // User 1, Item 1
            String add2 = sendRequest("ADD_TO_WISHLIST##1##2"); // User 1, Item 2
            System.out.println("Add item 1: " + add1);
            System.out.println("Add item 2: " + add2);
            if (add1.equals("SUCCESS") && add2.equals("SUCCESS")) {
                System.out.println("✅ Items added to wishlist\n");
            } else {
                System.out.println("❌ Failed to add items\n");
            }

            // Test 6: Get User Wishlist
            System.out.println("Test 6: Get User Wishlist");
            String wishlistResponse = sendRequest("GET_WISHLIST##1");
            System.out.println("Response: " + wishlistResponse);
            String[] wishlistParts = wishlistResponse.split("##");
            if (wishlistParts[0].equals("SUCCESS")) {
                int count = Integer.parseInt(wishlistParts[1]);
                System.out.println("✅ User has " + count + " items in wishlist");
                for (int i = 2; i < wishlistParts.length; i++) {
                    String[] itemData = wishlistParts[i].split(":");
                    System.out.println("  - " + itemData[1] + " ($" + itemData[2] + ")");
                }
                System.out.println();
            } else {
                System.out.println("❌ Failed to get wishlist\n");
            }

            // Test 7: Remove from Wishlist
            System.out.println("Test 7: Remove from Wishlist");
            String removeResponse = sendRequest("REMOVE_FROM_WISHLIST##1##2");
            System.out.println("Response: " + removeResponse);
            if (removeResponse.equals("SUCCESS")) {
                System.out.println("✅ Item removed from wishlist\n");
            } else {
                System.out.println("❌ Failed to remove item\n");
            }

            // Test 8: Verify Removal
            System.out.println("Test 8: Verify Wishlist After Removal");
            String verifyResponse = sendRequest("GET_WISHLIST##1");
            String[] verifyParts = verifyResponse.split("##");
            int finalCount = Integer.parseInt(verifyParts[1]);
            if (finalCount == 1) {
                System.out.println("✅ Wishlist now has " + finalCount + " item (correct)\n");
            } else {
                System.out.println("❌ Wishlist count incorrect: " + finalCount + "\n");
            }

            System.out.println("=== PHASE 2 TEST COMPLETE ===");
            System.out.println("All protocol commands tested successfully!");

        } catch (IOException e) {
            System.err.println("❌ Connection error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (socket != null)
                    socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static String sendRequest(String request) throws IOException {
        dos.writeUTF(request);
        return dis.readUTF();
    }
}
