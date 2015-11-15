package edu.thss;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * Created by wuwei on 2015/11/7.
 */
public class ZipUtil {

    public static File zip(String sourceDir, String zipFilePath) {

        File file = new File(sourceDir);
        File zipFile = new File(zipFilePath);

        try (OutputStream os = new FileOutputStream(zipFile);
             BufferedOutputStream bos = new BufferedOutputStream(os);
             ZipOutputStream zos = new ZipOutputStream(bos)) {

            String basePath = file.isDirectory() ? file.getPath() : file.getParent();
            zipFile(file, basePath, zos);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return zipFile;
    }


    private static void mkdirs(String dir) {
        File d = new File(dir);
        if (!d.exists()) d.mkdirs();
    }

    private static void zipFile(File source, String basePath, ZipOutputStream zos)
            throws IOException {
        File[] files;
        if (source.isDirectory()) {
            files = source.listFiles();
        } else {
            files = new File[1];
            files[0] = source;
        }

        InputStream is = null;
        String pathName;
        byte[] buf = new byte[Config.ZipBuffer];
        int length;
        try {
            for (File file : files) {
                if (file.isDirectory()) {
                    pathName = file.getPath().substring(basePath.length() + 1) + "/";
                    zos.putNextEntry(new ZipEntry(pathName));
                    zipFile(file, basePath, zos);
                } else {
                    pathName = file.getPath().substring(basePath.length() + 1);
                    is = new FileInputStream(file);
                    BufferedInputStream bis = new BufferedInputStream(is);
                    zos.putNextEntry(new ZipEntry(pathName));
                    while ((length = bis.read(buf)) > 0) {
                        zos.write(buf, 0, length);
                    }
                }
            }
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }


    public static void unzip(String zipFile, String outDir) {
        try (ZipInputStream zin = new ZipInputStream(new FileInputStream(zipFile))) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    mkdirs(outDir + entry.getName());
                    continue;
                }
                File file = new File(outDir + entry.getName());
                mkdirs(file.getParent());
                extractFile(zin, outDir + entry.getName());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void extractFile(ZipInputStream in, String file) throws IOException {
        byte[] buffer = new byte[Config.ZipBuffer];
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(new File(file)))) {
            int count;
            while ((count = in.read(buffer)) != -1)
                out.write(buffer, 0, count);
        }
    }


    public static void main(String[] args) {
        long start = System.currentTimeMillis();
        ZipUtil.zip("D:\\temp\\FileTransferDest\\thousands_of_tiny_files", "D:\\temp\\FileTransferDest\\thousands_of_tiny_files.zip");
        long end = System.currentTimeMillis();

        System.out.println("zip (end-start) = " + (end - start));

        start = System.currentTimeMillis();

        ZipUtil.unzip("D:\\temp\\FileTransferDest\\thousands_of_tiny_files.zip", "D:\\temp\\FileTransferDest\\unzip\\");

        end = System.currentTimeMillis();
        System.out.println("unzip (end-start) = " + (end - start));
    }
}
