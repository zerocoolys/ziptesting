package com.agoda.test.app;

import com.agoda.test.app.commons.FileType;
import com.agoda.test.compress.CompressHandler;
import com.agoda.test.compress.DeflaterCompressHandler;
import com.agoda.test.compress.OutputStreamWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * CompressPipeline
 *
 * @author yousheng
 * @since 2018/5/28
 */
public class CompressPipeline implements FileType {

    private final String input;
    private final String output;
    private final int size;

    private CompressHandler compressHandler;
    private OutputStreamWrapper wrapper;

    public CompressPipeline(String input, String output, int size) {
        this(input, output, size, new DeflaterCompressHandler(size));
    }

    public CompressPipeline(String input, String output, int size, CompressHandler compressHandler) {
        this.input = input;
        this.output = output;
        this.size = size * 1024 * 1024;
        this.compressHandler = compressHandler;
    }


    public void compress() throws IOException {
        Path file = Paths.get(input);

        wrapper = new OutputStreamWrapper(this.output, this.size);
        wrapper.init();

        Path parent = Paths.get(this.output).getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        byte[] buf = ByteBufUtils.requireArray(size);

        wrapper.writeInt(buf.length);
        doCompress(file, buf);
    }


    private void doCompress(Path file, byte[] buf) {
        if (file.toFile().isDirectory()) {
            doCompressDir(file, buf);
        } else {
            doCompressFile(file, buf);
        }
    }

    private void doCompressDir(Path file, byte[] buf) {

        System.out.println("dir = " + file.toString());
        // type bit for file
        wrapper.writeByte(BIT_DIR);
        wrapper.writeString(file.toString());

        try (Stream<Path> dirs = Files.list(file)) {
            dirs.parallel().filter(path -> !path.getFileName().toString().startsWith("."))
                    .forEach(file1 -> doCompress(file1, buf));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void doCompressFile(Path file, byte[] buf) {

//        byte[] fileData = splitFile(file, size);

        try (InputStream fileInputStream = Files.newInputStream(file)) {
            System.out.println(" compressing " + file.toString());

            wrapper.writeByte(BIT_FILE);
            wrapper.writeString(file.toString());

            int len = -1;
            byte[] outputBuf = new byte[buf.length];

            int total = fileInputStream.available();
            wrapper.writeInt(total);
            System.out.println("total = " + total);
            while ((len = fileInputStream.read(buf)) != -1) {

                int outputLen = compressHandler.compress(buf, outputBuf, 0, len);
                total -= len;
                wrapper.writeInt(outputLen);
                wrapper.writeBytes(outputBuf, outputLen);
                System.out.println("total = " + total);

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

}
