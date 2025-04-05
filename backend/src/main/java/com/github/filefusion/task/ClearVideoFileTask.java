package com.github.filefusion.task;

import com.github.filefusion.common.FileProperties;
import com.github.filefusion.constant.RedisAttribute;
import com.github.filefusion.file.model.FileHashUsageCountModel;
import com.github.filefusion.file.repository.FileDataRepository;
import com.github.filefusion.util.DistributedLock;
import com.github.filefusion.util.file.FileUtil;
import com.github.filefusion.util.file.MediaUtil;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ClearVideoFileTask
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public class ClearVideoFileTask {

    private static final String LOCK_KEY = "clearVideoFileTask";
    private static final int BATCH_FILE_SIZE = 1000;

    private final DistributedLock distributedLock;
    private final FileProperties fileProperties;
    private final FileDataRepository fileDataRepository;

    @Autowired
    public ClearVideoFileTask(DistributedLock distributedLock,
                              FileProperties fileProperties,
                              FileDataRepository fileDataRepository) {
        this.distributedLock = distributedLock;
        this.fileProperties = fileProperties;
        this.fileDataRepository = fileDataRepository;
    }

    @Scheduled(cron = "${task.clear-video-file}")
    public void clearVideoFileTask() {
        distributedLock.tryLock(RedisAttribute.LockType.task, LOCK_KEY, () -> {
            try {
                Map<String, Path> videoMap = new HashMap<>();
                Files.walkFileTree(fileProperties.getThumbnailDir(), new SimpleFileVisitor<>() {
                    @Override
                    @Nonnull
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) {
                        try {
                            String fileName = dir.getFileName().toString();
                            if (fileName.length() == 64) {
                                videoMap.put(fileName, dir);
                                if (videoMap.size() >= BATCH_FILE_SIZE) {
                                    clearVideo(new HashMap<>(videoMap));
                                    videoMap.clear();
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                if (!videoMap.isEmpty()) {
                    clearVideo(videoMap);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, null);
    }

    public void clearVideo(Map<String, Path> videoMap) throws IOException {
        List<String> hashList = new ArrayList<>(videoMap.keySet());
        Map<String, FileHashUsageCountModel> hashUsageCountMap = fileDataRepository.countByHashValueList(hashList)
                .stream().collect(Collectors.toMap(FileHashUsageCountModel::getHashValue, Function.identity()));
        List<String> filterHashList = hashList.stream()
                .filter(hash -> {
                    FileHashUsageCountModel hashUsageCount = hashUsageCountMap.get(hash);
                    return hashUsageCount == null || hashUsageCount.getCount() == null || hashUsageCount.getCount() == 0L
                            || !MediaUtil.isDashSupported(hashUsageCount.getMimeType(), fileProperties.getVideoPlayMimeType());
                }).toList();
        FileUtil.delete(videoMap.entrySet().stream()
                .filter(entry -> filterHashList.contains(entry.getKey()))
                .map(Map.Entry::getValue).toList());
    }

}
