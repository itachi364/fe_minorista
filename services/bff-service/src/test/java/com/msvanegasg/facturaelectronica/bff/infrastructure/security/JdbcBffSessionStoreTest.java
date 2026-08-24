package com.msvanegasg.facturaelectronica.bff.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.mockito.ArgumentMatchers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffAuthProperties;
import com.msvanegasg.facturaelectronica.bff.infrastructure.security.BffSessionCipher.StoredValue;

class JdbcBffSessionStoreTest {

    @Test
    void persistsEncryptedUserSessionInJdbcStore() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcBffSessionStore store = store(jdbcTemplate);
        BffUserSession session = new BffUserSession(UUID.randomUUID(), "subject-1", "user@example.com",
                "User Example", Set.of("COMPANY_ADMIN"), "internal-token", "id-token", "refresh-token",
                Instant.parse("2026-08-24T20:00:00Z"), Instant.parse("2026-08-24T19:00:00Z"));

        String id = store.createSession(session);

        assertThat(id).isNotBlank();
        verify(jdbcTemplate).update(startsWith("INSERT INTO secure_sessions"), eq(storedId(id)), eq("USER_SESSION"),
                any(byte[].class), any(byte[].class), eq(session.expiresAt()));
    }

    @Test
    void readsAndDecryptsUsableUserSessionFromJdbcStore() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        BffSessionCipher cipher = cipher();
        JdbcBffSessionStore store = new JdbcBffSessionStore(jdbcTemplate, cipher,
                Clock.fixed(Instant.parse("2026-08-24T19:00:00Z"), ZoneOffset.UTC));
        BffUserSession session = new BffUserSession(UUID.randomUUID(), "subject-1", "user@example.com",
                "User Example", Set.of("COMPANY_ADMIN"), "internal-token", "id-token", "refresh-token",
                Instant.parse("2026-08-24T20:00:00Z"), Instant.parse("2026-08-24T19:00:00Z"));
        StoredValue storedValue = cipher.encrypt(session, session.expiresAt());
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getBytes("nonce")).thenReturn(storedValue.nonce());
        when(resultSet.getBytes("encrypted_payload")).thenReturn(storedValue.encrypted());
        when(resultSet.getTimestamp("expires_at")).thenReturn(Timestamp.from(storedValue.expiresAt()));
        when(jdbcTemplate.query(startsWith("SELECT nonce"), ArgumentMatchers.<RowMapper<StoredValue>>any(),
                eq(storedId("session-1")), eq("USER_SESSION"),
                eq(Instant.parse("2026-08-24T19:00:00Z"))))
                .thenAnswer(invocation -> List.of(invocation.<RowMapper<StoredValue>>getArgument(1).mapRow(resultSet, 0)));

        assertThat(store.findSession("session-1")).contains(session);
    }

    @Test
    void revokesUserSessionFromJdbcStore() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        JdbcBffSessionStore store = store(jdbcTemplate);

        store.revokeSession("session-1");

        verify(jdbcTemplate).update("DELETE FROM secure_sessions WHERE id = ? AND session_type = ?", storedId("session-1"),
                "USER_SESSION");
    }

    private static JdbcBffSessionStore store(JdbcTemplate jdbcTemplate) {
        return new JdbcBffSessionStore(jdbcTemplate, cipher(),
                Clock.fixed(Instant.parse("2026-08-24T19:00:00Z"), ZoneOffset.UTC));
    }

    private static BffSessionCipher cipher() {
        BffAuthProperties properties = new BffAuthProperties("cognito", "prod", "https://auth.example.com",
                "client-id", "https://api.example.com/api/v1/auth/callback", "https://app.example.com",
                "https://app.example.com", "test-session-encryption-key-32-chars", true, "Strict", true);
        return new BffSessionCipher(new ObjectMapper().findAndRegisterModules(), properties);
    }

    private static String storedId(String id) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(id.getBytes(StandardCharsets.UTF_8));
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
