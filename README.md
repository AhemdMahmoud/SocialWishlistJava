<<<<<<< HEAD
# i-Wish Project Setup

Welcome to the **i-Wish** team development repository. This is your starting point.

## � What You Need

1. **JDK 17** or higher
2. **Maven**
3. **Apache Derby Database** (Network Server running on port 1527)
4. **Your IDE** (NetBeans, IntelliJ, Eclipse, or VS Code)

---

## � Project Structure

```
├── src/main/java/
│   ├── com.iwish.server/      # Server code (ServerMain, ClientHandler)
│   ├── com.iwish.client/      # Client code (Launcher, ClientMain, NetworkManager)
│   └── com.iwish.database/    # Database logic (DatabaseManager, seed_data.sql)
├── database/
│   └── schema.sql             # Database schema (auto-runs on first start)
└── pom.xml                    # Maven dependencies
```

---

## 💾 Database Tables

The database has **6 tables** that work together:

| Table | Purpose |
|-------|---------|
| **Users** | User accounts (username, password, email) |
| **Friends** | Friend connections (pending/accepted status) |
| **WishList** | Global catalog of items (shared by everyone) |
| **UserWishes** | Links users to items they want |
| **Contributions** | Tracks who contributed money to which item |
| **Notifications** | System alerts for users |

**Important**: The `WishList` table is a **shared catalog**. Users don't own items directly—they link to them via `UserWishes`.

---

## � How to Run

### 1. Start the Server

Run `ServerMain.java` from your IDE.

- The database will be **automatically created** on first run
- You'll see: `"Database initialized successfully"`

### 2. Start the Client

Run `Launcher.java` from your IDE.

- **Note**: Use `Launcher.java`, NOT `ClientMain.java` directly (this fixes JavaFX module issues)

---

## 📦 Sample Data (Optional)

To add 50 sample items to the WishList catalog:

1. Open `src/main/java/com/iwish/database/seed_data.sql`
2. Copy the INSERT statements
3. Run them in your Derby database using any SQL tool

Categories included: Electronics, Fashion, Home & Living, Sports & Fitness.

---

## 🛠 Development Notes

- **Entry Point**: `com.iwish.client.Launcher` (not ClientMain)
- **Database**: Auto-initializes from `database/schema.sql`
- **Port**: Server runs on port `5000`
- **Database URL**: `jdbc:derby://localhost:1527/iwish_db`
=======
# SocialWishlistJava
project
>>>>>>> d8ee83af141711cae037a1d62dc29a45eeec0d3b
