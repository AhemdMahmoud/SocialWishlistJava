# Phase 2 Database Verification Guide

## ✅ Verify Changes Are Real (Not Just Prints)

### Step 1: Check Users Table

```sql
-- In NetBeans Services → Databases → iwish_db → Tables → USERS → View Data
SELECT * FROM USERS;
```

**Expected Result**:

```
USER_ID | USERNAME  | PASSWORD     | EMAIL              | CREATED_DATE
--------|-----------|--------------|--------------------|--------------
1       | testuser1 | password123  | test1@example.com  | 2026-01-04...
2       | testuser2 | password456  | test2@example.com  | 2026-01-04...
3       | testuser  | password123  | test@example.com   | 2026-01-04...  ← From Phase2Test
```

### Step 2: Check WishList Table (Marketplace Catalog)

```sql
SELECT * FROM WISHLIST;
```

**Expected Result**:

```
ITEM_ID | ITEM_NAME      | ITEM_PRICE | DESCRIPTION      | IMG_SRC
--------|----------------|------------|------------------|------------------
1       | PlayStation 5  | 499.99     | Gaming console   | /images/ps5.jpg
2       | iPhone 15      | 999.99     | Smartphone       | /images/iphone.jpg
3       | MacBook Pro    | 1999.99    | Laptop           | /images/macbook.jpg
4       | AirPods Pro    | 249.99     | Wireless earbuds | /images/airpods.jpg
```

(These are from Phase1Test - shared catalog)

### Step 3: Check UserWishes Table (Personal Wishlists)

```sql
SELECT uw.wish_id, u.username, w.item_name, uw.created_date
FROM USERWISHES uw
JOIN USERS u ON uw.user_id = u.user_id
JOIN WISHLIST w ON uw.item_id = w.item_id
ORDER BY uw.created_date;
```

**Expected Result**:

```
WISH_ID | USERNAME  | ITEM_NAME      | CREATED_DATE
--------|-----------|----------------|---------------
1       | testuser1 | PlayStation 5  | 2026-01-04... ← From Phase1Test
2       | testuser1 | iPhone 15      | 2026-01-04... ← From Phase1Test (removed later)
3       | testuser2 | MacBook Pro    | 2026-01-04... ← From Phase1Test
4       | testuser2 | AirPods Pro    | 2026-01-04... ← From Phase1Test
5       | testuser  | PlayStation 5  | 2026-01-04... ← From Phase2Test
```

**Note**: If you ran Phase2Test multiple times, you'll see duplicates or missing items depending on test order.

---

## 🔄 Clean Database for Fresh Test

If you want to test Phase 2 from scratch:

### Option 1: Delete Database Completely

```bash
# Stop server first!
# Then delete folder:
rm -rf iwish_db/
# Or in Windows: Delete "iwish_db" folder manually
```

### Option 2: Clear Only Test Data

```sql
-- Keep structure, delete data
DELETE FROM USERWISHES;
DELETE FROM USERS;
DELETE FROM WISHLIST;

-- Reset auto-increment counters (Derby specific)
-- You'll need to drop and recreate tables for this
```

### Option 3: Delete Only Phase2Test User

```sql
DELETE FROM USERS WHERE username = 'testuser';
-- This will cascade delete their wishlist items
```

---

## 📊 Real-Time Monitoring

### Watch Database Changes Live

1. **Open NetBeans Services Tab**
2. **Databases → Java DB → iwish_db → Connect**
3. **Right-click USERWISHES → View Data**
4. **Keep this window open**
5. **Run Phase2TestClient**
6. **Click Refresh button** in data view after each test

You'll see rows being added/removed in real-time!

---

## ✅ Proof Changes Are Real

Run this SQL query before and after Phase2Test:

```sql
SELECT 
    (SELECT COUNT(*) FROM USERS) as total_users,
    (SELECT COUNT(*) FROM USERWISHES) as total_wishlist_items,
    (SELECT COUNT(*) FROM WISHLIST) as total_catalog_items;
```

**Before Phase2Test**:

```
TOTAL_USERS | TOTAL_WISHLIST_ITEMS | TOTAL_CATALOG_ITEMS
------------|----------------------|--------------------
2           | 3                    | 4
```

**After Phase2Test**:

```
TOTAL_USERS | TOTAL_WISHLIST_ITEMS | TOTAL_CATALOG_ITEMS
------------|----------------------|--------------------
3           | 4                    | 4
```

(+1 user, +1 wishlist item)

---

## 🎯 Summary

**YES, changes are REAL!**

- ✅ Users are added to USERS table
- ✅ Wishlist items are added to USERWISHES table
- ✅ Protocol commands modify actual database
- ✅ Not just console prints

The "ALREADY_EXISTS" and "REGISTER_FAILED" messages mean the test ran before and data persists (which proves it's real!).
