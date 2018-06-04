package com.agoda.test.compress;

import java.nio.file.Path;

/**
 * CompressHandler
 *
 * @author yousheng
 * @since 2018/5/28
 */
public interface CompressHandler {

    /**
     *
     * @param data source data
     * @param output output array of compressed data
     * @param i the start offset of source data
     * @param len the length of source data
     *
     * @return the actual data length after compressed
     */
    int compress(byte[] data, byte[] output, int i, int len);
}
