package edu.thss.tcp;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import edu.thss.Config;
import edu.thss.DirectoryManager;

/**
 * Created by wuwe on 12/7/2015.
 */
public class FileConcatenator {


    // Key : Original file name
    // Value : Sorted parts
    public static final ConcurrentHashMap<String, TreeSet<String>> map = new ConcurrentHashMap<>();

    /**
     * This is a synchronized method
     *
     * @param fileName
     * @param totalParts
     */
    public static synchronized void processParts(String fileName, int totalParts) {
//        int part = Integer.parseInt(fileName.substring(fileName.lastIndexOf('_') + 1));
//        System.out.println("part = " + part);
        String originalFileName = fileName.substring(0, fileName.lastIndexOf('.'));
//        System.out.println("originalFileName = " + originalFileName);

        TreeSet<String> partsReceived = map.get(originalFileName);

        if (null == partsReceived) {
            partsReceived = new TreeSet<>();
            map.put(originalFileName, partsReceived);
        }
        partsReceived.add(fileName);

        if (partsReceived.size() == totalParts) {
            // we have received all parts, so it's time to concatenate them
            concat(originalFileName);
        }
    }

    private static void concat(String originalFileName) {
//        System.out.println("concatenating");
        TreeSet<String> partsReceived = map.get(originalFileName);
        try (OutputStream os = new BufferedOutputStream(new FileOutputStream(DirectoryManager.concat(Config.getDestinationDir(), originalFileName)), Config.getBufferSize())) {

            byte[] buffer = new byte[Config.getBufferSize()];

            for (String filePart : partsReceived) {
                try (InputStream is = new BufferedInputStream(new FileInputStream(DirectoryManager.concat(Config.getDestinationDir(), filePart)), Config.getBufferSize())) {
                    int actualRead;
                    while ((actualRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, actualRead);
                    }
                    os.flush();
                }
                Files.delete(Paths.get(DirectoryManager.concat(Config.getDestinationDir(), filePart)));
            }
            partsReceived.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
