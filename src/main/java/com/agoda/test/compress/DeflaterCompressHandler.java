package com.agoda.test.compress;

import java.nio.file.Path;
import java.util.zip.Deflater;

/**
 * JavaZipCompressHandler
 *
 * @author yousheng
 * @since 2018/5/28
 */
public class DeflaterCompressHandler implements CompressHandler {


    private final int size;

    public DeflaterCompressHandler(int size) {

        this.size = size;
    }

    @Override
    public int compress(byte[] buf, byte[] output, int i, int len) {
        Deflater compresser = new Deflater();
        compresser.setInput(buf, i, len);
        compresser.finish();
        int compressedDataLength = compresser.deflate(output);
        compresser.end();
        return compressedDataLength;
    }


}
