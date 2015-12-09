package edu.thss;


import java.util.ResourceBundle;

public class Config {

    private static ResourceBundle resourceBundle = ResourceBundle.getBundle("config");

    public static String ZipExtension = ".zip";
    public static int ZipBuffer = 1024;

    public static int getDestPort() {
        return Integer.parseInt(resourceBundle.getString("DestPort"));
    }

    public static String getDestinationHost() {
        return resourceBundle.getString("DestHost");
    }

    public static String getSourceHost() {
        return resourceBundle.getString("SrcHost");
    }

    public static String getSourceDir() {
        return resourceBundle.getString("SrcDir");
    }

    public static String getDestinationDir() {
        return resourceBundle.getString("DestDir");
    }

    public static int getBufferSize() {
        return Integer.parseInt(resourceBundle.getString("BufferSize")) * 1024;
    }

    public static int getSocketBufferSize() {
        return Integer.parseInt(resourceBundle.getString("SocketBufferSize")) * 1024;
    }

    public static int getThreadCount() {
        return Integer.parseInt(resourceBundle.getString("ThreadCount"));
    }

    public static String getZipFile() {
        return resourceBundle.getString("ZipFile");
    }

    public static int getSrcUdpPort() {
        return Integer.parseInt(resourceBundle.getString("SrcUdpPort"));
    }

    public static int getHandlerMode() {
        return Integer.parseInt(resourceBundle.getString("HandlerMode"));
    }

    public static long getMaxFileSize() {
        return Long.parseLong(resourceBundle.getString("MaxFileSize")) * 1024 * 1024;
    }

    public static boolean getUseTcpForCommand() {
        return Boolean.valueOf(resourceBundle.getString("UseTcpForCommand"));
    }
}
