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
    private static final String GENERATE_VIDEO_DASH_EXEC = "ffmpeg -v error -hwaccel auto -i %s -filter_complex \"%s[0:a]asplit=%d%s\"%s -f " + VideoAttribute.VIDEO_FORMAT + " -seg_duration " + VideoAttribute.MEDIA_SEGMENT_DURATION + " -init_seg_name \"init-$RepresentationID$.mp4\" -media_seg_name \"seg-$RepresentationID$-$Number%%05d$.m4s\" -y %s";
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
        ExecUtil.ExecResult execResult = ExecUtil.exec(Arrays.asList(exec.split(" ")), videoGenerateTimeout);
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
                    if (resolution == VideoAttribute.Resolution.P480) {
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
        StringBuilder videoScale = new StringBuilder();
        StringBuilder audioStream = new StringBuilder();
        StringBuilder outStream = new StringBuilder();
        for (VideoAttribute.Resolution resolution : targetDimensionsMap.keySet()) {
            int[] targetDimensions = targetDimensionsMap.get(resolution);
            int width = targetDimensions[0];
            int height = targetDimensions[1];
            long bandwidth = resolution.bandwidth();
            long audioBandwidth = resolution.audioBandwidth();
            videoScale.append("[0:v]scale=").append(width).append(":").append(height).append("[v").append(index).append("];");
            audioStream.append("[a").append(index).append("]");
            outStream.append(" -map \"[v").append(index).append("]\" -c:v:").append(index - 1).append(" ").append(VideoAttribute.VIDEO_CODEC).append(" -preset ").append(VideoAttribute.VIDEO_CODEC_PRESET).append(" -b:v:").append(index - 1).append(" ").append(bandwidth).append("k");
            outStream.append(" -map \"[a").append(index).append("]\" -c:a:").append(index - 1).append(" ").append(VideoAttribute.AUDIO_CODEC).append(" -b:a:").append(index - 1).append(" ").append(audioBandwidth).append("k");
            index++;
        }
        String exec = GENERATE_VIDEO_DASH_EXEC.formatted(originalPath, videoScale, targetDimensionsMap.size(), audioStream, outStream, targetPath);
        Files.createDirectories(targetPath.getParent());
        ExecUtil.exec(Arrays.asList(exec.split(" ")), videoGenerateTimeout);
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
