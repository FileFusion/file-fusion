package com.github.filefusion.util;

import lombok.Data;
import org.apache.commons.exec.*;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.io.Serializable;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * ExecUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class ExecUtil {

    public static ExecResult exec(List<String> commandLineList, Duration execTimeout) throws IOException {
        if (CollectionUtils.isEmpty(commandLineList)) {
            return ExecResult.fail();
        }
        if (execTimeout == null || execTimeout.isNegative()) {
            return ExecResult.fail();
        }

        CommandLine commandLine = new CommandLine(commandLineList.getFirst());
        commandLineList.stream().skip(1).forEach(commandLine::addArgument);

        ExecuteWatchdog watchdog = ExecuteWatchdog.builder().setTimeout(execTimeout).get();
        Executor executor = DefaultExecutor.builder().get();
        executor.setWatchdog(watchdog);

        LinkedBlockingQueue<String> stdoutQueue = new LinkedBlockingQueue<>();
        LinkedBlockingQueue<String> stderrQueue = new LinkedBlockingQueue<>();
        executor.setStreamHandler(new PumpStreamHandler(
                new CollectingLogOutputStream(stdoutQueue),
                new CollectingLogOutputStream(stderrQueue)
        ));

        ExecResult result = new ExecResult();
        try {
            result.setSuccess(executor.execute(commandLine) == 0);
        } catch (ExecuteException e) {
            result.setSuccess(false);
            watchdog.killedProcess();
        } finally {
            result.setStdout(new ArrayList<>(stdoutQueue));
            result.setStderr(new ArrayList<>(stderrQueue));
        }
        return result;
    }

    @Data
    public static class ExecResult implements Serializable {
        private boolean success;
        private List<String> stdout;
        private List<String> stderr;

        public static ExecResult fail() {
            ExecResult result = new ExecResult();
            result.setSuccess(false);
            return result;
        }
    }

    private static class CollectingLogOutputStream extends LogOutputStream {
        private final LinkedBlockingQueue<String> queue;

        CollectingLogOutputStream(LinkedBlockingQueue<String> queue) {
            this.queue = queue;
        }

        @Override
        protected void processLine(String line, int logLevel) {
            queue.offer(line);
        }
    }

}
