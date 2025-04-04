package com.github.filefusion.util;

import org.apache.commons.exec.*;
import org.apache.commons.exec.Executor;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;

/**
 * ExecUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class ExecUtil {

    private static final long TIMEOUT_BUFFER_MS = 1000L;
    private static final ExecutorService EXECUTOR = Executors.newVirtualThreadPerTaskExecutor();

    public static void shutdown() {
        EXECUTOR.close();
    }

    public static ExecResult exec(String command, Duration execTimeout)
            throws IOException, ExecutionException, InterruptedException {
        ConcurrentLinkedQueue<String> stdoutQueue = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> stderrQueue = new ConcurrentLinkedQueue<>();
        CompletableFuture<Integer> completable;
        try (OutputStream outputStream = new CollectingLogOutputStream(stdoutQueue);
             OutputStream errorOutputStream = new CollectingLogOutputStream(stderrQueue)) {
            completable = exec(command, outputStream, errorOutputStream, execTimeout);
        }
        return new ExecResult(completable.get() == 0, List.copyOf(stdoutQueue), List.copyOf(stderrQueue));
    }

    public static CompletableFuture<Integer> exec(String command, OutputStream outputStream,
                                                  OutputStream errorOutputStream, Duration execTimeout) {
        if (!StringUtils.hasLength(command) || execTimeout == null || execTimeout.isNegative()) {
            return CompletableFuture.completedFuture(-1);
        }

        ExecuteWatchdog watchdog = ExecuteWatchdog.builder().setTimeout(execTimeout).get();
        Executor executor = DefaultExecutor.builder().get();
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(new PumpStreamHandler(outputStream, errorOutputStream));

        return CompletableFuture.supplyAsync(() -> {
                    try {
                        return executor.execute(CommandLine.parse(command));
                    } catch (ExecuteException e) {
                        return e.getExitValue();
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
                }, EXECUTOR)
                .orTimeout(execTimeout.toMillis() + TIMEOUT_BUFFER_MS, TimeUnit.MILLISECONDS)
                .exceptionally(ex -> {
                    watchdog.destroyProcess();
                    return -1;
                });
    }

    public record ExecResult(boolean success, List<String> stdout, List<String> stderr) implements Serializable {
    }

    private static class CollectingLogOutputStream extends LogOutputStream {
        private final ConcurrentLinkedQueue<String> queue;

        CollectingLogOutputStream(ConcurrentLinkedQueue<String> queue) {
            this.queue = queue;
        }

        @Override
        protected void processLine(String line, int logLevel) {
            queue.offer(line);
        }
    }

}
