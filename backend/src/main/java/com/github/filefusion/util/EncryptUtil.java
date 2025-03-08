package com.github.filefusion.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * EncryptUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Component
public class EncryptUtil {

    public static final String AES = "AES";
    public static final String AES_TRANSFORMATION = "AES/CBC/PKCS5Padding";
    public static final String SHA_256 = "SHA-256";
    public static final String MD5 = "MD5";

    private static final byte[] HEX_LOOKUP = new byte[128];
    private static final char[] HEX_TABLE = new char[256 * 2];

    private static String SECRET_KEY;
    private static String SECRET_IV;

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

    @Autowired
    public EncryptUtil(@Value("${security.secret.key}") String secretKey,
                       @Value("${security.secret.iv}") String secretIv) {
        SECRET_KEY = secretKey;
        SECRET_IV = secretIv;
    }

    private static Cipher getCipher(int cipherMode) {
        try {
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(cipherMode,
                    new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), AES),
                    new IvParameterSpec(SECRET_IV.getBytes(StandardCharsets.UTF_8)));
            return cipher;
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                 | NoSuchPaddingException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
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

    public static String sha256(String original) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(SHA_256);
            return bytesToHex(messageDigest.digest(original.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String md5(String original) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance(MD5);
            return bytesToHex(messageDigest.digest(original.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static String aesEncoder(String original) {
        try {
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
            return bytesToHex(cipher.doFinal(original.getBytes(StandardCharsets.UTF_8)));
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new RuntimeException(e);
        }
    }

    public static String aesDecoder(String original) {
        try {
            Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
            return new String(cipher.doFinal(hexToBytes(original)), StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new RuntimeException(e);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
