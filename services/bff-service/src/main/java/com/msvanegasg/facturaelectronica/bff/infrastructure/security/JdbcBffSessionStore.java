package com.msvanegasg.facturaelectronica.bff.infrastructure.security;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.security.SecureRandom;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.bff.infrastructure.security.BffSessionCipher.StoredValue;

@Component
@ConditionalOnProperty(name = "bff.auth.session-store", havingValue = "jdbc", matchIfMissing = true)
public class JdbcBffSessionStore implements BffSessionStore {

    private static final String OAUTH_ATTEMPT = "OAUTH_ATTEMPT";
    private static final String USER_SESSION = "USER_SESSION";

    private final JdbcTemplate jdbcTemplate;
    private final BffSessionCipher cipher;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    @Autowired
    public JdbcBffSessionStore(JdbcTemplate jdbcTemplate, BffSessionCipher cipher) {
        this(jdbcTemplate, cipher, Clock.systemUTC());
    }

    JdbcBffSessionStore(JdbcTemplate jdbcTemplate, BffSessionCipher cipher, Clock clock) {
        this.jdbcTemplate = jdbcTemplate;
        this.cipher = cipher;
        this.clock = clock;
    }

    @Override
    public String createOAuthAttempt(String state, String nonce, String codeVerifier) {
        String id = opaqueId();
        Instant expiresAt = clock.instant().plus(Duration.ofMinutes(5));
        save(id, OAUTH_ATTEMPT, cipher.encrypt(new BffOAuthAttempt(state, nonce, codeVerifier, expiresAt), expiresAt));
        return id;
    }

    @Override
    public Optional<BffOAuthAttempt> consumeOAuthAttempt(String id, String expectedState) {
        Optional<StoredValue> stored = findUsable(id, OAUTH_ATTEMPT);
        if (stored.isEmpty()) {
            return Optional.empty();
        }
        delete(id, OAUTH_ATTEMPT);
        BffOAuthAttempt attempt = cipher.decrypt(stored.get(), BffOAuthAttempt.class);
        if (!attempt.state().equals(expectedState)) {
            return Optional.empty();
        }
        return Optional.of(attempt);
    }

    @Override
    public String createSession(BffUserSession session) {
        String id = opaqueId();
        save(id, USER_SESSION, cipher.encrypt(session, session.expiresAt()));
        return id;
    }

    @Override
    public Optional<BffUserSession> findSession(String id) {
        return findUsable(id, USER_SESSION).map(value -> cipher.decrypt(value, BffUserSession.class));
    }

    @Override
    public void revokeSession(String id) {
        delete(id, USER_SESSION);
    }

    private void save(String id, String type, StoredValue value) {
        jdbcTemplate.update("""
                INSERT INTO secure_sessions (id, session_type, nonce, encrypted_payload, expires_at)
                VALUES (?, ?, ?, ?, ?)
                """, storedId(id), type, value.nonce(), value.encrypted(), value.expiresAt());
    }

    private Optional<StoredValue> findUsable(String id, String type) {
        if (id == null || id.isBlank()) {
            return Optional.empty();
        }
        List<StoredValue> values = jdbcTemplate.query("""
                SELECT nonce, encrypted_payload, expires_at
                  FROM secure_sessions
                 WHERE id = ?
                   AND session_type = ?
                   AND expires_at > ?
                """, this::mapStoredValue, storedId(id), type, clock.instant());
        if (values.isEmpty()) {
            delete(id, type);
            return Optional.empty();
        }
        return Optional.of(values.get(0));
    }

    private StoredValue mapStoredValue(ResultSet resultSet, int rowNumber) throws SQLException {
        return new StoredValue(resultSet.getBytes("nonce"), resultSet.getBytes("encrypted_payload"),
                resultSet.getTimestamp("expires_at").toInstant());
    }

    private void delete(String id, String type) {
        if (id != null && !id.isBlank()) {
            jdbcTemplate.update("DELETE FROM secure_sessions WHERE id = ? AND session_type = ?", storedId(id), type);
        }
    }

    private String opaqueId() {
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private static String storedId(String id) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(id.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to hash BFF session identifier.", exception);
        }
    }
}
