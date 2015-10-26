package edu.thss;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileTransferServer {
    private ServerSocket serverSocket;

    public FileTransferServer() {

    }


    private void startServer(int port) {
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("Server started at port :" + port);
            while (true) {
                Socket client = serverSocket.accept(); // blocked & waiting for income socket
                System.out.println("Just connected to " + client.getRemoteSocketAddress());
                ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
                executorService.submit(new FileReceiveHandler(client));

                //new Thread(new FileReceiveHandler(client)).start();
                System.out.println("while...... ");
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void run() {
        Thread startThread = new Thread(new Runnable() {
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