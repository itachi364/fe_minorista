package com.msvanegasg.facturaelectronica.bff.infrastructure.security;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffAuthProperties;
import com.msvanegasg.facturaelectronica.bff.infrastructure.security.BffSessionCipher.StoredValue;

@Component
@ConditionalOnProperty(name = "bff.auth.session-store", havingValue = "memory")
public class BffEncryptedSessionStore implements BffSessionStore {

    private final ConcurrentMap<String, StoredValue> oauthAttempts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, StoredValue> sessions = new ConcurrentHashMap<>();
    private final BffSessionCipher cipher;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public BffEncryptedSessionStore(BffSessionCipher cipher) {
        this(cipher, Clock.systemUTC());
    }

    public BffEncryptedSessionStore(ObjectMapper objectMapper, BffAuthProperties properties) {
        this(new BffSessionCipher(objectMapper, properties), Clock.systemUTC());
    }

    BffEncryptedSessionStore(ObjectMapper objectMapper, BffAuthProperties properties, Clock clock) {
        this(new BffSessionCipher(objectMapper, properties), clock);
    }

    BffEncryptedSessionStore(BffSessionCipher cipher, Clock clock) {
        this.cipher = cipher;
        this.clock = clock;
    }

    @Override
    public String createOAuthAttempt(String state, String nonce, String codeVerifier) {
        String id = opaqueId();
        Instant expiresAt = clock.instant().plus(Duration.ofMinutes(5));
        oauthAttempts.put(id, cipher.encrypt(new BffOAuthAttempt(state, nonce, codeVerifier, expiresAt), expiresAt));
        return id;
    }

    @Override
    public Optional<BffOAuthAttempt> consumeOAuthAttempt(String id, String expectedState) {
        StoredValue stored = removeIfUsable(oauthAttempts, id);
        if (stored == null) {
            return Optional.empty();
        }
        BffOAuthAttempt attempt = cipher.decrypt(stored, BffOAuthAttempt.class);
        if (!attempt.state().equals(expectedState)) {
            return Optional.empty();
        }
        return Optional.of(attempt);
    }

    @Override
    public String createSession(BffUserSession session) {
        String id = opaqueId();
        sessions.put(id, cipher.encrypt(session, session.expiresAt()));
        return id;
    }

    @Override
    public Optional<BffUserSession> findSession(String id) {
        StoredValue stored = removeIfExpired(sessions, id);
        if (stored == null) {
            return Optional.empty();
        }
        return Optional.of(cipher.decrypt(stored, BffUserSession.class));
    }

    @Override
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

    private String opaqueId() {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes(32));
    }

    private byte[] randomBytes(int size) {
        byte[] bytes = new byte[size];
        secureRandom.nextBytes(bytes);
        return bytes;
    }
}
