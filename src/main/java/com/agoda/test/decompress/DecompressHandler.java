package com.agoda.test.decompress;

public interface DecompressHandler {
    /**
     * thread safe
     * @param buf
     * @param result
     * @param i
     * @param c
     * @return
     */
    int decompress(byte[] buf, byte[] result, int i, int c);

//    public void decompress(byte[] input, byte)
}
