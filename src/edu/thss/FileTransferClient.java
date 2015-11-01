package edu.thss;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileTransferClient {

    public void send() {
        SocketAddress address = new InetSocketAddress(Config.getDestinationHost(), Config.getPort());
        List<File> files = new ArrayList<>();
        getFiles(files, Paths.get(Config.getSourceDir()));
        ExecutorService threadPool = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
        CompletionService pool = new ExecutorCompletionService(threadPool);

        Object result = new Object();
        for (File f : files) {
            pool.submit(new FileSendHandler(f, address), result);
        }

        try {
            for (int i = 0; i < files.size(); i++) {
                Object o = pool.take().get();
                //System.out.println("o = " + o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        threadPool.shutdown();
    }

    private void getFiles(List<File> files, Path dir) {
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if(path.toFile().isDirectory()) {
                    getFiles(files, path);
                } else {
                    files.add(path.toFile());
                    //System.out.println(path.getFileName());
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        System.out.println("Start client"); 
        FileTransferClient client = new FileTransferClient();
        long start = System.currentTimeMillis();

        client.send();

        long end = System.currentTimeMillis();

        System.out.println("Execution time: " + (end - start) + "ms");
        System.out.println("Terminating");
    }

}