package com.github.filefusion.util;

import jakarta.annotation.Nonnull;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * HashingInputStream
 *
 * @author hackyo
 * @since 2022/4/1
 */
public class HashingInputStream extends InputStream {

    private final InputStream inputStream;
    private final MessageDigest messageDigest;

    public HashingInputStream(InputStream inputStream) throws NoSuchAlgorithmException {
        this.inputStream = inputStream;
        this.messageDigest = MessageDigest.getInstance("SHA-256");
    }

    @Override
    public int read() throws IOException {
        int byteValue = inputStream.read();
        if (byteValue != -1) {
            messageDigest.update((byte) byteValue);
        }
        return byteValue;
    }

    @Override
    public int read(@Nonnull byte[] b) throws IOException {
        int byteValue = inputStream.read(b);
        if (byteValue != -1) {
            messageDigest.update((byte) byteValue);
        }
        return byteValue;
    }

    @Override
    public int read(@Nonnull byte[] b, int off, int len) throws IOException {
        int byteValue = inputStream.read(b, off, len);
        if (byteValue != -1) {
            messageDigest.update(b, off, byteValue);
        }
        return byteValue;

    }

    @Override
    public void close() throws IOException {
        inputStream.close();
    }

    public String getHashString() {
        return EncryptUtil.bytesToHex(messageDigest.digest());
    }

}
