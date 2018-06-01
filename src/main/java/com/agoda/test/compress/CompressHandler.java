package com.agoda.test.compress;

import java.nio.file.Path;

/**
 * CompressHandler
 *
 * @author yousheng
 * @since 2018/5/28
 */
public interface CompressHandler {

    byte[] compress(Path path);


    int compress(byte[] buf, byte[] output, int i, int len);
}
