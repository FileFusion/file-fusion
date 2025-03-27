package com.github.filefusion.constant;

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

    public enum Resolution {
        P1080("1080p", 1920, 1080, 9000128),
        P720("720p", 1280, 720, 3750128),
        P480("480p", 854, 480, 1500128);

        private final String alias;
        private final int width;
        private final int height;
        private final long bandwidth;

        Resolution(String alias, int width, int height, long bandwidth) {
            this.alias = alias;
            this.width = width;
            this.height = height;
            this.bandwidth = bandwidth;
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
    }

}
