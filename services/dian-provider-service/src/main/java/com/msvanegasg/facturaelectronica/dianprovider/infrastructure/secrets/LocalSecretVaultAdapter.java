package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.secrets;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.SecretVaultPort;

@Component
public class LocalSecretVaultAdapter implements SecretVaultPort {

    private final String environment;

    public LocalSecretVaultAdapter(@Value("${dian-provider.secrets.environment:local}") String environment) {
        this.environment = environment;
    }

    @Override
    public String storeCompanySecret(UUID companyId, String secretName, String secretValue) {
        if (companyId == null || secretName == null || secretName.isBlank() || secretValue == null
                || secretValue.isBlank()) {
            throw new IllegalArgumentException("secret metadata and value are required");
        }
        return "/facturaelectronica/%s/companies/%s/%s#sha256:%s".formatted(environment, companyId,
                normalize(secretName), sha256(secretValue).substring(0, 16));
    }

    private static String normalize(String value) {
        return value.strip().replace("\\", "/").replaceAll("[^A-Za-z0-9/_-]", "-");
    }

    private static String sha256(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder();
            for (byte current : digest) {
                builder.append("%02x".formatted(current));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }
}
