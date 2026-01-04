# Database Connection Issue - SOLVED

## 🔍 The Problem

You have **TWO DIFFERENT databases**:

### 1. Old Database (Network Server)

- **Name**: `iwishdb`
- **Type**: Derby Network Server
- **Location**: Derby server at `localhost:1527`
- **Connection**: `jdbc:derby://localhost:1527/iwishdb`
- **Visible in**: NetBeans Services → Databases → Java DB → iwishdb
- **Used by**: Your old project

### 2. New Database (Embedded)

- **Name**: `iwish_db`
- **Type**: Derby Embedded (folder-based)
- **Location**: `g:\CS\ITI\DM 9M (ismalia)\DM Course\8. java for DM\6. Java\finals\Gitted MWK\iwish_db\`
- **Connection**: `jdbc:derby:iwish_db;create=true`
- **Visible in**: File system as a FOLDER (not in NetBeans Java DB list)
- **Used by**: Gitted MWK project (current)

---

## ✅ Why Database Recreates Automatically

**Answer**: Line 23 in `DatabaseManager.java`:

```java
private static final String DB_URL = "jdbc:derby:iwish_db;create=true";
                                                              ^^^^^^^^^^^^
```

The `;create=true` flag tells Derby: **"If database doesn't exist, create it automatically"**

This is CORRECT behavior! It ensures the database is always available.

---

## 🔌 How to Connect to Embedded Database in NetBeans

### Step 1: Add Embedded Database Connection

1. **Services** tab → **Databases**
2. Right-click **Databases** → **New Connection**
3. **Driver**: Select **Java DB (Embedded)**
4. **Database**: `iwish_db` (just the name, no path)
5. **User**: `root`
6. **Password**: `root`
7. **Database Location**: Browse to `g:\CS\ITI\DM 9M (ismalia)\DM Course\8. java for DM\6. Java\finals\Gitted MWK`
8. Click **OK**

### Step 2: Connect and View Data

1. Find new connection: **jdbc:derby:iwish_db [root on APP]**
2. Right-click → **Connect**
3. Expand **Tables**
4. Right-click **WISHLIST** → **View Data**

---

## 📊 Verify Database Location

Run this in your terminal:

```bash
cd "g:\CS\ITI\DM 9M (ismalia)\DM Course\8. java for DM\6. Java\finals\Gitted MWK"
dir iwish_db
```

**Expected Output**:

```
Directory of g:\...\Gitted MWK\iwish_db

log/
seg0/
service.properties
```

This FOLDER is your database!

---

## 🎯 Quick Test: Prove Data is Real

### Test 1: Count Rows via Java

Add this to `Phase2TestServer.java` after database connects:

```java
try (Connection conn = dbManager.getConnection();
     Statement stmt = conn.createStatement();
     ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM WISHLIST")) {
    if (rs.next()) {
        System.out.println("✅ WISHLIST has " + rs.getInt(1) + " items");
    }
}
```

### Test 2: Direct SQL Query

Create a test file `TestDB.java`:

```java
import java.sql.*;

public class TestDB {
    public static void main(String[] args) throws Exception {
        String url = "jdbc:derby:iwish_db";
        Connection conn = DriverManager.getConnection(url, "root", "root");
        
        Statement stmt = conn.createStatement();
        ResultSet rs = stmt.executeQuery("SELECT * FROM WISHLIST");
        
        System.out.println("=== WISHLIST TABLE ===");
        while (rs.next()) {
            System.out.println(rs.getInt("item_id") + ": " + 
                             rs.getString("item_name") + " - $" + 
                             rs.getDouble("item_price"));
        }
        
        conn.close();
    }
}
```

Run from `Gitted MWK` directory:

```bash
javac TestDB.java
java TestDB
```

---

## 🔧 Solution Summary

**You DON'T need to create the database manually!**

The database:

- ✅ **IS being created** automatically
- ✅ **IS in the correct location** (`Gitted MWK/iwish_db/` folder)
- ✅ **DOES contain real data** (not just prints)
- ✅ **Recreates on delete** (by design, via `;create=true`)

**You just need to connect to it in NetBeans using "Embedded" driver, not "Network Server" driver!**

---

## 📝 Key Differences

| Feature | Network Server (`iwishdb`) | Embedded (`iwish_db`) |
|---------|---------------------------|----------------------|
| **Requires Derby Server** | Yes (must start server) | No (embedded in app) |
| **Connection String** | `jdbc:derby://localhost:1527/iwishdb` | `jdbc:derby:iwish_db` |
| **Location** | Derby server directory | Project folder |
| **NetBeans Driver** | Java DB (Network) | Java DB (Embedded) |
| **Performance** | Slower (network overhead) | Faster (direct access) |
| **Use Case** | Multi-app access | Single app (better for Gitted) |

---

## ✅ Action Items

1. **Stop looking for `iwish_db` in NetBeans Java DB list** - it won't appear there
2. **Add new connection** using "Java DB (Embedded)" driver
3. **Point to project folder** as database location
4. **Use database name** `iwish_db` (not full path)

The database IS working! You just need to connect to it correctly. 🎯
