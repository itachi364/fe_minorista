package com.msvanegasg.facturaelectronica.dianprovider.application.port.out;

import java.util.Base64;
import java.util.UUID;

public interface SecretVaultPort {

    String storeCompanySecret(UUID companyId, String secretName, String secretValue);

    default String storeCompanySecret(UUID companyId, String secretName, byte[] secretValue) {
        if (secretValue == null || secretValue.length == 0) {
            throw new IllegalArgumentException("secret metadata and value are required");
        }
        return storeCompanySecret(companyId, secretName, Base64.getEncoder().encodeToString(secretValue));
    }

    default boolean isCompanySecretRef(UUID companyId, String secretName, String secretRef) {
        if (companyId == null || secretName == null || secretName.isBlank() || secretRef == null
                || secretRef.isBlank()) {
            return false;
        }
        String normalizedSecret = secretName.strip().replace("\\", "/").replaceAll("[^A-Za-z0-9/_-]", "-");
        return secretRef.contains("/companies/" + companyId + "/" + normalizedSecret);
    }
}
