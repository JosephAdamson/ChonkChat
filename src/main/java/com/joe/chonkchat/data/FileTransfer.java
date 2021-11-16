package com.joe.chonkchat.data;

import java.io.Serializable;

/**
 * Basic data class for transferring files (.txt, .pdf, .png etc.)
 * 
 * @author Joseph Adamson
 */
public class FileTransfer implements Serializable {
    
    private String name;
    private byte[] content;
    private String extension;
    
    public FileTransfer() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getContent() {
        return content;
    }

    public void setContent(byte[] content) {
        this.content = content;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }
}
