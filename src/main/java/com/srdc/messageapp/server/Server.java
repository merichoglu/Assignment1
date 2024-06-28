package com.srdc.messageapp.server;

/**
 * The Server class represents a server that listens for client connections and handles them using a ClientHandler.
 */

import com.srdc.messageapp.database.DatabaseHandler;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@SuppressWarnings("InfiniteLoopStatement")
public class Server {

    private ServerSocket serverSocket;
    private DatabaseHandler dbHandler;

    /**
     * Constructor for Server with parameters
     *
     * @param port       the port number to listen for client connections
     * @param dbUrl      the URL of the database
     * @param dbUser     the username for the database connection
     * @param dbPassword the password for the database connection
     * @throws Exception if an error occurs during server initialization
     */
    public Server(int port, String dbUrl, String dbUser, String dbPassword) throws Exception {
        try {
            serverSocket = new ServerSocket(port);
            dbHandler = new DatabaseHandler(dbUrl, dbUser, dbPassword);
        } catch (IOException e) {
            System.out.println("Error initializing server socket: " + e.getMessage());
        }
    }

    /**
     * Starts the server by accepting client connections and creating a new
     * ClientHandler for each connection.
     */
    public void start() {
        // init socket and db, connect clients if requested
        if (serverSocket == null) {
            System.out.println("Server socket not initialized. Exiting...");
            return;
        }
        if (dbHandler == null) {
            System.out.println("Database handler not initialized. Exiting...");
            return;
        }
        System.out.println("Server started");
        while (true)
            try {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                new ClientHandler(socket, dbHandler).start();
            } catch (IOException e) {
                System.out.println("Error accepting client connection: " + e.getMessage());
            }
    }

    /**
     * Main method to start the server
     * 
     * @param args the command line arguments
     * @throws Exception if an error occurs during server initialization
     */
    public static void main(String[] args) throws Exception {
        String dbUrl = "jdbc:postgresql://localhost:5432/postgres";
        String dbUser = "postgres";
        String dbPassword = "5611Me_0";
        int port = 5000;
        Server server = new Server(port, dbUrl, dbUser, dbPassword);
        server.start();
    }
}
