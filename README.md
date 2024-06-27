
# SRDC - Assignment 1

## Overview

This is a simple chat application implemented in Java. The application consists of a server that handles multiple clients, allowing them to send and receive messages. It also includes database handling for storing and retrieving user and message data.

## Files

- `Client.java`: Contains the client-side logic, allowing users to connect to the server and communicate with other users.
- `ClientHandler.java`: Manages each client's connection on the server-side, handling the incoming and outgoing messages.
- `DatabaseHandler.java`: Manages the database interactions, including storing and retrieving user and message data.
- `Message.java`: Represents a message object with relevant properties and methods.
- `Server.java`: Contains the server-side logic, including accepting client connections and coordinating message exchanges.
- `User.java`: Represents a user object with relevant properties and methods.

## How to Use

### Prerequisites

- Java Development Kit (JDK) 8 or higher (due to the PostgreSQL driver used)
- A relational database (e.g., MySQL, PostgreSQL) (PostgreSQL is recommended)

## File Descriptions

### Client.java

This file contains the client-side logic. It connects to the server and provides the user interface for sending and receiving messages.

### ClientHandler.java

This class manages the server-side handling of client connections. It reads messages from clients and forwards them to other clients.

### DatabaseHandler.java

This class handles all database operations, such as saving and retrieving user and message data. It includes methods for connecting to the database and executing SQL queries.

### Message.java

This class represents a message with properties such as sender, receiver, content, and timestamp. It includes methods for creating and handling message objects.

### Server.java

This file contains the server-side logic. It accepts client connections and uses `ClientHandler` to manage each client.

### User.java

This class represents a user with properties such as username, password, and status. It includes methods for creating and handling user objects.

## Usage

1. Launch the server by running `Server.java`.
2. Connect one or more clients by running `Client.java`.
3. Clients can now chat with each other through the server.
