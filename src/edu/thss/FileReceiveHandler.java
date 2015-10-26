package edu.thss;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileReceiveHandler implements Runnable{
    private Socket _mSocket;
    public FileReceiveHandler(Socket client) {
        this._mSocket = client;
    }


    private void constructDirectories(String str) {
        String fullFileName = Config.getDestinationDir() + File.separator + str;
        Path fullPath = Paths.get(fullFileName);
        try {
            Files.createDirectories(fullPath.getParent());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void run() {
        BufferedOutputStream output = null;
        DataInputStream input = null;
        try {
            // get file meta information
            input = new DataInputStream(_mSocket.getInputStream());
            String fileName = input.readUTF();
            long fileLength = input.readLong(); // number of total bytes
            constructDirectories(fileName);
            File file = new File(Config.getDestinationDir() + File.separator + fileName);
            output = new BufferedOutputStream(new FileOutputStream(file));
            //System.out.println("Received File Name = " + fileName);
            //System.out.println("Received File size = " + fileLength);

            // start to receive the content of the file and write them
            byte[] content = new byte[Config.getBufferSize()];
            long offset = 0;
            int numReadBytes = 0;
            while (offset < fileLength && (numReadBytes = input.read(content)) > 0) {
                output.write(content, 0, numReadBytes);
                offset += numReadBytes;
            }
            //System.out.println("numReadBytes = " + offset);
            if (offset < fileLength) {
                //numReadBytes = input.read(content);
                throw new RuntimeException("File content error at server side");
            } else {
                //System.out.println("File Receive Task has done correctly");
            }

            // tell client to close the socket now, we already receive the file successfully!!
//            BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(_mSocket.getOutputStream()));
//            bufferedWriter.write("DONE\r\n");
//            bufferedWriter.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // close the file and socket
            try {
                output.close();
                input.close();
                _mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

    }
}
