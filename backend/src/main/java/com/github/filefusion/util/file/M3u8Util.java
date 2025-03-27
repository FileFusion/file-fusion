package com.github.filefusion.util.file;

import com.github.filefusion.constant.FileAttribute;
import io.lindstrom.m3u8.model.*;
import io.lindstrom.m3u8.parser.MasterPlaylistParser;
import io.lindstrom.m3u8.parser.MediaPlaylistParser;
import org.springframework.util.StringUtils;

/**
 * M3u8Util
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class M3u8Util {

    private static final MasterPlaylistParser MASTER_PLAYLIST_PARSER = new MasterPlaylistParser();
    private static final MediaPlaylistParser MEDIA_PLAYLIST_PARSER = new MediaPlaylistParser();

    public static boolean notSupportM3u8(String mimeType) {
        if (!StringUtils.hasLength(mimeType)) {
            return true;
        }
        return !mimeType.startsWith(FileAttribute.VIDEO_MIME_TYPE_PREFIX);
    }

    public static String getM3u8MasterPlaylist(String id) {
        MasterPlaylist playlist = MasterPlaylist.builder()
                .version(4)
                .independentSegments(true)
                .addAlternativeRenditions(AlternativeRendition.builder()
                        .type(MediaType.AUDIO)
                        .name("Default audio")
                        .groupId("AUDIO")
                        .build())
                .addVariants(
                        Variant.builder()
                                .addCodecs("avc1.4d401f", "mp4a.40.2")
                                .bandwidth(900000)
                                .uri("v0.m3u8")
                                .build(),
                        Variant.builder()
                                .addCodecs("avc1.4d401f", "mp4a.40.2")
                                .bandwidth(900000)
                                .uri("v1.m3u8")
                                .resolution(1280, 720)
                                .build())
                .build();
        return MASTER_PLAYLIST_PARSER.writePlaylistAsString(playlist);
    }

    public static String getM3u8MediaPlaylist(String id) {
        MediaPlaylist mediaPlaylist = MediaPlaylist.builder()
                .version(3)
                .targetDuration(10)
                .mediaSequence(1)
                .ongoing(false)
                .addMediaSegments(
                        MediaSegment.builder()
                                .duration(9.009)
                                .uri("http://media.example.com/first.ts")
                                .build(),
                        MediaSegment.builder()
                                .duration(9.009)
                                .uri("http://media.example.com/second.ts")
                                .build(),
                        MediaSegment.builder()
                                .duration(3.003)
                                .uri("http://media.example.com/third.ts")
                                .build())
                .build();
        return MEDIA_PLAYLIST_PARSER.writePlaylistAsString(mediaPlaylist);
    }

}
