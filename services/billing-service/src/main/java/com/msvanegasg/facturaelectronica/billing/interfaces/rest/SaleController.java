package com.msvanegasg.facturaelectronica.billing.interfaces.rest;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.billing.application.port.in.ManageSaleUseCase;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.SaleRequest;
import com.msvanegasg.facturaelectronica.billing.interfaces.rest.dto.SaleResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/sales")
public class SaleController {

    private final ManageSaleUseCase saleUseCase;

    public SaleController(ManageSaleUseCase saleUseCase) {
        this.saleUseCase = saleUseCase;
    }

    @PostMapping
    public ResponseEntity<SaleResponse> create(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader(value = "X-User-Id", required = false) UUID userId,
            @RequestHeader("Idempotency-Key") String idempotencyKey, @Valid @RequestBody SaleRequest request) {
        SaleResponse response = BillingRestMapper
                .toResponse(saleUseCase.create(BillingRestMapper.toCommand(companyId, request, userId, idempotencyKey)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{saleId}/confirm")
    public SaleResponse confirm(@RequestHeader("X-Company-Id") UUID companyId,
            @RequestHeader("Idempotency-Key") String idempotencyKey, @PathVariable UUID saleId) {
        return BillingRestMapper.toResponse(saleUseCase.confirm(companyId, saleId, idempotencyKey));
    }

    @GetMapping("/{saleId}")
    public SaleResponse findById(@RequestHeader("X-Company-Id") UUID companyId, @PathVariable UUID saleId) {
        return BillingRestMapper.toResponse(saleUseCase.findById(companyId, saleId));
    }
}
