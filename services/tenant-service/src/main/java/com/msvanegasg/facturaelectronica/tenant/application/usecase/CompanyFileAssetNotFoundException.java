package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import java.util.UUID;

public class CompanyFileAssetNotFoundException extends RuntimeException {

    public CompanyFileAssetNotFoundException(UUID assetId) {
        super("No existe archivo empresarial " + assetId + ".");
    }
}
