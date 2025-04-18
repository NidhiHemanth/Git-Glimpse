package org.bootcamp.com.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.nio.file.Path;
import java.security.MessageDigest;

@Slf4j
public class ManifestUtil {
    @SneakyThrows
    public static String calculateCheckSum(Path filePath) {
        log.info("Calculating checkSum of the file {}", filePath.toString());
        final MessageDigest messageDigest = MessageDigest.getInstance("MD5");

        try (InputStream inputStream = new ClassPathResource(filePath.toString()).getInputStream()) {
            byte[] byteArray = new byte[1024];
            int byteCount = 0;

            while((byteCount = inputStream.read(byteArray)) != -1) {
                messageDigest.update(byteArray, 0, byteCount);
            }
        }

        byte[] bytes = messageDigest.digest();
        StringBuilder stringBuilder = new StringBuilder();
        for(byte b: bytes) {
            stringBuilder.append(Integer.toString((b & 0xff) + 0x100, 16).substring(1));
        }
        
        return stringBuilder.toString();
    }
}
