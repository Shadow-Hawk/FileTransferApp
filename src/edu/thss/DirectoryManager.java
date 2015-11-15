package edu.thss;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Create directories
 * @author Stephan
 */
public final class DirectoryManager {
    /**
     * Concats two string paths with a file separator
     * @param p1 first path
     * @param p2 second path
     * @return concatted path
     */
    public static final String concat(String p1, String p2) {
        return p1 + File.separator + p2; 
    }
    
    public static final void constructDirectories(String str) {
        String fullFileName = concat(Config.getDestinationDir(), str);
        Path fullPath = Paths.get(fullFileName);
        try {
            Files.createDirectories(fullPath.getParent());
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public static void cleanTempFile(String folder, String fileName) {
        try {

            Files.deleteIfExists(Paths.get(folder + File.separator + fileName));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
