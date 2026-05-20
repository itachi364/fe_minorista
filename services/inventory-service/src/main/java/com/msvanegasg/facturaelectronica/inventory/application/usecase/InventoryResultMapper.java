package com.msvanegasg.facturaelectronica.inventory.application.usecase;

import java.math.BigDecimal;
import java.util.List;

import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ProductResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseLineResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseResult;
import com.msvanegasg.facturaelectronica.inventory.domain.model.InventoryMovement;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Product;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseLine;
import com.msvanegasg.facturaelectronica.inventory.domain.model.StockBalance;

final class InventoryResultMapper {

    private InventoryResultMapper() {
    }

    static ProductResult toProductResult(Product product, StockBalance balance) {
        BigDecimal stock = balance == null ? BigDecimal.ZERO : balance.currentStock();
        return new ProductResult(product.id(), product.companyId(), product.sku(), product.barcode(), product.name(),
                product.description(), product.salePrice(), product.cost(), product.active(), stock,
                product.createdAt(), product.updatedAt());
    }

    static InventoryMovementResult toMovementResult(InventoryMovement movement) {
        return new InventoryMovementResult(movement.id(), movement.companyId(), movement.productId(),
                movement.movementType(), movement.quantity(), movement.unitCost(), movement.previousStock(),
                movement.resultingStock(), movement.sourceDocumentType(), movement.sourceDocumentId(),
                movement.idempotencyKey(), movement.createdBy(), movement.movementAt());
    }

    static PurchaseResult toPurchaseResult(Purchase purchase) {
        return new PurchaseResult(purchase.id(), purchase.companyId(), purchase.supplierId(), purchase.status(),
                purchase.subtotal(), purchase.taxTotal(), purchase.total(), purchase.evidenceUrl(),
                purchase.idempotencyKey(), purchase.createdAt(), purchase.confirmedAt(),
                purchase.lines().stream().map(InventoryResultMapper::toPurchaseLineResult).toList());
    }

    private static PurchaseLineResult toPurchaseLineResult(PurchaseLine line) {
        return new PurchaseLineResult(line.id(), line.productId(), line.quantity(), line.unitCost(), line.subtotal(),
                line.tax(), line.total());
    }

    static List<InventoryMovementResult> toMovementResults(List<InventoryMovement> movements) {
        return movements.stream().map(InventoryResultMapper::toMovementResult).toList();
    }
}
