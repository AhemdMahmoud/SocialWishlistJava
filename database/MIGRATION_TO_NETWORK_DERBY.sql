-- ============================================================
-- MIGRATION SCRIPT: Prepare iwishdb for Gitted MWK
-- ============================================================
-- Run this in NetBeans SQL Command window BEFORE starting server
-- Database: iwishdb (localhost:1527)
-- ============================================================

-- Step 1: Check if WishList table exists with correct structure
-- If you already have a WishList table, check its columns:
-- Expected: item_id, item_name, item_price, description, img_src

-- Step 2: Add img_src column if missing (if your table has image_path instead)
-- Uncomment if needed:
-- ALTER TABLE WishList ADD COLUMN img_src VARCHAR(500);

-- Step 3: Check if UserWishes table exists
-- Expected: wish_id, user_id, item_id, created_date

-- Step 4: If tables are MISSING, create them:

-- Only run if WishList doesn't exist:
CREATE TABLE WishList (
    item_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    item_name VARCHAR(200) NOT NULL,
    item_price DECIMAL(10,2) NOT NULL,
    description VARCHAR(500),
    img_src VARCHAR(500)
);

-- Only run if UserWishes doesn't exist:
CREATE TABLE UserWishes (
    wish_id INT PRIMARY KEY GENERATED ALWAYS AS IDENTITY (START WITH 1, INCREMENT BY 1),
    user_id INT NOT NULL,
    item_id INT NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES Users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (item_id) REFERENCES WishList(item_id) ON DELETE CASCADE
);

-- Step 5: Add sample marketplace items (optional)
INSERT INTO WishList (item_name, item_price, description, img_src) VALUES
('PlayStation 5', 499.99, 'Gaming console', '/images/ps5.jpg'),
('iPhone 15', 999.99, 'Smartphone', '/images/iphone.jpg'),
('MacBook Pro', 1999.99, 'Laptop', '/images/macbook.jpg'),
('AirPods Pro', 249.99, 'Wireless earbuds', '/images/airpods.jpg');

-- Step 6: Create indexes for performance
CREATE INDEX idx_userwishes_user ON UserWishes(user_id);
CREATE INDEX idx_userwishes_item ON UserWishes(item_id);

-- ============================================================
-- VERIFICATION QUERIES
-- ============================================================

-- Check tables exist:
SELECT tablename FROM sys.systables WHERE tabletype='T' AND tablename IN ('WISHLIST', 'USERWISHES', 'USERS');

-- Check WishList data:
SELECT * FROM WishList;

-- Check UserWishes (should be empty initially):
SELECT * FROM UserWishes;

-- ============================================================
-- DONE! Now you can start Phase2TestServer
-- ============================================================
