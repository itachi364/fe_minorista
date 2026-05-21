package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

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

public class RegisterInventoryMovementService implements RegisterInventoryMovementUseCase {

    private final ProductRepositoryPort productRepository;
    private final StockBalanceRepositoryPort stockBalanceRepository;
    private final InventoryMovementRepositoryPort movementRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public RegisterInventoryMovementService(ProductRepositoryPort productRepository,
            StockBalanceRepositoryPort stockBalanceRepository, InventoryMovementRepositoryPort movementRepository,
            IdGeneratorPort idGenerator, ClockPort clock) {
        this.productRepository = Objects.requireNonNull(productRepository);
        this.stockBalanceRepository = Objects.requireNonNull(stockBalanceRepository);
        this.movementRepository = Objects.requireNonNull(movementRepository);
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
        return InventoryResultMapper.toMovementResult(movementRepository.save(movement));
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
