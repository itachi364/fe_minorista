package com.msvanegasg.facturaelectronica.bff.infrastructure.security;

import java.time.Instant;

public record BffOAuthAttempt(String state, String nonce, String codeVerifier, Instant expiresAt) {
}
