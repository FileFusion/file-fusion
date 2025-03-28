package com.github.filefusion.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * VideoAttribute
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class VideoAttribute {

    public static final String MASTER_PLAYLIST_NAME = "master.m3u8";
    public static final String MEDIA_PLAYLIST_NAME = "media.m3u8";
    public static final String MEDIA_SEGMENT_NAME = "segment.ts";
    public static final int MEDIA_SEGMENT_DURATION = 5;
    public static final String VIDEO_CODEC = "libx264";
    public static final String VIDEO_FORMAT = "mpegts";
    public static final String AUDIO_CODEC = "aac";
    public static final String VIDEO_CODEC_PRESET = "fast";

    public enum Resolution {
        P480("480P", 854, 480, 1500000, 64000),
        P720("720P", 1280, 720, 3000000, 128000),
        P1080("1080P", 1920, 1080, 6000000, 192000),
        K2("2K", 2560, 1440, 12000000, 256000),
        K4("4K", 3840, 2160, 24000000, 320000);

        private static final Map<String, Resolution> ALIAS_MAP = new HashMap<>();

        static {
            for (Resolution resolution : Resolution.values()) {
                ALIAS_MAP.put(resolution.alias(), resolution);
            }
        }

        private final String alias;
        private final int width;
        private final int height;
        private final long bandwidth;
        private final long audioBandwidth;

        Resolution(String alias, int width, int height, long bandwidth, long audioBandwidth) {
            this.alias = alias;
            this.width = width;
            this.height = height;
            this.bandwidth = bandwidth;
            this.audioBandwidth = audioBandwidth;
        }

        public static Resolution fromAlias(String alias) {
            return ALIAS_MAP.get(alias);
        }

        public String alias() {
            return this.alias;
        }

        public int width() {
            return this.width;
        }

        public int height() {
            return this.height;
        }

        public long bandwidth() {
            return this.bandwidth;
        }

        public long audioBandwidth() {
            return this.audioBandwidth;
        }
    }

}
