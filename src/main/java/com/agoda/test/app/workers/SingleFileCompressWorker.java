package com.agoda.test.app.workers;

import com.agoda.test.app.FileNameUtils;
import com.agoda.test.compress.OutputStreamWrapper;

import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

/**
 * SingleFileCompressWorker
 *
 * @author yousheng
 * @since 2018/6/4
 */
public class SingleFileCompressWorker implements Callable<List<String>> {

    private final int size;
    private final Path output;
    private final Path file;
    private final String id = UUID.randomUUID().toString();

    public SingleFileCompressWorker(int size, Path output, Path file) {
        this.size = size;
        this.output = output;
        this.file = file;
    }

    @Override
    public List<String> call() {

        OutputStreamWrapper outputStreamWrapper = new OutputStreamWrapper(this.output, this.size);
        outputStreamWrapper.setFileName(id);
        outputStreamWrapper.init();

//        CompressUtils.doCompressFile(outputStreamWrapper, file);

        if (outputStreamWrapper.getFileNames().size() == 1) {
            return outputStreamWrapper.getFileNames();
        } else {
            return outputStreamWrapper.getFileNames().stream()
                    .sorted(Comparator.comparing(FileNameUtils::getFileId))
                    .collect(Collectors.toList());

        }
    }

    public String getId() {
        return id;
    }
}