package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageCountryUseCase;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.CountryRestMapper;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CountryRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CountryResponse;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/paises")
public class PaisController {

    private final ManageCountryUseCase manageCountryUseCase;

    public PaisController(ManageCountryUseCase manageCountryUseCase) {
        this.manageCountryUseCase = manageCountryUseCase;
    }

    @GetMapping
    public ResponseEntity<List<CountryResponse>> findAll() {
    	List<CountryResponse> all = manageCountryUseCase.findAll().stream()
                .map(CountryRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(all);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<CountryResponse>> findActive() {
    	List<CountryResponse> active = manageCountryUseCase.findActive().stream()
                .map(CountryRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(active);
    }
    
    @GetMapping("/inactive")
    public ResponseEntity<List<CountryResponse>> findActiveFalse() {
    	List<CountryResponse> active = manageCountryUseCase.findInactive().stream()
                .map(CountryRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(active);
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<CountryResponse> getByCodigo(@PathVariable("codigo") String codigo) {
        return ResponseEntity.ok(CountryRestMapper.toResponse(manageCountryUseCase.findByCode(codigo)));
    }

    @PostMapping
    public ResponseEntity<CountryRequest> create(@Valid @RequestBody CountryRequest paisDTO) {
        return ResponseEntity.ok(CountryRestMapper.toRequest(manageCountryUseCase.create(CountryRestMapper.toCommand(paisDTO))));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<CountryRequest> update(@PathVariable("codigo") String codigo, @Valid @RequestBody CountryRequest paisDTO) {
        return ResponseEntity.ok(CountryRestMapper.toRequest(manageCountryUseCase.update(codigo, CountryRestMapper.toCommand(paisDTO))));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> disable(@PathVariable("codigo") String codigo) {
        manageCountryUseCase.disable(codigo);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{codigoPais}/activar")
    public ResponseEntity<Void> activarPais(@PathVariable("codigoPais") String codigoPais) {
        manageCountryUseCase.enable(codigoPais);
        return ResponseEntity.noContent().build();
    }
}
