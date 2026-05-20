package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.inventory.application.dto.CreateProductCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ProductResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.RegisterInventoryMovementCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.StockAvailabilityResult;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManageProductUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.RegisterInventoryMovementUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.StockBalanceRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovementType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventorySourceDocumentType;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Product;
import com.msvanegasg.facturaelectronica.inventory.domain.model.StockBalance;

public class ProductManagementService implements ManageProductUseCase {

    private final ProductRepositoryPort productRepository;
    private final StockBalanceRepositoryPort stockBalanceRepository;
    private final RegisterInventoryMovementUseCase movementUseCase;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public ProductManagementService(ProductRepositoryPort productRepository,
            StockBalanceRepositoryPort stockBalanceRepository, RegisterInventoryMovementUseCase movementUseCase,
            IdGeneratorPort idGenerator, ClockPort clock) {
        this.productRepository = Objects.requireNonNull(productRepository);
        this.stockBalanceRepository = Objects.requireNonNull(stockBalanceRepository);
        this.movementUseCase = Objects.requireNonNull(movementUseCase);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public ProductResult create(CreateProductCommand command) {
        validate(command);
        if (productRepository.existsByCompanyIdAndSku(command.companyId(), command.sku())) {
            throw new ProductAlreadyExistsException(command.sku());
        }
        var now = clock.now();
        Product product = Product.create(idGenerator.newId(), command.companyId(), command.sku(), command.barcode(),
                command.name(), command.description(), command.salePrice(), command.cost(), now);
        Product saved = productRepository.save(product);
        if (command.initialStock() != null && command.initialStock().signum() > 0) {
            movementUseCase.register(new RegisterInventoryMovementCommand(command.companyId(), saved.id(),
                    InventoryMovementType.ADJUSTMENT_IN, command.initialStock(), command.cost(),
                    InventorySourceDocumentType.INITIAL_STOCK, saved.id(), command.idempotencyKey(),
                    command.createdBy()));
        }
        return findById(saved.companyId(), saved.id());
    }

    @Override
    public ProductResult findById(UUID companyId, UUID productId) {
        Product product = productRepository.findByCompanyIdAndId(companyId, productId)
                .orElseThrow(() -> new ProductNotFoundException(productId));
        StockBalance balance = stockBalanceRepository.findByCompanyIdAndProductId(companyId, productId).orElse(null);
        return InventoryResultMapper.toProductResult(product, balance);
    }

    @Override
    public StockAvailabilityResult checkAvailability(UUID companyId, UUID productId, BigDecimal quantity) {
        ProductResult product = findById(companyId, productId);
        boolean available = product.currentStock().compareTo(quantity) >= 0;
        return new StockAvailabilityResult(companyId, productId, quantity, product.currentStock(), available);
    }

    private static void validate(CreateProductCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.sku(), "sku is required");
        Objects.requireNonNull(command.name(), "name is required");
        Objects.requireNonNull(command.salePrice(), "salePrice is required");
        Objects.requireNonNull(command.cost(), "cost is required");
    }
}
