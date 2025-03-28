package com.github.filefusion.util;

import org.apache.commons.exec.*;
import org.apache.commons.exec.Executor;
import org.springframework.util.CollectionUtils;

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

    public static ExecResult exec(List<String> commandLineList, Duration execTimeout)
            throws IOException, ExecutionException, InterruptedException {
        ConcurrentLinkedQueue<String> stdoutQueue = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<String> stderrQueue = new ConcurrentLinkedQueue<>();
        CompletableFuture<Integer> completable;
        try (OutputStream outputStream = new CollectingLogOutputStream(stdoutQueue);
             OutputStream errorOutputStream = new CollectingLogOutputStream(stderrQueue)) {
            completable = exec(commandLineList, outputStream, errorOutputStream, execTimeout);
        }
        return new ExecResult(completable.get() == 0, List.copyOf(stdoutQueue), List.copyOf(stderrQueue));
    }

    public static CompletableFuture<Integer> exec(List<String> commandLineList, OutputStream outputStream,
                                                  OutputStream errorOutputStream, Duration execTimeout) {
        if (CollectionUtils.isEmpty(commandLineList) || execTimeout == null || execTimeout.isNegative()) {
            return CompletableFuture.completedFuture(-1);
        }
        CommandLine commandLine = new CommandLine(commandLineList.getFirst().trim());
        commandLine.addArguments(commandLineList.stream().skip(1).map(String::trim).toArray(String[]::new));

        ExecuteWatchdog watchdog = ExecuteWatchdog.builder().setTimeout(execTimeout).get();
        Executor executor = DefaultExecutor.builder().get();
        executor.setWatchdog(watchdog);
        executor.setStreamHandler(new PumpStreamHandler(outputStream, errorOutputStream));

        return CompletableFuture.supplyAsync(() -> {
                    try {
                        return executor.execute(commandLine);
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
