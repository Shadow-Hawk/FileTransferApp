package edu.thss.tcp;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;

import edu.thss.Config;
import edu.thss.DirectoryManager;
import edu.thss.ZipUtil;

public class FileReceiveHandler implements Runnable {
    private Socket _mSocket;

    public FileReceiveHandler(Socket client) {
        this._mSocket = client;
    }

    @Override
    public void run() {
        String fileName = "";
        BufferedOutputStream output = null;
        DataInputStream input = null;
        try {
            //System.out.println("_mSocket getReceiveBufferSize= " + _mSocket.getReceiveBufferSize());
            // get file meta information
            input = new DataInputStream(_mSocket.getInputStream());
            fileName = input.readUTF();
            long fileLength = input.readLong(); // number of total bytes
            DirectoryManager.constructDirectories(fileName);
            File file = new File(Config.getDestinationDir() + File.separator + fileName);
            output = new BufferedOutputStream(new FileOutputStream(file), Config.getBufferSize());
            //System.out.println("Received File Name = " + fileName);
            //System.out.println("Received File size = " + fileLength);

            // start to receive the content of the file and write them
            byte[] content = new byte[Config.getBufferSize()];
            long offset = 0;
            int numReadBytes;
            while (offset < fileLength && (numReadBytes = input.read(content)) > 0) {
                output.write(content, 0, numReadBytes);
                offset += numReadBytes;
            }

            //System.out.println("numReadBytes = " + offset);
            if (offset < fileLength) {
                //numReadBytes = input.read(content);
                throw new RuntimeException("File content error at server side");
            } else {
                output.flush();
            }

            if (fileName.endsWith(Config.getZipFile() + Config.ZipExtension)) {
                ZipUtil.unzip(file.getPath(), Config.getDestinationDir() + File.separator + fileName.substring(0, fileName.lastIndexOf(Config.ZipExtension)) + File.separator);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            // close the file and socket
            try {
                if (output != null) output.close();
                if (input != null) input.close();
                _mSocket.close();
                if (fileName.endsWith(Config.getZipFile() + Config.ZipExtension)) {
                    DirectoryManager.cleanTempFile(Config.getDestinationDir(), fileName);
                }
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }

    }
}
