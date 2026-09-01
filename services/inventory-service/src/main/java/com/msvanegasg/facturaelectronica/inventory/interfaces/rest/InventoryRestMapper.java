package com.msvanegasg.facturaelectronica.inventory.interfaces.rest;

import java.util.List;
import java.util.UUID;
import java.math.BigDecimal;

import com.msvanegasg.facturaelectronica.inventory.application.dto.ConfirmServiceSupplyConsumptionCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ConfirmedServiceSupplyConsumptionResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.CreateProductCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.CreateServiceSupplyReferenceCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.CreatePurchaseCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ProductResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseLineCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseLineResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.RegisterInventoryMovementCommand;
import com.msvanegasg.facturaelectronica.inventory.application.dto.ServiceSupplyReferenceResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.StockAvailabilityResult;
import com.msvanegasg.facturaelectronica.inventory.application.dto.SuggestedSupplyConsumptionResult;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.ConfirmServiceSupplyConsumptionRequest;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.ConfirmedServiceSupplyConsumptionResponse;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.InventoryMovementRequest;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.InventoryMovementResponse;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.ProductRequest;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.ProductResponse;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.PurchaseLineRequest;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.PurchaseLineResponse;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.PurchaseRequest;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.PurchaseResponse;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.ServiceSupplyReferenceRequest;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.ServiceSupplyReferenceResponse;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.StockAvailabilityResponse;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.SuggestedSupplyConsumptionResponse;

final class InventoryRestMapper {

    private static final String DEFAULT_TAX_CATEGORY_CODE = "IVA";
    private static final String DEFAULT_TAX_CODE = "IVA_19";
    private static final String DEFAULT_TAX_LABEL = "IVA 19%";
    private static final BigDecimal DEFAULT_TAX_RATE = new BigDecimal("19");

    private InventoryRestMapper() {
    }

    static CreateProductCommand toCommand(UUID companyId, ProductRequest request, UUID createdBy,
            String idempotencyKey) {
        return new CreateProductCommand(companyId, request.sku(), request.barcode(), request.name(),
                request.description(), request.itemType(), request.saleEnabled(), request.purchaseEnabled(),
                request.stockTracked(), request.salePrice(), request.cost(), request.initialStock(),
                defaultText(request.taxCategoryCode(), DEFAULT_TAX_CATEGORY_CODE),
                defaultText(request.taxCode(), DEFAULT_TAX_CODE),
                defaultText(request.taxLabel(), DEFAULT_TAX_LABEL),
                request.taxRate() == null ? DEFAULT_TAX_RATE : request.taxRate(), createdBy,
                idempotencyKey);
    }

    static RegisterInventoryMovementCommand toCommand(UUID companyId, InventoryMovementRequest request, UUID createdBy,
            String idempotencyKey) {
        return new RegisterInventoryMovementCommand(companyId, request.productId(), request.movementType(),
                request.quantity(), request.unitCost(), request.sourceDocumentType(), request.sourceDocumentId(),
                idempotencyKey, request.reason(), createdBy);
    }

    static CreateServiceSupplyReferenceCommand toCommand(UUID companyId, ServiceSupplyReferenceRequest request) {
        return new CreateServiceSupplyReferenceCommand(companyId, request.serviceProductId(),
                request.supplyProductId(), request.notes());
    }

    static ConfirmServiceSupplyConsumptionCommand toCommand(UUID companyId,
            ConfirmServiceSupplyConsumptionRequest request, UUID createdBy, String idempotencyKey) {
        return new ConfirmServiceSupplyConsumptionCommand(companyId, request.serviceProductId(),
                request.sourceDocumentId(), request.reason(), createdBy, idempotencyKey,
                request.lines().stream().map(InventoryRestMapper::toLineCommand).toList());
    }

    static CreatePurchaseCommand toCommand(UUID companyId, PurchaseRequest request, UUID createdBy,
            String idempotencyKey) {
        return new CreatePurchaseCommand(companyId, request.supplierId(), request.subtotal(), request.taxTotal(),
                request.total(), request.paymentCondition(), request.dueDate(), request.evidenceUrl(),
                idempotencyKey, createdBy,
                request.lines().stream().map(InventoryRestMapper::toLineCommand).toList());
    }

    static ProductResponse toResponse(ProductResult result) {
        return new ProductResponse(result.id(), result.companyId(), result.sku(), result.barcode(), result.name(),
                result.description(), result.itemType(), result.saleEnabled(), result.purchaseEnabled(),
                result.stockTracked(), result.salePrice(), result.cost(), result.taxCategoryCode(), result.taxCode(),
                result.taxLabel(), result.taxRate(), result.active(), result.currentStock(), result.createdAt(),
                result.updatedAt());
    }

    static InventoryMovementResponse toResponse(InventoryMovementResult result) {
        return new InventoryMovementResponse(result.id(), result.companyId(), result.productId(),
                result.movementType(), result.quantity(), result.unitCost(), result.previousStock(),
                result.resultingStock(), result.sourceDocumentType(), result.sourceDocumentId(),
                result.idempotencyKey(), result.createdBy(), result.reason(), result.movementAt());
    }

    static List<InventoryMovementResponse> toMovementResponses(List<InventoryMovementResult> results) {
        return results.stream().map(InventoryRestMapper::toResponse).toList();
    }

    static StockAvailabilityResponse toResponse(StockAvailabilityResult result) {
        return new StockAvailabilityResponse(result.companyId(), result.productId(), result.requestedQuantity(),
                result.currentStock(), result.available());
    }

    static ServiceSupplyReferenceResponse toResponse(ServiceSupplyReferenceResult result) {
        return new ServiceSupplyReferenceResponse(result.id(), result.companyId(), result.serviceProductId(),
                result.supplyProductId(), result.notes(), result.active(), result.createdAt());
    }

    static List<ServiceSupplyReferenceResponse> toServiceSupplyReferenceResponses(
            List<ServiceSupplyReferenceResult> results) {
        return results.stream().map(InventoryRestMapper::toResponse).toList();
    }

    static SuggestedSupplyConsumptionResponse toResponse(SuggestedSupplyConsumptionResult result) {
        return new SuggestedSupplyConsumptionResponse(result.serviceProductId(), result.supplyProductId(),
                result.supplySku(), result.supplyName(), result.currentStock(), result.unitCost(), result.notes());
    }

    static List<SuggestedSupplyConsumptionResponse> toSuggestedConsumptionResponses(
            List<SuggestedSupplyConsumptionResult> results) {
        return results.stream().map(InventoryRestMapper::toResponse).toList();
    }

    static ConfirmedServiceSupplyConsumptionResponse toResponse(ConfirmedServiceSupplyConsumptionResult result) {
        return new ConfirmedServiceSupplyConsumptionResponse(result.serviceProductId(), result.sourceDocumentId(),
                toMovementResponses(result.movements()));
    }

    static PurchaseResponse toResponse(PurchaseResult result) {
        return new PurchaseResponse(result.id(), result.companyId(), result.supplierId(), result.status(),
                result.subtotal(), result.taxTotal(), result.total(), result.paymentCondition(), result.dueDate(),
                result.evidenceUrl(), result.idempotencyKey(), result.createdAt(), result.confirmedAt(),
                result.lines().stream().map(InventoryRestMapper::toLineResponse).toList());
    }

    private static PurchaseLineCommand toLineCommand(PurchaseLineRequest request) {
        return new PurchaseLineCommand(request.productId(), request.description(), request.quantity(), request.unitCost(), request.subtotal(),
                request.tax(), request.total());
    }

    private static ConfirmServiceSupplyConsumptionCommand.Line toLineCommand(
            ConfirmServiceSupplyConsumptionRequest.Line request) {
        return new ConfirmServiceSupplyConsumptionCommand.Line(request.supplyProductId(), request.quantity());
    }

    private static PurchaseLineResponse toLineResponse(PurchaseLineResult result) {
        return new PurchaseLineResponse(result.id(), result.productId(), result.description(), result.quantity(), result.unitCost(),
                result.subtotal(), result.tax(), result.total());
    }

    private static String defaultText(String value, String defaultValue) {
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
