package com.msvanegasg.facturaelectronica.inventory.application.port.out;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.domain.model.ServiceSupplyReference;

public interface ServiceSupplyReferenceRepositoryPort {

    boolean existsByCompanyIdAndServiceProductIdAndSupplyProductId(UUID companyId, UUID serviceProductId,
            UUID supplyProductId);

    List<ServiceSupplyReference> findByCompanyIdAndServiceProductId(UUID companyId, UUID serviceProductId);

    ServiceSupplyReference save(ServiceSupplyReference reference);
}
