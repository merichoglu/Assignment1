package com.srdc.messageapp;

/**
 * The Client class represents a client that connects to a server and sends requests to it.
 */

import java.io.*;
import java.net.*;
import java.util.Scanner;

@SuppressWarnings({ "BusyWait" })
public class Client {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private Scanner scanner;
    private boolean loggedIn;
    private boolean isAdmin;
    private boolean running;

    /**
     * Constructor for Client with parameters
     * 
     * @param address the IP address of the server
     * @param port    the port number of the server
     */

    public Client(String address, int port) {
        try {
            socket = new Socket(address, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);
            loggedIn = false;
            isAdmin = false;
            running = true;
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    /**
     * Reads user input from the console and returns the formatted request string.
     * 
     * @return the formatted request string
     */
    private String readUserInput() {
        if (!loggedIn) {
            return login();
        }

        String action;
        if (isAdmin) {
            System.out.print(
                    "Enter action (LOGOUT, SENDMSG, ADDUSER, REMOVEUSER, UPDATEUSER, LISTUSERS, GETINBOX, GETOUTBOX): ");
        } else {
            System.out.print("Enter action (LOGOUT, SENDMSG, GETINBOX, GETOUTBOX): ");
        }
        action = scanner.nextLine().toUpperCase();

        StringBuilder sb = new StringBuilder(action + ":::");

        switch (action) {
            case "LOGOUT", "GETINBOX", "GETOUTBOX" -> {
                if (!loggedIn) {
                    System.out.println("You are not logged in.");
                    return null;
                }
            }
            case "SENDMSG" -> {
                if (!loggedIn) {
                    System.out.println("You are not logged in.");
                    return null;
                }
                System.out.print("Receiver: ");
                sb.append(scanner.nextLine()).append(":::");
                System.out.print("Title: ");
                sb.append(scanner.nextLine()).append(":::");
                System.out.print("Message: ");
                sb.append(scanner.nextLine());
            }
            case "ADDUSER", "REMOVEUSER", "UPDATEUSER", "LISTUSERS" -> {
                if (!loggedIn) {
                    System.out.println("You are not logged in.");
                    return null;
                }
                if (!isAdmin) {
                    String formattedAction = action.replaceAll("USER(S)?$", "").toLowerCase();
                    System.out.println("Permission denied. Only administrators can " + formattedAction + " users.");
                    return null;
                }
                if (action.equals("ADDUSER") || action.equals("UPDATEUSER")) {
                    System.out.print("Username: ");
                    sb.append(scanner.nextLine()).append(":::");
                    System.out.print("Name: ");
                    sb.append(scanner.nextLine()).append(":::");
                    System.out.print("Surname: ");
                    sb.append(scanner.nextLine()).append(":::");
                    System.out.print("Birthdate (YYYY-MM-DD): ");
                    sb.append(scanner.nextLine()).append(":::");
                    System.out.print("Gender (M or F): ");
                    sb.append(scanner.nextLine()).append(":::");
                    System.out.print("Email: ");
                    sb.append(scanner.nextLine()).append(":::");
                    System.out.print("Location: ");
                    sb.append(scanner.nextLine()).append(":::");
                    System.out.print("Password: ");
                    sb.append(scanner.nextLine()).append(":::");
                    System.out.print("Is Admin (true/false): ");
                    sb.append(scanner.nextLine());
                } else if (action.equals("REMOVEUSER")) {
                    System.out.print("Username: ");
                    sb.append(scanner.nextLine());
                }
            }
            default -> {
                System.out.println("Invalid action. Please try again.");
                return "INVALID_ACTION";
            }
        }
        return sb.toString();
    }

    /**
     * Prompts the user to log in and returns the formatted request string.
     * 
     * @return the formatted request string
     */
    private String login() {
        System.out.println("Please log in.");
        System.out.print("Username: ");
        String username = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        return "LOGIN:::" + username + ":::" + password;
    }

    /**
     * Starts the client by reading user input and sending requests to the server.
     */
    public void start() {
        if (socket == null || input == null || output == null) {
            System.out.println("Client not properly initialized. Exiting...");
            return;
        }

        Thread responseReader = new Thread(() -> {
            try {
                String response;
                while ((response = input.readLine()) != null) {
                    if (response.isEmpty()) {
                        continue;
                    }
                    System.out.println(response);
                    if (response.contains("Login successful")) {
                        loggedIn = true;
                        isAdmin = response.contains("Admin: true");
                    } else if (response.contains("Logout successful")) {
                        loggedIn = false;
                        isAdmin = false;
                    } else if (response.contains("You have been removed")) {
                        loggedIn = false;
                        isAdmin = false;
                        System.out.println("Exiting due to user removal...");
                        running = false;
                        closeClient();
                        break;
                    }
                }
            } catch (IOException e) {
                System.out.println("Error reading from server: " + e.getMessage());
            }
        });
        responseReader.start();

        while (running) {
            String userInput = readUserInput();
            if (userInput != null) {
                if (userInput.equals("INVALID_ACTION")) {
                    System.out.println("Failed to read user input.");
                } else {
                    output.println(userInput);
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        System.out.println("Interrupted while waiting for server response: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Closes the client by closing the socket, input and output streams.
     */
    private void closeClient() {
        try {
            if (socket != null) {
                socket.close();
            }
            if (input != null) {
                input.close();
            }
            if (output != null) {
                output.close();
            }
            System.out.println("Client closed.");
            System.exit(0); // terminate after removal
        } catch (IOException e) {
            System.out.println("Error closing client: " + e.getMessage());
        }
    }

    /*
     * Main method to start the client.
     */
    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 5000);
        client.start();
    }
}
