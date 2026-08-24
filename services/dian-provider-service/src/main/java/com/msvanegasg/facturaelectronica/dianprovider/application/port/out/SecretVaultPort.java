package com.msvanegasg.facturaelectronica.dianprovider.application.port.out;

import java.util.UUID;

public interface SecretVaultPort {

    String storeCompanySecret(UUID companyId, String secretName, String secretValue);
}
