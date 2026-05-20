package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.msvanegasg.facturaelectronica.exception.compra.CompraNoEditableException;
import com.msvanegasg.facturaelectronica.exception.compra.CompraNotFoundException;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ProductPurchaseInfo;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseLineCommand;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManagePurchaseUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductLookupPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.ProductStockPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.PurchaseRepositoryPort;
import com.msvanegasg.facturaelectronica.inventory.application.port.out.SupplierLookupPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseLine;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;

public class PurchaseManagementService implements ManagePurchaseUseCase {

    private final PurchaseRepositoryPort purchaseRepository;
    private final ProductLookupPort productLookup;
    private final ProductStockPort productStock;
    private final SupplierLookupPort supplierLookup;
    private final ClockPort clock;

    public PurchaseManagementService(PurchaseRepositoryPort purchaseRepository, ProductLookupPort productLookup,
            ProductStockPort productStock, SupplierLookupPort supplierLookup, ClockPort clock) {
        this.purchaseRepository = Objects.requireNonNull(purchaseRepository);
        this.productLookup = Objects.requireNonNull(productLookup);
        this.productStock = Objects.requireNonNull(productStock);
        this.supplierLookup = Objects.requireNonNull(supplierLookup);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public Purchase create(PurchaseCommand command) {
        validate(command);
        Long supplierId = supplierLookup.findSupplierIdByDocument(command.supplierDocumentNumber(),
                command.supplierDocumentTypeId());
        Map<Long, ProductPurchaseInfo> products = lookupProducts(command.lines());
        Purchase purchase = Purchase.create(supplierId, now(), command.subtotal(), command.taxTotal(), command.total(),
                command.evidenceUrl(), toLines(command.lines(), products));
        Purchase saved = purchaseRepository.save(purchase);
        increaseStock(command.lines());
        return purchaseRepository.save(saved.markProcessed());
    }

    @Override
    public Purchase updateIfPending(Long purchaseId, PurchaseCommand command) {
        validate(command);
        Purchase current = findById(purchaseId);
        if (current.status() != PurchaseStatus.PENDING) {
            throw new CompraNoEditableException(purchaseId);
        }
        Long supplierId = supplierLookup.findSupplierIdByDocument(command.supplierDocumentNumber(),
                command.supplierDocumentTypeId());
        Map<Long, ProductPurchaseInfo> products = lookupProducts(command.lines());
        Purchase updated = current.replacePending(supplierId, command.subtotal(), command.taxTotal(), command.total(),
                command.evidenceUrl(), toLines(command.lines(), products));
        Purchase saved = purchaseRepository.save(updated);
        increaseStock(command.lines());
        return purchaseRepository.save(saved.markProcessed());
    }

    @Override
    public Purchase findById(Long purchaseId) {
        return purchaseRepository.findById(purchaseId)
                .orElseThrow(() -> new CompraNotFoundException(purchaseId));
    }

    @Override
    public List<Purchase> findActive() {
        return purchaseRepository.findActive();
    }

    @Override
    public List<PurchaseLine> findDetailsByPurchaseId(Long purchaseId) {
        if (!purchaseRepository.existsById(purchaseId)) {
            throw new CompraNotFoundException(purchaseId);
        }
        return findById(purchaseId).lines();
    }

    private Map<Long, ProductPurchaseInfo> lookupProducts(List<PurchaseLineCommand> lines) {
        return lines.stream()
                .map(PurchaseLineCommand::barcode)
                .distinct()
                .map(productLookup::findByBarcode)
                .collect(Collectors.toMap(ProductPurchaseInfo::barcode, Function.identity()));
    }

    private static List<PurchaseLine> toLines(List<PurchaseLineCommand> lines, Map<Long, ProductPurchaseInfo> products) {
        return lines.stream()
                .map(line -> {
                    ProductPurchaseInfo product = products.get(line.barcode());
                    return PurchaseLine.create(line.barcode(), product.id(), line.quantity(), line.unitPrice(),
                            line.subtotal(), line.tax(), line.lineTotal());
                })
                .toList();
    }

    private void increaseStock(List<PurchaseLineCommand> lines) {
        lines.forEach(line -> productStock.increaseStock(line.barcode(), line.quantity()));
    }

    private LocalDateTime now() {
        Instant now = clock.now();
        return LocalDateTime.ofInstant(now, ZoneId.systemDefault());
    }

    private static void validate(PurchaseCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.supplierDocumentNumber(), "supplierDocumentNumber is required");
        Objects.requireNonNull(command.supplierDocumentTypeId(), "supplierDocumentTypeId is required");
        requireNonNegative(command.subtotal(), "subtotal");
        requireNonNegative(command.taxTotal(), "taxTotal");
        requireNonNegative(command.total(), "total");
        Objects.requireNonNull(command.lines(), "lines are required");
        if (command.lines().isEmpty()) {
            throw new IllegalArgumentException("lines are required");
        }
    }

    private static void requireNonNegative(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must be zero or positive");
        }
    }
}
