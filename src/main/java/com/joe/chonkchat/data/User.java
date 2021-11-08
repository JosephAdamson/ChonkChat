package com.joe.chonkchat.data;

import java.io.Serializable;

/**
 * Encapsulates information about active user
 */
public class User implements Serializable {

    private final String username;
    private final String colourTag;
    private final String avatar;
    private Status status;

    public User(String username, String colourTag, String avatar) {
        this.username = username;
        this.colourTag = colourTag;
        this.avatar = avatar;
        this.status = Status.ONLINE;
    }
    
    @Override
    public boolean equals(Object obj) {
        
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof User)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return this.username.equals(((User) obj).username);
    }

    @Override
    public int hashCode() {
        int prime = 31;
        int result = 1;
        result = prime * result +
                (username == null ? 0 : username.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "User{" +
                "username='" + username + '\'' +
                ", colourTag='" + colourTag + '\'' +
                ", avatar='" + avatar + '\'' +
                ", status=" + status +
                '}';
    }
    
    public String getUsername() {
        return username;
    }

    public String getColourTag() {
        return colourTag;
    }

    public String getAvatar() {
        return avatar;
    }
    
    public Status getStatus() {
        return status;
    }
    
    public void setStatus(Status status) {
        this.status = status;
    }
}
