package com.msvanegasg.facturaelectronica.dianprovider.application.port.in;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianConfigurationCommand;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianConfigurationResult;

public interface ManageDianConfigurationUseCase {

    Optional<DianConfigurationResult> findByCompanyId(UUID companyId);

    DianConfigurationResult save(DianConfigurationCommand command);

    DianConfigurationResult testConnection(UUID companyId, UUID userId);

    DianConfigurationResult activate(UUID companyId, UUID userId);

    DianConfigurationResult deactivate(UUID companyId, UUID userId);
}
