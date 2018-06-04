package com.agoda.test.utils;

import com.agoda.test.app.ByteBufUtils;
import com.agoda.test.app.FileNameUtils;
import com.agoda.test.app.commons.ByteType;
import com.agoda.test.compress.CompressHandler;
import com.agoda.test.compress.OutputStreamWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;

/**
 * CompressUtils
 *
 * @author yousheng
 * @since 2018/6/4
 */
public class CompressUtils implements ByteType {

    public static void doCompressFile(OutputStreamWrapper wrapper, CompressHandler compressHandler, Path parent, Path file) {
        try (InputStream fileInputStream = Files.newInputStream(file)) {

            wrapper.writeByte(BIT_FILE);
            writeFileName(wrapper, parent, file);
            int len = -1;
            byte[] buf = new byte[ByteBufUtils.requireArray()];
            byte[] outputBuf = new byte[buf.length * 2];

            long total = Files.size(file);

            if (total <= Integer.MAX_VALUE) {
                wrapper.writeInt((int) total);
            } else {
                wrapper.writeLong(total);
            }

            while ((len = fileInputStream.read(buf)) != -1) {
                int outputLen = compressHandler.compress(buf, outputBuf, 0, len);
                total -= len;
                wrapper.writeInt(outputLen);
                wrapper.writeBytes(outputBuf, outputLen);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }


    /**
     * write file name base on the parent path,
     * if the give path is absolute , should be transformed based on the parent path.
     *
     * @param wrapper
     * @param file
     */

    public static void writeFileName(OutputStreamWrapper wrapper, Path parent, Path file) {
        if (file.isAbsolute()) {
            wrapper.writeString(file.toString().substring(parent.toString().length() + 1));
        } else {
            wrapper.writeString(file.toString());
        }
    }


    public static void merge(OutputStreamWrapper wrapper, Path output, List<String> ids) {

        ids.stream().map(s -> Paths.get(output.toString(), s))
                .sorted(Comparator.comparing(FileNameUtils::getFileId))
                .forEach(path -> {

                    try (InputStream is = Files.newInputStream(path)) {
                        byte[] buf = new byte[ByteBufUtils.requireArray()];
                        int c = -1;
                        while ((c = is.read(buf)) != -1) {
                            wrapper.writeBytes(buf, c);
                        }

                        Files.deleteIfExists(path);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

    }

}
