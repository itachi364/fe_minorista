package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageExpenseTypeUseCase;
import com.msvanegasg.facturaelectronica.catalog.domain.model.ExpenseType;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.ExpenseTypeRestMapper;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ExpenseTypeRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ExpenseTypeResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/tipogasto")
public class TipoGastoController {

    private final ManageExpenseTypeUseCase manageExpenseTypeUseCase;

    public TipoGastoController(ManageExpenseTypeUseCase manageExpenseTypeUseCase) {
        this.manageExpenseTypeUseCase = manageExpenseTypeUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ExpenseTypeResponse>> findAll() {
    	List<ExpenseTypeResponse> all = manageExpenseTypeUseCase.findAll().stream()
                .map(ExpenseTypeRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(all);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<ExpenseTypeResponse>> findActiveTrue() {
    	List<ExpenseTypeResponse> active = manageExpenseTypeUseCase.findActive().stream()
                .map(ExpenseTypeRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(active);
    }
    
    @GetMapping("/inactive")
    public ResponseEntity<List<ExpenseTypeResponse>> findActiveFalse() {
    	List<ExpenseTypeResponse> inactive = manageExpenseTypeUseCase.findInactive().stream()
                .map(ExpenseTypeRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(inactive);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseTypeRequest> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ExpenseTypeRestMapper.toRequest(manageExpenseTypeUseCase.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ExpenseTypeRequest> create(@Valid @RequestBody ExpenseTypeRequest dto) {
        ExpenseType created = manageExpenseTypeUseCase.create(ExpenseTypeRestMapper.toCommand(dto));
        return ResponseEntity.created(URI.create("/api/tipogasto/" + created.id()))
                .body(ExpenseTypeRestMapper.toRequest(created));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseTypeRequest> update(@PathVariable("id") Long id, @Valid @RequestBody ExpenseTypeRequest dto) {
        return ResponseEntity.ok(ExpenseTypeRestMapper.toRequest(
                manageExpenseTypeUseCase.update(id, ExpenseTypeRestMapper.toCommand(dto))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        manageExpenseTypeUseCase.disable(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/activar")
    public ResponseEntity<Void> activarImpuesto(@PathVariable("id") Long id) {
		manageExpenseTypeUseCase.enable(id);
        return ResponseEntity.noContent().build();
    }
}
