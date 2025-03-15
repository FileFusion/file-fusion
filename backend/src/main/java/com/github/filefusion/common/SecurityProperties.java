package com.github.filefusion.common;

import com.github.filefusion.util.EncryptUtil;
import io.jsonwebtoken.security.Jwks;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
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
        private static final String ALGORITHM = "Ed25519";

        private PublicKey publicKey;
        private PrivateKey privateKey;

        public Secret(Path publicKey, Path privateKey) throws IOException,
                NoSuchAlgorithmException, InvalidKeySpecException {
            if (!Files.exists(publicKey) || !Files.exists(privateKey)) {
                KeyPair pair = Jwks.CRV.Ed25519.keyPair().build();
                this.publicKey = pair.getPublic();
                this.privateKey = pair.getPrivate();
                writeKey(publicKey, this.publicKey, KeyType.PUBLIC);
                writeKey(privateKey, this.privateKey, KeyType.PRIVATE);
            } else {
                this.publicKey = (PublicKey) loadKey(publicKey, KeyType.PUBLIC);
                this.privateKey = (PrivateKey) loadKey(privateKey, KeyType.PRIVATE);
            }
        }

        private static Key loadKey(Path path, KeyType type)
                throws NoSuchAlgorithmException, InvalidKeySpecException, IOException {
            byte[] key = EncryptUtil.base64ToBytes(Files.readString(path)
                    .replaceAll(".*?-----", "")
                    .replaceAll("\\s+", ""));
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            if (KeyType.PUBLIC.equals(type)) {
                return keyFactory.generatePublic(new X509EncodedKeySpec(key));
            } else {
                return keyFactory.generatePrivate(new PKCS8EncodedKeySpec(key));
            }
        }

        private static void writeKey(Path path, Key key, KeyType type) throws IOException {
            Path parent = path.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            String keyString = type.getHeader() + "\n"
                    + formatBase64(EncryptUtil.bytesToBase64(key.getEncoded()))
                    + type.getFooter() + "\n";
            Files.writeString(path, keyString, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        }

        private static String formatBase64(String base64) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < base64.length(); i += 64) {
                sb.append(base64, i, Math.min(i + 64, base64.length())).append("\n");
            }
            return sb.toString();
        }

        @Getter
        @AllArgsConstructor
        private enum KeyType {
            PUBLIC("-----BEGIN PUBLIC KEY-----", "-----END PUBLIC KEY-----"),
            PRIVATE("-----BEGIN PRIVATE KEY-----", "-----END PRIVATE KEY-----");

            private final String header;
            private final String footer;
        }
    }

}
