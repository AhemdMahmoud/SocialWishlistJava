package com.iwish.database;

import com.iwish.models.Item;
import java.sql.SQLException;
import java.util.List;

/**
 * Phase 1 Test Script - Shared Catalog Model
 * Tests marketplace catalog + user wishlist selections
 */
public class Phase1Test {

    public static void main(String[] args) {
        System.out.println("=== PHASE 1: SHARED CATALOG MODEL TEST ===\n");

        DatabaseManager dbManager = new DatabaseManager();

        // Test 1: Database Connection
        System.out.println("Test 1: Database Connection");
        if (dbManager.connect()) {
            System.out.println("✅ Database connected successfully");
            System.out.println("✅ Schema initialized from schema.sql\n");
        } else {
            System.out.println("❌ Database connection failed");
            return;
        }

        // Test 2: User Registration
        System.out.println("Test 2: User Registration");
        try {
            int userId1 = dbManager.registerUser("testuser1", "password123", "test1@example.com");
            int userId2 = dbManager.registerUser("testuser2", "password456", "test2@example.com");

            if (userId1 > 0 && userId2 > 0) {
                System.out.println("✅ User 1 registered (ID: " + userId1 + ")");
                System.out.println("✅ User 2 registered (ID: " + userId2 + ")");
            } else {
                System.out.println("❌ Registration failed");
            }

            // Test duplicate username
            int duplicate = dbManager.registerUser("testuser1", "newpass", "duplicate@example.com");
            if (duplicate == -1) {
                System.out.println("✅ Duplicate username correctly rejected\n");
            } else {
                System.out.println("❌ Duplicate username not detected\n");
            }
        } catch (SQLException e) {
            System.out.println("❌ Registration error: " + e.getMessage());
            e.printStackTrace();
        }

        // Test 3: User Login
        System.out.println("Test 3: User Login");
        try {
            int loginId = dbManager.loginUser("testuser1", "password123");
            if (loginId > 0) {
                System.out.println("✅ Login successful (User ID: " + loginId + ")");
            } else {
                System.out.println("❌ Login failed");
            }

            // Test wrong password
            int wrongPass = dbManager.loginUser("testuser1", "wrongpassword");
            if (wrongPass == -1) {
                System.out.println("✅ Wrong password correctly rejected\n");
            } else {
                System.out.println("❌ Wrong password not detected\n");
            }
        } catch (SQLException e) {
            System.out.println("❌ Login error: " + e.getMessage());
            e.printStackTrace();
        }

        // Test 4: Add Items to Marketplace Catalog
        System.out.println("Test 4: Add Items to Marketplace Catalog");
        ItemDAO itemDAO = new ItemDAO(dbManager);

        boolean item1 = itemDAO.addItemToCatalog("PlayStation 5", 499.99, "Gaming console", "/images/ps5.jpg");
        boolean item2 = itemDAO.addItemToCatalog("iPhone 15", 999.99, "Smartphone", "/images/iphone.jpg");
        boolean item3 = itemDAO.addItemToCatalog("MacBook Pro", 1999.99, "Laptop", "/images/macbook.jpg");
        boolean item4 = itemDAO.addItemToCatalog("AirPods Pro", 249.99, "Wireless earbuds", "/images/airpods.jpg");

        if (item1 && item2 && item3 && item4) {
            System.out.println("✅ Added PlayStation 5 to catalog");
            System.out.println("✅ Added iPhone 15 to catalog");
            System.out.println("✅ Added MacBook Pro to catalog");
            System.out.println("✅ Added AirPods Pro to catalog\n");
        } else {
            System.out.println("❌ Failed to add items to catalog\n");
        }

        // Test 5: View Marketplace (All Items)
        System.out.println("Test 5: View Marketplace (All Items)");
        List<Item> allItems = itemDAO.getAllItems();
        System.out.println("Total items in marketplace: " + allItems.size());
        for (Item item : allItems) {
            System.out.println("  - " + item.getName() + " ($" + item.getPrice() + ")");
        }

        if (allItems.size() == 4) {
            System.out.println("✅ Marketplace shows all catalog items\n");
        } else {
            System.out.println("❌ Marketplace item count incorrect (expected 4, got " + allItems.size() + ")\n");
        }

        // Test 6: Add Items to User Wishlists
        System.out.println("Test 6: Add Items to User Wishlists");
        // User 1 wants PlayStation and iPhone
        boolean user1_item1 = itemDAO.addToUserWishlist(1, 1); // PS5
        boolean user1_item2 = itemDAO.addToUserWishlist(1, 2); // iPhone

        // User 2 wants MacBook and AirPods
        boolean user2_item1 = itemDAO.addToUserWishlist(2, 3); // MacBook
        boolean user2_item2 = itemDAO.addToUserWishlist(2, 4); // AirPods

        if (user1_item1 && user1_item2 && user2_item1 && user2_item2) {
            System.out.println("✅ User 1 added PS5 and iPhone to wishlist");
            System.out.println("✅ User 2 added MacBook and AirPods to wishlist\n");
        } else {
            System.out.println("❌ Failed to add items to user wishlists\n");
        }

        // Test 7: Get User-Specific Wishlists
        System.out.println("Test 7: Get User-Specific Wishlists");
        List<Item> user1Wishlist = itemDAO.getUserWishlist(1);
        List<Item> user2Wishlist = itemDAO.getUserWishlist(2);

        System.out.println("User 1's Wishlist (" + user1Wishlist.size() + " items):");
        for (Item item : user1Wishlist) {
            System.out.println("  - " + item.getName() + " ($" + item.getPrice() + ")");
        }

        System.out.println("\nUser 2's Wishlist (" + user2Wishlist.size() + " items):");
        for (Item item : user2Wishlist) {
            System.out.println("  - " + item.getName() + " ($" + item.getPrice() + ")");
        }

        if (user1Wishlist.size() == 2 && user2Wishlist.size() == 2) {
            System.out.println("\n✅ Wishlist isolation working correctly\n");
        } else {
            System.out.println("\n❌ Wishlist isolation failed (User1: " + user1Wishlist.size() + ", User2: "
                    + user2Wishlist.size() + ")\n");
        }

        // Test 8: Prevent Duplicate Wishlist Items
        System.out.println("Test 8: Prevent Duplicate Wishlist Items");
        boolean duplicate = itemDAO.addToUserWishlist(1, 1); // Try to add PS5 again
        if (!duplicate) {
            System.out.println("✅ Duplicate wishlist item correctly prevented\n");
        } else {
            System.out.println("❌ Duplicate wishlist item was allowed\n");
        }

        // Test 9: Remove from Wishlist
        System.out.println("Test 9: Remove from Wishlist");
        boolean removed = itemDAO.removeFromUserWishlist(1, 2); // Remove iPhone from User 1
        if (removed) {
            System.out.println("✅ Item removed from wishlist");
            List<Item> updatedWishlist = itemDAO.getUserWishlist(1);
            System.out.println("✅ User 1's wishlist now has " + updatedWishlist.size() + " items\n");
        } else {
            System.out.println("❌ Failed to remove item from wishlist\n");
        }

        System.out.println("=== PHASE 1 TEST COMPLETE ===");
        System.out.println("\n📊 SUMMARY:");
        System.out.println("- Marketplace Catalog: " + allItems.size() + " items (shared by all users)");
        System.out.println("- User 1 Wishlist: " + itemDAO.getUserWishlist(1).size() + " items (personal selection)");
        System.out.println("- User 2 Wishlist: " + itemDAO.getUserWishlist(2).size() + " items (personal selection)");
        System.out.println("\nReview results above. All ✅ means Phase 1 is successful.");

        dbManager.shutdown();
    }
}
