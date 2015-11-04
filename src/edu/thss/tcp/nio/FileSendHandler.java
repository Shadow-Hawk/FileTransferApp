package edu.thss.nio;

import edu.thss.Config;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Stephan
 */
public final class FileSendHandler implements Runnable {

    private final File selectedFile; 
    private final SocketAddress address; 
    
    public FileSendHandler(File file, SocketAddress address) {
        this.selectedFile = file; 
        this.address = address; 
    }
    
    @Override
    public final void run() {
        try(FileChannel fileChannel = new FileInputStream(this.selectedFile).getChannel(); 
                SocketChannel socketChannel = SocketChannel.open(this.address);
                DataOutputStream dout = new DataOutputStream(socketChannel.socket().getOutputStream())) {

            long fileSize = fileChannel.size(), 
                    missingBytes = fileSize, 
                    transferredBytes = 0; 
            
            // Write meta data (file name and size)
            dout.writeUTF(this.selectedFile.getAbsolutePath().substring(Config.getSourceDir().length()));
            dout.writeLong(fileSize); 
            dout.flush(); 
            
            // Actual file
            do {
                transferredBytes = fileChannel.transferTo(fileSize - missingBytes, missingBytes, socketChannel); 
                missingBytes -= transferredBytes; 
            } while(missingBytes != 0 && transferredBytes != 0);
            
            // Finish
            socketChannel.socket().shutdownOutput();
        } 
        catch (IOException ex) {
            Logger.getLogger(FileSendHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
}
