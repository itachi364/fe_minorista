package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageParameterUseCase;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.ParameterRestMapper;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ParameterRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ParameterResponse;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/parametros")
public class ParametroController {

    private final ManageParameterUseCase manageParameterUseCase;

    public ParametroController(ManageParameterUseCase manageParameterUseCase) {
        this.manageParameterUseCase = manageParameterUseCase;
    }

    @GetMapping
    public ResponseEntity<List<ParameterResponse>> findAll() {
    	List<ParameterResponse> all = manageParameterUseCase.findAll().stream()
                .map(ParameterRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(all);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<ParameterResponse>> findActiveTrue() {
    	List<ParameterResponse> all = manageParameterUseCase.findActive().stream()
                .map(ParameterRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(all);
    }
    
    @GetMapping("/inactive")
    public ResponseEntity<List<ParameterResponse>> findActiveFalse() {
    	List<ParameterResponse> all = manageParameterUseCase.findInactive().stream()
                .map(ParameterRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParameterRequest> getById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(ParameterRestMapper.toRequest(manageParameterUseCase.findById(id)));
    }

    @PostMapping
    public ResponseEntity<ParameterRequest> create(@Valid @RequestBody ParameterRequest dto) {
        return ResponseEntity.ok(ParameterRestMapper.toRequest(
                manageParameterUseCase.create(ParameterRestMapper.toCommand(dto))));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParameterRequest> update(@PathVariable("id") Long id, @Valid @RequestBody ParameterRequest dto) {
        return ResponseEntity.ok(ParameterRestMapper.toRequest(
                manageParameterUseCase.update(id, ParameterRestMapper.toCommand(dto))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disable(@PathVariable("id") Long id) {
        manageParameterUseCase.disable(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{id}/activar")
    public ResponseEntity<Void> activarParametro(@PathVariable("id") Long id) {
    	manageParameterUseCase.enable(id);
        return ResponseEntity.noContent().build();
    }
}
