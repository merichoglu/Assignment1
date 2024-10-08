package com.srdc.messageapp.client;

/**
 * This is the class definition of Client that user interacts with.
 * It takes inputs address and port as input, and initializes client.
 */

import com.srdc.messageapp.server.ClientHandler;

import java.io.*;
import java.net.*;
import java.util.Scanner;

@SuppressWarnings({"BusyWait", "ThrowablePrintedToSystemOut"})
public class Client {

    private Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private Scanner scanner;
    private boolean loggedIn;
    private boolean isAdmin;
    private boolean running = true;

    /**
     * Constructor for Client with
     *
     * @param address the IP address
     * @param port    the port server runs on
     */
    public Client(String address, int port) {
        try {
            socket = new Socket(address, port);
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
            scanner = new Scanner(System.in);
            loggedIn = false;
            isAdmin = false;
        } catch (IOException i) {
            System.out.println(i);
        }
    }

    public static void main(String[] args) {
        Client client = new Client("127.0.0.1", 5000);
        client.start();
    }

    /**
     * Method that reads user input by first forcing login.
     * When logged in, user can access different commands depending on their privilege.
     *
     * @return sb StringBuilder object for server-side to handle operations, protocol is ":::"
     */
    private String readUserInput() {
        if (!loggedIn) {
            System.out.print("Please log in.\nUsername: ");
            String username = scanner.nextLine();
            System.out.print("Password: ");
            String password = scanner.nextLine();
            return "LOGIN:::" + username + ":::" + password;
        }

        System.out.print("Enter action (LOGOUT, SENDMSG, " + (isAdmin ? "ADDUSER, REMOVEUSER, UPDATEUSER, LISTUSERS, " : "") + "GETINBOX, GETOUTBOX): ");
        String action = scanner.nextLine().toUpperCase();
        StringBuilder sb = new StringBuilder(action + ":::");

        switch (action) {
            case "LOGIN":
                System.out.println("A user is already logged in.");
                return null;
            case "LOGOUT", "GETINBOX", "GETOUTBOX":
                break;
            case "SENDMSG":
                System.out.print("Receiver: ");
                sb.append(scanner.nextLine()).append(":::");
                System.out.print("Title: ");
                sb.append(scanner.nextLine()).append(":::");
                System.out.print("Message: ");
                sb.append(scanner.nextLine());
                break;
            case "ADDUSER", "REMOVEUSER", "UPDATEUSER", "LISTUSERS":
                if (!isAdmin) {
                    System.out.println("Permission denied. Only administrators can " + action.toLowerCase() + ".");
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
                break;
            default:
                System.out.println("Invalid action. Please try again.");
                return null;
        }
        return sb.toString();
    }

    /**
     * Method to start client. First check if user is logged in, else force to do so.
     * Run an infinite loop to take input constantly. That is,
     * client will be running until it's closed.
     */
    public void start() {
        if (socket == null || input == null || output == null) {
            System.out.println("Client not properly initialized. Exiting...");
            return;
        }

        Thread responseReader = new Thread(() -> {
            try {
                String response;
                while (running && (response = input.readLine()) != null) {
                    if (response.isEmpty()) {
                        continue;
                    }
                    if (response.startsWith("LISTUSERS:::")) {
                        formatListUsers(response);
                    } else if (response.startsWith("GETINBOX:::") || response.startsWith("GETOUTBOX:::")) {
                        formatMessages(response);
                    } else {
                        System.out.println(response);
                        if (response.contains("Login successful")) {
                            loggedIn = true;
                            isAdmin = response.contains("Admin: true");
                        } else if (response.contains("Logout successful")) {
                            loggedIn = false;
                            isAdmin = false;
                        } else if (response.contains("Client will now close")) {
                            running = false;
                            closeClient();
                        }
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
                output.println(userInput);
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    System.out.println("Interrupted while waiting for server response: " + e.getMessage());
                }
            }
        }
    }

    /**
     * Format the user list output taken from server-side by detokenizing.
     * Protocol is again ":::".
     */
    private void formatListUsers(String response) {
        String[] parts = response.split(":::");
        System.out.println("\nUser List:");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-15s %-15s %-15s %-10s %-30s %-20s %-10s%n", "USERNAME", "NAME", "SURNAME", "GENDER", "EMAIL", "LOCATION", "ADMIN");
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
        for (int i = 1; i < parts.length; i += 7) {
            System.out.printf("%-15s %-15s %-15s %-10s %-30s %-20s %-10s%n",
                    parts[i], parts[i + 1], parts[i + 2], parts[i + 3], parts[i + 4], parts[i + 5], parts[i + 6]);
        }
        System.out.println("-----------------------------------------------------------------------------------------------------------------------");
    }

    /**
     * Format the inbox / outbox output taken from server-side by detokenizing.
     * Protocol is again ":::".
     */
    private void formatMessages(String response) {
        String[] parts = response.split(":::");
        boolean isInbox = response.startsWith("GETINBOX:::");
        if (isInbox) {
            System.out.println("\nInbox Messages:");
            System.out.println("-------------------------------------------------------------------------------------------------------");
            System.out.printf("%-15s %-20s %-20s %-50s%n", "FROM", "TITLE", "TIMESTAMP", "CONTENT");
        } else {
            System.out.println("\nOutbox Messages:");
            System.out.println("-------------------------------------------------------------------------------------------------------");
            System.out.printf("%-15s %-20s %-20s %-50s%n", "TO", "TITLE", "TIMESTAMP", "CONTENT");
        }
        System.out.println("-------------------------------------------------------------------------------------------------------");
        for (int i = 1; i < parts.length; i += 4) {
            System.out.printf("%-15s %-20s %-20s %-50s%n", parts[i], parts[i + 1], parts[i + 3], parts[i + 2]);
        }
        System.out.println("-------------------------------------------------------------------------------------------------------");
    }

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
            System.exit(0); // Exit the program
        } catch (IOException e) {
            System.err.println("Error closing client: " + e.getMessage());
        }
    }
}
