package com.github.filefusion.util.file;

import com.github.filefusion.constant.FileAttribute;
import com.github.filefusion.constant.VideoAttribute;
import com.github.filefusion.util.ExecUtil;
import com.github.filefusion.util.Json;
import io.lindstrom.m3u8.model.*;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;

/**
 * M3u8Util
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class VideoUtil {

    private static final String GET_VIDEO_WIDTH_HEIGHT = "ffprobe -v error -select_streams v:0 -show_entries stream=width,height -of json %s";
    private static final String GET_VIDEO_DURATION = "ffprobe -v error -show_entries format=duration -of json %s";

    private static final int M3U8_VERSION = 3;
    private static final String URL_SEPARATOR = "/";
    private static final MasterPlaylistParser MASTER_PLAYLIST_PARSER = new MasterPlaylistParser();
    private static final MediaPlaylistParser MEDIA_PLAYLIST_PARSER = new MediaPlaylistParser();

    private static int[] getVideoWidthHeight(Path path, Duration videoPlayTimeout) throws VideoReadWidthHeightException, IOException {
        String exec = GET_VIDEO_WIDTH_HEIGHT.formatted(path);
        ExecUtil.ExecResult execResult = ExecUtil.exec(Arrays.asList(exec.split(" ")), videoPlayTimeout);
        if (!execResult.isSuccess()) {
            throw new VideoReadWidthHeightException();
        }
        GetVideoWidthHeightResult result = Json.parseObject(String.join("\n", execResult.getStdout()), GetVideoWidthHeightResult.class);
        return new int[]{result.getStreams().getFirst().getWidth(), result.getStreams().getFirst().getHeight()};
    }

    private static double getVideoDuration(Path path, Duration videoPlayTimeout) throws VideoReadDurationException, IOException {
        String exec = GET_VIDEO_DURATION.formatted(path);
        ExecUtil.ExecResult execResult = ExecUtil.exec(Arrays.asList(exec.split(" ")), videoPlayTimeout);
        if (!execResult.isSuccess()) {
            throw new VideoReadDurationException();
        }
        GetVideoDurationResult result = Json.parseObject(String.join("\n", execResult.getStdout()), GetVideoDurationResult.class);
        return result.getFormat().getDuration();
    }

    private static int[] getVideoScaleWidthHeight(int[] originalWidthHeight, VideoAttribute.Resolution resolution) {
        int originalWidth = originalWidthHeight[0];
        int originalHeight = originalWidthHeight[1];
        boolean isPortrait = originalHeight > originalWidth;

        int maxWidth = isPortrait ? resolution.height() : resolution.width();
        int maxHeight = isPortrait ? resolution.width() : resolution.height();

        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double scale = Math.min(widthRatio, heightRatio);

        return new int[]{
                (int) Math.round(originalWidth * scale),
                (int) Math.round(originalHeight * scale)
        };
    }

    public static boolean notSupportM3u8(String mimeType) {
        if (!StringUtils.hasLength(mimeType)) {
            return true;
        }
        return !mimeType.startsWith(FileAttribute.VIDEO_MIME_TYPE_PREFIX);
    }

    public static String getMasterPlaylist(Path path, Duration videoPlayTimeout)
            throws VideoReadWidthHeightException, IOException {
        int[] videoWidthHeight = getVideoWidthHeight(path, videoPlayTimeout);
        List<Variant> variantList = Arrays.stream(VideoAttribute.Resolution.values())
                .map(resolution -> {
                    int[] videoScaleWidthHeight = getVideoScaleWidthHeight(videoWidthHeight, resolution);
                    return Variant.builder()
                            .bandwidth(resolution.bandwidth())
                            .resolution(videoScaleWidthHeight[0], videoScaleWidthHeight[1])
                            .uri(resolution.alias() + URL_SEPARATOR + VideoAttribute.MEDIA_PLAYLIST_NAME)
                            .build();
                }).toList();
        return MASTER_PLAYLIST_PARSER.writePlaylistAsString(MasterPlaylist.builder()
                .version(M3U8_VERSION)
                .addVariants(variantList.toArray(new Variant[0]))
                .build());
    }

    public static String getMediaPlaylist(Path path, Duration videoPlayTimeout)
            throws VideoReadDurationException, IOException {
        double videoDuration = getVideoDuration(path, videoPlayTimeout);
        int segmentCount = (int) (videoDuration + VideoAttribute.MEDIA_SEGMENT_DURATION - 1) / VideoAttribute.MEDIA_SEGMENT_DURATION;
        MediaSegment[] mediaSegmentList = new MediaSegment[segmentCount];
        for (int i = 0; i < segmentCount; i++) {
            double duration = Math.min(videoDuration, VideoAttribute.MEDIA_SEGMENT_DURATION);
            mediaSegmentList[i] = MediaSegment.builder()
                    .duration(duration)
                    .uri(i + URL_SEPARATOR + VideoAttribute.MEDIA_SEGMENT_NAME)
                    .build();
            videoDuration -= VideoAttribute.MEDIA_SEGMENT_DURATION;
        }
        return MEDIA_PLAYLIST_PARSER.writePlaylistAsString(MediaPlaylist.builder()
                .version(M3U8_VERSION)
                .targetDuration(VideoAttribute.MEDIA_SEGMENT_DURATION)
                .mediaSequence(0)
                .playlistType(PlaylistType.VOD)
                .ongoing(false)
                .addMediaSegments(mediaSegmentList)
                .build());
    }

    @Data
    private static class GetVideoWidthHeightResult implements Serializable {
        List<VideoWidthHeight> streams;

        @Data
        private static class VideoWidthHeight implements Serializable {
            private int width;
            private int height;
        }
    }

    @Data
    private static class GetVideoDurationResult implements Serializable {
        VideoDuration format;

        @Data
        private static class VideoDuration implements Serializable {
            private double duration;
        }
    }

    public static class VideoReadWidthHeightException extends Exception {
    }

    public static class VideoReadDurationException extends Exception {
    }

}
