package edu.thss.tcp.nio;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import edu.thss.Config;
import edu.thss.DirectoryManager;
import edu.thss.ZipUtil;
import edu.thss.tcp.FileConcatenator;

/**
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

            assert (socketChannel != null);

            // Receiving meta data and prepare receiving
            int numberOfParts = din.readByte();
            String fileName = din.readUTF();
            String fullFileName = DirectoryManager.concat(Config.getDestinationDir(), fileName);
            long fileSize = din.readLong(),
                    expectedBytes = fileSize,
                    transferredBytes;

            DirectoryManager.constructDirectories(fileName);

            try (FileChannel fileChannel = new FileOutputStream(fullFileName).getChannel()) {
                // Receive data
                do {
                    transferredBytes = fileChannel.transferFrom(socketChannel, fileSize - expectedBytes, expectedBytes);
                    expectedBytes -= transferredBytes;
                } while (expectedBytes != 0 && transferredBytes != 0);
            }

            if (fileName.endsWith(Config.getZipFile() + Config.ZipExtension)) {
                ZipUtil.unzip(fullFileName, Config.getDestinationDir() + File.separator + fileName.substring(0, fileName.lastIndexOf(Config.ZipExtension)) + File.separator);
                DirectoryManager.cleanTempFile(Config.getDestinationDir(), fileName);
            } else if (numberOfParts > 1 && fileName.contains(".part_")) {
                FileConcatenator.processParts(fileName, numberOfParts);
            }
        } catch (IOException ex) {
            Logger.getLogger(FileReceiveHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
