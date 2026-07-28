package com.msvanegasg.facturaelectronica.identity.infrastructure.security;

import java.security.SecureRandom;
import java.util.Base64;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.identity.application.port.out.TokenGeneratorPort;

@Component
public class SecureTokenGeneratorAdapter implements TokenGeneratorPort {

    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public String generate() {
        byte[] token = new byte[32];
        RANDOM.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }
}
