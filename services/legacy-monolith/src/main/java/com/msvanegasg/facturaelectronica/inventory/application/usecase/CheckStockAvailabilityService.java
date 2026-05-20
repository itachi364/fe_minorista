package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import java.math.BigDecimal;
import java.util.Objects;

import com.msvanegasg.facturaelectronica.inventory.application.dto.CheckStockAvailabilityCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.StockAvailabilityResult;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.CheckStockAvailabilityUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.StockBalanceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.StockBalance;

public class CheckStockAvailabilityService implements CheckStockAvailabilityUseCase {

    private final StockBalanceRepositoryPort stockBalanceRepository;

    public CheckStockAvailabilityService(StockBalanceRepositoryPort stockBalanceRepository) {
        this.stockBalanceRepository = Objects.requireNonNull(stockBalanceRepository);
    }

    @Override
    public StockAvailabilityResult check(CheckStockAvailabilityCommand command) {
        validate(command);

        BigDecimal availableStock = stockBalanceRepository.findByCompanyIdAndProductId(
                command.companyId(),
                command.productId())
                .map(this::availableStock)
                .orElse(BigDecimal.ZERO);
        boolean available = availableStock.compareTo(command.requestedQuantity()) >= 0;

        return new StockAvailabilityResult(
                command.companyId(),
                command.productId(),
                command.requestedQuantity(),
                availableStock,
                available);
    }

    @Override
    public void requireAvailable(CheckStockAvailabilityCommand command) {
        StockAvailabilityResult result = check(command);
        if (!result.available()) {
            throw new IllegalStateException("stock is insufficient for requested quantity");
        }
    }

    private BigDecimal availableStock(StockBalance stockBalance) {
        return stockBalance.currentStock().subtract(stockBalance.reservedStock());
    }

    private static void validate(CheckStockAvailabilityCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.productId(), "productId is required");
        Objects.requireNonNull(command.requestedQuantity(), "requestedQuantity is required");
        if (command.requestedQuantity().signum() <= 0) {
            throw new IllegalArgumentException("requestedQuantity must be greater than zero");
        }
    }
}
