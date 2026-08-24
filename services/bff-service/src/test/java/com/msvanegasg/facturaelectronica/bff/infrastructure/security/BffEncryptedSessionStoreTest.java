package com.msvanegasg.facturaelectronica.bff.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffAuthProperties;

class BffEncryptedSessionStoreTest {

    @Test
    void storesOauthAttemptsAsSingleUseValues() {
        BffEncryptedSessionStore store = store();

        String id = store.createOAuthAttempt("state-1", "nonce-1", "verifier-1");

        assertThat(store.consumeOAuthAttempt(id, "wrong-state")).isEmpty();
        assertThat(store.consumeOAuthAttempt(id, "state-1")).isEmpty();
    }

    @Test
    void returnsOauthAttemptWhenStateMatches() {
        BffEncryptedSessionStore store = store();

        String id = store.createOAuthAttempt("state-1", "nonce-1", "verifier-1");

        assertThat(store.consumeOAuthAttempt(id, "state-1"))
                .get()
                .extracting(BffOAuthAttempt::codeVerifier)
                .isEqualTo("verifier-1");
    }

    @Test
    void storesAndRevokesEncryptedUserSessions() {
        BffEncryptedSessionStore store = store();
        BffUserSession session = new BffUserSession(UUID.randomUUID(), "subject-1", "user@example.com", "User Example",
                Set.of("COMPANY_ADMIN"), "access", "id", "refresh", Instant.now().plusSeconds(3600), Instant.now());

        String id = store.createSession(session);

        assertThat(store.findSession(id)).contains(session);

        store.revokeSession(id);

        assertThat(store.findSession(id)).isEmpty();
    }

    private static BffEncryptedSessionStore store() {
        BffAuthProperties properties = new BffAuthProperties("cognito", "prod", "https://auth.example.com",
                "client-id", "https://api.example.com/api/v1/auth/callback", "https://app.example.com",
                "https://app.example.com", "test-session-encryption-key-32-chars", true, "Strict", true);
        return new BffEncryptedSessionStore(new ObjectMapper().findAndRegisterModules(), properties);
    }
}
