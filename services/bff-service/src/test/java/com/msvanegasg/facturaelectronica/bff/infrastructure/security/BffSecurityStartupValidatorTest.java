package com.msvanegasg.facturaelectronica.bff.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffAuthProperties;

class BffSecurityStartupValidatorTest {

    @Test
    void acceptsSecureProductionConfiguration() {
        BffSecurityStartupValidator validator = new BffSecurityStartupValidator(properties("cognito", "prod",
                "https://auth.example.com", "client-id", true, "Strict", true,
                "test-session-encryption-key-32-chars"));

        assertThatCode(() -> validator.run(new DefaultApplicationArguments()))
                .doesNotThrowAnyException();
    }

    @Test
    void rejectsProductionWithoutSecureCookiesAndCsrf() {
        BffSecurityStartupValidator validator = new BffSecurityStartupValidator(properties("cognito", "prod",
                "https://auth.example.com", "client-id", false, "Strict", false,
                "test-session-encryption-key-32-chars"));

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BFF_COOKIE_SECURE");
    }

    @Test
    void rejectsUnknownSameSiteValue() {
        BffSecurityStartupValidator validator = new BffSecurityStartupValidator(properties("local", "local",
                "", "", false, "Invalid", false, "test-session-encryption-key-32-chars"));

        assertThatThrownBy(() -> validator.run(new DefaultApplicationArguments()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("BFF_COOKIE_SAME_SITE");
    }

    private static BffAuthProperties properties(String mode, String environment, String cognitoBaseUrl,
            String cognitoClientId, boolean cookieSecure, String sameSite, boolean csrfEnabled,
            String sessionEncryptionKey) {
        return new BffAuthProperties(mode, environment, cognitoBaseUrl, cognitoClientId,
                "https://api.example.com/api/v1/auth/callback", "https://app.example.com",
                "https://app.example.com", sessionEncryptionKey, cookieSecure, sameSite, csrfEnabled);
    }
}
