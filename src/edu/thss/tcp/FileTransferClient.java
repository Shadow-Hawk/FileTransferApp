package edu.thss.tcp;

import edu.thss.Config;
import edu.thss.DirectoryManager;
import edu.thss.ZipUtil;

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
import java.util.concurrent.*;

public class FileTransferClient {

    public void send() {
        List<File> files = new ArrayList<>();
        getFiles(files, Paths.get(Config.getSourceDir()));
        SocketAddress address = new InetSocketAddress(Config.getDestinationHost(), Config.getPort());
        ExecutorService threadPool = Executors.newFixedThreadPool(Config.getThreadCount());
        CompletionService pool = new ExecutorCompletionService(threadPool);

        Object result = new Object();
        for (File f : files) {
            pool.submit(new FileSendHandler(f, address), result);
        }

        threadPool.shutdown();
        try {
            while (!threadPool.awaitTermination(2, TimeUnit.SECONDS)) {
                //System.out.println("running");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("end");
    }

    private void getFiles(List<File> files, Path dir) {
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if(path.toFile().isDirectory()) {
                    //if (path.endsWith("huge_file") || path.endsWith("several_normal_files")) continue;
                    //if (path.endsWith("huge_file")) continue;
                    if (path.endsWith(Config.getZipFile())) {
                        ZipUtil.zip(path.toString(), path.toString() + Config.ZipExtension);
                        files.add(Paths.get(path.toString() + Config.ZipExtension).toFile());
                        continue;
                    }
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
        DirectoryManager.cleanTempFile(Config.getSourceDir(), Config.getZipFile() + Config.ZipExtension);
    }

}