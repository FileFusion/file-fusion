package com.github.filefusion.util.file;

import com.github.filefusion.common.HttpException;
import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.util.ExecUtil;
import com.github.filefusion.util.I18n;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

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
    private final FileUtil fileUtil;

    @Autowired
    public ThumbnailUtil(@Value("${thumbnail.dir}") String thumbnailDir,
                         @Value("${thumbnail.generate-timeout}") Duration thumbnailGenerateTimeout,
                         @Value("${thumbnail.image-mime-type}") List<String> thumbnailImageMimeType,
                         @Value("${thumbnail.video-mime-type}") List<String> thumbnailVideoMimeType,
                         FileUtil fileUtil) {
        this.baseDir = Paths.get(thumbnailDir).normalize().toAbsolutePath();
        this.thumbnailGenerateTimeout = thumbnailGenerateTimeout;
        this.thumbnailImageMimeType = thumbnailImageMimeType;
        this.thumbnailVideoMimeType = thumbnailVideoMimeType;
        this.fileUtil = fileUtil;
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

    public Path generateThumbnail(String path, String mimeType, String hash) {
        Path thumbnailFilePath = baseDir.resolve(hash + FileAttribute.THUMBNAIL_FILE_TYPE);
        if (Files.isRegularFile(thumbnailFilePath)) {
            return thumbnailFilePath;
        }
        fileUtil.delete(thumbnailFilePath);
        Path sourceFilePath = fileUtil.validatePath(path);
        String exec;
        if (thumbnailImageMimeType.contains(mimeType)) {
            exec = GENERATE_IMAGE_THUMBNAIL_EXEC.formatted(sourceFilePath, thumbnailFilePath);
        } else if (thumbnailVideoMimeType.contains(mimeType)) {
            exec = GENERATE_VIDEO_THUMBNAIL_EXEC.formatted(sourceFilePath, thumbnailFilePath);
        } else {
            throw new HttpException(I18n.get("fileNotSupportThumbnail"));
        }
        boolean execResult = ExecUtil.exec(Arrays.asList(exec.split(" ")), thumbnailGenerateTimeout);
        if (!execResult || !Files.exists(thumbnailFilePath)) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, I18n.get("thumbnailGenerationFailed"));
        }
        return thumbnailFilePath;
    }

    public void deleteThumbnail(List<String> hashList) {
        if (CollectionUtils.isEmpty(hashList)) {
            return;
        }
        List<Path> pathList = hashList.stream()
                .map(hash -> baseDir.resolve(hash + FileAttribute.THUMBNAIL_FILE_TYPE))
                .toList();
        fileUtil.delete(pathList);
    }

}
