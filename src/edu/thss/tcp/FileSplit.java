package edu.thss.tcp;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import edu.thss.Config;

/**
 * Created by wuwe on 12/5/2015.
 */
public class FileSplit {

    public static final String FILE_PART_Suffix = ".part_%d";

    private final File file;

    private final long position;

    private final long limit;

    private int total;

    private final byte id;

    private String name;

    private FileSplit(File file, long position, long limit, byte id) {
        this.file = file;
        this.position = position;
        this.limit = limit;
        this.id = id;

    }

    public static List<FileSplit> calculateParts(File file) {
        long MAX_FILE_SIZE = Config.getMaxFileSize();
        List<FileSplit> list = new ArrayList<>();
        if (file.length() > MAX_FILE_SIZE) {
            long position = 0;
            long limit = MAX_FILE_SIZE;

            for (byte i = 1; position < file.length(); i++) {
                list.add(new FileSplit(file, position, limit, i));
                position += MAX_FILE_SIZE;
                limit = limit + MAX_FILE_SIZE > file.length() ? file.length() : limit + MAX_FILE_SIZE;
            }

        } else {
            list.add(new FileSplit(file, 0, file.length(), (byte) 0));
        }

        for (FileSplit fileSplit : list) {
            fileSplit.setTotalAndPartName(list.size());
        }

        return list;
    }

    private void setTotalAndPartName(int total) {
        this.total = total;
        String fileName = file.getAbsolutePath().substring(Config.getSourceDir().length()).replaceAll("\\\\", "/");
        if (total > 1) {
            this.name = fileName + String.format(FILE_PART_Suffix, id);
        } else {
            this.name = fileName;
        }
    }

    public File getFile() {
        return file;
    }

    public long getPosition() {
        return position;
    }

    public long getLimit() {
        return limit;
    }

    public byte getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getTotal() {
        return total;
    }


}
