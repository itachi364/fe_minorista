package com.msvanegasg.facturaelectronica.bff.infrastructure.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffAuthProperties;

@Component
public class BffEncryptedSessionStore {

    private static final String CIPHER = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH_BITS = 128;
    private static final int NONCE_LENGTH_BYTES = 12;

    private final ConcurrentMap<String, StoredValue> oauthAttempts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, StoredValue> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;
    private final SecretKey secretKey;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public BffEncryptedSessionStore(ObjectMapper objectMapper, BffAuthProperties properties) {
        this(objectMapper, properties, Clock.systemUTC());
    }

    BffEncryptedSessionStore(ObjectMapper objectMapper, BffAuthProperties properties, Clock clock) {
        this.objectMapper = objectMapper;
        this.secretKey = keyFrom(properties.sessionEncryptionKey());
        this.clock = clock;
    }

    public String createOAuthAttempt(String state, String nonce, String codeVerifier) {
        String id = opaqueId();
        Instant expiresAt = clock.instant().plus(Duration.ofMinutes(5));
        oauthAttempts.put(id, encrypt(new BffOAuthAttempt(state, nonce, codeVerifier, expiresAt), expiresAt));
        return id;
    }

    public Optional<BffOAuthAttempt> consumeOAuthAttempt(String id, String expectedState) {
        StoredValue stored = removeIfUsable(oauthAttempts, id);
        if (stored == null) {
            return Optional.empty();
        }
        BffOAuthAttempt attempt = decrypt(stored, BffOAuthAttempt.class);
        if (!attempt.state().equals(expectedState)) {
            return Optional.empty();
        }
        return Optional.of(attempt);
    }

    public String createSession(BffUserSession session) {
        String id = opaqueId();
        sessions.put(id, encrypt(session, session.expiresAt()));
        return id;
    }

    public Optional<BffUserSession> findSession(String id) {
        StoredValue stored = removeIfExpired(sessions, id);
        if (stored == null) {
            return Optional.empty();
        }
        return Optional.of(decrypt(stored, BffUserSession.class));
    }

    public void revokeSession(String id) {
        if (id != null && !id.isBlank()) {
            sessions.remove(id);
        }
    }

    private StoredValue removeIfUsable(ConcurrentMap<String, StoredValue> store, String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        StoredValue value = removeIfExpired(store, id);
        if (value == null) {
            return null;
        }
        store.remove(id);
        return value;
    }

    private StoredValue removeIfExpired(ConcurrentMap<String, StoredValue> store, String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        StoredValue value = store.get(id);
        if (value == null) {
            return null;
        }
        if (!value.expiresAt().isAfter(clock.instant())) {
            store.remove(id);
            return null;
        }
        return value;
    }

    private StoredValue encrypt(Object value, Instant expiresAt) {
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

    private <T> T decrypt(StoredValue value, Class<T> type) {
        try {
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(GCM_TAG_LENGTH_BITS, value.nonce()));
            return objectMapper.readValue(cipher.doFinal(value.encrypted()), type);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to decrypt BFF session data.", exception);
        }
    }

    private String opaqueId() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes(32));
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

    private record StoredValue(byte[] nonce, byte[] encrypted, Instant expiresAt) {
    }
}
