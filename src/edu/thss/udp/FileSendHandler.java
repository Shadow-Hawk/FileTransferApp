package edu.thss.udp;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Reads data from a file
 * @author Stephan
 */
public class FileSendHandler implements Runnable {
    
    private final File file; 
    private final SocketAddress serverAddress; 
    
    protected FileSendHandler(File file, SocketAddress serverAddress) {
        this.file = file; 
        this.serverAddress = serverAddress; 
    }

    @Override
    public void run() {
        System.out.println("Send " + this.file.getAbsolutePath());
        try(DatagramChannel udpChannel = DatagramChannel.open(); 
                FileChannel fc = new FileInputStream(this.file).getChannel()) {
            udpChannel.connect(this.serverAddress);
            long bytes = fc.transferTo(0, fc.size(), udpChannel); 
            System.out.print(" " + bytes); 
        } 
        catch (IOException ex) {
            Logger.getLogger(FileSendHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
