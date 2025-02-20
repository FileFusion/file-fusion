package com.github.filefusion.util;

import com.github.filefusion.common.HttpException;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.ExecuteWatchdog;
import org.apache.commons.exec.Executor;
import org.springframework.http.HttpStatus;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

/**
 * ExecUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class ExecUtil {

    public static boolean exec(List<String> commandLineList, Duration execTimeout) {
        if (CollectionUtils.isEmpty(commandLineList)) {
            return false;
        }
        if (execTimeout == null || execTimeout.isNegative()) {
            return false;
        }

        CommandLine commandLine = new CommandLine(commandLineList.getFirst());
        if (commandLineList.size() > 1) {
            commandLine.addArguments(commandLineList.subList(1, commandLineList.size()).toArray(new String[0]));
        }

        ExecuteWatchdog watchdog = ExecuteWatchdog.builder().setTimeout(execTimeout).get();
        Executor executor = DefaultExecutor.builder().get();
        executor.setWatchdog(watchdog);

        try {
            return executor.execute(commandLine) == 0;
        } catch (IOException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

}
