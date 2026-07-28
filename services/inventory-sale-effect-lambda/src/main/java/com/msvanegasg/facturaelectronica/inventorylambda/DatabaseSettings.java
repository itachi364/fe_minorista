package com.msvanegasg.facturaelectronica.inventorylambda;

import com.fasterxml.jackson.databind.ObjectMapper;

public record DatabaseSettings(String url, String username, String password) {

    public DatabaseSettings {
        url = required(url, "url");
        username = required(username, "username");
        password = password == null ? "" : password;
    }

    public static DatabaseSettings fromEnvironment() {
        String password = firstNonBlank("INVENTORY_DB_PASSWORD", "DB_PASSWORD");
        if (password == null || password.isBlank()) {
            password = new SecretsManagerPasswordResolver(new ObjectMapper()).resolve(
                    System.getenv("INVENTORY_DB_PASSWORD_SECRET_ARN"),
                    System.getenv("INVENTORY_DB_PASSWORD_SECRET_JSON_KEY"));
        }
        return new DatabaseSettings(
                firstNonBlank("INVENTORY_DB_URL", "DB_URL"),
                firstNonBlank("INVENTORY_DB_USERNAME", "DB_USERNAME"),
                password);
    }

    private static String firstNonBlank(String primary, String fallback) {
        String primaryValue = System.getenv(primary);
        if (primaryValue != null && !primaryValue.isBlank()) {
            return primaryValue;
        }
        return System.getenv(fallback);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Database " + field + " is required");
        }
        return value.trim();
    }
}
