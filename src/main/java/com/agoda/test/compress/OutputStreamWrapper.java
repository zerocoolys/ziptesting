package com.agoda.test.compress;

import com.agoda.test.app.ByteBufUtils;
import com.agoda.test.app.commons.ByteType;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * OutputStreamWrapper
 *
 * @author yousheng
 * @since 2018/5/31
 */
public class OutputStreamWrapper implements ByteType {

    private final Path outputPath;
    private final int size;
    private final String FILENAME = "default.data";
    private int id;
    private int count = 0;
    private List<String> fileNames = new ArrayList<>();
    private String fileName = FILENAME;
    private OutputStream outputStream;

    public OutputStreamWrapper(Path output, int size) {
        this.size = size;
        this.id = 0;
        this.outputPath = output;
    }

    public boolean init() {
        try {
            this.outputStream = Files.newOutputStream(Paths.get(outputPath.toString(), fileName), StandardOpenOption.CREATE);
            fileNames.add(fileName);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void writeInt(int val) {
        byte[] bytes = ByteBufUtils.convertInt(val);
        writeByte(BIT_INT);
        writeBytes(bytes);

    }

    public void writeLong(long val) {
        byte[] bytes = ByteBufUtils.convertLong(val);
        writeByte(BIT_LONG);
        writeBytes(bytes);
    }

    public void writeString(String val) {
//        System.out.println("val = " + val);
        writeInt(val.length());
        writeBytes(val.getBytes());
    }

    public void writeBytes(byte[] bytes) {
        writeBytes(bytes, bytes.length);
    }

    public void writeBytes(byte[] bytes, int len) {

        try {
            if (ifOverLimitation(count, len, size)) {
                int actual = size - count;

                this.outputStream.write(bytes, 0, actual);
                this.outputStream.flush();
                this.outputStream.close();

                String newName = fileName + "." + id++;
                fileNames.add(newName);
                this.outputStream = Files.newOutputStream(Paths.get(this.outputPath.toString(), newName), StandardOpenOption.CREATE);
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

    public void close() {
        if (this.outputStream != null) {
            try {
                this.outputStream.flush();
                this.outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
