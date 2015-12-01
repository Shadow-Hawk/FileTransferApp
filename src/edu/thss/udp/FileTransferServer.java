package edu.thss.udp;

import edu.thss.Config;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Blocking file transfer server that uses UDP for file transmission
 Note: Currently, it will only be able to store 1 file! Otherwise,
 *       the client has to send context information (filename, offset) 
 *       along with the data.
 * @author Stephan
 */
public class FileTransferServer {
    
    public static void main(String args[]) {
        FileTransferServer.run(Config.getDestPort(), Config.getBufferSize());
    }
    
    public static void run(int port, int bufferSize) {
        int counter = 0; 
        ExecutorService exService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()); 
        try(DatagramChannel udpChannel = DatagramChannel.open()) {
            udpChannel.bind(new InetSocketAddress(port));
            while(true) {
                ByteBuffer buffer = ByteBuffer.allocate(bufferSize); // Better use a pool of buffers (allocation probably slow)
                buffer.clear(); 
                System.out.println("Wait");
                udpChannel.receive(buffer); 
                System.out.println(" Receive"); 
                exService.submit(new DataReceiveHandler("" + counter++, buffer)); 
            }
        } 
        catch (IOException ex) {
            Logger.getLogger(FileTransferServer.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
