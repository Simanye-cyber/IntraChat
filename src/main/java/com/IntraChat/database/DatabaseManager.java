package com.IntraChat.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class DatabaseManager {

    private static final String DB_URL = "jdbc:sqlite:chat.db";
    public record Message(String sender, String recipient, String content, String timestamp) {}

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(DB_URL);
    }

    /*hgngmhn */
    public static void initializeDatabase() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " username TEXT NOT NULL UNIQUE,"
                + "password TEXT NOT NULL,"
                + " created_at TEXT DEFAULT CURRENT_TIMESTAMP"
                + ");";

        String createMessagesTable = "CREATE TABLE IF NOT EXISTS messages ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " sender TEXT NOT NULL,"
                + " recipient TEXT NOT NULL,"
                + " content TEXT NOT NULL,"
                + " timestamp TEXT DEFAULT CURRENT_TIMESTAMP"
                + ");";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement()) {

            stmt.execute(createUsersTable);
            stmt.execute(createMessagesTable);
            System.out.println("(DB) Database tables initialized successfully.");

        } catch (SQLException e) {
            System.err.println("(DB ERROR) Table creation failed: " + e.getMessage());
        }
    }

    // NB: using PreparedStatement to prevents SQL Injection.
    public static void addUser(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";

        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            System.out.println("(DB) User registered: " + username);

        } catch (SQLException e) {
            System.err.println("(DB ERROR) Could not add user: " + e.getMessage());
        }
    }


    public static void logMessage(String sender, String recipient, String content) {
        String sql = "INSERT INTO messages(sender, recipient, content) VALUES(?, ?, ?)";

        try (Connection conn = connect();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, sender);
            pstmt.setString(2, recipient);
            pstmt.setString(3, content);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            System.err.println("(DB ERROR) Could not log message: " + e.getMessage());
        }
    }

    public static void displayChatHistory() {
        String sql = "SELECT sender, recipient, content, timestamp FROM messages ORDER BY id ASC";

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            System.out.println("\n--- STORED CHAT HISTORY ---");
            while (rs.next()) {
                String sender = rs.getString("sender");
                String recipient = rs.getString("recipient");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");

                System.out.printf("[%s] %s -> %s: %s%n", timestamp, sender, recipient, content);
            }

        } catch (SQLException e) {
            System.err.println("(DB ERROR) Fetch failed: " + e.getMessage());
        }
    }

    public static boolean authenticateUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection conn = connect();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            return rs.next();

        } catch (SQLException e) {
            System.err.println("(DB ERROR) Authentication failed: " + e.getMessage());
            return false;
        }
    }

    public static ArrayList<Message> getChatHistoryArrayList() {
        String sql = "SELECT sender, recipient, content, timestamp FROM messages ORDER BY id ASC";
        ArrayList<Message> chatHistory = new ArrayList<>();

        try (Connection conn = connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String sender = rs.getString("sender");
                String recipient = rs.getString("recipient");
                String content = rs.getString("content");
                String timestamp = rs.getString("timestamp");

                chatHistory.add(new Message(sender, recipient, content, timestamp));
            }

        } catch (SQLException e) {
            System.err.println("(DB ERROR) Fetch failed: " + e.getMessage());
        }

        return chatHistory;
    }

    /* 
        Usage:

            initializeDatabase();

            addUser("Alice", "password123");
            addUser("Bob", "password456");


            logMessage("Alice", "Bob", "Hello Bob!");
            logMessage("Bob", "Alice", "Hey Alice, how are you?");

            authenticateUser("Alice", "password123"); //returns true
            authenticateUser("Bob", "password456"); //returns true

            displayChatHistory();
    */
}