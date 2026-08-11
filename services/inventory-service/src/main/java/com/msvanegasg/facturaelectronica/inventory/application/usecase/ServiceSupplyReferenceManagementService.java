package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.msvanegasg.facturaelectronica.inventory.application.dto.ConfirmServiceSupplyConsumptionCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ConfirmedServiceSupplyConsumptionResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.CreateServiceSupplyReferenceCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.RegisterInventoryMovementCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ServiceSupplyReferenceResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.SuggestedSupplyConsumptionResult;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManageServiceSupplyReferenceUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.RegisterInventoryMovementUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ServiceSupplyReferenceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.StockBalanceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryItemType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Product;
import com.msvanegasg.facturaelectronica.inventory.domain.model.ServiceSupplyReference;
import com.msvanegasg.facturaelectronica.inventory.domain.model.StockBalance;

public class ServiceSupplyReferenceManagementService implements ManageServiceSupplyReferenceUseCase {

    private final ServiceSupplyReferenceRepositoryPort referenceRepository;
    private final ProductRepositoryPort productRepository;
    private final StockBalanceRepositoryPort stockBalanceRepository;
    private final RegisterInventoryMovementUseCase movementUseCase;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public ServiceSupplyReferenceManagementService(ServiceSupplyReferenceRepositoryPort referenceRepository,
            ProductRepositoryPort productRepository, StockBalanceRepositoryPort stockBalanceRepository,
            RegisterInventoryMovementUseCase movementUseCase, IdGeneratorPort idGenerator, ClockPort clock) {
        this.referenceRepository = Objects.requireNonNull(referenceRepository);
        this.productRepository = Objects.requireNonNull(productRepository);
        this.stockBalanceRepository = Objects.requireNonNull(stockBalanceRepository);
        this.movementUseCase = Objects.requireNonNull(movementUseCase);
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

    @Override
    public List<SuggestedSupplyConsumptionResult> suggestConsumptions(UUID companyId, UUID serviceProductId) {
        Product service = findServiceProduct(companyId, serviceProductId);
        return referenceRepository.findByCompanyIdAndServiceProductId(companyId, service.id()).stream()
                .filter(ServiceSupplyReference::active)
                .map(reference -> toSuggestedConsumption(reference, findProduct(companyId, reference.supplyProductId())))
                .toList();
    }

    @Override
    public ConfirmedServiceSupplyConsumptionResult confirmConsumption(ConfirmServiceSupplyConsumptionCommand command) {
        validate(command);
        Product service = findServiceProduct(command.companyId(), command.serviceProductId());
        Map<UUID, ServiceSupplyReference> references = referenceRepository
                .findByCompanyIdAndServiceProductId(command.companyId(), service.id()).stream()
                .filter(ServiceSupplyReference::active)
                .collect(Collectors.toMap(ServiceSupplyReference::supplyProductId, Function.identity()));
        Set<UUID> seenSupplyIds = new HashSet<>();
        List<InventoryMovementResult> movements = command.lines().stream()
                .map(line -> registerConsumptionLine(command, references, seenSupplyIds, line))
                .toList();
        return new ConfirmedServiceSupplyConsumptionResult(service.id(), command.sourceDocumentId(), movements);
    }

    private InventoryMovementResult registerConsumptionLine(ConfirmServiceSupplyConsumptionCommand command,
            Map<UUID, ServiceSupplyReference> references, Set<UUID> seenSupplyIds,
            ConfirmServiceSupplyConsumptionCommand.Line line) {
        validate(line);
        if (!seenSupplyIds.add(line.supplyProductId())) {
            throw new IllegalArgumentException("supply product cannot be duplicated in the same confirmation");
        }
        if (!references.containsKey(line.supplyProductId())) {
            throw new IllegalStateException("supply product is not associated with the service");
        }
        Product supply = findProduct(command.companyId(), line.supplyProductId());
        if (supply.itemType() == InventoryItemType.SERVICE || !supply.stockTracked()) {
            throw new IllegalStateException("supply product must be a stock tracked item");
        }
        return movementUseCase.register(new RegisterInventoryMovementCommand(command.companyId(), supply.id(),
                InventoryMovementType.CONSUMPTION_OUT, line.quantity(), unitCost(command.companyId(), supply),
                InventorySourceDocumentType.MANUAL_SUPPLY_CONSUMPTION, command.sourceDocumentId(),
                command.idempotencyKey() + "-service-supply-" + supply.id(), command.reason(), command.createdBy()));
    }

    private Product findServiceProduct(UUID companyId, UUID serviceProductId) {
        Product service = findProduct(companyId, serviceProductId);
        if (service.itemType() != InventoryItemType.SERVICE) {
            throw new IllegalStateException("service product must have item type SERVICE");
        }
        return service;
    }

    private SuggestedSupplyConsumptionResult toSuggestedConsumption(ServiceSupplyReference reference, Product supply) {
        StockBalance balance = stockBalanceRepository.findByCompanyIdAndProductId(reference.companyId(), supply.id())
                .orElseGet(() -> StockBalance.empty(reference.companyId(), supply.id(), clock.now()));
        return new SuggestedSupplyConsumptionResult(reference.serviceProductId(), supply.id(), supply.sku(),
                supply.name(), balance.currentStock(), unitCost(balance, supply), reference.notes());
    }

    private Product findProduct(UUID companyId, UUID productId) {
        return productRepository.findByCompanyIdAndId(companyId, productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
    }

    private static ServiceSupplyReferenceResult toResult(ServiceSupplyReference reference) {
        return new ServiceSupplyReferenceResult(reference.id(), reference.companyId(), reference.serviceProductId(),
                reference.supplyProductId(), reference.notes(), reference.active(), reference.createdAt());
    }

    private BigDecimal unitCost(UUID companyId, Product supply) {
        return stockBalanceRepository.findByCompanyIdAndProductId(companyId, supply.id())
                .map(balance -> unitCost(balance, supply))
                .orElse(supply.cost());
    }

    private static BigDecimal unitCost(StockBalance balance, Product supply) {
        return balance.averageCost().signum() > 0 ? balance.averageCost() : supply.cost();
    }

    private static void validate(CreateServiceSupplyReferenceCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.serviceProductId(), "serviceProductId is required");
        Objects.requireNonNull(command.supplyProductId(), "supplyProductId is required");
    }

    private static void validate(ConfirmServiceSupplyConsumptionCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.serviceProductId(), "serviceProductId is required");
        Objects.requireNonNull(command.sourceDocumentId(), "sourceDocumentId is required");
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("idempotencyKey is required");
        }
        if (command.reason() == null || command.reason().isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }
        if (command.lines() == null || command.lines().isEmpty()) {
            throw new IllegalArgumentException("consumption lines are required");
        }
    }

    private static void validate(ConfirmServiceSupplyConsumptionCommand.Line line) {
        Objects.requireNonNull(line, "line is required");
        Objects.requireNonNull(line.supplyProductId(), "supplyProductId is required");
        Objects.requireNonNull(line.quantity(), "quantity is required");
        if (line.quantity().signum() <= 0) {
            throw new IllegalArgumentException("quantity must be greater than zero");
        }
    }
}
