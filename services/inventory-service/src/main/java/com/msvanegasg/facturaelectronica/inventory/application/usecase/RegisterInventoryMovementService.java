package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementQuery;
import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.RegisterInventoryMovementCommand;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.RegisterInventoryMovementUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.InventoryMovementRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.StockBalanceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovement;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Product;
import com.msvanegasg.facturaelectronica.inventory.domain.model.StockBalance;
import com.msvanegasg.facturaelectronica.eventing.DomainEventEnvelope;
import com.msvanegasg.facturaelectronica.eventing.DomainEventPublisherPort;
import com.msvanegasg.facturaelectronica.eventing.EventTypes;

public class RegisterInventoryMovementService implements RegisterInventoryMovementUseCase {

    private final ProductRepositoryPort productRepository;
    private final StockBalanceRepositoryPort stockBalanceRepository;
    private final InventoryMovementRepositoryPort movementRepository;
    private final DomainEventPublisherPort eventPublisher;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public RegisterInventoryMovementService(ProductRepositoryPort productRepository,
            StockBalanceRepositoryPort stockBalanceRepository, InventoryMovementRepositoryPort movementRepository,
            IdGeneratorPort idGenerator, ClockPort clock) {
        this(productRepository, stockBalanceRepository, movementRepository, DomainEventPublisherPort.noop(), idGenerator,
                clock);
    }

    public RegisterInventoryMovementService(ProductRepositoryPort productRepository,
            StockBalanceRepositoryPort stockBalanceRepository, InventoryMovementRepositoryPort movementRepository,
            DomainEventPublisherPort eventPublisher, IdGeneratorPort idGenerator, ClockPort clock) {
        this.productRepository = Objects.requireNonNull(productRepository);
        this.stockBalanceRepository = Objects.requireNonNull(stockBalanceRepository);
        this.movementRepository = Objects.requireNonNull(movementRepository);
        this.eventPublisher = Objects.requireNonNull(eventPublisher);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public InventoryMovementResult register(RegisterInventoryMovementCommand command) {
        validate(command);
        return movementRepository.findIdempotent(command.companyId(), command.sourceDocumentType(),
                command.sourceDocumentId(), command.movementType(), command.idempotencyKey())
                .map(InventoryResultMapper::toMovementResult)
                .orElseGet(() -> createMovement(command));
    }

    @Override
    public List<InventoryMovementResult> kardex(UUID companyId, UUID productId) {
        return InventoryResultMapper.toMovementResults(movementRepository.findKardex(companyId, productId));
    }

    @Override
    public List<InventoryMovementResult> kardex(InventoryMovementQuery query) {
        Objects.requireNonNull(query, "query is required");
        Objects.requireNonNull(query.companyId(), "companyId is required");
        Objects.requireNonNull(query.productId(), "productId is required");
        if (query.from() != null && query.to() != null && query.from().isAfter(query.to())) {
            throw new IllegalArgumentException("from cannot be after to");
        }
        return InventoryResultMapper.toMovementResults(
                movementRepository.findKardex(query.companyId(), query.productId(), query.from(), query.to()));
    }

    private InventoryMovementResult createMovement(RegisterInventoryMovementCommand command) {
        Product product = productRepository.findByCompanyIdAndId(command.companyId(), command.productId())
                .orElseThrow(() -> new ProductNotFoundException(command.productId()));
        if (!product.stockTracked()) {
            throw new IllegalStateException("inventory movements are only allowed for stock tracked items");
        }
        var now = clock.now();
        StockBalance previous = stockBalanceRepository.findByCompanyIdAndProductId(command.companyId(),
                command.productId()).orElseGet(() -> StockBalance.empty(command.companyId(), command.productId(), now));
        StockBalance resulting = previous.apply(command.movementType(), command.quantity(), command.unitCost(), now);
        InventoryMovement movement = InventoryMovement.from(idGenerator.newId(), previous, resulting,
                command.movementType(), command.quantity(), command.unitCost(), command.sourceDocumentType(),
                command.sourceDocumentId(), command.idempotencyKey(), command.reason(), command.createdBy(), now);
        stockBalanceRepository.save(resulting);
        InventoryMovement saved = movementRepository.save(movement);
        publishMovementRegistered(saved);
        return InventoryResultMapper.toMovementResult(saved);
    }

    private void publishMovementRegistered(InventoryMovement movement) {
        eventPublisher.publish(new DomainEventEnvelope(idGenerator.newId(), EventTypes.INVENTORY_MOVEMENT_REGISTERED,
                1, clock.now(), movement.companyId(), "InventoryMovement", movement.id(), "inventory-service", null,
                movement.idempotencyKey() + ":inventory-movement-registered", movementPayload(movement)));
    }

    private static Map<String, Object> movementPayload(InventoryMovement movement) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("movementId", movement.id().toString());
        payload.put("productId", movement.productId().toString());
        payload.put("movementType", movement.movementType().name());
        payload.put("quantity", movement.quantity());
        payload.put("unitCost", movement.unitCost());
        payload.put("previousStock", movement.previousStock());
        payload.put("resultingStock", movement.resultingStock());
        payload.put("sourceDocumentType", movement.sourceDocumentType().name());
        payload.put("sourceDocumentId", movement.sourceDocumentId().toString());
        payload.put("movementAt", movement.movementAt().toString());
        return payload;
    }
    private static void validate(RegisterInventoryMovementCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.productId(), "productId is required");
        Objects.requireNonNull(command.movementType(), "movementType is required");
        Objects.requireNonNull(command.quantity(), "quantity is required");
        Objects.requireNonNull(command.unitCost(), "unitCost is required");
        Objects.requireNonNull(command.sourceDocumentType(), "sourceDocumentType is required");
        Objects.requireNonNull(command.sourceDocumentId(), "sourceDocumentId is required");
        if (command.movementType().requiresReason() && (command.reason() == null || command.reason().isBlank())) {
            throw new IllegalArgumentException("reason is required for " + command.movementType());
        }
    }
}
