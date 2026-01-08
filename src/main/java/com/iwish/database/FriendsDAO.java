package com.iwish.database;

import com.iwish.models.Friend;
import com.iwish.models.FriendRequest;
import com.iwish.models.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object for Friends functionality
 */
public class FriendsDAO {
    private final DatabaseManager dbManager;

    public FriendsDAO(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Get all accepted friends for a user
     */
    public List<Friend> getFriends(int userId) {
        List<Friend> friends = new ArrayList<>();
        String sql = "SELECT u.user_id, u.username, u.email " +
                "FROM USERS u " +
                "JOIN FRIENDS f ON (u.user_id = f.friend_id OR u.user_id = f.user_id) " +
                "WHERE (f.user_id = ? OR f.friend_id = ?) " +
                "AND f.status = 'accepted' " +
                "AND u.user_id != ? " +
                "ORDER BY f.friendship_id DESC";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                friends.add(new Friend(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return friends;
    }

    /**
     * Get pending friend requests for a user (requests sent TO this user)
     */
    public List<FriendRequest> getFriendRequests(int userId) {
        List<FriendRequest> requests = new ArrayList<>();
        String sql = "SELECT f.friendship_id, u.user_id, u.username, f.request_date " +
                "FROM FRIENDS f " +
                "JOIN USERS u ON f.user_id = u.user_id " +
                "WHERE f.friend_id = ? AND f.status = 'pending'";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                requests.add(new FriendRequest(
                        rs.getInt("friendship_id"),
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getTimestamp("request_date")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return requests;
    }

    /**
     * Send friend request by username
     */
    public boolean sendFriendRequest(int userId, String friendUsername) {
        // First, get friend's user_id
        String getUserSql = "SELECT user_id FROM USERS WHERE username = ?";
        int friendId = -1;

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(getUserSql)) {

            pstmt.setString(1, friendUsername);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                friendId = rs.getInt("user_id");
            } else {
                return false; // User not found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Check if already friends or request exists
        String checkSql = "SELECT friendship_id FROM FRIENDS " +
                "WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(checkSql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, friendId);
            pstmt.setInt(3, friendId);
            pstmt.setInt(4, userId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return false; // Already friends or request exists
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        // Send friend request
        String insertSql = "INSERT INTO FRIENDS (user_id, friend_id, status) VALUES (?, ?, 'pending')";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(insertSql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, friendId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Accept friend request
     */
    public boolean acceptFriendRequest(int friendshipId) {
        String sql = "UPDATE FRIENDS SET status = 'accepted' WHERE friendship_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, friendshipId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Decline friend request
     */
    public boolean declineFriendRequest(int friendshipId) {
        String sql = "DELETE FROM FRIENDS WHERE friendship_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, friendshipId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Remove friend
     */
    public boolean removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM FRIENDS " +
                "WHERE (user_id = ? AND friend_id = ?) OR (user_id = ? AND friend_id = ?)";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, userId);
            pstmt.setInt(2, friendId);
            pstmt.setInt(3, friendId);
            pstmt.setInt(4, userId);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Search users by username
     */
    public List<User> searchUsers(String query) {
        List<User> users = new ArrayList<>();
        // Use standard SQL for Derby (case insensitive match)
        String sql = "SELECT user_id, username, email FROM USERS " +
                "WHERE LOWER(username) LIKE LOWER(?) " +
                "ORDER BY username";

        // Only limit if it's a specific search, otherwise (empty query) return all (or
        // large limit) for client-side filtering
        if (!query.trim().isEmpty()) {
            sql += " FETCH FIRST 20 ROWS ONLY";
        } else {
            // Use a larger safe limit for "all users" view, or no limit if explicitly
            // requested "whole users"
            sql += " FETCH FIRST 1000 ROWS ONLY";
        }

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            String wildcardQuery = "%" + query.trim() + "%";
            pstmt.setString(1, wildcardQuery);

            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(new User(
                        rs.getInt("user_id"),
                        rs.getString("username"),
                        rs.getString("email")));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public static class FriendshipDetails {
        private int requesterId;
        private String requesterUsername;
        private int targetId;
        private String targetUsername;

        public int getRequesterId() {
            return requesterId;
        }

        public void setRequesterId(int requesterId) {
            this.requesterId = requesterId;
        }

        public String getRequesterUsername() {
            return requesterUsername;
        }

        public void setRequesterUsername(String requesterUsername) {
            this.requesterUsername = requesterUsername;
        }

        public int getTargetId() {
            return targetId;
        }

        public void setTargetId(int targetId) {
            this.targetId = targetId;
        }

        public String getTargetUsername() {
            return targetUsername;
        }

        public void setTargetUsername(String targetUsername) {
            this.targetUsername = targetUsername;
        }
    }

    public FriendshipDetails getFriendshipDetails(int friendshipId) {
        String sql = "SELECT f.user_id, f.friend_id, u1.username AS requester_username, u2.username AS target_username "
                +
                "FROM FRIENDS f " +
                "JOIN USERS u1 ON f.user_id = u1.user_id " +
                "JOIN USERS u2 ON f.friend_id = u2.user_id " +
                "WHERE f.friendship_id = ?";

        try (Connection conn = dbManager.getConnection();
                PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, friendshipId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                FriendshipDetails details = new FriendshipDetails();
                details.setRequesterId(rs.getInt("user_id"));
                details.setTargetId(rs.getInt("friend_id"));
                details.setRequesterUsername(rs.getString("requester_username"));
                details.setTargetUsername(rs.getString("target_username"));
                return details;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
