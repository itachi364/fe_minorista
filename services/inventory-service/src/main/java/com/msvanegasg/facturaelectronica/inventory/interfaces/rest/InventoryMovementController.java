package com.msvanegasg.facturaelectronica.inventory.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.inventory.application.port.in.RegisterInventoryMovementUseCase;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.InventoryMovementRequest;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.InventoryMovementResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/inventory-movements")
public class InventoryMovementController {

    private final RegisterInventoryMovementUseCase movementUseCase;

    public InventoryMovementController(RegisterInventoryMovementUseCase movementUseCase) {
        this.movementUseCase = movementUseCase;
    }

    @PostMapping
    public ResponseEntity<InventoryMovementResponse> register(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody InventoryMovementRequest request) {
        InventoryMovementResponse response = InventoryRestMapper
                .toResponse(movementUseCase.register(InventoryRestMapper.toCommand(companyId, request, userId,
                        idempotencyKey)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
