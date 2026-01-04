package com.iwish.client;

import com.iwish.models.Friend;
import com.iwish.models.FriendRequest;
import com.iwish.models.User;
import java.util.List;

public class Phase4Test {

    public static void main(String[] args) {
        System.out.println("Starting Phase 4 Test...");
        NetworkManager nm = NetworkManager.getInstance();

        // Connect to server
        if (!nm.connect()) {
            System.err.println("Failed to connect to server");
            return;
        }
        System.out.println("Connected to server.");

        // Login as test user
        // Ensure you have a user 'testuser' with password '123456' or similar in DB
        // If not, register one
        int userId = nm.login("testuser", "123456");

        if (userId == -1) {
            System.out.println("Login failed, attempting registration...");
            // Try explicit registration
            userId = nm.register("testuser", "123456", "test@test.com");

            if (userId == -1) {
                // If registration failed, maybe user exists but password was wrong initially?
                // Let's try to register a NEW unique user for this test run
                String uniqueUser = "testuser_" + System.currentTimeMillis();
                System.out.println("Registration failed (User might exist). Creating unique test user: " + uniqueUser);
                userId = nm.register(uniqueUser, "123456", uniqueUser + "@test.com");
            }
        }

        if (userId == -1) {
            System.err.println("Authentication failed completely.");
            return;
        }

        System.out.println("✅ Logged in as user ID: " + userId);

        // Test 1: Get Friends
        System.out.println("\n--- Test 1: Get Friends ---");
        List<Friend> friends = nm.getFriends(userId);
        System.out.println("Friends count: " + friends.size());
        for (Friend friend : friends) {
            System.out.println("  - " + friend.getUsername() + " (ID: " + friend.getUserId() + ")");
        }

        // Test 2: Get Friend Requests
        System.out.println("\n--- Test 2: Get Friend Requests ---");
        List<FriendRequest> requests = nm.getFriendRequests(userId);
        System.out.println("Pending requests: " + requests.size());
        for (FriendRequest req : requests) {
            System.out.println("  - From: " + req.getUsername() + " (Friendship ID: " + req.getFriendshipId() + ")");
        }

        // Test 3: Search Users
        System.out.println("\n--- Test 3: Search Users ---");
        List<User> users = nm.searchUsers("test"); // Assuming other users exist
        System.out.println("Search results: " + users.size());
        for (User user : users) {
            System.out.println("  - " + user.getUsername() + " (ID: " + user.getUserId() + ")");
        }

        // Test 4: Send Friend Request (Needs a second user)
        // nm.addFriend(userId, "otheruser");

        System.out.println("\n✅ Test sequence completed.");
        nm.close();
    }
}
