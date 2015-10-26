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


//    public void send() {
//
//        File f = new File("D:\\Downloads\\couchbase-server-enterprise_2.5.1_x86_64.setup.exe"); // TODO
//        File f2 = new File("D:\\Downloads\\CentOS-6.6-i386-bin-DVD1.iso"); // TODO
//        SocketAddress address = new InetSocketAddress(getServer(), getPort());
//        Thread task = new Thread(new FileSendHandler(f, address));
//        Thread t2 = new Thread(new FileSendHandler(f2, address));
//        task.start();
//        t2.start();
//        try {
//            task.join();
//            t2.join();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }

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
        return;
    }

    public static void main(String[] args) {
        FileTransferClient client = new FileTransferClient();
        long start = System.currentTimeMillis();

        client.send();

        long end = System.currentTimeMillis();

        System.out.println("end-start = " + (end - start));
        System.out.println("end main ");
    }

}