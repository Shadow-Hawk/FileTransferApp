package edu.thss.tcp;

import edu.thss.Config;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileTransferServer {
    private ServerSocketChannel serverSocketChannel;

    public FileTransferServer() {

    }


    private void startServer(int port) {
        try {
            serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(port)); 
            System.out.println("Server started at port: " + port);
            while (true) {
                SocketChannel client = serverSocketChannel.accept(); // blocked & waiting for income socket
                System.out.println("Just connected to " + client.getRemoteAddress());
                ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                executorService.submit(new FileReceiveHandler(client.socket()));
                System.out.println("Receiving information...");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void run() {
        Thread startThread = new Thread(new Runnable() {
            @Override
            public void run() {
                startServer(Config.getPort());
            }
        });
        startThread.start();
    }

    public static void main(String[] args) {
        FileTransferServer server = new FileTransferServer();
        server.run();
    }
}