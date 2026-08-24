package com.msvanegasg.facturaelectronica.dianprovider.application.usecase;

import java.util.UUID;

public class DianConfigurationNotFoundException extends RuntimeException {

    public DianConfigurationNotFoundException(UUID companyId) {
        super("No existe configuracion DIAN para la empresa " + companyId + ".");
    }
}
