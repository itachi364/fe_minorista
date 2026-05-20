package com.msvanegasg.facturaelectronica.inventory.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManagePurchaseUseCase;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.PurchaseRequest;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.PurchaseResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/purchases")
public class PurchaseController {

    private final ManagePurchaseUseCase purchaseUseCase;

    public PurchaseController(ManagePurchaseUseCase purchaseUseCase) {
        this.purchaseUseCase = purchaseUseCase;
    }

    @PostMapping
    public ResponseEntity<PurchaseResponse> create(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody PurchaseRequest request) {
        PurchaseResponse response = InventoryRestMapper
                .toResponse(purchaseUseCase.create(InventoryRestMapper.toCommand(companyId, request, userId,
                        idempotencyKey)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{purchaseId}/confirm")
    public PurchaseResponse confirm(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId, @PathVariable UUID purchaseId) {
        return InventoryRestMapper.toResponse(purchaseUseCase.confirm(companyId, purchaseId, userId));
    }
}
