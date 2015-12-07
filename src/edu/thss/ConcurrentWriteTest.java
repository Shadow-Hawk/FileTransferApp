
package edu.thss;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This test class is used to experiment with writing a file concurrently by 
 * many threads. *NOT WORKING*
 * 
 * Problems: File writers delete the previous content. Appending the content 
 * does solve this, but then we have to manage the ordering of the input
 * 
 * @author Stephan
 */
public class ConcurrentWriteTest {
    private static final String dest = "D:\\tmp\\ABC.txt"; 
    private static final int numberOfThreads = 8; 
    private static final long filesize = 256; 
    public static void main(String args[]) {
        ConcurrentWriteTest cwt = new ConcurrentWriteTest(); 
        ExecutorService exService = Executors.newFixedThreadPool(numberOfThreads); 
        ByteArrayInputStream istream = new ByteArrayInputStream(
                FileContentGenerator
                        .generateABC(filesize)
                        .getBytes(Charset.defaultCharset()));
        for(int i = 0; i < numberOfThreads; i++) {
            int length = (int) filesize / numberOfThreads; 
            int offset = i * length; 
            exService.submit(new FilePartWriter(istream, dest, offset, length)); 
            System.out.println("Just started writer (" + offset + "," + (offset + length) + ")"); 
        }
        exService.shutdown();
        try {
            exService.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
        } 
        catch (InterruptedException ex) {
            Logger.getLogger(ConcurrentWriteTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Finish");
    }
    private static class FilePartWriter implements Runnable {
        private int offset; // Where to begin
        private int length; // Number of bytes to write
        private InputStream istream; 
        private String destination; 
        
        public FilePartWriter(InputStream istream, String destination, int offset, int length) {
            this.offset = offset; 
            this.length = length; 
            this.istream = istream; 
            this.destination = destination; 
        }
        
        @Override
        public synchronized void run() {
            try (RandomAccessFile fout = new RandomAccessFile(this.destination, "rw")) { 
                int readBytes;
                byte[] buffer = new byte[this.length];
                istream.skip(this.offset); 
                if ((readBytes = this.istream.read(buffer, 0, this.length)) > 0) {
                    fout.write(buffer, 0, readBytes); 
                }
            }
            catch (IOException ex) {
                Logger.getLogger(ConcurrentWriteTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        }    
    }
    private static class FileContentGenerator {
        public static final String generateABC(long size) {
            StringBuilder res = new StringBuilder(); 
            for (long i = 0; i < size; i++) {
                char b = (char) ('A' + i % ('Z' - 'A')); 
                res.append(b); 
            }
            return res.toString(); 
        }
    }
}
