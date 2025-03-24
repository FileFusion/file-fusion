package com.github.filefusion.util.file;

import com.github.filefusion.util.ExecUtil;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * ThumbnailUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class ThumbnailUtil {

    private static final String GENERATE_IMAGE_THUMBNAIL_EXEC = "vipsthumbnail %s --size 256 --export-profile srgb -o %s[Q=75,keep=none]";
    private static final String GENERATE_VIDEO_THUMBNAIL_EXEC = "ffmpeg -hwaccel auto -i %s -loglevel error -vf \"thumbnail,scale=256:-1\" -an -quality 75 -vframes 1 -y %s";

    public static boolean hasThumbnail(String mimeType,
                                       List<String> thumbnailImageMimeType,
                                       List<String> thumbnailVideoMimeType) {
        if (!StringUtils.hasLength(mimeType)) {
            return false;
        }
        return thumbnailImageMimeType.contains(mimeType) || thumbnailVideoMimeType.contains(mimeType);
    }

    public static Path generateThumbnail(String mimeType,
                                         Path originalPath, Path targetPath,
                                         List<String> thumbnailImageMimeType,
                                         List<String> thumbnailVideoMimeType, Duration thumbnailGenerateTimeout)
            throws FileNotSupportThumbnailException, ThumbnailGenerationFailedException, IOException {
        if (Files.isRegularFile(targetPath)) {
            return targetPath;
        }
        String exec;
        if (thumbnailImageMimeType.contains(mimeType)) {
            exec = GENERATE_IMAGE_THUMBNAIL_EXEC.formatted(originalPath, targetPath);
        } else if (thumbnailVideoMimeType.contains(mimeType)) {
            exec = GENERATE_VIDEO_THUMBNAIL_EXEC.formatted(originalPath, targetPath);
        } else {
            throw new FileNotSupportThumbnailException();
        }
        Files.createDirectories(targetPath.getParent());
        boolean execResult = ExecUtil.exec(Arrays.asList(exec.split(" ")), thumbnailGenerateTimeout);
        if (!execResult || !Files.exists(targetPath)) {
            throw new ThumbnailGenerationFailedException();
        }
        return targetPath;
    }

    public static class FileNotSupportThumbnailException extends Exception {
    }

    public static class ThumbnailGenerationFailedException extends Exception {
    }

}
