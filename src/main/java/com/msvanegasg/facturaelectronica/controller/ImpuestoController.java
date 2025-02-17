package com.msvanegasg.facturaelectronica.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.msvanegasg.facturaelectronica.models.Impuesto;
import com.msvanegasg.facturaelectronica.service.ImpuestoService;

import java.util.List;

@RestController
@RequestMapping("/api/impuesto")
public class ImpuestoController {

    @Autowired
    private ImpuestoService impuestoService;

    @GetMapping
    public List<Impuesto> getAll() {
        return impuestoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Impuesto> getById(@PathVariable Long id) {
        return impuestoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Impuesto create(@RequestBody Impuesto impuesto) {
        return impuestoService.save(impuesto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Impuesto> update(@PathVariable Long id, @RequestBody Impuesto impuesto) {
        return impuestoService.findById(id)
                .map(existing -> {
                	impuesto.setIdImpuesto(existing.getIdImpuesto());
                    return ResponseEntity.ok(impuestoService.save(impuesto));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        if (impuestoService.findById(id).isPresent()) {
        	impuestoService.disableById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

