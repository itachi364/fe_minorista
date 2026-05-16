package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManagePaymentMethodUseCase;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.PaymentMethodRestMapper;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.PaymentMethodRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.PaymentMethodResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/metodopago")
public class MetodoPagoController {

    private final ManagePaymentMethodUseCase managePaymentMethodUseCase;

    public MetodoPagoController(ManagePaymentMethodUseCase managePaymentMethodUseCase) {
        this.managePaymentMethodUseCase = managePaymentMethodUseCase;
    }

    @GetMapping
    public ResponseEntity<List<PaymentMethodResponse>> findAll() {
    	List<PaymentMethodResponse> all = managePaymentMethodUseCase.findAll().stream()
                .map(PaymentMethodRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(all);
    }
    
    @GetMapping("/activos")
    public ResponseEntity<PaymentMethodResponse> findActivos() {
        return ResponseEntity.ok(PaymentMethodRestMapper.toResponse(managePaymentMethodUseCase.findActive()));
    }
    
    @GetMapping("/inactivos")
    public ResponseEntity<PaymentMethodResponse> findInactivos() {
        return ResponseEntity.ok(PaymentMethodRestMapper.toResponse(managePaymentMethodUseCase.findInactive()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentMethodRequest> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(PaymentMethodRestMapper.toRequest(managePaymentMethodUseCase.findById(id)));
    }

    @PostMapping
    public ResponseEntity<PaymentMethodRequest> create(@Valid @RequestBody PaymentMethodRequest dto) {
        return ResponseEntity.ok(PaymentMethodRestMapper.toRequest(
                managePaymentMethodUseCase.create(PaymentMethodRestMapper.toCommand(dto))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PaymentMethodRequest> update(@PathVariable("id") Long id, @Valid @RequestBody PaymentMethodRequest dto) {
        return ResponseEntity.ok(PaymentMethodRestMapper.toRequest(
                managePaymentMethodUseCase.update(id, PaymentMethodRestMapper.toCommand(dto))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disable(@PathVariable("id") Long id) {
        managePaymentMethodUseCase.disable(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/activar")
    public ResponseEntity<Void> activarMetodo(@PathVariable("id") Long id) {
    	managePaymentMethodUseCase.enable(id);
        return ResponseEntity.noContent().build();
    }
}
