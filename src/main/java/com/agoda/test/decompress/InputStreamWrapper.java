package com.agoda.test.decompress;


import com.agoda.test.app.ByteBufUtils;
import com.agoda.test.app.FileNameUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * InputStreamWrapper
 *
 * @author yousheng
 * @since 2018/5/31
 */
public class InputStreamWrapper {

    private String input;

    private InputStream inputStream;

    private Queue<Path> queue;

    public InputStreamWrapper(String input) {
        this.input = input;
    }

    public void init() {
        try (Stream<Path> stream = Files.list(Paths.get(input))) {

            List<Path> paths = stream
                    .filter(path -> path.getFileName().toString().startsWith("default.data"))
                    .sorted(Comparator.comparing(FileNameUtils::getFileId))
                    .collect(Collectors.toList());

            if (paths.isEmpty()) {
                throw new FileNotFoundException("no compressed data found.");
            }

            queue = new LinkedBlockingQueue<>(paths);
            setupInputStream(queue.poll());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public boolean isFinished() {
        try {
            return queue.isEmpty() && this.inputStream.available() == 0;
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
    }

    private boolean setupInputStream(Path path) {
        if (path == null) {
            return false;
        }
        try {
            this.inputStream = Files.newInputStream(path);
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }


    public int readInt() {
        byte[] tmp = readBytes(4);

        return ByteBufUtils.parseInt(tmp);

    }


    public String readString(int len) {

        byte[] bytes = readBytes(len);
        if (bytes == null || bytes.length == 0) {
            return null;
        } else {
            return new String(bytes);
        }

    }


    public byte[] readBytes(int len) {
        byte[] bytes = null;
        try {
            if (this.inputStream.available() >= len) {
                bytes = ByteBufUtils.readBytes(this.inputStream, len);
            } else {
                int actual = this.inputStream.available();
                byte[] tmp = new byte[len];
                this.inputStream.read(tmp, 0, actual);

                if (!setupInputStream(queue.poll())) {
                    throw new Exception("compressed data missing.");
                }

                this.inputStream.read(tmp, actual, len - actual);
                bytes = tmp;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }


        return bytes;
    }

}
