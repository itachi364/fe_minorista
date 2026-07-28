package com.msvanegasg.facturaelectronica.accountinglambda;

import java.util.Objects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

public class SecretsManagerPasswordResolver {

    private final SecretsManagerClient client;
    private final ObjectMapper objectMapper;

    public SecretsManagerPasswordResolver(ObjectMapper objectMapper) {
        this(SecretsManagerClient.create(), objectMapper);
    }

    SecretsManagerPasswordResolver(SecretsManagerClient client, ObjectMapper objectMapper) {
        this.client = Objects.requireNonNull(client, "client is required");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
    }

    public String resolve(String secretArn, String jsonKey) {
        if (secretArn == null || secretArn.isBlank()) {
            return null;
        }
        String secret = client.getSecretValue(GetSecretValueRequest.builder().secretId(secretArn).build()).secretString();
        if (secret == null || secret.isBlank()) {
            return null;
        }
        String key = jsonKey == null || jsonKey.isBlank() ? "password" : jsonKey.trim();
        try {
            JsonNode root = objectMapper.readTree(secret);
            JsonNode value = root.path(key);
            if (!value.isMissingNode() && !value.isNull() && !value.asText().isBlank()) {
                return value.asText();
            }
        } catch (Exception ignored) {
            // Plain text secrets remain supported for non-RDS deployments.
        }
        return secret;
    }
}
