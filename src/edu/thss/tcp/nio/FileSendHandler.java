package edu.thss.tcp.nio;

import edu.thss.Config;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.net.StandardSocketOptions;
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

        FileChannel fileChannel = null;
        SocketChannel socketChannel = null;
        DataOutputStream dout = null;
        try {
            fileChannel = new FileInputStream(this.selectedFile).getChannel();
            socketChannel = SocketChannel.open();
            socketChannel.setOption(StandardSocketOptions.TCP_NODELAY, true);
            socketChannel.setOption(StandardSocketOptions.SO_SNDBUF, Config.getSocketBufferSize());
            socketChannel.setOption(StandardSocketOptions.SO_RCVBUF, Config.getSocketBufferSize());
            socketChannel.setOption(StandardSocketOptions.SO_LINGER, 60);

            socketChannel.connect(this.address);

            dout = new DataOutputStream(socketChannel.socket().getOutputStream());

            long fileSize = fileChannel.size(), 
                    missingBytes = fileSize, 
                    transferredBytes;
            
            // Write meta data (file name and size)
            dout.writeUTF(selectedFile.getAbsolutePath().substring(Config.getSourceDir().length()).replaceAll("\\\\", "/"));
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
        } finally {
            try {
                if (dout != null) dout.close();
                if (fileChannel != null && fileChannel.isOpen()) fileChannel.close();
                if (socketChannel != null && socketChannel.isOpen()) socketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        
    }
    
}
