package com.msvanegasg.facturaelectronica.identity.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.OperationalPin;

public interface OperationalPinRepositoryPort {

    OperationalPin save(OperationalPin operationalPin);

    Optional<OperationalPin> findByCompanyId(UUID companyId);
}
