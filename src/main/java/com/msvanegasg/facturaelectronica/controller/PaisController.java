package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.PaisDTO;
import com.msvanegasg.facturaelectronica.models.Pais;
import com.msvanegasg.facturaelectronica.service.PaisService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/paises")
public class PaisController {

    @Autowired
    private PaisService paisService;

    @GetMapping
    public ResponseEntity<List<Pais>> findAll() {
    	List<Pais> all = paisService.findAll();
        return ResponseEntity.ok(all);
    }
    
    @GetMapping("/active")
    public ResponseEntity<List<Pais>> findActive() {
    	List<Pais> active = paisService.findActive();
        return ResponseEntity.ok(active);
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<Pais> getByCodigo(@PathVariable("codigo") String codigo) {
        return ResponseEntity.ok(paisService.findById(codigo));
    }

    @PostMapping
    public ResponseEntity<PaisDTO> create(@Valid @RequestBody PaisDTO paisDTO) {
        return ResponseEntity.ok(paisService.save(paisDTO));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<PaisDTO> update(@PathVariable("codigo") String codigo, @Valid @RequestBody PaisDTO paisDTO) {
        return ResponseEntity.ok(paisService.update(codigo, paisDTO));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> disable(@PathVariable("codigo") String codigo) {
        paisService.disableById(codigo);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{codigoPais}/activar")
    public ResponseEntity<Void> activarPais(@PathVariable("codigoPais") String codigoPais) {
        paisService.activarPais(codigoPais);
        return ResponseEntity.noContent().build();
    }
}
