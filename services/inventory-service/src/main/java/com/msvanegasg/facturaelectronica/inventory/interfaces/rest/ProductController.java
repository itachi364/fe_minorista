package com.msvanegasg.facturaelectronica.inventory.interfaces.rest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.inventory.application.dto.InventoryMovementQuery;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManageProductUseCase;
import com.msvanegasg.facturaelectronica.inventory.application.port.in.RegisterInventoryMovementUseCase;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.InventoryMovementResponse;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.ProductRequest;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.ProductResponse;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.StockAvailabilityResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController {

    private final ManageProductUseCase productUseCase;
    private final RegisterInventoryMovementUseCase movementUseCase;

    public ProductController(ManageProductUseCase productUseCase, RegisterInventoryMovementUseCase movementUseCase) {
        this.productUseCase = productUseCase;
        this.movementUseCase = movementUseCase;
    }

    @PostMapping
    public ResponseEntity<ProductResponse> create(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody ProductRequest request) {
        ProductResponse response = InventoryRestMapper
                .toResponse(productUseCase.create(InventoryRestMapper.toCommand(companyId, request, userId,
                        idempotencyKey)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{productId}")
    public ProductResponse findById(@RequestHeader("X-Company-Id") UUID companyId, @PathVariable UUID productId) {
        return InventoryRestMapper.toResponse(productUseCase.findById(companyId, productId));
    }

    @GetMapping("/{productId}/availability")
    public StockAvailabilityResponse checkAvailability(@RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID productId, @RequestParam BigDecimal quantity) {
        return InventoryRestMapper.toResponse(productUseCase.checkAvailability(companyId, productId, quantity));
    }

    @GetMapping("/{productId}/kardex")
    public List<InventoryMovementResponse> kardex(@RequestHeader("X-Company-Id") UUID companyId,
            @PathVariable UUID productId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to) {
        return InventoryRestMapper.toMovementResponses(
                movementUseCase.kardex(new InventoryMovementQuery(companyId, productId, from, to)));
    }
}
