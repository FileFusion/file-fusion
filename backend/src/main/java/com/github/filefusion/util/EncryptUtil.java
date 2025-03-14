package com.github.filefusion.util;

import org.bouncycastle.jcajce.provider.digest.Blake3;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;

/**
 * EncryptUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
public final class EncryptUtil {

    private static final byte[] HEX_LOOKUP = new byte[128];
    private static final char[] HEX_TABLE = new char[256 * 2];

    static {
        Arrays.fill(HEX_LOOKUP, (byte) -1);
        for (int i = 0; i < 10; i++) {
            HEX_LOOKUP['0' + i] = (byte) i;
        }
        for (int i = 0; i < 6; i++) {
            HEX_LOOKUP['a' + i] = (byte) (10 + i);
            HEX_LOOKUP['A' + i] = (byte) (10 + i);
        }

        for (int i = 0; i < 256; i++) {
            HEX_TABLE[i << 1] = Character.forDigit((i >> 4) & 0x0F, 16);
            HEX_TABLE[(i << 1) + 1] = Character.forDigit(i & 0x0F, 16);
        }
        String tableStr = new String(HEX_TABLE);
        tableStr.getChars(0, tableStr.length(), HEX_TABLE, 0);
    }

    public static String bytesToHex(byte[] bytes) {
        int len = bytes.length;
        char[] hexChars = new char[len * 2];
        for (int i = 0; i < len; i++) {
            int byteVal = bytes[i] & 0xFF;
            hexChars[i * 2] = HEX_TABLE[byteVal << 1];
            hexChars[i * 2 + 1] = HEX_TABLE[(byteVal << 1) + 1];
        }
        return new String(hexChars);
    }

    public static byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] result = new byte[len / 2];
        char[] chars = hex.toCharArray();
        for (int i = 0; i < len; i += 2) {
            char c1 = chars[i];
            char c2 = chars[i + 1];
            byte b1 = HEX_LOOKUP[c1];
            byte b2 = HEX_LOOKUP[c2];
            result[i / 2] = (byte) ((b1 << 4) | b2);
        }
        return result;
    }

    public static String bytesToBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] base64ToBytes(String base64) {
        return Base64.getDecoder().decode(base64.getBytes(StandardCharsets.UTF_8));
    }

    public static String blake3(String original) {
        Blake3.Blake3_256 digest = new Blake3.Blake3_256();
        return bytesToHex(digest.digest(original.getBytes(StandardCharsets.UTF_8)));
    }

}
