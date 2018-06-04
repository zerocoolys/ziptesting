package com.agoda.test.app;

import com.agoda.test.app.commons.ByteType;
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
public class DecompressPipeline implements ByteType {

    public static final String DEFAULT_FILENAME = "default.data";
    private final String input;
    private final String output;
    private DecompressHandler decompressHandler = new InflaterDecompressHandler();

    public DecompressPipeline(String input, String output) {

        this.input = input;
        this.output = output;
    }

    public void decompress() throws IOException {

        if (!Files.exists(Paths.get(this.input, DEFAULT_FILENAME))) {
            throw new FileNotFoundException("data file not exists.");
        }

        InputStreamWrapper wrapper = new InputStreamWrapper(input);
        wrapper.init();

        int bufSize = (int) wrapper.readVal();
        while (!wrapper.isFinished()) {

            byte[] type = wrapper.readBytes(1);
            if (type[0] == BIT_FILE) {
                int len = (int) wrapper.readVal();

                String fileName = wrapper.readString(len);
//                System.out.println("fileName = " + fileName);
                Path tmp = Paths.get(output, fileName);

                Files.createDirectories(tmp.getParent());

                if (!Files.exists(tmp)) {
                    Files.createFile(tmp);
                }
                long total = wrapper.readVal();
//                System.out.println("total = " + total);

                OutputStream outputStream = Files.newOutputStream(tmp, StandardOpenOption.CREATE);
                while (total != 0) {
                    len = (int) wrapper.readVal();

                    byte[] val = wrapper.readBytes(len);
                    byte[] result = new byte[bufSize];
                    int length = decompressHandler.decompress(val, result, 0, val.length);

//                    System.out.println("before decompress: " + val.length + " , after decompress: " + length + ", delta : " + (length - len));
                    outputStream.write(result, 0, length);
                    total -= length;
//                    System.out.println("total = " + total);
                }

                outputStream.flush();
                outputStream.close();
            } else {
                int len = (int) wrapper.readVal();

                String dirName = wrapper.readString(len);
//                System.out.println("dirName = " + dirName);
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
