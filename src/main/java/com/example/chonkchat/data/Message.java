package com.example.chonkchat.data;

import java.io.Serializable;

/**
 * Data model for all types of message.
 * 
 * @author Joseph Adamson
 */
public class Message implements Serializable {
    
    private String sender;
    private String message;
    private MessageType messageType;
    
    public Message () {}

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }
}
