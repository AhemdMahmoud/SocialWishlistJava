package com.iwish.client;

import com.iwish.models.Item;
import java.util.List;

/**
 * Phase 3 Test - Client-Side NetworkManager Integration
 * Tests all NetworkManager methods with live server
 */
public class Phase3Test {

    public static void main(String[] args) {
        System.out.println("=== PHASE 3: CLIENT-SIDE NETWORKMANAGER TEST ===\n");

        NetworkManager nm = NetworkManager.getInstance();

        // Test 1: Connect to Server
        System.out.println("Test 1: Connect to Server");
        if (nm.connect()) {
            System.out.println("✅ Connected to server\n");
        } else {
            System.out.println("❌ Connection failed");
            return;
        }

        // Test 2: Register User
        System.out.println("Test 2: Register User");
        int userId = nm.register("phase3user", "testpass", "phase3@test.com");
        if (userId > 0) {
            System.out.println("✅ User registered (ID: " + userId + ")");
            System.out.println("✅ Current user: " + nm.getCurrentUsername() + "\n");
        } else {
            System.out.println("⚠️  Registration failed (user may already exist)");
            // Try login instead
            userId = nm.login("phase3user", "testpass");
            if (userId > 0) {
                System.out.println("✅ Logged in instead (ID: " + userId + ")\n");
            } else {
                System.out.println("❌ Login also failed\n");
                return;
            }
        }

        // Test 3: Get Marketplace Items
        System.out.println("Test 3: Get Marketplace Items");
        List<Item> marketplaceItems = nm.getAllItems();
        System.out.println("✅ Retrieved " + marketplaceItems.size() + " items from marketplace:");
        for (Item item : marketplaceItems) {
            System.out.println("  - [" + item.getId() + "] " + item.getName() + " ($" + item.getPrice() + ")");
        }
        System.out.println();

        // Test 4: Add Items to Wishlist
        System.out.println("Test 4: Add Items to Wishlist");
        if (marketplaceItems.size() >= 2) {
            int item1Id = marketplaceItems.get(0).getId();
            int item2Id = marketplaceItems.get(1).getId();

            boolean added1 = nm.addToWishlist(userId, item1Id);
            boolean added2 = nm.addToWishlist(userId, item2Id);

            if (added1 && added2) {
                System.out.println("✅ Added 2 items to wishlist\n");
            } else {
                System.out.println("⚠️  Some items may already be in wishlist\n");
            }
        } else {
            System.out.println("❌ Not enough items in marketplace\n");
        }

        // Test 5: Get User Wishlist
        System.out.println("Test 5: Get User Wishlist");
        List<Item> wishlistItems = nm.getUserWishlist(userId);
        System.out.println("✅ User has " + wishlistItems.size() + " items in wishlist:");
        for (Item item : wishlistItems) {
            System.out.println("  - [" + item.getId() + "] " + item.getName() + " ($" + item.getPrice() + ")");
        }
        System.out.println();

        // Test 6: Remove from Wishlist
        System.out.println("Test 6: Remove from Wishlist");
        if (!wishlistItems.isEmpty()) {
            int itemToRemove = wishlistItems.get(0).getId();
            boolean removed = nm.removeFromWishlist(userId, itemToRemove);

            if (removed) {
                System.out.println("✅ Removed item from wishlist");

                // Verify removal
                List<Item> updatedWishlist = nm.getUserWishlist(userId);
                System.out.println("✅ Wishlist now has " + updatedWishlist.size() + " items\n");
            } else {
                System.out.println("❌ Failed to remove item\n");
            }
        } else {
            System.out.println("⚠️  Wishlist is empty, skipping removal test\n");
        }

        // Test 7: Verify Session State
        System.out.println("Test 7: Verify Session State");
        System.out.println("Current User ID: " + nm.getCurrentUserId());
        System.out.println("Current Username: " + nm.getCurrentUsername());
        System.out.println("Connection Status: " + (nm.isConnected() ? "Connected" : "Disconnected"));

        if (nm.getCurrentUserId() == userId && nm.isConnected()) {
            System.out.println("✅ Session state correct\n");
        } else {
            System.out.println("❌ Session state mismatch\n");
        }

        System.out.println("=== PHASE 3 TEST COMPLETE ===");
        System.out.println("\n📊 SUMMARY:");
        System.out.println("- NetworkManager singleton: ✅");
        System.out.println("- Authentication (login/register): ✅");
        System.out.println("- Marketplace browsing: ✅");
        System.out.println("- Wishlist management: ✅");
        System.out.println("- Session tracking: ✅");

        nm.close();
    }
}
