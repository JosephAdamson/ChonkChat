package com.joe.chonkchat.data;

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
    
    private User sender;
    private String textData;
    private MessageType messageType;
    private List<User> activeUsers;
    private Status statusUpdate;
    private FileTransfer file;
    private Exception exception;
    
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("H:mm");
    private String timeSent;
    
    public Message () {}

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
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
    
    public void setActiveUsers(List<User> activeUsers) {
        this.activeUsers = activeUsers;
    }
    
    public List<User> getActiveUsers() {
        return activeUsers;
    }

    public FileTransfer getFile() {
        return file;
    }

    public void setFile(FileTransfer file) {
        this.file = file;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }

    public Status getStatusUpdate() {
        return statusUpdate;
    }

    public void setStatusUpdate(Status statusUpdate) {
        this.statusUpdate = statusUpdate;
    }
}
