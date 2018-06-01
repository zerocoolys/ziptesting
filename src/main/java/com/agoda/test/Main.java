package com.agoda.test;

import com.agoda.test.app.CompressPipeline;
import com.agoda.test.app.DecompressPipeline;
import org.apache.commons.lang3.time.StopWatch;

import java.io.IOException;

/**
 * Main
 *
 * @author yousheng
 * @since 2018/5/28
 */
public class Main {

    public static void main(String[] args) throws IOException {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        CompressPipeline compressPipeline = new CompressPipeline("src", "output", 21);
        compressPipeline.compress();

        System.out.println("=============" + stopWatch.getTime());
        DecompressPipeline decompressPipeline = new DecompressPipeline("output", "unzip");
        decompressPipeline.decompress();
        stopWatch.stop();
        System.out.println("=============" + stopWatch.getTime());

    }
}
