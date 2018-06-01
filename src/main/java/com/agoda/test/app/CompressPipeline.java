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

    private CompressHandler compressHandler;
    private OutputStreamWrapper wrapper;

    private ExecutorService service = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2);

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

        cleanOutputPath(this.output);

        wrapper = new OutputStreamWrapper(this.output, this.size);
        wrapper.init();

        Path parent = this.output.getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }

        wrapper.writeInt(size);
        doCompress(this.input, size);
    }

    private void cleanOutputPath(Path output) {

        try {
            Files.walk(output).forEach(path -> {
                try {
                    Files.deleteIfExists(path);
                } catch (IOException io) {
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private void doCompress(Path file, int size) {
        Collection<CompressWorker> workers = new ArrayList<>();

        if (file.toFile().isDirectory()) {
            doCompressDir(file, size, workers);
        } else {
            doCompressFile(file, size);
        }

        try {
            List<Future<String>> futures = service.invokeAll(workers);
            List<String> ids = futures.parallelStream().map(f -> {
                try {
                    return f.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());


            merge(ids);

            service.shutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void merge(List<String> ids) {
        System.out.println("ids = " + ids.toString());

        ids.stream().map(id -> id.split(","))
                .flatMap(Arrays::stream)
                .map(name -> Paths.get(this.output.toString(), name))
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
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                });

//        this.wrapper.writeBytes();

    }

    private void doCompressDir(Path file, int size, Collection<CompressWorker> workers) {

        System.out.println("dir = " + file.toString());
        // type bit for file
        wrapper.writeByte(BIT_DIR);
        wrapper.writeString(file.toString());

        try (Stream<Path> dirs = Files.list(file)) {

            CompressWorker worker = new CompressWorker(size,
                    this.output,
                    dirs.filter(path -> !path.getFileName().toString().startsWith(".")).filter(path -> Files.isRegularFile(path)).collect(Collectors.toList()));
            workers.add(worker);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        try (Stream<Path> dirs = Files.list(file)) {
            dirs.filter(path -> !path.getFileName().toString().startsWith(".")).filter(Files::isDirectory).forEach(path -> this.doCompressDir(path, size, workers));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void doCompressFile(Path file, int size) {
        doCompressFile(this.wrapper, file, size);
    }


    private void doCompressFile(OutputStreamWrapper wrapper, Path file, int size) {

//        byte[] fileData = splitFile(file, size);

        try (InputStream fileInputStream = Files.newInputStream(file)) {
            System.out.println(" compressing " + file.toString());

            wrapper.writeByte(BIT_FILE);
            wrapper.writeString(file.toString());

            int len = -1;
            byte[] buf = new byte[size];
            byte[] outputBuf = new byte[size];

            int total = fileInputStream.available();
            wrapper.writeInt(total);
            System.out.println("total = " + total);
            while ((len = fileInputStream.read(buf)) != -1) {

                int outputLen = compressHandler.compress(buf, outputBuf, 0, len);
                total -= len;
                wrapper.writeInt(outputLen);
                wrapper.writeBytes(outputBuf, outputLen);
                System.out.println("total = " + total);

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    class CompressWorker implements Callable<String> {

        private final int size;
        private List<Path> paths;
        private final Path output;
        private final String id;

        public CompressWorker(int size, Path output, List<Path> paths) {
            this.size = size;
            this.paths = paths;
            this.output = output;
            id = UUID.randomUUID().toString();
        }

        @Override
        public String call() throws Exception {
            if (paths.isEmpty()) {
                return null;
            }
            OutputStreamWrapper outputStreamWrapper = new OutputStreamWrapper(this.output, this.size);
            outputStreamWrapper.setFileName(id);
            outputStreamWrapper.init();

            paths.forEach(path -> doCompressFile(outputStreamWrapper, path, size));
            return outputStreamWrapper.getFileNames().stream().collect(Collectors.joining(","));
        }

    }

}
