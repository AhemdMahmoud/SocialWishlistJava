-- i-Wish Database Schema
-- Apache Derby SQL Script

-- Users Table
CREATE TABLE Users (
    user_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    username VARCHAR(50) UNIQUE NOT NULL,
    password VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Friends Table
CREATE TABLE Friends (
    friendship_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    user_id INT NOT NULL,
    friend_id INT NOT NULL,
    status VARCHAR(20) DEFAULT 'pending',
    request_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (friend_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- WishList Table (Global Marketplace Catalog)
-- This is the shared catalog of ALL available items
CREATE TABLE WishList (
    item_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    item_name VARCHAR(200) NOT NULL,
    item_price DECIMAL(10,2) NOT NULL,
    description VARCHAR(500),
    img_src VARCHAR(500),
    is_claimed SMALLINT DEFAULT 0
);

-- UserWishes Table (Personal Wishlist Selection)
-- Links users to items they want from the marketplace
CREATE TABLE UserWishes (
    wish_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES WishList(item_id) ON DELETE CASCADE
);

-- Contributions Table
CREATE TABLE Contributions (
    contribution_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    wish_id INT NOT NULL,
    contributor_id INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    contribution_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (wish_id) REFERENCES UserWishes(wish_id) ON DELETE CASCADE,
    FOREIGN KEY (contributor_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Notifications Table
CREATE TABLE Notifications (
    notification_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    user_id INT NOT NULL,
    type VARCHAR(50),
    message VARCHAR(500) NOT NULL,
    related_id INT,
    is_read SMALLINT DEFAULT 0,
    notification_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE
);

-- Create indexes for better performance
CREATE INDEX idx_friends_user ON Friends(user_id);
CREATE INDEX idx_friends_friend ON Friends(friend_id);
CREATE INDEX idx_userwishes_user ON UserWishes(user_id);
CREATE INDEX idx_userwishes_item ON UserWishes(item_id);
CREATE INDEX idx_contributions_wish ON Contributions(wish_id);
CREATE INDEX idx_notifications_user ON Notifications(user_id);
