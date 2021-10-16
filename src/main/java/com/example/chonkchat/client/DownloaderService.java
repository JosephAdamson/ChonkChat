package com.example.chonkchat.client;

import com.example.chonkchat.data.FileTransfer;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class DownloaderService extends Service<Void> {

    private FileTransfer fileTransfer;

    /**
     * Protocol for downloading files. Download location is set as the default OS 
     * downloads folder. Executed in another thread separate form the client thread to 
     * avoid failed download backing up the client thread.
     * 
     * @return a download task which is executed by the service.
     */
    @Override
    protected Task<Void> createTask() {
        return new Task<Void>() {
            
            @Override
            protected Void call() throws Exception {
                String filename = fileTransfer.getName();
                String extension = fileTransfer.getExtension();

                String downloadsFolderPath = System.getProperty("user.home") + "/Downloads/";

                try {
                    // check for any previously existing copies of the file in 
                    // the user's Downloads folder.
                    File fileToDownload =  new File(downloadsFolderPath + filename + extension);

                    int version = 0;
                    StringBuilder fileVer = new StringBuilder(filename);
                    while (fileToDownload.exists()) {
                        version++;
                        System.out.println(version);

                        if (filename.equals(fileVer.toString())) {
                            fileVer.append("(1)");
                        } else {
                            fileVer.delete(fileVer.length() - 3, fileVer.length());
                            fileVer.append("(").append(version).append(")");
                        }
                        fileToDownload = new File(downloadsFolderPath + fileVer + extension);
                    }

                    // get stream to write data to the new file in downloads.
                    ByteArrayInputStream byteArrayInputStream =
                            new ByteArrayInputStream(fileTransfer.getContent());
                    FileOutputStream fileOutputStream = new FileOutputStream(fileToDownload);

                    long totalSize = fileTransfer.getContent().length;
                    byte[] chunkHolder = new byte[1024];
                    long downloadSize = 0;
                    int chunk;
                    while((chunk = byteArrayInputStream.read(chunkHolder, 0, 1024)) >= 0) {

                        downloadSize += chunk;
                        
                        // compute progress percentage
                        double progress = ((double) downloadSize / (double) totalSize) * 100;
                        this.updateProgress(progress, totalSize);
                        
                        fileOutputStream.write(chunkHolder, 0, chunk);
                    }
                    fileOutputStream.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    public FileTransfer getFileTDownload() {
        return fileTransfer;
    }

    public void setFileTDownload(FileTransfer fileTransfer) {
        this.fileTransfer = fileTransfer;
    }
}
