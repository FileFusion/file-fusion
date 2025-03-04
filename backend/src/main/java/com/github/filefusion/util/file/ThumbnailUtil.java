package com.github.filefusion.util.file;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.file.model.FileHashUsageCount;
import com.github.filefusion.file.repository.FileDataRepository;
import com.github.filefusion.util.ExecUtil;
import com.github.filefusion.util.I18n;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ThumbnailUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public final class ThumbnailUtil {

    private static final String GENERATE_IMAGE_THUMBNAIL_EXEC = "vipsthumbnail %s --size 256 --export-profile srgb -o %s[Q=75,keep=none]";
    private static final String GENERATE_VIDEO_THUMBNAIL_EXEC = "ffmpeg -i %s -loglevel error -vf \"thumbnail,scale=256:-1\" -an -q:v 31 -vframes 1 -update 1 -y %s";

    @Getter
    private final Path baseDir;
    private final Duration thumbnailGenerateTimeout;
    @Getter
    private final List<String> thumbnailImageMimeType;
    @Getter
    private final List<String> thumbnailVideoMimeType;
    private final FileDataRepository fileDataRepository;

    @Autowired
    public ThumbnailUtil(@Value("${thumbnail.dir}") String thumbnailDir,
                         @Value("${thumbnail.generate-timeout}") Duration thumbnailGenerateTimeout,
                         @Value("${thumbnail.image-mime-type}") List<String> thumbnailImageMimeType,
                         @Value("${thumbnail.video-mime-type}") List<String> thumbnailVideoMimeType,
                         FileDataRepository fileDataRepository) {
        this.baseDir = Paths.get(thumbnailDir).normalize().toAbsolutePath();
        this.thumbnailGenerateTimeout = thumbnailGenerateTimeout;
        this.thumbnailImageMimeType = thumbnailImageMimeType;
        this.thumbnailVideoMimeType = thumbnailVideoMimeType;
        this.fileDataRepository = fileDataRepository;
        if (!Files.exists(this.baseDir)) {
            try {
                Files.createDirectories(this.baseDir);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean hasThumbnail(String mimeType) {
        if (!StringUtils.hasLength(mimeType)) {
            return false;
        }
        return thumbnailImageMimeType.contains(mimeType) || thumbnailVideoMimeType.contains(mimeType);
    }

    public Path generateThumbnail(Path originalPath, String mimeType, String hash) {
        Path targetPath = PathUtil.resolvePath(baseDir, hash + FileAttribute.THUMBNAIL_FILE_TYPE, false);
        if (Files.isRegularFile(targetPath)) {
            return targetPath;
        }
        String exec;
        if (thumbnailImageMimeType.contains(mimeType)) {
            exec = GENERATE_IMAGE_THUMBNAIL_EXEC.formatted(originalPath, targetPath);
        } else if (thumbnailVideoMimeType.contains(mimeType)) {
            exec = GENERATE_VIDEO_THUMBNAIL_EXEC.formatted(originalPath, targetPath);
        } else {
            throw new HttpException(I18n.get("fileNotSupportThumbnail"));
        }
        boolean execResult = ExecUtil.exec(Arrays.asList(exec.split(" ")), thumbnailGenerateTimeout);
        if (!execResult || !Files.exists(targetPath)) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("thumbnailGenerationFailed"));
        }
        return targetPath;
    }

    public void clearThumbnail(List<String> hashList) {
        Map<String, FileHashUsageCount> hashUsageCountMap = fileDataRepository.countByHashValueList(hashList)
                .stream().collect(Collectors.toMap(FileHashUsageCount::getHashValue, Function.identity()));
        hashList = hashList.stream()
                .filter(hash -> {
                    FileHashUsageCount hashUsageCount = hashUsageCountMap.get(hash);
                    return hashUsageCount == null
                            || hashUsageCount.getCount() == null || hashUsageCount.getCount() == 0L
                            || !StringUtils.hasLength(hashUsageCount.getMimeType())
                            || (!thumbnailImageMimeType.contains(hashUsageCount.getMimeType())
                            && !thumbnailVideoMimeType.contains(hashUsageCount.getMimeType()));
                })
                .map(hash -> hash + FileAttribute.THUMBNAIL_FILE_TYPE)
                .toList();
        PathUtil.delete(PathUtil.resolvePath(baseDir, hashList, false));
    }

}
