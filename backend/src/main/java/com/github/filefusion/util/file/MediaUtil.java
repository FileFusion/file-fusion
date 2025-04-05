package com.github.filefusion.util.file;

import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.VideoAttribute;
import com.github.filefusion.util.ExecUtil;
import com.github.filefusion.util.Json;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.exec.CommandLine;
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

    private static final String GET_VIDEO_DIMENSIONS_COMMAND = "ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of json %s";
    private static final Map<String, MimeType> DASH_FILE_MIME_TYPE = Collections.unmodifiableMap(new HashMap<>() {{
        put(".mpd", FileAttribute.MimeType.DASH.value());
        put(".m4s", FileAttribute.MimeType.MP4.value());
    }});

    private static CommandLine buildDashGenerationCommand(Path input, Integer dimensionsSize,
                                                          StringBuilder videoSplit, StringBuilder videoScale,
                                                          List<MapStream> mapStreamList, Path output) {
        CommandLine commandLine = new CommandLine("ffmpeg");
        commandLine.addArgument("-v");
        commandLine.addArgument("error");
        commandLine.addArgument("-hwaccel");
        commandLine.addArgument("auto");
        commandLine.addArgument("-i");
        commandLine.addArgument(input.toString());

        commandLine.addArgument("-filter_complex");
        commandLine.addArgument("[0:v]split=%s%s;%s".formatted(dimensionsSize, videoSplit, videoScale));

        commandLine.addArgument("-c:v");
        commandLine.addArgument(VideoAttribute.VIDEO_CODEC);
        commandLine.addArgument("-pix_fmt");
        commandLine.addArgument(VideoAttribute.VIDEO_PIX_FORMAT);
        commandLine.addArgument("-r");
        commandLine.addArgument(String.valueOf(VideoAttribute.VIDEO_FPS));
        commandLine.addArgument("-fps_mode");
        commandLine.addArgument("cfr");
        commandLine.addArgument("-g");
        commandLine.addArgument(String.valueOf(VideoAttribute.VIDEO_FPS * VideoAttribute.MEDIA_SEGMENT_DURATION));
        commandLine.addArgument("-keyint_min");
        commandLine.addArgument(String.valueOf(VideoAttribute.VIDEO_FPS * VideoAttribute.MEDIA_SEGMENT_DURATION));
        commandLine.addArgument("-sc_threshold");
        commandLine.addArgument("0");
        commandLine.addArgument("-b_strategy");
        commandLine.addArgument("0");

        for (MapStream mapStream : mapStreamList) {
            commandLine.addArgument(mapStream.getMap());
            commandLine.addArgument(mapStream.getOutName());
            commandLine.addArgument(mapStream.getPresetName());
            commandLine.addArgument(mapStream.getPreset());
            commandLine.addArgument(mapStream.getCrfName());
            commandLine.addArgument(mapStream.getCrf());
            commandLine.addArgument(mapStream.getMaxRateName());
            commandLine.addArgument(mapStream.getMaxRate());
            commandLine.addArgument(mapStream.getBufSizeName());
            commandLine.addArgument(mapStream.getBufSize());
        }
        commandLine.addArgument("-map");
        commandLine.addArgument("0:a:0?");
        commandLine.addArgument("-c:a");
        commandLine.addArgument(VideoAttribute.AUDIO_CODEC);
        commandLine.addArgument("-b:a");
        commandLine.addArgument(VideoAttribute.AUDIO_BANDWIDTH);
        commandLine.addArgument("-ar");
        commandLine.addArgument(VideoAttribute.AUDIO_RATE);
        commandLine.addArgument("-ac");
        commandLine.addArgument(VideoAttribute.AUDIO_CHANNEL);

        commandLine.addArgument("-f");
        commandLine.addArgument("dash");
        commandLine.addArgument("-adaptation_sets");
        commandLine.addArgument("id=0,streams=v id=1,streams=a", false);
        commandLine.addArgument("-frag_type");
        commandLine.addArgument("none");
        commandLine.addArgument("-seg_duration");
        commandLine.addArgument(String.valueOf(VideoAttribute.MEDIA_SEGMENT_DURATION));
        commandLine.addArgument("-init_seg_name");
        commandLine.addArgument("init-stream$RepresentationID$.$ext$");
        commandLine.addArgument("-media_seg_name");
        commandLine.addArgument("chunk-stream$RepresentationID$-$Number%05d$.$ext$");
        commandLine.addArgument("-y");
        commandLine.addArgument(output.toString());
        return commandLine;
    }

    private static GetVideoInfoResult getVideoDimensionsInfo(Path path, Duration videoGenerateTimeout)
            throws ReadVideoInfoException, IOException, ExecutionException, InterruptedException {
        String command = GET_VIDEO_DIMENSIONS_COMMAND.formatted(path);
        ExecUtil.ExecResult execResult = ExecUtil.exec(command, videoGenerateTimeout);
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
                    if (resolution == VideoAttribute.Resolution.P360) {
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

    public static boolean isDashSupported(String mimeType, List<String> supportedMimeTypes) {
        return StringUtils.hasLength(mimeType) && supportedMimeTypes.contains(mimeType);
    }

    public static MimeType getDashFileMimeType(String fileName) {
        return DASH_FILE_MIME_TYPE.entrySet().stream()
                .filter(entry -> fileName.endsWith(entry.getKey()))
                .findFirst()
                .map(Map.Entry::getValue)
                .orElse(null);
    }

    public static void generateMediaDash(Path originalPath, Path targetPath, Duration videoGenerateTimeout)
            throws ReadVideoInfoException, IOException, ExecutionException, InterruptedException {
        GetVideoInfoResult videoInfo = getVideoDimensionsInfo(originalPath, videoGenerateTimeout);
        int[] originalDimensions = new int[]{videoInfo.getStreams().getFirst().getWidth(), videoInfo.getStreams().getFirst().getHeight()};
        Map<VideoAttribute.Resolution, int[]> targetDimensionsMap = getVideoScaleDimensions(originalDimensions);

        int index = 1;
        StringBuilder videoSplit = new StringBuilder();
        StringBuilder videoScale = new StringBuilder();
        List<MapStream> mapStreamList = new ArrayList<>();
        for (Map.Entry<VideoAttribute.Resolution, int[]> entry : targetDimensionsMap.entrySet()) {
            VideoAttribute.Resolution resolution = entry.getKey();
            int[] targetDimensions = entry.getValue();
            int width = targetDimensions[0];
            int height = targetDimensions[1];
            String preset = resolution.preset();
            long crf = resolution.crf();
            long maxRate = resolution.maxRate();
            videoSplit.append("[v").append(index).append("]");
            videoScale.append("[v").append(index).append("]scale=").append(width).append(":").append(height)
                    .append(":force_original_aspect_ratio=decrease,pad=ceil(iw/2)*2:ceil(ih/2)*2")
                    .append("[v").append(index).append("out];");
            mapStreamList.add(new MapStream("-map", "[v" + index + "out]",
                    "-preset", preset,
                    "-crf", String.valueOf(crf),
                    "-maxrate:v:" + (index - 1), maxRate + "k",
                    "-bufsize:v:" + (index - 1), (maxRate * 2) + "k"));
            index++;
        }
        CommandLine commandLine = buildDashGenerationCommand(originalPath, targetDimensionsMap.size(), videoSplit, videoScale, mapStreamList, targetPath);
        Files.createDirectories(targetPath.getParent());
        ExecUtil.exec(commandLine, videoGenerateTimeout);
    }

    @Data
    @AllArgsConstructor
    private static class MapStream implements Serializable {
        String map;
        String outName;
        String presetName;
        String preset;
        String crfName;
        String crf;
        String maxRateName;
        String maxRate;
        String bufSizeName;
        String bufSize;
    }

    @Data
    private static class GetVideoInfoResult implements Serializable {
        private List<Streams> streams;

        @Data
        private static class Streams implements Serializable {
            private int width;
            private int height;
        }
    }

    public static class ReadVideoInfoException extends Exception {
    }

}
