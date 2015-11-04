package edu.thss.nio;

import edu.thss.Config;
import edu.thss.DirectoryManager;
import java.io.DataInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Stephan
 */
public class FileReceiveHandler implements Runnable {

    private final Socket socket; 
    
    public FileReceiveHandler(Socket client) {
        this.socket = client; 
    }

    @Override
    public void run() {
        try (SocketChannel socketChannel = this.socket.getChannel(); 
                DataInputStream din = new DataInputStream(this.socket.getInputStream())) {
            
            assert(socketChannel != null); 
            
            // Receiving meta data and prepare receiving
            String fileName = din.readUTF(), 
                    fullFileName = DirectoryManager.concat(Config.getDestinationDir(), fileName); 
            long fileSize = din.readLong(), 
                    expectedBytes = fileSize, 
                    transferredBytes = 0;
            
            DirectoryManager.constructDirectories(fileName);
            
            try (FileChannel fileChannel = new FileOutputStream(fullFileName).getChannel()) {
                // Receive data
                do {
                    transferredBytes = fileChannel.transferFrom(socketChannel, fileSize - expectedBytes, expectedBytes); 
                    expectedBytes -= transferredBytes; 
                } while(expectedBytes != 0 && transferredBytes != 0);
            } 
        } catch (IOException ex) {
            Logger.getLogger(FileReceiveHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
}
