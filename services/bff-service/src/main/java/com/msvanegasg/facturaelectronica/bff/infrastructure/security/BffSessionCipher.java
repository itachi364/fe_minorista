package com.msvanegasg.facturaelectronica.bff.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffAuthProperties;

@Component
public class BffSessionCipher {

    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int NONCE_LENGTH_BYTES = 12;

    private final ObjectMapper objectMapper;
    private final SecretKey secretKey;
    private final SecureRandom secureRandom = new SecureRandom();

    public BffSessionCipher(ObjectMapper objectMapper, BffAuthProperties properties) {
        this.objectMapper = objectMapper;
        this.secretKey = keyFrom(properties.sessionEncryptionKey());
    }

    public StoredValue encrypt(Object value, Instant expiresAt) {
        try {
            byte[] nonce = randomBytes(NONCE_LENGTH_BYTES);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, nonce));
            byte[] encrypted = cipher.doFinal(objectMapper.writeValueAsBytes(value));
            return new StoredValue(nonce, encrypted, expiresAt);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to encrypt BFF session data.", exception);
        }
    }

    public <T> T decrypt(StoredValue value, Class<T> type) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, value.nonce()));
            return objectMapper.readValue(cipher.doFinal(value.encrypted()), type);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to decrypt BFF session data.", exception);
        }
    }

    private byte[] randomBytes(int size) {
        byte[] bytes = new byte[size];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

    private static SecretKey keyFrom(String configuredKey) {
        try {
            byte[] material;
            if (configuredKey == null || configuredKey.isBlank()) {
                material = new byte[32];
                new SecureRandom().nextBytes(material);
            } else {
                material = MessageDigest.getInstance("SHA-256")
                        .digest(configuredKey.strip().getBytes(StandardCharsets.UTF_8));
            }
            return new SecretKeySpec(Arrays.copyOf(material, 32), "AES");
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to prepare BFF session encryption key.", exception);
        }
    }

    public record StoredValue(byte[] nonce, byte[] encrypted, Instant expiresAt) {
    }
}
