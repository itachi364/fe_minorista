package com.msvanegasg.facturaelectronica.bff.infrastructure.security;

import java.util.Optional;

public interface BffSessionStore {

    String createOAuthAttempt(String state, String nonce, String codeVerifier);

    Optional<BffOAuthAttempt> consumeOAuthAttempt(String id, String expectedState);

    String createSession(BffUserSession session);

    Optional<BffUserSession> findSession(String id);

    void revokeSession(String id);
}
