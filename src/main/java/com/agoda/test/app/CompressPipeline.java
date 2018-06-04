package com.agoda.test.app;

import com.agoda.test.app.commons.ByteType;
import com.agoda.test.app.workers.CompressWorker;
import com.agoda.test.app.workers.CompressWorkerExecutor;
import com.agoda.test.compress.CompressHandler;
import com.agoda.test.compress.DeflaterCompressHandler;
import com.agoda.test.compress.OutputStreamWrapper;
import com.agoda.test.utils.CompressUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CompressPipeline
 *
 * @author yousheng
 * @since 2018/5/28
 */
public class CompressPipeline implements ByteType {

    private final Path input;
    private final Path output;
    private final int size;
    
    private final List<Callable<List<String>>> callables = new ArrayList<>();
    private final Path inputRoot;
    private CompressHandler compressHandler;
    private OutputStreamWrapper wrapper;
//    private ExecutorCompletionService<List<String>> completionService = new ExecutorCompletionService(executorService);

    private CompressWorkerExecutor<List<String>> compressWorkerExecutor = new CompressWorkerExecutor<>();

    public CompressPipeline(String input, String output, int size) {
        this(input, output, size, new DeflaterCompressHandler(size));
    }

    public CompressPipeline(String input, String output, int size, CompressHandler compressHandler) {
        this.input = Paths.get(input);
        this.inputRoot = this.input.getParent();
        this.output = Paths.get(output);
        this.size = size * 1024 * 1024;
        this.compressHandler = compressHandler;
    }


    public void compress() throws IOException {

        wrapper = new OutputStreamWrapper(this.output, this.size);
        wrapper.init();

        Path parent = this.output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        int bufSize = ByteBufUtils.requireArray();
        wrapper.writeInt(bufSize);
        doCompress(this.input, size);
    }

    private void doCompress(Path file, int size) {

        if (file.toFile().isDirectory()) {
            doCompressDir(file, size);
        } else {
            CompressUtils.doCompressFile(this.wrapper, compressHandler, this.inputRoot, file);
        }

        if (callables.isEmpty()) {
            return;
        }

        compressWorkerExecutor.submit(callables);

        int n = callables.size();
        while (n > 0) {
            try {
                List<String> fileIds = compressWorkerExecutor.take();
                if (fileIds == null || fileIds.isEmpty()) {
                    continue;
                }
                CompressUtils.merge(this.wrapper, this.output, fileIds);
            } finally {
                n--;
            }
        }

        compressWorkerExecutor.shutdown();
    }

    /**
     * compress the dir object and all its sub-path, will create a file compress worker
     * for later invoke.
     *
     * @param file
     * @param size
     */
    private void doCompressDir(Path file, int size) {

        // type bit for file
        wrapper.writeByte(BIT_DIR);

        CompressUtils.writeFileName(this.wrapper, this.inputRoot, file);

        try (Stream<Path> dirs = Files.list(file)) {

            List<Path> files = dirs.filter(path -> !path.getFileName().toString().startsWith(".") && !path.getFileName().toString().endsWith(".zip"))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            if (files.size() > 0) {

                // file compress worker
                CompressWorker worker = new CompressWorker(size, this.compressHandler, this.inputRoot, this.output, files);

                callables.add(worker);
            }

        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try (Stream<Path> dirs = Files.list(file)) {
            dirs.filter(path -> !path.getFileName().toString().startsWith("."))
                    .filter(Files::isDirectory)
                    .forEach(path -> this.doCompressDir(path, size));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
