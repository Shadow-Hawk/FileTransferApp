/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.thss.udp;

import edu.thss.Config;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Stephan
 */
public class FileTransferClient {
    public static void main(String args[]) {
        FileTransferClient.run(Config.getDestinationHost(), Config.getPort(), Config.getSourceDir()); 
    }
    public static void run(String destinationHost, int port, String directory) {
        try {
            InetSocketAddress serverAddress = new InetSocketAddress(destinationHost, port);
            ExecutorService exService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            FileVisitor<Path> fileVisitor = new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    File f = file.toFile();
                    if (f.isFile()) { 
                        exService.submit(new FileSendHandler(f, serverAddress));
                    }
                    return FileVisitResult.CONTINUE;
                }
            };
            Files.walkFileTree(Paths.get(directory), fileVisitor);
        } 
        catch (IOException ex) {
            Logger.getLogger(FileTransferClient.class.getName()).log(Level.SEVERE, null, ex); 
        }
    }
}
