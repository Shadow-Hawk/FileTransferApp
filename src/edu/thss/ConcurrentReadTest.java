package edu.thss;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wuwe on 12/4/2015.
 */
public class ConcurrentReadTest {

    public static String Src = "D:\\tmp\\test\\FileTransferApp\\dest\\1.pdf";
    public static String Dest = "D:\\tmp\\test\\FileTransferApp\\dest\\OK.pdf";

    public static long size = 1024 * 1024 * 10; // 10 MB

    public void read() {

        File sourceFile = new File(Src);

        List<Runnable> runnables = new ArrayList<>();


        long position = 0;
        long offset = size;

        for (int i = 1; position < sourceFile.length(); i++) {
            runnables.add(new FilePart(sourceFile, position, offset, i));
            position += size;
            offset = offset + size > sourceFile.length() ? sourceFile.length() : offset + size;
        }

        for (Runnable r : runnables) {
            new Thread(r).start();
        }

    }

    public void combine() {

        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(Dest))) {

            byte[] buffer = new byte[1024 * 1024];

            for (int i = 1; i <= 3; i++) {

                try (InputStream in = new BufferedInputStream(new FileInputStream(Dest + ".part" + i))) {
                    int actualRead;
                    long offset = 0;
                    while ((actualRead = in.read(buffer)) != -1) {
                        offset += actualRead;
                        out.write(buffer, 0, actualRead);
                    }
                    out.flush();
                }
                Files.delete(Paths.get(Dest + ".part" + i));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        ConcurrentReadTest app = new ConcurrentReadTest();
        app.read();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        app.combine();
    }

    class FilePart implements Runnable {

        private long position;
        private long offset;
        private File file;

        private int id;

        public FilePart(File file, long position, long offset, int id) {
            this.offset = offset;
            this.position = position;
            this.file = file;
            this.id = id;
        }


        @Override
        public void run() {
            try (InputStream in = new BufferedInputStream(new FileInputStream(file));
                 OutputStream out = new BufferedOutputStream(new FileOutputStream(Dest + ".part" + id))) {
                byte[] buffer = new byte[1024 * 1024];
                int actualRead;
                int readLength = offset - position > buffer.length ? buffer.length : (int) (offset - position);

                in.skip(position);

                while (position < offset && (actualRead = in.read(buffer, 0, readLength)) != -1) {
                    position += actualRead;
                    readLength = offset - position > buffer.length ? buffer.length : (int) (offset - position);
                    out.write(buffer, 0, actualRead);
                }
                out.flush();

                if (position < offset) System.err.println("position < offset");

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
