package com.iwish.models;

/**
 * Friend model - represents an accepted friend
 */
public class Friend {
    private int userId;
    private String username;
    private String email;
    private int friendshipId;

    public Friend(int userId, String username, String email, int friendshipId) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.friendshipId = friendshipId;
    }

    public int getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public int getFriendshipId() {
        return friendshipId;
    }

    @Override
    public String toString() {
        return username;
    }
}
