package com.agoda.test.app.workers;

import java.util.List;
import java.util.concurrent.*;

/**
 * CompressWorkerExecutor
 *
 * @author yousheng
 * @since 2018/6/4
 */
public class CompressWorkerExecutor<C> {

    private final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    private final ExecutorCompletionService<C> service = new ExecutorCompletionService<>(executorService);

    public void submit(List<Callable<C>> callables) {
        if (callables.isEmpty()) {
            return;
        } else {
            callables.stream().forEach(service::submit);
        }
    }

    public C take() {
        try {
            return service.take().get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        return null;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
