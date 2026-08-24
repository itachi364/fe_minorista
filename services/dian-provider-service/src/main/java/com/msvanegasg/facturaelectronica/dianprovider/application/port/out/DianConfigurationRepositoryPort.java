package com.msvanegasg.facturaelectronica.dianprovider.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;

public interface DianConfigurationRepositoryPort {

    DianCompanyConfiguration save(DianCompanyConfiguration configuration);

    Optional<DianCompanyConfiguration> findByCompanyId(UUID companyId);
}
