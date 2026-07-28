package com.msvanegasg.facturaelectronica.inventory.interfaces.rest;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementQuery;
import com.msvanegasg.facturaelectronica.inventory.application.dto.PurchaseQuery;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManageProductUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManagePurchaseUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.RegisterInventoryMovementUseCase;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.InventoryMovementResponse;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PurchaseStatus;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.ProductResponse;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.PurchaseResponse;

@RestController
@RequestMapping("/api/v1/reports")
public class InventoryReportController {

    private final ManageProductUseCase productUseCase;
    private final RegisterInventoryMovementUseCase movementUseCase;
    private final ManagePurchaseUseCase purchaseUseCase;

    public InventoryReportController(ManageProductUseCase productUseCase,
            RegisterInventoryMovementUseCase movementUseCase, ManagePurchaseUseCase purchaseUseCase) {
        this.productUseCase = productUseCase;
        this.movementUseCase = movementUseCase;
        this.purchaseUseCase = purchaseUseCase;
    }

    @GetMapping("/inventory-stock")
    public List<ProductResponse> inventoryStock(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestParam(required = false) Boolean active) {
        return productUseCase.findStock(companyId, active).stream()
                .map(InventoryRestMapper::toResponse)
                .toList();
    }

    @GetMapping("/purchases")
    public List<PurchaseResponse> purchases(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestParam(required = false) PurchaseStatus status,
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return purchaseUseCase.find(new PurchaseQuery(companyId, status, supplierId, from, to)).stream()
                .map(InventoryRestMapper::toResponse)
                .toList();
    }

    @GetMapping("/kardex")
    public List<InventoryMovementResponse> kardex(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestParam UUID productId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return InventoryRestMapper.toMovementResponses(
                movementUseCase.kardex(new InventoryMovementQuery(companyId, productId, from, to)));
    }
}
