package edu.thss;


import java.util.ResourceBundle;

public class Config {

    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("config");

    public static int getPort() {
        return Integer.parseInt(resourceBundle.getString("port"));
    }

    public static String getDestinationHost() {
        return resourceBundle.getString("DestHost");
    }

    public static String getSourceDir() {
        return  resourceBundle.getString("SrcDir");
    }

    public static String getDestinationDir() {
        return resourceBundle.getString("DestDir");
    }

    public static int getBufferSize() {
        return Integer.parseInt(resourceBundle.getString("BufferSize"));
    }
}
