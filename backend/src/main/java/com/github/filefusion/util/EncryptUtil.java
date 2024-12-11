package com.github.filefusion.util;

import com.github.filefusion.common.HttpException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * EncryptUtil
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Configuration
public class EncryptUtil {

    private static String SECRET_KEY;
    private static String SECRET_IV;

    @Autowired
    public EncryptUtil(@Value("${security.secret.key}") String secretKey,
                       @Value("${security.secret.iv}") String secretIv) {
        SECRET_KEY = secretKey;
        SECRET_IV = secretIv;
    }

    public static String sha256(String original) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            byte[] hash = messageDigest.digest(original.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public static String aesEncoder(String original) {
        try {
            Cipher cipher = getCipher(Cipher.ENCRYPT_MODE);
            byte[] byteOriginal = original.getBytes(StandardCharsets.UTF_8);
            byte[] byteCipher = cipher.doFinal(byteOriginal);
            return Base64.getEncoder().encodeToString(byteCipher);
        } catch (BadPaddingException | IllegalBlockSizeException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    public static String aesDecoder(String original) {
        try {
            Cipher cipher = getCipher(Cipher.DECRYPT_MODE);
            byte[] byteOriginal = Base64.getDecoder().decode(original);
            byte[] byteCipher = cipher.doFinal(byteOriginal);
            return new String(byteCipher, StandardCharsets.UTF_8);
        } catch (IllegalBlockSizeException | BadPaddingException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    private static Cipher getCipher(int cipherMode) {
        try {
            SecretKey secretKey = new SecretKeySpec(SECRET_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(cipherMode, secretKey, new IvParameterSpec(SECRET_IV.getBytes(StandardCharsets.UTF_8)));
            return cipher;
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException
                 | NoSuchPaddingException | InvalidKeyException e) {
            throw new HttpException(HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}
