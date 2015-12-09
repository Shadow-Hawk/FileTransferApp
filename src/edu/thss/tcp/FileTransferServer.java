package edu.thss.tcp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import edu.thss.CommandEnum;
import edu.thss.Config;
import edu.thss.DirectoryManager;
import edu.thss.ZipUtil;

public class FileTransferServer {

    private void listen() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<List<FileSplit>> future = executor.submit(new Callable<List<FileSplit>>() {
            @Override
            public List<FileSplit> call() throws Exception {
                List<FileSplit> files = new ArrayList<>();
                getFiles(files, Paths.get(Config.getSourceDir()));
                return files;
            }
        });
        List<FileSplit> files = null;
        while (true) {
            try (DatagramSocket server = new DatagramSocket(Config.getSrcUdpPort())) {
                byte[] recvBuf = new byte[128];
                DatagramPacket recvPacket = new DatagramPacket(recvBuf, recvBuf.length);
                server.receive(recvPacket);

                String command = new String(recvPacket.getData(), 0, recvPacket.getLength());
//                System.out.println("command = " + command);
                if (CommandEnum.Start$.toString().equalsIgnoreCase(command)) {
                    int port = recvPacket.getPort();
                    InetAddress addr = recvPacket.getAddress();
                    files = future.get(5, TimeUnit.SECONDS);

                    String reply = CommandEnum.Total$.toString() + files.size();
                    byte[] replyBuf = reply.getBytes();
                    DatagramPacket replyPacket = new DatagramPacket(replyBuf, replyBuf.length, addr, port);
                    server.send(replyPacket);
                    executor.shutdown();
                } else if (CommandEnum.Ready$.toString().equalsIgnoreCase(command)) {
                    send(files);
                    break;
                }
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
    }

    public void send(List<FileSplit> files) {

        SocketAddress address = new InetSocketAddress(Config.getDestinationHost(), Config.getDestPort());
        ExecutorService threadPool = Executors.newFixedThreadPool(Config.getThreadCount());
        CompletionService pool = new ExecutorCompletionService(threadPool);

        Object result = new Object();
        for (FileSplit f : files) {
            pool.submit(getHandlerImpl(f, address), result);
        }

        threadPool.shutdown();

        try {
            while (!threadPool.awaitTermination(2, TimeUnit.SECONDS)) {
                //System.out.println("running");
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
//            System.out.println("Cleaning");
            DirectoryManager.cleanTempFile(Config.getSourceDir(), Config.getZipFile() + Config.ZipExtension);
        }
//        System.out.println("end");
    }

    private void getFiles(List<FileSplit> files, Path dir) {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path path : stream) {
                if (path.toFile().isDirectory()) {
                    if (path.endsWith(Config.getZipFile())) {
                        ZipUtil.zip(path.toString(), path.toString() + Config.ZipExtension);
                        files.addAll(FileSplit.calculateParts(Paths.get(path.toString() + Config.ZipExtension).toFile()));
                        continue;
                    }
                    getFiles(files, path);
                } else {
                    List<FileSplit> subList = FileSplit.calculateParts(path.toFile());
                    if (subList.size() > 1) {
                        files.addAll(0, subList);
                    } else {
                        files.addAll(subList);
                        //System.out.println(path.getFileName());
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Runnable getHandlerImpl(FileSplit file, SocketAddress address) {
        if (Config.getHandlerMode() == 1) {
            return new edu.thss.tcp.nio.FileSendHandler(file, address);
        } else {
            return new FileSendHandler(file, address);
        }
    }

    public static void main(String[] args) {
        FileTransferServer client = new FileTransferServer();
        client.listen();
    }

}