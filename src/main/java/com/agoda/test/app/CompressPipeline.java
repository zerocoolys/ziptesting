package com.agoda.test.app;

import com.agoda.test.app.commons.FileType;
import com.agoda.test.compress.CompressHandler;
import com.agoda.test.compress.DeflaterCompressHandler;
import com.agoda.test.compress.OutputStreamWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * CompressPipeline
 *
 * @author yousheng
 * @since 2018/5/28
 */
public class CompressPipeline implements FileType {

    private final Path input;
    private final Path output;
    private final int size;
    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

    private final List<Callable<List<String>>> callables = new ArrayList<>();
    private CompressHandler compressHandler;
    private OutputStreamWrapper wrapper;
    private ExecutorCompletionService<List<String>> completionService = new ExecutorCompletionService(executorService);

    public CompressPipeline(String input, String output, int size) {
        this(input, output, size, new DeflaterCompressHandler(size));
    }

    public CompressPipeline(String input, String output, int size, CompressHandler compressHandler) {
        this.input = Paths.get(input);
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

        wrapper.writeInt(size);
        doCompress(this.input, size);
    }

    private void doCompress(Path file, int size) {

        if (file.toFile().isDirectory()) {
            doCompressDir(file, size);
        } else {
            doCompressFile(file, size);
        }

        if (callables.isEmpty()) {
            return;
        }

        callables.forEach(completionService::submit);

        int n = callables.size();
        while (n > 0) {
            try {
                List<String> fileIds = completionService.take().get();
                if (fileIds == null || fileIds.isEmpty()) {
                    continue;
                }
                merge(fileIds);


            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            } finally {
                n--;
            }
        }

        executorService.shutdown();
    }

    private void merge(List<String> ids) {
        System.out.println("merge = " + ids);

        ids.stream().map(s -> Paths.get(this.output.toString(), s))
                .sorted(Comparator.comparing(FileNameUtils::getFileId))
                .forEach(path -> {
                    try {
                        InputStream is = Files.newInputStream(path);
                        byte[] buf = new byte[size / 10];
                        int c = -1;
                        while ((c = is.read(buf)) != -1) {
                            this.wrapper.writeBytes(buf, c);
                        }

                        Files.deleteIfExists(path);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                });

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
        wrapper.writeString(file.toString());

        try (Stream<Path> dirs = Files.list(file)) {

            List<Path> files = dirs.filter(path -> !path.getFileName().toString().startsWith("."))
                    .filter(Files::isRegularFile)
                    .collect(Collectors.toList());
            if (files.size() > 0) {
                // file compress worker
                CompressWorker worker = new CompressWorker(size, this.output, files);

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

    private void doCompressFile(Path file, int size) {
        doCompressFile(this.wrapper, file, size);
    }


    private void doCompressFile(OutputStreamWrapper wrapper, Path file, int size) {


        try (InputStream fileInputStream = Files.newInputStream(file)) {

            wrapper.writeByte(BIT_FILE);
            wrapper.writeString(file.toString());

            int len = -1;
            byte[] buf = ByteBufUtils.requireArray(size);
            byte[] outputBuf = new byte[buf.length];

            int total = fileInputStream.available();
            wrapper.writeInt(total);
            while ((len = fileInputStream.read(buf)) != -1) {

                int outputLen = compressHandler.compress(buf, outputBuf, 0, len);
                total -= len;
                wrapper.writeInt(outputLen);
                wrapper.writeBytes(outputBuf, outputLen);

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    class CompressWorker implements Callable<List<String>> {

        private final int size;
        private final Path output;
        private final String id;
        private List<Path> paths;

        public CompressWorker(int size, Path output, List<Path> paths) {
            this.size = size;
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

                paths.forEach(path -> doCompressFile(outputStreamWrapper, path, size));
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

}
