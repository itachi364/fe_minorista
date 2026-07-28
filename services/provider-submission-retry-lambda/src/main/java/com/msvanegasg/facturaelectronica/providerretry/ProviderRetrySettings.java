package com.msvanegasg.facturaelectronica.providerretry;

import java.net.URI;

public record ProviderRetrySettings(URI providerBaseUri) {

    public ProviderRetrySettings {
        if (providerBaseUri == null) {
            throw new IllegalArgumentException("providerBaseUri is required");
        }
    }

    public static ProviderRetrySettings fromEnvironment() {
        String value = firstNonBlank("PROVIDER_RETRY_PROVIDER_BASE_URL", "BILLING_PROVIDER_SERVICE_URL");
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Provider retry base URL is required");
        }
        return new ProviderRetrySettings(URI.create(value.trim()));
    }

    private static String firstNonBlank(String primary, String fallback) {
        String primaryValue = System.getenv(primary);
        if (primaryValue != null && !primaryValue.isBlank()) {
            return primaryValue;
        }
        return System.getenv(fallback);
    }
}