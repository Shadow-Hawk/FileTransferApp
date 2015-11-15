package edu.thss.tcp;

import edu.thss.Config;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

public class FileSendHandler implements Runnable {


    private File selectedFile;
    private Socket mSocket;
    private SocketAddress address;

    public FileSendHandler(File file, SocketAddress address) {
        this.address = address;
        this.selectedFile = file;
        mSocket = new Socket();
    }

    @Override
    public void run() {
        DataOutputStream dout = null;
        DataInputStream is = null;

        try {
            //System.out.println("Being....");

            // Get the size of the file
            long length = selectedFile.length();
            mSocket.setSendBufferSize(Config.getSocketBufferSize());
            mSocket.setReceiveBufferSize(Config.getSocketBufferSize());
            mSocket.setTcpNoDelay(true);
            mSocket.setSoLinger(true, 60);
            mSocket.connect(address);

            dout = new DataOutputStream(mSocket.getOutputStream());
            // now we start to send the file meta info.
            dout.writeUTF(selectedFile.getAbsolutePath().substring(Config.getSourceDir().length()).replaceAll("\\\\", "/"));
            dout.writeLong(length);
            dout.flush();

            is = new DataInputStream(new FileInputStream(selectedFile));
            byte[] bytes = new byte[Config.getBufferSize()];

            // Read in the bytes
            long offset = 0;
            int numRead;
            while (offset < length && (numRead = is.read(bytes, 0, bytes.length)) >= 0) {
                offset += numRead;
                dout.write(bytes, 0, numRead);
                dout.flush();
            }
            //System.out.println("total send bytes = " + offset);
            // Ensure all the bytes have been read in
            if (offset < length) {
                throw new IOException("Could not completely transfer file " + selectedFile.getName());
            }
            mSocket.shutdownOutput();

            // receive the file transfer successfully message from connection

//            BufferedInputStream streamReader = new BufferedInputStream(mSocket.getInputStream());
//            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(streamReader));
//            String doneMsg = bufferedReader.readLine();
//            if (!"DONE".equals(doneMsg)) {
//                throw new RuntimeException("Done signal not received!!!");
//            }


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