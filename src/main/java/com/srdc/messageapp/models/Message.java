package com.srdc.messageapp.models;

/**
 * Represents a message with sender, receiver, title, content, and timestamp.
 */

import java.time.LocalDateTime;

public class Message {

    private final String sender;
    private final String receiver;
    private final String title;
    private final String content;
    private final LocalDateTime timestamp;

    /**
     * Constructs for a Message object with parameters.
     *
     * @param sender    the sender of the message
     * @param receiver  the receiver of the message
     * @param title     the title of the message
     * @param content   the content of the message
     * @param timestamp the time message was sent
     */
    public Message(String sender, String receiver, String title, String content, LocalDateTime timestamp) {
        this.sender = sender;
        this.receiver = receiver;
        this.title = title;
        this.content = content;
        this.timestamp = timestamp;
    }

    // GETTERS

    /**
     * @return the sender of the message
     */
    public String getSender() {
        return sender;
    }

    /**
     * @return the receiver of the message
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * @return the title of the message
     */
    public String getTitle() {
        return title;
    }

    /**
     * @return the content of the message
     */
    public String getContent() {
        return content;
    }

    /**
     * @return the timestamp of the message
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
}
