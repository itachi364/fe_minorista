package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.application.dto.CreateServiceSupplyReferenceCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ServiceSupplyReferenceResult;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManageServiceSupplyReferenceUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ServiceSupplyReferenceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryItemType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Product;
import com.msvanegasg.facturaelectronica.inventory.domain.model.ServiceSupplyReference;

public class ServiceSupplyReferenceManagementService implements ManageServiceSupplyReferenceUseCase {

    private final ServiceSupplyReferenceRepositoryPort referenceRepository;
    private final ProductRepositoryPort productRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public ServiceSupplyReferenceManagementService(ServiceSupplyReferenceRepositoryPort referenceRepository,
            ProductRepositoryPort productRepository, IdGeneratorPort idGenerator, ClockPort clock) {
        this.referenceRepository = Objects.requireNonNull(referenceRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public ServiceSupplyReferenceResult create(CreateServiceSupplyReferenceCommand command) {
        validate(command);
        Product service = findProduct(command.companyId(), command.serviceProductId());
        Product supply = findProduct(command.companyId(), command.supplyProductId());
        if (service.itemType() != InventoryItemType.SERVICE) {
            throw new IllegalStateException("service product must have item type SERVICE");
        }
        if (supply.itemType() == InventoryItemType.SERVICE || !supply.stockTracked()) {
            throw new IllegalStateException("supply product must be a stock tracked item");
        }
        if (referenceRepository.existsByCompanyIdAndServiceProductIdAndSupplyProductId(command.companyId(),
                command.serviceProductId(), command.supplyProductId())) {
            throw new IllegalStateException("service supply reference already exists");
        }
        ServiceSupplyReference reference = ServiceSupplyReference.create(idGenerator.newId(), command.companyId(),
                command.serviceProductId(), command.supplyProductId(), command.notes(), clock.now());
        return toResult(referenceRepository.save(reference));
    }

    @Override
    public List<ServiceSupplyReferenceResult> findByService(UUID companyId, UUID serviceProductId) {
        Product service = findProduct(companyId, serviceProductId);
        if (service.itemType() != InventoryItemType.SERVICE) {
            throw new IllegalStateException("service product must have item type SERVICE");
        }
        return referenceRepository.findByCompanyIdAndServiceProductId(companyId, serviceProductId).stream()
                .map(ServiceSupplyReferenceManagementService::toResult)
                .toList();
    }

    private Product findProduct(UUID companyId, UUID productId) {
        return productRepository.findByCompanyIdAndId(companyId, productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private static ServiceSupplyReferenceResult toResult(ServiceSupplyReference reference) {
        return new ServiceSupplyReferenceResult(reference.id(), reference.companyId(), reference.serviceProductId(),
                reference.supplyProductId(), reference.notes(), reference.active(), reference.createdAt());
    }

    private static void validate(CreateServiceSupplyReferenceCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.serviceProductId(), "serviceProductId is required");
        Objects.requireNonNull(command.supplyProductId(), "supplyProductId is required");
    }
}
