package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.inventory.application.port.in.ManagePurchaseUseCase;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.PurchaseRestMapper;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.PurchaseLineRequest;
import com.msvanegasg.facturaelectronica.inventory.interfaces.rest.dto.PurchaseRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final ManagePurchaseUseCase managePurchaseUseCase;

    @PostMapping
    public ResponseEntity<PurchaseRequest> registrarCompra(@Valid @RequestBody PurchaseRequest compraDTO) {
        Purchase purchase = managePurchaseUseCase.create(PurchaseRestMapper.toCommand(compraDTO));
        return ResponseEntity.ok(PurchaseRestMapper.toResponse(purchase));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseRequest> obtenerCompraPorId(@PathVariable Long id) {
        Purchase purchase = managePurchaseUseCase.findById(id);
        return ResponseEntity.ok(PurchaseRestMapper.toResponse(purchase));
    }

    @GetMapping("/{id}/detalles")
    public ResponseEntity<List<PurchaseLineRequest>> obtenerDetallesDeCompra(@PathVariable Long id) {
        List<PurchaseLineRequest> details = managePurchaseUseCase.findDetailsByPurchaseId(id).stream()
                .map(PurchaseRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(details);
    }

    @GetMapping
    public ResponseEntity<List<PurchaseRequest>> listarComprasActivas() {
        List<PurchaseRequest> purchases = managePurchaseUseCase.findActive().stream()
                .map(PurchaseRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(purchases);
    }
}

