# i-Wish Login Application

A modern JavaFX desktop application implementing secure user authentication with client-server architecture and database integration.

## 📋 Project Overview

This application provides a complete login and registration system for the i-Wish platform, featuring a polished user interface, robust server-side authentication, and secure database connectivity.

## ✨ Features Implemented

### 🎨 User Interface

- **Modern Login Screen**: Clean, professional design with brand colors and styling
- **Registration Form**: Complete user registration with validation
- **Password Visibility Toggle**: Eye icon to show/hide passwords on all password fields
- **Inline Notifications**: Real-time feedback messages displayed directly in the UI (no popups)
- **Form Switching**: Seamless toggle between Login and Registration views
- **Responsive Design**: Centered floating card layout with shadow effects

### 🔐 Authentication System

- **User Registration**: Create new accounts with username, email, and password
- **User Login**: Secure authentication with username and password
- **Password Validation**: Confirm password matching during registration
- **Field Validation**: Empty field detection with user-friendly error messages

### 🖥️ Server Architecture

- **Multi-threaded Server**: Handles multiple client connections simultaneously
- **Port Configuration**: Listens on port `5005`
- **Text-based Protocol**: Simple, efficient communication protocol
  - `LOGIN:username:password`
  - `REGISTER:username:password:email`
- **Error Handling**: Graceful handling of database and connection failures

### 💾 Database Integration

- **Apache Derby**: Network server mode database connectivity
- **Auto-schema Creation**: Automatically creates `Users` table if not exists
- **Secure Queries**: PreparedStatement usage to prevent SQL injection
- **Connection Management**: Proper resource cleanup and connection pooling

### 🌐 Client-Server Communication

- **NetworkHandler Singleton**: Centralized connection management
- **Socket-based Communication**: TCP/IP networking with DataInputStream/PrintStream
- **Response Handling**: Success/error message parsing and display
- **Connection Resilience**: Graceful handling of server unavailability

## 🏗️ Architecture

```
┌─────────────────┐         ┌─────────────────┐         ┌─────────────────┐
│   JavaFX UI     │◄───────►│  NetworkHandler │◄───────►│   ServerMain    │
│ (LoginController)│         │   (Singleton)   │         │  (Port 5005)    │
└─────────────────┘         └─────────────────┘         └─────────────────┘
                                                                  │
                                                                  ▼
                                                         ┌─────────────────┐
                                                         │ ClientHandler   │
                                                         │   (Thread)      │
                                                         └─────────────────┘
                                                                  │
                                                                  ▼
                                                         ┌─────────────────┐
                                                         │ DatabaseManager │
                                                         │  (Derby JDBC)   │
                                                         └─────────────────┘
```

## 🛠️ Technology Stack

- **Java**: 17
- **JavaFX**: 17.0.6
- **Apache Derby**: 10.16.1.1 (Client, Core, Tools, Shared)
- **Maven**: Build and dependency management
- **ControlsFX**: 11.1.2 (UI components)
- **Ikonli**: 12.3.1 (Material Design icons)

## 📦 Project Structure

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── iwish/
│   │           ├── login/
│   │           │   ├── App.java
│   │           │   ├── Launcher.java
│   │           │   ├── controllers/
│   │           │   │   └── LoginController.java
│   │           │   └── networking/
│   │           │       └── NetworkHandler.java
│   │           ├── server/
│   │           │   ├── ServerMain.java
│   │           │   └── ClientHandler.java
│   │           └── database/
│   │               ├── DatabaseManager.java
│   │               └── DbTester.java
│   └── resources/
│       └── com/
│           └── iwish/
│               └── login/
│                   ├── fxml/
│                   │   └── login_view.fxml
│                   ├── styles/
│                   │   └── login.css
│                   └── images/
│                       └── Logo.png
```

## 🚀 Getting Started

### Prerequisites

- Java Development Kit (JDK) 17 or higher
- Apache Maven 3.6+
- NetBeans IDE (recommended) or any Java IDE
- Apache Derby Network Server

### Setup Instructions

1. **Clone the Repository**

   ```bash
   git clone <repository-url>
   cd "Real one"
   ```

2. **Start Derby Database Server**
   - Open NetBeans
   - Navigate to: Services → Databases → Java DB
   - Right-click and select "Start Server"
   - Verify server is running on `localhost:1527`

3. **Build the Project**

   ```bash
   mvn clean install
   ```

4. **Run the Server**

   ```bash
   # In NetBeans: Right-click ServerMain.java → Run File
   # Or via Maven:
   mvn exec:java -Dexec.mainClass="com.iwish.server.ServerMain"
   ```

   Expected output: `Server started successfully!`

5. **Run the Client Application**

   ```bash
   # In NetBeans: Right-click Launcher.java → Run File
   # Or via Maven:
   mvn javafx:run
   ```

### Testing Database Connection

Run the `DbTester` utility to verify database connectivity:

```bash
# In NetBeans: Right-click DbTester.java → Run File
```

Expected output: `✅ Driver Loaded` and `✅ Connection Successful!`

## 📝 Protocol Specification

### Registration Request

```
REGISTER:username:password:email
```

**Response:**

- Success: `SUCCESS:Registration completed`
- Error: `ERROR:Username already exists` or `ERROR:Invalid registration format`

### Login Request

```
LOGIN:username:password
```

**Response:**

- Success: `SUCCESS:user_id:username`
- Error: `ERROR:Invalid credentials` or `ERROR:Invalid login format`

## 🎯 Key Achievements

1. ✅ **Complete UI/UX Implementation**: Professional, modern interface with inline feedback
2. ✅ **Full-stack Authentication**: End-to-end login and registration flow
3. ✅ **Robust Server Architecture**: Multi-threaded, scalable server design
4. ✅ **Database Integration**: Secure, efficient Derby database connectivity
5. ✅ **Error Handling**: Comprehensive error handling at all layers
6. ✅ **Code Quality**: Clean, maintainable code following best practices

## 🐛 Known Issues & Solutions

### Derby Driver Not Found

**Solution**: Ensure all Derby dependencies are in `pom.xml` and run `mvn clean install`

### Server Database Unavailable

**Solution**: Start Derby Network Server in NetBeans before running ServerMain

### Connection Refused

**Solution**: Verify ServerMain is running and listening on port 5005

## 🔜 Future Enhancements

- [ ] Dashboard implementation after successful login
- [ ] Session management and token-based authentication
- [ ] Password encryption (hashing with bcrypt/argon2)
- [ ] Email verification for registration
- [ ] Password reset functionality
- [ ] Remember me feature
- [ ] Multi-language support

## 👥 Team

Developed as part of the ITI Digital Media course project.

## 📄 License

[Specify your license here]

## 🙏 Acknowledgments

- JavaFX community for excellent documentation
- Apache Derby team for reliable database solution
- ControlsFX for enhanced UI components

---

**Last Updated**: January 2, 2026  
**Version**: 1.0.0  
**Status**: ✅ Login/Registration Module Complete
