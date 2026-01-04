package com.iwish.models;

import java.sql.Timestamp;

public class FriendRequest {
    private int friendshipId;
    private int userId;
    private String username;
    private Timestamp requestDate;
    
    public FriendRequest(int friendshipId, int userId, String username, Timestamp requestDate) {
        this.friendshipId = friendshipId;
        this.userId = userId;
        this.username = username;
        this.requestDate = requestDate;
    }
    
    public int getFriendshipId() {
        return friendshipId;
    }
    
    public void setFriendshipId(int friendshipId) {
        this.friendshipId = friendshipId;
    }
    
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public Timestamp getRequestDate() {
        return requestDate;
    }
    
    public void setRequestDate(Timestamp requestDate) {
        this.requestDate = requestDate;
    }
    
    @Override
    public String toString() {
        return username + " (requested " + requestDate + ")";
    }
}
