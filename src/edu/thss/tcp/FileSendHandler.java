package edu.thss.tcp;

import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketAddress;

import edu.thss.Config;

public class FileSendHandler implements Runnable {

    private FileSplit selectedFile;
    private Socket mSocket;
    private SocketAddress address;

    public FileSendHandler(FileSplit file, SocketAddress address) {
        this.address = address;
        this.selectedFile = file;
    }

    @Override
    public void run() {
        DataOutputStream dout = null;
        InputStream is = null;

        try {
            int retryCount = 0;
            while (true) {
                try {
                    mSocket = new Socket();
                    mSocket.setSendBufferSize(Config.getSocketBufferSize());
                    mSocket.setReceiveBufferSize(Config.getSocketBufferSize());
                    mSocket.setTcpNoDelay(true);
                    mSocket.setSoLinger(true, 60);
                    mSocket.connect(address, 1000);
                    break;
                } catch (ConnectException e) {
                    retryCount++;
                    System.out.println("retryCount = " + retryCount + "; Thread Name= " + Thread.currentThread().getName());
                    if (retryCount > 10) {
                        throw  e;
                    }
                }
            }
            dout = new DataOutputStream(mSocket.getOutputStream());
            // Get the size of the file or part of file
            long length = selectedFile.getLimit() - selectedFile.getPosition();
            // now we start to send the file meta info.
            dout.writeByte(selectedFile.getTotal());
            dout.writeUTF(selectedFile.getName());
            dout.writeLong(length);
            dout.flush();

            is = new BufferedInputStream(new FileInputStream(selectedFile.getFile()), Config.getBufferSize());
            byte[] bytes = new byte[Config.getBufferSize()];

            // Read in the bytes
            long offset = selectedFile.getPosition();
            int readLength = selectedFile.getLimit() - offset > bytes.length ? bytes.length : (int) (selectedFile.getLimit() - offset);
            is.skip(selectedFile.getPosition());
            int numRead;
            while (offset < selectedFile.getLimit() && (numRead = is.read(bytes, 0, readLength)) >= 0) {
                offset += numRead;
                readLength = selectedFile.getLimit() - offset > bytes.length ? bytes.length : (int) (selectedFile.getLimit() - offset);
                dout.write(bytes, 0, numRead);
                dout.flush();
            }
            //System.out.println("total send bytes = " + offset);
            // Ensure all the bytes have been read in
            if (offset < selectedFile.getLimit()) {
                throw new IOException("Could not completely transfer file " + selectedFile.getName());
            }
            mSocket.shutdownOutput();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (dout != null) dout.close();
                if (is != null) is.close();
                if (!mSocket.isClosed())
                    mSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            //System.out.println("close it now......");
        }

    }
}