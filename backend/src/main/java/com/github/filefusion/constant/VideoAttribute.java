package com.github.filefusion.constant;

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
    public static final int VIDEO_FPS = 24;
    public static final String AUDIO_CODEC = "aac";
    public static final String AUDIO_BANDWIDTH = "128k";
    public static final String AUDIO_RATE = "44100";
    public static final String AUDIO_CHANNEL = "2";

    public enum Resolution {
        P480("480P", 854, 480, "faster", 27, 2250),
        P720("720P", 1280, 720, "fast", 25, 4500),
        P1080("1080P", 1920, 1080, "medium", 23, 9000),
        K2("2K", 2560, 1440, "slow", 21, 18000),
        K4("4K", 3840, 2160, "slower", 19, 36000);

        private final String alias;
        private final int width;
        private final int height;
        private final String preset;
        private final long crf;
        private final long maxRate;

        Resolution(String alias, int width, int height, String preset, long crf, long maxRate) {
            this.alias = alias;
            this.width = width;
            this.height = height;
            this.preset = preset;
            this.crf = crf;
            this.maxRate = maxRate;
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

        public String preset() {
            return this.preset;
        }

        public long crf() {
            return this.crf;
        }

        public long maxRate() {
            return this.maxRate;
        }
    }

}
