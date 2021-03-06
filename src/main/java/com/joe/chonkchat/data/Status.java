package com.joe.chonkchat.data;

/**
 * Enum defines current status of client connected to
 * the server
 * 
 * @author Joseph Adamson
 */
public enum Status {
    ONLINE, BUSY, AWAY;
    
    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
