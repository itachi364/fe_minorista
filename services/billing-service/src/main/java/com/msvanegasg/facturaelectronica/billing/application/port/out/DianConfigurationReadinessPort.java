package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.UUID;

public interface DianConfigurationReadinessPort {

    boolean isReadyForElectronicIssuing(UUID companyId);

    static DianConfigurationReadinessPort alwaysReady() {
        return companyId -> true;
    }
}
