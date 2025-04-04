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

    public static final String MEDIA_MANIFEST_NAME = "stream.mpd";
    public static final int MEDIA_SEGMENT_DURATION = 3;
    public static final String VIDEO_CODEC = "libx264";
    public static final String VIDEO_PIX_FORMAT = "yuv420p";
    public static final String VIDEO_CODEC_PRESET = "fast";
    public static final int VIDEO_FPS = 24;
    public static final String AUDIO_CODEC = "aac";
    public static final String AUDIO_BANDWIDTH = "128k";
    public static final String AUDIO_RATE = "44100";
    public static final String AUDIO_CHANNEL = "2";

    public enum Resolution {
        P480("480P", 854, 480, 1500, 2250),
        P720("720P", 1280, 720, 3000, 4500),
        P1080("1080P", 1920, 1080, 6000, 9000),
        K2("2K", 2560, 1440, 12000, 18000),
        K4("4K", 3840, 2160, 24000, 36000);

        private static final Map<String, Resolution> ALIAS_MAP = new HashMap<>();

        static {
            for (Resolution resolution : Resolution.values()) {
                ALIAS_MAP.put(resolution.alias(), resolution);
            }
        }

        private final String alias;
        private final int width;
        private final int height;
        private final long rate;
        private final long maxRate;

        Resolution(String alias, int width, int height, long rate, long maxRate) {
            this.alias = alias;
            this.width = width;
            this.height = height;
            this.rate = rate;
            this.maxRate = maxRate;
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

        public long rate() {
            return this.rate;
        }

        public long maxRate() {
            return this.maxRate;
        }
    }

}
