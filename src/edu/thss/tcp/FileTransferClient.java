package edu.thss.tcp;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import edu.thss.CommandEnum;
import edu.thss.Config;

public class FileTransferClient {
    private int total = 0;

    private void startServer(int port) {
        ServerSocketChannel serverSocketChannel = null;
        int count = 0;
        ExecutorService executorService = Executors.newFixedThreadPool(Config.getThreadCount());
        CompletionService join = new ExecutorCompletionService(executorService);
        try {
            serverSocketChannel = ServerSocketChannel.open().bind(new InetSocketAddress(port));
            serverSocketChannel.socket().setReceiveBufferSize(Config.getSocketBufferSize());
            System.out.println("Started at port: " + port);
            while (count < total) {
                SocketChannel client = serverSocketChannel.accept(); // blocked & waiting for income socket
                join.submit(getHandlerImpl(client.socket()), new Object());
                count++;
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
            try {
                while (!executorService.awaitTermination(1, TimeUnit.SECONDS)) {}
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            stopServer(serverSocketChannel);
        }
        System.out.println("Completed: " + (System.currentTimeMillis() - start) / 1000.0);
    }

    public void stopServer(ServerSocketChannel serverSocketChannel) {
        if (null != serverSocketChannel && serverSocketChannel.isOpen()) {
            try {
                serverSocketChannel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void init() {

        if (Config.getUseTcpForCommand()) {
            sendCommandViaTcp();
        } else {
            sendCommandViaUdp();
        }
    }

    private void sendCommandViaUdp() {
        try (DatagramSocket socket = new DatagramSocket()) {

            InetAddress address = InetAddress.getByName(Config.getSourceHost());
            DatagramPacket packet = new DatagramPacket(CommandEnum.Start$.toString().getBytes(), CommandEnum.Start$.toString().getBytes().length, address, Config.getSrcUdpPort());
            socket.send(packet);
            byte[] recvBuf = new byte[128];
            DatagramPacket recvPackt = new DatagramPacket(recvBuf, recvBuf.length);
            socket.receive(recvPackt);
            String command = new String(recvPackt.getData(), 0, recvPackt.getLength());
            if (command.startsWith(CommandEnum.Total$.toString())) {
                total = Integer.parseInt(command.substring(CommandEnum.Total$.toString().length()));
//                System.out.println("total = " + total);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startServer(Config.getDestPort());
                    }
                }).start();

                packet = new DatagramPacket(CommandEnum.Ready$.toString().getBytes(), CommandEnum.Ready$.toString().getBytes().length, address, Config.getSrcUdpPort());
                socket.send(packet);
                //System.out.println("UDP time: " + (System.currentTimeMillis() - start) / 1000.0);

            } else {
                throw new IllegalArgumentException(command);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendCommandViaTcp() {
        SocketAddress address = new InetSocketAddress(Config.getSourceHost(), Config.getSrcUdpPort());
        DataOutputStream dout = null;
        DataInputStream is = null;
        String response = null;
        try {
            Socket socket = new Socket();
            socket.setTcpNoDelay(true);
            socket.setSoLinger(true, 60);
            socket.connect(address);
            dout = new DataOutputStream(socket.getOutputStream());
            is = new DataInputStream(socket.getInputStream());


            dout.writeUTF(CommandEnum.Start$.toString());
            dout.flush();
            response = is.readUTF();
            if (response.startsWith(CommandEnum.Total$.toString())) {
                total = Integer.parseInt(response.substring(CommandEnum.Total$.toString().length()));
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        startServer(Config.getDestPort());
                    }
                }).start();
                dout.writeUTF(CommandEnum.Ready$.toString());
                dout.flush();
            } else {
                throw new IllegalArgumentException(response);
            }
            //socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Runnable getHandlerImpl(Socket socket) {
        if (Config.getHandlerMode() == 1) {
            return new edu.thss.tcp.nio.FileReceiveHandler(socket);
        } else {
            return new FileReceiveHandler(socket);
        }
    }

    public static void main(String[] args) {
        FileTransferClient server = new FileTransferClient();
        server.start = System.currentTimeMillis();
        server.init();
    }

    private long start;
}