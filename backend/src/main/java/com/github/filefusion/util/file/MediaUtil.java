package com.github.filefusion.util.file;

import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.VideoAttribute;
import com.github.filefusion.util.ExecUtil;
import com.github.filefusion.util.Json;
import lombok.Data;
import org.springframework.util.MimeType;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * MediaUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class MediaUtil {

    private static final String GET_VIDEO_DIMENSIONS_EXEC = "ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of json %s";
    private static final String GENERATE_VIDEO_DASH_EXEC = "ffmpeg -v error -hwaccel auto -i %s -filter_complex '[0:v]split=%s%s;%s'%s -map 0:a:0? -c:a "
            + VideoAttribute.AUDIO_CODEC
            + " -b:a " + VideoAttribute.AUDIO_BANDWIDTH
            + " -ar " + VideoAttribute.AUDIO_RATE
            + " -c:v " + VideoAttribute.VIDEO_CODEC
            + " -pix_fmt " + VideoAttribute.VIDEO_PIX_FORMAT
            + " -preset " + VideoAttribute.VIDEO_CODEC_PRESET
            + " -r " + VideoAttribute.VIDEO_FPS + " -fps_mode cfr -g " + (VideoAttribute.VIDEO_FPS * VideoAttribute.MEDIA_SEGMENT_DURATION)
            + " -keyint_min " + (VideoAttribute.VIDEO_FPS * VideoAttribute.MEDIA_SEGMENT_DURATION)
            + " -sc_threshold 0 -b_strategy 0"
            + " -f dash -adaptation_sets 'id=0,streams=v id=1,streams=a' -seg_duration " + VideoAttribute.MEDIA_SEGMENT_DURATION
            + " -frag_type none -init_seg_name 'init-stream$RepresentationID$.$ext$' -media_seg_name 'chunk-stream$RepresentationID$-$Number%%05d$.$ext$' -y %s";
    private static final List<String> SUPPORT_DASH_MIME_TYPE = List.of(
            "video/x-ms-wmv",
            "video/x-flv",
            "video/webm",
            "video/quicktime",
            "video/mpeg",
            "video/mp4",
            "video/avi",
            "video/3gpp",
            "application/vnd.rn-realmedia");
    private static final Map<String, MimeType> DASH_FILE_MIME_TYPE = new HashMap<>() {{
        put(".mpd", FileAttribute.MimeType.DASH.value());
        put(".mp4", FileAttribute.MimeType.MP4.value());
        put(".m4s", FileAttribute.MimeType.MP4.value());
    }};

    private static GetVideoInfoResult getVideoDimensionsInfo(Path path, Duration videoGenerateTimeout)
            throws ReadVideoInfoException, IOException, ExecutionException, InterruptedException {
        String exec = GET_VIDEO_DIMENSIONS_EXEC.formatted(path);
        ExecUtil.ExecResult execResult = ExecUtil.exec(exec, videoGenerateTimeout);
        if (!execResult.success()) {
            throw new ReadVideoInfoException();
        }
        return Json.parseObject(String.join("\n", execResult.stdout()), GetVideoInfoResult.class);
    }

    private static Map<VideoAttribute.Resolution, int[]> getVideoScaleDimensions(int[] originalDimensions) {
        int originalWidth = originalDimensions[0];
        int originalHeight = originalDimensions[1];
        boolean isPortrait = originalHeight > originalWidth;

        return Arrays.stream(VideoAttribute.Resolution.values())
                .filter(resolution -> {
                    if (resolution == VideoAttribute.Resolution.P720) {
                        return true;
                    }
                    return isPortrait ?
                            originalHeight >= resolution.width() || originalWidth >= resolution.height() :
                            originalWidth >= resolution.width() || originalHeight >= resolution.height();
                })
                .collect(Collectors.toMap(
                        Function.identity(),
                        resolution -> {
                            int maxWidth = isPortrait ? resolution.height() : resolution.width();
                            int maxHeight = isPortrait ? resolution.width() : resolution.height();

                            double widthRatio = (double) maxWidth / originalWidth;
                            double heightRatio = (double) maxHeight / originalHeight;
                            int scaledWidth;
                            int scaledHeight;
                            if (widthRatio < heightRatio) {
                                scaledWidth = (int) Math.round(originalWidth * widthRatio);
                                scaledHeight = -2;
                            } else {
                                scaledWidth = -2;
                                scaledHeight = (int) Math.round(originalHeight * heightRatio);
                            }

                            return new int[]{
                                    scaledWidth != -1 && scaledWidth % 2 != 0 ? scaledWidth - 1 : scaledWidth,
                                    scaledHeight != -1 && scaledHeight % 2 != 0 ? scaledHeight - 1 : scaledHeight
                            };
                        },
                        (a, b) -> a,
                        () -> new EnumMap<>(VideoAttribute.Resolution.class)
                ));
    }

    public static boolean supportGenerateDash(String mimeType) {
        return StringUtils.hasLength(mimeType) && SUPPORT_DASH_MIME_TYPE.contains(mimeType);
    }

    public static MimeType getDashFileMimeType(String fileName) {
        for (String extension : DASH_FILE_MIME_TYPE.keySet()) {
            if (fileName.endsWith(extension)) {
                return DASH_FILE_MIME_TYPE.get(extension);
            }
        }
        return null;
    }

    public static void generateMediaDash(Path originalPath, Path targetPath, Duration videoGenerateTimeout)
            throws ReadVideoInfoException, IOException, ExecutionException, InterruptedException {
        GetVideoInfoResult videoInfo = getVideoDimensionsInfo(originalPath, videoGenerateTimeout);
        int[] originalDimensions = new int[]{videoInfo.getStreams().getFirst().getWidth(), videoInfo.getStreams().getFirst().getHeight()};
        Map<VideoAttribute.Resolution, int[]> targetDimensionsMap = getVideoScaleDimensions(originalDimensions);

        int index = 1;
        StringBuilder videoSplit = new StringBuilder();
        StringBuilder videoScale = new StringBuilder();
        StringBuilder mapStream = new StringBuilder();
        for (VideoAttribute.Resolution resolution : targetDimensionsMap.keySet()) {
            int[] targetDimensions = targetDimensionsMap.get(resolution);
            int width = targetDimensions[0];
            int height = targetDimensions[1];
            long bandwidth = resolution.bandwidth();
            videoSplit.append("[v").append(index).append("]");
            videoScale.append("[v").append(index).append("]scale=").append(width).append(":").append(height).append("[v").append(index).append("out];");
            mapStream.append(" -map '[v").append(index).append("out]' -b:v:").append(index - 1).append(" ").append(bandwidth).append("k");
            index++;
        }
        String exec = GENERATE_VIDEO_DASH_EXEC.formatted(originalPath, targetDimensionsMap.size(), videoSplit, videoScale, mapStream, targetPath);
        Files.createDirectories(targetPath.getParent());
        ExecUtil.exec(exec, videoGenerateTimeout);
    }

    @Data
    private static class GetVideoInfoResult implements Serializable {
        List<Streams> streams;

        @Data
        private static class Streams implements Serializable {
            private int width;
            private int height;
        }
    }

    public static class ReadVideoInfoException extends Exception {
    }

}
