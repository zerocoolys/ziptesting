package com.agoda.test.compress;

import com.agoda.test.app.ByteBufUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * OutputStreamWrapper
 *
 * @author yousheng
 * @since 2018/5/31
 */
public class OutputStreamWrapper {

    private final Path outputPath;
    private final int size;
    private int id;
    private int count = 0;

    private List<String> fileNames = new ArrayList<>();
    private int counter = 0;

    private final String FILENAME = "default.data";

    private String fileName = FILENAME;
    private OutputStream outputStream;

    public OutputStreamWrapper(Path output, int size) throws IOException {
        this.size = size;
        this.id = 0;
        this.outputPath = output;

        Files.createDirectories(this.outputPath);
    }

    public boolean init() {
        try {
            Files.list(this.outputPath).filter(path -> path.getFileName().toString().startsWith(fileName)).forEach(path -> {
                try {
                    Files.delete(path);
                } catch (IOException io) {
                    io.printStackTrace();
                }
            });

            this.outputStream = Files.newOutputStream(Paths.get(outputPath.toString(), fileName));
            fileNames.add(fileName);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void writeInt(int val) {
        byte[] bytes = ByteBufUtils.convertInt(val);
        writeBytes(bytes);

    }

    public void writeString(String val) {
        writeInt(val.length());
        writeBytes(val.getBytes());
    }

    public void writeBytes(byte[] bytes) {
        writeBytes(bytes, bytes.length);
    }

    public void writeBytes(byte[] bytes, int len) {

//        System.out.println(++counter + ": len = " + len);

//        if (len < 1000) {
//        }

        try {
            if (ifOverLimitation(count, len, size)) {
                int actual = size - count;

                this.outputStream.write(bytes, 0, actual);
                this.outputStream.flush();
                this.outputStream.close();

                String newName = fileName + "." + id++;
                fileNames.add(newName);
                this.outputStream = Files.newOutputStream(Paths.get(this.outputPath.toString(), newName));
                outputStream.write(bytes, actual, len - actual);
                count = len - actual;
            } else {
                outputStream.write(bytes, 0, len);
                outputStream.flush();
                count += len;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void writeByte(byte b) {
        writeBytes(new byte[]{b});
    }

    private boolean ifOverLimitation(int current, int len, int size) {
        return current + len > size;
    }

    public String getFileName() {
        return fileName;
    }

    public OutputStreamWrapper setFileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public List<String> getFileNames() {
        return fileNames;
    }
}
