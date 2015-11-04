package edu.thss.udp;

import edu.thss.DirectoryManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles received data
 * @author Stephan
 */
public final class DataReceiveHandler implements Runnable {
    
    private final ByteBuffer buffer; 
    private final String destinationPath; 
    
    protected DataReceiveHandler(String destinationPath, ByteBuffer buffer) {
        this.destinationPath = destinationPath; 
        this.buffer = buffer; 
    }

    @Override
    public void run() {
        File destination = new File(DirectoryManager.constructDirectories(this.destinationPath)); 
        try(FileChannel fileChannel = new FileOutputStream(destination).getChannel()) {
            buffer.flip(); 
            fileChannel.write(buffer);
        } 
        catch (IOException ex) {
            Logger.getLogger(DataReceiveHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
