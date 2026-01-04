package com.iwish.models;

import java.sql.Timestamp;

/**
 * FriendRequest model - represents a pending friend request
 */
public class FriendRequest {
    private int friendshipId;
    private int requesterId;
    private String requesterUsername;
    private String requesterEmail;
    private Timestamp requestDate;

    public FriendRequest(int friendshipId, int requesterId, String requesterUsername,
            String requesterEmail, Timestamp requestDate) {
        this.friendshipId = friendshipId;
        this.requesterId = requesterId;
        this.requesterUsername = requesterUsername;
        this.requesterEmail = requesterEmail;
        this.requestDate = requestDate;
    }

    public int getFriendshipId() {
        return friendshipId;
    }

    public int getRequesterId() {
        return requesterId;
    }

    public String getRequesterUsername() {
        return requesterUsername;
    }

    public String getRequesterEmail() {
        return requesterEmail;
    }

    public Timestamp getRequestDate() {
        return requestDate;
    }

    @Override
    public String toString() {
        return requesterUsername + " (pending)";
    }
}
