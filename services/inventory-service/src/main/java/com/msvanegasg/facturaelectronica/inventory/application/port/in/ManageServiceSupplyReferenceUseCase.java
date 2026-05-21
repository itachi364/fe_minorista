package com.msvanegasg.facturaelectronica.inventory.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.application.dto.CreateServiceSupplyReferenceCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ServiceSupplyReferenceResult;

public interface ManageServiceSupplyReferenceUseCase {

    ServiceSupplyReferenceResult create(CreateServiceSupplyReferenceCommand command);

    List<ServiceSupplyReferenceResult> findByService(UUID companyId, UUID serviceProductId);
}
