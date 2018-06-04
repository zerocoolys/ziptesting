package com.agoda.test.decompress;

/**
 * @author yousheng
 */
public interface DecompressHandler {
    /**
     * thread safe
     *
     * @param data the source compressed data
     * @param result the output data array
     * @param i
     * @param c
     * @return
     */
    int decompress(byte[] data, byte[] result, int i, int c);

}
