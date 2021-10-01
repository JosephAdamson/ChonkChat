package com.example.chonkchat.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Data model for all types of message.
 * 
 * @author Joseph Adamson
 */
public class Message implements Serializable {
    
    private String sender;
    private String senderColourTag;
    private String message;
    private MessageType messageType;
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm");
    private String timeSent;
    
    public Message () {}

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderColourTag() {
        return senderColourTag;
    }

    public void setSenderColourTag(String senderColourTag) {
        this.senderColourTag = senderColourTag;
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
    
    
    
    public void setTimeSent(Date timeSent) {
        this.timeSent = dateFormat.format(timeSent);
    }
    
    public String getTimeSent() {
        return timeSent;
    }
}
