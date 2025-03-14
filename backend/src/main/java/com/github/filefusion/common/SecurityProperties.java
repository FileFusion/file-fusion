package com.github.filefusion.common;

import com.github.filefusion.util.EncryptUtil;
import io.jsonwebtoken.security.Jwks;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.List;
import java.util.Map;

/**
 * SecurityProperties
 *
 * @author hackyo
 * @since 2022/4/1
 */
@Data
@Component
@ConfigurationProperties("security")
public class SecurityProperties {

    private String[] allWhitelist;
    private Map<HttpMethod, String[]> whitelist;
    private Secret secret;

    @Data
    public static class Secret {
        private PublicKey publicKey;
        private PrivateKey privateKey;

        public Secret(Path publicKey, Path privateKey) throws IOException,
                NoSuchAlgorithmException, InvalidKeySpecException {
            if (!Files.exists(publicKey) || !Files.exists(privateKey)) {
                KeyPair pair = Jwks.CRV.Ed25519.keyPair().build();
                this.publicKey = pair.getPublic();
                this.privateKey = pair.getPrivate();
                writePublicKey(publicKey, this.publicKey);
                writePrivateKey(privateKey, this.privateKey);
            } else {
                this.publicKey = loadPublicKey(publicKey);
                this.privateKey = loadPrivateKey(privateKey);
            }
        }

        private static PublicKey loadPublicKey(Path path)
                throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
            List<String> keyString = Files.readAllLines(path);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(EncryptUtil.base64ToBytes(keyString.get(1)));
            return KeyFactory.getInstance("Ed25519").generatePublic(spec);
        }

        private static PrivateKey loadPrivateKey(Path path)
                throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
            List<String> keyString = Files.readAllLines(path);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(EncryptUtil.base64ToBytes(keyString.get(1)));
            return KeyFactory.getInstance("Ed25519").generatePrivate(spec);
        }

        private static void writePublicKey(Path path, PublicKey key) throws IOException {
            Files.createDirectories(path.getParent());
            String keyString = "-----BEGIN PUBLIC KEY-----\n"
                    + EncryptUtil.bytesToBase64(key.getEncoded())
                    + "\n-----END PUBLIC KEY-----\n";
            Files.writeString(path, keyString, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        }

        private static void writePrivateKey(Path path, PrivateKey key) throws IOException {
            Files.createDirectories(path.getParent());
            String keyString = "-----BEGIN PRIVATE KEY-----\n"
                    + EncryptUtil.bytesToBase64(key.getEncoded())
                    + "\n-----END PRIVATE KEY-----\n";
            Files.writeString(path, keyString, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
        }
    }

}
