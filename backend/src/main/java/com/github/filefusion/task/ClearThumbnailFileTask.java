package com.github.filefusion.task;

import com.github.filefusion.common.FileProperties;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.RedisAttribute;
import com.github.filefusion.file.model.FileHashUsageCountModel;
import com.github.filefusion.file.repository.FileDataRepository;
import com.github.filefusion.util.DistributedLock;
import com.github.filefusion.util.file.FileUtil;
import jakarta.annotation.Nonnull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ClearThumbnailFileTask
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public class ClearThumbnailFileTask {

    private static final String LOCK_KEY = "clearThumbnailFileTask";
    private static final int BATCH_FILE_SIZE = 1000;

    private final DistributedLock distributedLock;
    private final FileProperties fileProperties;
    private final FileDataRepository fileDataRepository;

    @Autowired
    public ClearThumbnailFileTask(DistributedLock distributedLock,
                                  FileProperties fileProperties,
                                  FileDataRepository fileDataRepository) {
        this.distributedLock = distributedLock;
        this.fileProperties = fileProperties;
        this.fileDataRepository = fileDataRepository;
    }

    @Scheduled(cron = "${task.clear-thumbnail-file}")
    public void clearThumbnailFileTask() {
        distributedLock.tryLock(RedisAttribute.LockType.task, LOCK_KEY, () -> {
            try {
                Map<String, Path> thumbnailMap = new HashMap<>();
                Files.walkFileTree(fileProperties.getThumbnailDir(), new SimpleFileVisitor<>() {
                    @Override
                    @Nonnull
                    public FileVisitResult visitFile(Path file, @Nonnull BasicFileAttributes attrs) {
                        try {
                            String fileName = file.getFileName().toString();
                            if (!FileAttribute.THUMBNAIL_FILE_SUFFIX.equals(fileName)
                                    && fileName.endsWith(FileAttribute.THUMBNAIL_FILE_SUFFIX)) {
                                String hash = fileName.substring(0, fileName.length() - FileAttribute.THUMBNAIL_FILE_SUFFIX.length());
                                thumbnailMap.put(hash, file);
                                if (thumbnailMap.size() >= BATCH_FILE_SIZE) {
                                    clearThumbnail(new HashMap<>(thumbnailMap));
                                    thumbnailMap.clear();
                                }
                            }
                        } catch (Exception ignored) {
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });
                if (!thumbnailMap.isEmpty()) {
                    clearThumbnail(thumbnailMap);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }, null);
    }

    public void clearThumbnail(Map<String, Path> thumbnailMap) throws IOException {
        List<String> hashList = new ArrayList<>(thumbnailMap.keySet());
        Map<String, FileHashUsageCountModel> hashUsageCountMap = fileDataRepository.countByHashValueList(hashList)
                .stream().collect(Collectors.toMap(FileHashUsageCountModel::getHashValue, Function.identity()));
        List<String> filterHashList = hashList.stream()
                .filter(hash -> {
                    FileHashUsageCountModel hashUsageCount = hashUsageCountMap.get(hash);
                    return hashUsageCount == null
                            || hashUsageCount.getCount() == null || hashUsageCount.getCount() == 0L
                            || !StringUtils.hasLength(hashUsageCount.getMimeType())
                            || (!fileProperties.getThumbnailImageMimeType().contains(hashUsageCount.getMimeType())
                            && !fileProperties.getThumbnailVideoMimeType().contains(hashUsageCount.getMimeType()));
                })
                .toList();
        FileUtil.delete(thumbnailMap.entrySet().stream()
                .filter(entry -> filterHashList.contains(entry.getKey()))
                .map(Map.Entry::getValue).toList());
    }

}
