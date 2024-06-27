package com.srdc.messageapp;

/*
 * This class is responsible for handling all database operations.
 * It establishes a connection to the database and provides methods for
 * user authentication, user management, and message management.
 * It also provides methods for checking if a user exists and if a user is removed.
 * The class is used by the Server class to interact with the database.
*/

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("ALL")
public class DatabaseHandler {

    private final Connection connection;

    /**
     * Constructor for DatabaseHandler with parameters
     * 
     * @param url      the URL of the database
     * @param user     the username for the database connection
     * @param password the password for the database connection
     * @throws Exception if an error occurs during database connection
     */

    public DatabaseHandler(String url, String user, String password) throws Exception {
        connection = DriverManager.getConnection(url, user, password);
        System.out.println("Database connection successful.");
    }

    /**
     * Authenticates a user by checking the username and password against the
     * database.
     * 
     * @param username the username of the user
     * @param password the password of the user
     * @return the User object if authentication is successful, null otherwise
     */
    public User authenticateUser(String username, String password) {
        String query = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("username"),
                            rs.getString("name"),
                            rs.getString("surname"),
                            rs.getDate("birthdate"),
                            rs.getString("gender"),
                            rs.getString("email"),
                            rs.getString("location"),
                            rs.getString("password"),
                            rs.getBoolean("isAdmin"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error authenticating user: " + e.getMessage());
        }
        return null;
    }

    /**
     * Checks if a user with the given username exists in the database.
     * 
     * @param username the username to check
     * @return true if the user exists, false otherwise
     */
    public boolean userExists(String username) {
        String query = "SELECT 1 FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking user existence: " + e.getMessage());
        }
        return false;
    }

    /**
     * Adds a new user to the database.
     * 
     * @param admin   the admin user adding the new user
     * @param newUser the new user to add
     * @throws Exception if the admin user is not an admin
     */
    public void addUser(User admin, User newUser) throws Exception {
        if (!admin.isAdmin()) {
            throw new Exception("Only admins can add users.");
        }
        String query = "INSERT INTO users (username, name, surname, birthdate, gender, email, location, password, isAdmin) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, newUser.getUsername());
            stmt.setString(2, newUser.getName());
            stmt.setString(3, newUser.getSurname());
            stmt.setDate(4, newUser.getBirthdate());
            stmt.setString(5, newUser.getGender());
            stmt.setString(6, newUser.getEmail());
            stmt.setString(7, newUser.getLocation());
            stmt.setString(8, newUser.getPassword());
            stmt.setBoolean(9, newUser.isAdmin());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User added successfully.");
            } else {
                System.out.println("User not added.");
            }
        } catch (SQLException e) {
            System.err.println("Error adding user: " + e.getMessage());
        }
    }

    /**
     * Removes a user from the database.
     * 
     * @param admin    the admin user removing the user
     * @param username the username of the user to remove
     * @throws Exception if the admin user is not an admin or user does not exist
     */
    public void removeUser(User admin, String username) throws Exception {
        if (!admin.isAdmin()) {
            throw new Exception("Only admins can remove users.");
        }
        String query = "DELETE FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected == 0) {
                throw new SQLException("User not found or could not be deleted.");
            }
            System.out.println("User removed successfully.");
        } catch (SQLException e) {
            System.err.println("Error removing user: " + e.getMessage());
        }
    }

    /**
     * Updates a user in the database.
     * 
     * @param admin       the admin user updating the user
     * @param updatedUser the updated user information
     * @throws Exception if the admin user is not an admin or some other SQL error
     *                   occurs
     */
    public void updateUser(User admin, User updatedUser) throws Exception {
        if (!admin.isAdmin()) {
            throw new Exception("Only admins can update users.");
        }
        String query = "UPDATE users SET name = ?, surname = ?, birthdate = ?, gender = ?, email = ?, location = ?, password = ?, isAdmin = ? WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, updatedUser.getName());
            stmt.setString(2, updatedUser.getSurname());
            stmt.setDate(3, updatedUser.getBirthdate());
            stmt.setString(4, updatedUser.getGender());
            stmt.setString(5, updatedUser.getEmail());
            stmt.setString(6, updatedUser.getLocation());
            stmt.setString(7, updatedUser.getPassword());
            stmt.setBoolean(8, updatedUser.isAdmin());
            stmt.setString(9, updatedUser.getUsername());
            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                System.out.println("User updated successfully.");
            } else {
                System.out.println("User not updated.");
            }
        } catch (SQLException e) {
            System.err.println("Error updating user: " + e.getMessage());
        }
    }

    /**
     * Fetches a user from the database by username.
     * 
     * @param username the username of the user to fetch
     * @return the User object if found, null otherwise
     */
    public User getUserByUsername(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new User(
                            rs.getString("username"),
                            rs.getString("name"),
                            rs.getString("surname"),
                            rs.getDate("birthdate"),
                            rs.getString("gender"),
                            rs.getString("email"),
                            rs.getString("location"),
                            rs.getString("password"),
                            rs.getBoolean("isAdmin"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching user: " + e.getMessage());
        }
        return null;
    }

    /**
     * Lists all users in the database.
     * 
     * @param admin the admin user listing the users
     * @return a list of User objects
     * @throws Exception if the admin user is not an admin
     */
    public List<User> listUsers(User admin) throws Exception {
        if (!admin.isAdmin()) {
            throw new Exception("Only admins can list users.");
        }
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM users ORDER BY isadmin DESC;";
        try (Statement stmt = connection.createStatement(); ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                User user = new User(
                        rs.getString("username"),
                        rs.getString("name"),
                        rs.getString("surname"),
                        rs.getDate("birthdate"),
                        rs.getString("gender"),
                        rs.getString("email"),
                        rs.getString("location"),
                        rs.getString("password"),
                        rs.getBoolean("isAdmin"));
                users.add(user);
            }
        } catch (SQLException e) {
            System.err.println("Error listing users: " + e.getMessage());
        }
        return users;
    }

    /**
     * Saves a message to the database.
     * 
     * @param message the message to save
     */
    public void saveMessage(Message message) {
        String query = "INSERT INTO messages (sender, receiver, title, content, timestamp) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, message.getSender());
            stmt.setString(2, message.getReceiver());
            stmt.setString(3, message.getTitle());
            stmt.setString(4, message.getContent());
            stmt.setTimestamp(5, Timestamp.valueOf(message.getTimestamp()));
            stmt.executeUpdate();
            System.out.println("Message sent.");
        } catch (SQLException e) {
            System.err.println("Error saving message: " + e.getMessage());
        }
    }

    /**
     * Fetches messages from the database for a given user's inbox.
     * 
     * @param username the username of the user
     * @return a list of Message objects
     */
    public List<Message> getInbox(String username) {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM messages WHERE receiver = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message(
                            rs.getString("sender"),
                            rs.getString("receiver"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getTimestamp("timestamp").toLocalDateTime());
                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching inbox: " + e.getMessage());
        }
        return messages;
    }

    /**
     * Fetches messages from the database for a given user's outbox.
     * 
     * @param username the username of the user
     * @return a list of Message objects
     */
    public List<Message> getOutbox(String username) {
        List<Message> messages = new ArrayList<>();
        String query = "SELECT * FROM messages WHERE sender = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Message message = new Message(
                            rs.getString("sender"),
                            rs.getString("receiver"),
                            rs.getString("title"),
                            rs.getString("content"),
                            rs.getTimestamp("timestamp").toLocalDateTime());
                    messages.add(message);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error fetching outbox: " + e.getMessage());
        }
        return messages;
    }

    /**
     * Checks if a user has been removed from the database.
     * 
     * @param username the username of the user
     * @return true if the user is removed, false otherwise
     */
    public boolean isUserRemoved(String username) {
        String query = "SELECT * FROM users WHERE username = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, username);
            try (ResultSet rs = stmt.executeQuery()) {
                return !rs.next();
            }
        } catch (SQLException e) {
            System.err.println("Error checking if user is removed: " + e.getMessage());
            return true; // assuming user is removed if there's an error, may need to change
        }
    }

}
