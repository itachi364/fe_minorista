package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.RegisterInventoryMovementCommand;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.RegisterInventoryMovementUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.InventoryMovementRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.StockBalanceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovement;
import com.msvanegasg.facturaelectronica.inventory.domain.model.StockBalance;

public class RegisterInventoryMovementService implements RegisterInventoryMovementUseCase {

    private final StockBalanceRepositoryPort stockBalanceRepository;
    private final InventoryMovementRepositoryPort movementRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public RegisterInventoryMovementService(
            StockBalanceRepositoryPort stockBalanceRepository,
            InventoryMovementRepositoryPort movementRepository,
            IdGeneratorPort idGenerator,
            ClockPort clock) {
        this.stockBalanceRepository = Objects.requireNonNull(stockBalanceRepository);
        this.movementRepository = Objects.requireNonNull(movementRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public InventoryMovementResult register(RegisterInventoryMovementCommand command) {
        validate(command);

        StockBalance previousBalance = stockBalanceRepository.findByCompanyIdAndProductId(
                command.companyId(),
                command.productId())
                .orElseGet(() -> StockBalance.empty(command.companyId(), command.productId()));
        StockBalance resultingBalance = previousBalance.apply(command.movementType(), command.quantity());

        InventoryMovement movement = InventoryMovement.fromStockChange(
                idGenerator.newId(),
                previousBalance,
                resultingBalance,
                command.movementType(),
                command.quantity(),
                command.sourceDocumentType(),
                command.sourceDocumentId(),
                command.createdBy(),
                clock.now());

        StockBalance savedBalance = stockBalanceRepository.save(resultingBalance);
        InventoryMovement savedMovement = movementRepository.save(movement);

        return new InventoryMovementResult(
                savedMovement.id(),
                savedBalance.companyId(),
                savedBalance.productId(),
                savedMovement.movementType(),
                savedMovement.quantity(),
                savedMovement.previousStock(),
                savedBalance.currentStock(),
                savedMovement.sourceDocumentType(),
                savedMovement.sourceDocumentId(),
                savedMovement.createdBy(),
                savedMovement.movementAt());
    }

    private static void validate(RegisterInventoryMovementCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.productId(), "productId is required");
        Objects.requireNonNull(command.movementType(), "movementType is required");
        Objects.requireNonNull(command.quantity(), "quantity is required");
        Objects.requireNonNull(command.sourceDocumentType(), "sourceDocumentType is required");
        Objects.requireNonNull(command.sourceDocumentId(), "sourceDocumentId is required");
    }
}
