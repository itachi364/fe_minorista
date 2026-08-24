package com.msvanegasg.facturaelectronica.bff.infrastructure.security;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffAuthProperties;

@Component
public class BffSecurityStartupValidator implements ApplicationRunner {

    private final BffAuthProperties properties;

    public BffSecurityStartupValidator(BffAuthProperties properties) {
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (properties.isProductionEnvironment() && !properties.isCognitoMode()) {
            throw new IllegalStateException("AUTH_MODE=cognito is required in production.");
        }
        if (properties.isCognitoMode() && isBlank(properties.cognitoBaseUrl())) {
            throw new IllegalStateException("COGNITO_BASE_URL is required when AUTH_MODE=cognito.");
        }
        if (properties.isCognitoMode() && isBlank(properties.cognitoClientId())) {
            throw new IllegalStateException("COGNITO_CLIENT_ID is required when AUTH_MODE=cognito.");
        }
        if (properties.isProductionEnvironment() && isBlank(properties.sessionEncryptionKey())) {
            throw new IllegalStateException("BFF_SESSION_ENCRYPTION_KEY is required in production.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
