package com.agoda.test.app;

import com.agoda.test.app.commons.FileType;
import com.agoda.test.decompress.DecompressHandler;
import com.agoda.test.decompress.InflaterDecompressHandler;
import com.agoda.test.decompress.InputStreamWrapper;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * DecompressPipeline
 *
 * @author yousheng
 * @since 2018/5/28
 */
public class DecompressPipeline implements FileType {

    private final String input;
    private final String output;
    private DecompressHandler decompressHandler = new InflaterDecompressHandler();

    public DecompressPipeline(String input, String output) {

        this.input = input;
        this.output = output;
    }

    public void decompress() throws IOException {

        if (!Files.exists(Paths.get(this.input, "default.data"))) {
            throw new FileNotFoundException("data file not exists.");
        }

        InputStreamWrapper wrapper = new InputStreamWrapper(input);
        wrapper.init();

//        int buf_size = ByteBufUtils.readInt(wrapper.getInputStream());

        int buf_size = wrapper.readInt();
        while (true) {

            if (wrapper.isFinished()) {
                break;
            }
            byte[] type = wrapper.readBytes(1);
            if (type[0] == FileType.BIT_FILE) {
                int len = wrapper.readInt();

                String fileName = wrapper.readString(len);
                System.out.println("fileName = " + fileName);
                Path tmp = Paths.get(output, fileName);

                Files.createDirectories(tmp.getParent());

                if (!Files.exists(tmp)) {
                    Files.createFile(tmp);
                }
                int total = wrapper.readInt();
                System.out.println("total = " + total);

                OutputStream outputStream = Files.newOutputStream(tmp, StandardOpenOption.APPEND);

                while (total != 0) {
                    len = wrapper.readInt();

                    byte[] val = wrapper.readBytes(len);
                    byte[] result = new byte[buf_size];
                    int length = decompressHandler.decompress(val, result, 0, val.length);

                    System.out.println("before decompress: " + len + " , after decompress: " + length);
                    outputStream.write(result, 0, length);
                    total -= length;
                    System.out.println("total = " + total);
                }

                outputStream.flush();
                outputStream.close();
            } else {
                int len = wrapper.readInt();

                String dirName = wrapper.readString(len);
                System.out.println("dirName = " + dirName);
                Path tmp = Paths.get(output, dirName);

                Files.createDirectories(tmp);
            }

        }
    }


    private boolean requireData(InputStream inputStream, int expected) {
        try {
            return inputStream.available() > expected;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }


}
