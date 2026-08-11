package com.msvanegasg.facturaelectronica.inventory.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.application.dto.ConfirmServiceSupplyConsumptionCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ConfirmedServiceSupplyConsumptionResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.CreateServiceSupplyReferenceCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ServiceSupplyReferenceResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.SuggestedSupplyConsumptionResult;

public interface ManageServiceSupplyReferenceUseCase {

    ServiceSupplyReferenceResult create(CreateServiceSupplyReferenceCommand command);

    List<ServiceSupplyReferenceResult> findByService(UUID companyId, UUID serviceProductId);

    List<SuggestedSupplyConsumptionResult> suggestConsumptions(UUID companyId, UUID serviceProductId);

    ConfirmedServiceSupplyConsumptionResult confirmConsumption(ConfirmServiceSupplyConsumptionCommand command);
}
