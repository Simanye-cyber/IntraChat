package com.IntraChat.network;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final Server server;
    private BufferedReader in;
    private PrintWriter out;
    private String username = "Anonymous";
    private String password = "password";

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;
        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("[HANDLER ERROR] Stream setup failed: " + e.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            String initialName = in.readLine();
            if (initialName != null && !initialName.trim().isEmpty()) {
                this.username = initialName.trim();
            }
            String initialPassword = in.readLine();
            if (initialPassword != null && !initialPassword.trim().isEmpty()) {
                this.password = initialPassword.trim();
            }

            /*  Note: register the user to database here (not implemented yet) or 
            reject connection if username already exists or password is incorrect.*/

            server.broadcast("(server says) " + username + " joined the chat!", this);

            String incomingMessage;
            while ((incomingMessage = in.readLine()) != null) {
                if (incomingMessage.equalsIgnoreCase("/quit")) {
                    break;
                }
                String formatted = username + ": " + incomingMessage;
                System.out.println("[LOG] " + formatted);
                server.broadcast(formatted, this);
            }
        } catch (IOException e) {
            System.out.println("(handler says) Connection lost with " + username);
        } finally {
            closeConnection();
        }
    }


    public void sendMessage(String message) {
        if (out != null) {
            out.println(message);
        }
    }


    public void closeConnection() {
        server.removeClient(this);
        server.broadcast("(server says) " + username + " left the chat.", this);
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null && !socket.isClosed()) socket.close();
        } catch (IOException e) {
            System.err.println("(handler says) Error closing socket: " + e.getMessage());
        }
    }

    public String getUsername() {
        return username;
    }
}
