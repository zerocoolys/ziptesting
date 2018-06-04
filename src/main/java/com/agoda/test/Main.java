package com.agoda.test;

import com.agoda.test.app.CompressPipeline;
import com.agoda.test.app.DecompressPipeline;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.time.StopWatch;

import java.io.IOException;
import java.util.Date;

/**
 * Main
 *
 * @author yousheng
 * @since 2018/5/28
 */
public class Main {

    public static void main(String[] args) throws IOException, ParseException {

        Options options = new Options()
                .addRequiredOption("i", "input", true, "input path for source data")
                .addRequiredOption("o", "output", true, "output path of the compressed data")
                .addOption("s", true, "the max compressed file size")
                .addRequiredOption("d", "unzip", true, "output path of the decompressed data");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);

        String data = cmd.getOptionValue("i");
        String compressedPath = cmd.getOptionValue("o");
        String decompressedPath = cmd.getOptionValue("d");

        int size = Integer.parseInt(cmd.getOptionValue("s"));

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        CompressPipeline compressPipeline = new CompressPipeline(data, compressedPath, size);
        compressPipeline.compress();


        long compressTime = stopWatch.getTime();
        System.out.println("compress time :" + compressTime);
        DecompressPipeline decompressPipeline = new DecompressPipeline(compressedPath, decompressedPath);
        decompressPipeline.decompress();
        stopWatch.stop();
        System.out.println("decompress time : " + (stopWatch.getTime() - compressTime));

    }
}
