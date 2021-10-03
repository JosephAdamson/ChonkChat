package com.example.chonkchat.data;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Data model for all types of message.
 * 
 * @author Joseph Adamson
 */
public class Message implements Serializable {
    
    private String sender;
    private String senderColourTag;
    private String textData;
    private MessageType messageType;
    private
    List<String> activeUsers; 
    
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

    public String getTextData() {
        return textData;
    }

    public void setTextData(String textData) {
        this.textData = textData;
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
    
    public void setActiveUsers(List<String> activeUsers) {
        this.activeUsers = activeUsers;
    }
    
    public List<String> getActiveUsers() {
        return activeUsers;
    }
}
