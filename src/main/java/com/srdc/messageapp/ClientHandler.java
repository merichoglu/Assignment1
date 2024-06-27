
/*
 * This class is responsible for handling client requests and responses.
 * It reads input from the client, processes the request, and sends a response back.
 * It also checks the validity of the user every 5 seconds to ensure the user is still active or has not been removed.
 * The class extends the Thread class to run in a separate thread.
 * Implying that every client connection will be handled in a separate thread.
*/

import java.io.*;
import java.net.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Objects;
import java.util.StringTokenizer;

@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class ClientHandler extends Thread {

    private final Socket socket;
    private BufferedReader input;
    private PrintWriter output;
    private final DatabaseHandler dbHandler;
    private User currentUser;
    private boolean running = true;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Constructor for ClientHandler with parameters
     * 
     * @param socket    the client socket
     * @param dbHandler the database handler
     * @throws IOException if an error occurs during I/O operations
     */
    public ClientHandler(Socket socket, DatabaseHandler dbHandler) {
        this.socket = socket;
        this.dbHandler = dbHandler;
        try {
            input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            output = new PrintWriter(socket.getOutputStream(), true);
        } catch (IOException e) {
            System.err.println("Error initializing I/O streams: " + e.getMessage());
        }
    }

    /**
     * The run method is called when the thread is started.
     * It reads input from the client, processes the request, and sends a response
     * back.
     */
    @Override
    public void run() {
        new Thread(this::checkUserValidity).start();

        String received;
        while (running) {
            try {
                received = input.readLine();
                if (received == null) {
                    break;
                }
                StringTokenizer st = new StringTokenizer(received, ":");

                String action = st.nextToken();
                switch (action.toUpperCase()) {
                    case "LOGIN" -> handleLogin(st);
                    case "LOGOUT" -> handleLogout();
                    case "SENDMSG" -> handleSendMsg(st);
                    case "ADDUSER" -> handleAddUser(st);
                    case "REMOVEUSER" -> handleRemoveUser(st);
                    case "UPDATEUSER" -> handleUpdateUser(st);
                    case "LISTUSERS" -> handleListUsers();
                    case "GETINBOX" -> handleGetInbox();
                    case "GETOUTBOX" -> handleGetOutbox();
                    default -> output.println("\nUnknown command");
                }
            } catch (IOException e) {
                System.err.println("Error reading client input: " + e.getMessage());
                break;
            }
        }
        try {
            socket.close();
        } catch (IOException e) {
            System.err.println("Error closing socket: " + e.getMessage());
        }
    }

    /**
     * Checks the validity of the user every 5 seconds to ensure the user is still
     * active or has not been removed.
     */
    @SuppressWarnings("BusyWait")
    private void checkUserValidity() {
        while (running) {
            try {
                if (currentUser != null && dbHandler.isUserRemoved(currentUser.getUsername())) {
                    output.println("\nYou have been removed by an admin. Client will now close.");
                    currentUser = null;
                    running = false;
                    // exit the process
                    closeClient();
                    break;
                }
                Thread.sleep(5000); // check activity every 5 secs
            } catch (InterruptedException e) {
                System.err.println("Error checking user validity: " + e.getMessage());
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
        } catch (IOException e) {
            System.err.println("Error closing client: " + e.getMessage());
        }
    }

    /**
     * Handles the login request from the client. The user is authenticated using
     * the provided username and password. If the user is successfully
     * authenticated,
     * the user object is stored in the currentUser field.
     * 
     * @param st the string tokenizer containing the username and password
     */
    private void handleLogin(StringTokenizer st) {
        if (currentUser == null) {
            String username = st.hasMoreTokens() ? st.nextToken() : "";
            String password = st.hasMoreTokens() ? st.nextToken() : "";

            if (username.isEmpty() || password.isEmpty()) {
                output.println("\nUsername or password cannot be empty.");
                return;
            }

            currentUser = dbHandler.authenticateUser(username, password);
            if (currentUser != null) {
                output.println("\nLogin successful. Admin: " + currentUser.isAdmin());
            } else {
                output.println("\nLogin failed");
            }
        } else {
            output.println("\nAlready logged in");
        }
    }

    /**
     * Handles the logout request from the client. If a user is logged in, the
     * currentUser field is set to null.
     */
    private void handleLogout() {
        if (currentUser == null) {
            output.println("\nNo user is logged in.");
            return;
        }
        currentUser = null;
        output.println("\nLogout successful");
    }

    /**
     * Handles the send message request from the client. The message is sent from
     * the current user to the specified receiver. The message is saved in the
     * database.
     * 
     * @param st the string tokenizer containing the receiver, title, and message
     */
    private void handleSendMsg(StringTokenizer st) {
        if (currentUser == null) {
            output.println("\nNo user is logged in.");
            return;
        }
        String sender = currentUser.getUsername();
        String receiver = st.nextToken();
        LocalDateTime timestamp = LocalDateTime.now();
        if (!dbHandler.userExists(receiver)) {
            output.println("\nUser " + receiver + " does not exist");
            return;
        }
        String title = st.nextToken();
        String messageContent = st.nextToken();
        Message message = new Message(sender, receiver, title, messageContent, timestamp);
        dbHandler.saveMessage(message);
        output.println("\nMessage sent successfully at " + timestamp);
    }

    /**
     * Handles the add user request from the client. The user is added to the
     * database with the provided details. The user must be an admin to perform this
     * operation.
     * 
     * @param st the string tokenizer containing the user details
     */
    private void handleAddUser(StringTokenizer st) {
        if (!isAdmin()) {
            return;
        }
        String username = st.nextToken();
        String name = st.nextToken();
        String surname = st.nextToken();
        String birthdate = st.nextToken();
        String gender = st.nextToken();
        String email = st.nextToken();
        String location = st.nextToken();
        String password = st.nextToken();
        boolean isAdmin = Boolean.parseBoolean(st.nextToken());

        // enforce unique usernames
        if (dbHandler.getUserByUsername(username) != null) {
            output.println("\nUsername already taken.");
            return;
        }

        // enforce valid date (YYYY-MM-DD format and 0<month<=12, 0<day<=31)
        LocalDate parsedBirthdate;
        if (!isValidDate(birthdate)) {
            output.println("\nInvalid birthdate. Please use YYYY-MM-DD, and make sure values are correct.");
            return;
        } else {
            try {
                parsedBirthdate = LocalDate.parse(birthdate, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                output.println("\nInvalid birthdate. Please use YYYY-MM-DD, and make sure values are correct.");
                return;
            }
        }

        // enforce binary genders
        if (!Objects.equals(gender, "M") && !Objects.equals(gender, "F")) {
            output.println("\nInvalid gender. Make sure to enter either M or F.");
            return;
        }

        try {
            User user = new User(username, name, surname, java.sql.Date.valueOf(parsedBirthdate), gender, email,
                    location, password, isAdmin);
            dbHandler.addUser(currentUser, user);
            output.println("\nUser added successfully");
        } catch (Exception e) {
            output.println("\nError adding user: " + e.getMessage());
        }
    }

    /**
     * Handles the remove user request from the client. The user is removed from the
     * database. The user must be an admin to perform this operation.
     * 
     * @param st the string tokenizer containing the username
     */
    private void handleRemoveUser(StringTokenizer st) {
        if (!isAdmin()) {
            return;
        }
        String username = st.nextToken();
        if (dbHandler.getUserByUsername(username) == null) {
            output.println("\nUser not found.");
            return;
        }
        try {
            dbHandler.removeUser(currentUser, username);
            output.println("\nUser removed successfully");
        } catch (Exception e) {
            output.println("\nError removing user: " + e.getMessage());
        }
    }

    /**
     * Handles the update user request from the client. The user details are updated
     * in the database. The user must be an admin to perform this operation.
     * 
     * @param st the string tokenizer containing the user details
     */
    private void handleUpdateUser(StringTokenizer st) {
        // username cannot be updated, unique id
        if (!isAdmin()) {
            return;
        }
        String username = st.nextToken();
        String name = st.nextToken();
        String surname = st.nextToken();
        String birthdate = st.nextToken();
        String gender = st.nextToken();
        String email = st.nextToken();
        String location = st.nextToken();
        String password = st.nextToken();
        boolean isAdmin = Boolean.parseBoolean(st.nextToken());

        LocalDate parsedBirthdate;
        if (!isValidDate(birthdate)) {
            output.println("\n\nInvalid birthdate. Please use YYYY-MM-DD, and make sure values are correct.");
            return;
        } else {
            try {
                parsedBirthdate = LocalDate.parse(birthdate, DATE_FORMATTER);
            } catch (DateTimeParseException e) {
                output.println("\nInvalid birthdate. Please use YYYY-MM-DD, and make sure values are correct.");
                return;
            }
        }

        // enforce binary genders
        if (!Objects.equals(gender, "M") && !Objects.equals(gender, "F")) {
            output.println("\nInvalid gender. Make sure to enter either M or F.");
            return;
        }

        try {
            User user = new User(username, name, surname, java.sql.Date.valueOf(parsedBirthdate), gender, email,
                    location, password, isAdmin);
            dbHandler.updateUser(currentUser, user);
            output.println("\nUser updated successfully");
        } catch (Exception e) {
            output.println("\nError updating user: " + e.getMessage());
        }
    }

    /**
     * Handles the list users request from the client. The list of users is
     * retrieved
     * from the database and displayed to the client. The user must be an admin to
     * perform this operation.
     */
    private void handleListUsers() {
        if (!isAdmin()) {
            return;
        }
        try {
            List<User> users = dbHandler.listUsers(currentUser);
            output.println("\nUser List:");
            output.println("---------------------------------------------------");
            for (User user : users) {
                output.println("Username: " + user.getUsername());
                output.println("Name: " + user.getName() + " " + user.getSurname());
                output.println("Email: " + user.getEmail());
                output.println("Location: " + user.getLocation());
                output.println("Admin: " + user.isAdmin());
                output.println("---------------------------------------------------");
            }
        } catch (Exception e) {
            output.println("\nError listing users: " + e.getMessage());
        }
    }

    /**
     * Handles the get inbox request from the client. The inbox messages are
     * retrieved
     * from the database and displayed to the client.
     */
    private void handleGetInbox() {
        if (currentUser == null) {
            output.println("\nPermission denied. User not authenticated.");
            return;
        }
        try {
            List<Message> inbox = dbHandler.getInbox(currentUser.getUsername());
            output.println("\nInbox Messages:");
            output.println("---------------------------------------------------");
            for (Message message : inbox) {
                if (!dbHandler.userExists(message.getSender())) {
                    output.println("From: " + message.getSender() + " (account deleted)");
                } else {
                    output.println("From: " + message.getSender());
                }
                output.println("Title: " + message.getTitle());
                output.println("Message: " + message.getContent());
                output.println("Received: " + message.getTimestamp());
                output.println("---------------------------------------------------");
            }
        } catch (Exception e) {
            output.println("\nError retrieving inbox: " + e.getMessage());
        }
    }

    /**
     * Handles the get outbox request from the client. The outbox messages are
     * retrieved
     * from the database and displayed to the client.
     */
    private void handleGetOutbox() {
        if (currentUser == null) {
            output.println("\nPermission denied. User not authenticated.");
            return;
        }
        try {
            List<Message> outbox = dbHandler.getOutbox(currentUser.getUsername());
            output.println("\nOutbox Messages:");
            output.println("---------------------------------------------------");
            for (Message message : outbox) {
                if (!dbHandler.userExists(message.getReceiver())) {
                    output.println("To: " + message.getReceiver() + " (account deleted)");
                } else {
                    output.println("To: " + message.getReceiver());
                }
                output.println("Title: " + message.getTitle());
                output.println("Message: " + message.getContent());
                output.println("Sent: " + message.getTimestamp());
                output.println("---------------------------------------------------");
            }
        } catch (Exception e) {
            output.println("\nError retrieving outbox: " + e.getMessage());
        }
    }

    /**
     * Checks if the date string is in the correct format (YYYY-MM-DD).
     * 
     * @param dateStr the date string to check
     * @return true if the date string is in the correct format, false otherwise
     */
    private boolean isValidDate(String dateStr) {
        try {
            LocalDate.parse(dateStr, DATE_FORMATTER);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Checks if the current user is an admin and prints an error message if not.
     * 
     * @return true if the current user is an admin, false otherwise
     */
    private boolean isAdmin() {
        if (currentUser == null || !currentUser.isAdmin()) {
            output.println("\nPermission denied. Only admins can perform this operation.");
            return false;
        }
        return true;
    }
}
