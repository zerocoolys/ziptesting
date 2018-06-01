package com.agoda.test.decompress;

import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

/**
 * InflaterDecompressHandler
 *
 * @author yousheng
 * @since 2018/5/28
 */
public class InflaterDecompressHandler implements DecompressHandler {
    @Override
    public int decompress(byte[] buf, byte[] result, int i, int c) {
        Inflater inflater = new Inflater();

        inflater.setInput(buf, 0, c);
        try {
            int length = inflater.inflate(result);
            inflater.end();

            return length;
        } catch (DataFormatException e) {
            e.printStackTrace();
        }
        return -1;
    }
}
