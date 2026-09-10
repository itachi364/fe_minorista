package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.util.Optional;

import com.msvanegasg.facturaelectronica.accounting.domain.model.MunicipalityReference;

public interface MunicipalityCatalogPort {
    Optional<MunicipalityReference> findActive(String municipalityCode);
}
