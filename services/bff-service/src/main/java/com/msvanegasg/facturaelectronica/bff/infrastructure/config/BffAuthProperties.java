package com.msvanegasg.facturaelectronica.bff.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "bff.auth")
public record BffAuthProperties(
        String mode,
        String environment,
        String cognitoBaseUrl,
        String cognitoClientId,
        String cognitoRedirectUri,
        String cognitoLogoutUri,
        String frontendBaseUrl,
        String sessionEncryptionKey,
        boolean cookieSecure,
        String cookieSameSite,
        boolean csrfEnabled) {

    public boolean isCognitoMode() {
        return "cognito".equalsIgnoreCase(mode);
    }

    public boolean isProductionEnvironment() {
        return "production".equalsIgnoreCase(environment) || "prod".equalsIgnoreCase(environment);
    }

    public String resolvedMode() {
        return mode == null || mode.isBlank() ? "local" : mode.strip().toLowerCase();
    }

    public String resolvedSameSite() {
        return cookieSameSite == null || cookieSameSite.isBlank() ? "Strict" : cookieSameSite.strip();
    }

    public String resolvedFrontendBaseUrl() {
        return frontendBaseUrl == null || frontendBaseUrl.isBlank() ? "http://localhost:5173" : frontendBaseUrl.strip();
    }
}
