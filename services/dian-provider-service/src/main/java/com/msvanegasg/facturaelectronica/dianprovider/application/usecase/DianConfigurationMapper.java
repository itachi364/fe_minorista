package com.msvanegasg.facturaelectronica.dianprovider.application.usecase;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianConfigurationResult;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;

final class DianConfigurationMapper {

    private DianConfigurationMapper() {
    }

    static DianConfigurationResult toResult(DianCompanyConfiguration configuration) {
        return new DianConfigurationResult(configuration.id(), configuration.companyId(), configuration.mode(),
                configuration.environment(), configuration.softwareId(), configuration.softwarePinSecretRef() != null,
                configuration.technicalKeySecretRef() != null, configuration.certificateSecretRef() != null,
                configuration.certificateAlias(), configuration.certificateFingerprint(),
                configuration.certificateExpiresAt(), configuration.serviceBaseUrl(), configuration.testSetId(),
                configuration.acceptedResponsibility(), configuration.status(), configuration.lastTestStatus(),
                configuration.lastTestAt(), configuration.lastTestMessage(), configuration.createdAt(),
                configuration.updatedAt());
    }
}
