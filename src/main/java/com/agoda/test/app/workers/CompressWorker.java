package com.agoda.test.app.workers;

/**
 * CompressWorker
 *
 * @author yousheng
 * @since 2018/6/4
 */

import com.agoda.test.app.FileNameUtils;
import com.agoda.test.compress.CompressHandler;
import com.agoda.test.compress.OutputStreamWrapper;
import com.agoda.test.utils.CompressUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * Compress thread runner for each single folder.
 * TODO multi thread processing when the paths size over a value
 */
public class CompressWorker implements Callable<List<String>> {

    private final int size;
    private final Path output;
    private final String id;
    private final CompressHandler compressHandler;
    private final Path parent;
    private List<Path> paths;
    private final int MULTI_THREAD_THRESHOLD = 10;
    private CountDownLatch latch;

    public CompressWorker(int size, CompressHandler compressHandler, Path parent, Path output, List<Path> paths) {
        this.size = size;
        this.compressHandler = compressHandler;
        this.parent = parent;
        this.paths = paths;
        this.output = output;
        id = UUID.randomUUID().toString();
    }

    @Override
    public List<String> call() {
        if (paths.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            OutputStreamWrapper outputStreamWrapper = new OutputStreamWrapper(this.output, this.size);
            outputStreamWrapper.setFileName(id);
            outputStreamWrapper.init();

//            if (paths.size() > MULTI_THREAD_THRESHOLD) {
//
//                List<Callable<List<String>>> callables = paths.parallelStream().map(path -> {
//                    SingleFileCompressWorker compressWorker = new SingleFileCompressWorker(size, output, path);
//                    return compressWorker;
//                }).collect(Collectors.toList());
//                latch = new CountDownLatch(callables.size());
//
//                callables.forEach(folderWorkerService::submit);
//
//                latch.await();
//
//                for (int i = 0; i < callables.size(); i++) {
//                    try {
//                        CompressUtils.merge(outputStreamWrapper);
//                    } catch (InterruptedException | ExecutionException e) {
//                        e.printStackTrace();
//                    }
//                }
//            } else {
                paths.forEach(path -> CompressUtils.doCompressFile(outputStreamWrapper, compressHandler, this.parent, path));
//            }
            return outputStreamWrapper
                    .getFileNames()
                    .stream()
                    .sorted(Comparator.comparing(FileNameUtils::getFileId))
                    .collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Collections.emptyList();

    }

}