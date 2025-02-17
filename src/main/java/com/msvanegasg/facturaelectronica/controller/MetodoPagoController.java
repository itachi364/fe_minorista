package com.msvanegasg.facturaelectronica.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.msvanegasg.facturaelectronica.models.MetodoPago;
import com.msvanegasg.facturaelectronica.service.MetodoPagoService;

import java.util.List;

@RestController
@RequestMapping("/api/metodo-pago")
public class MetodoPagoController {

    @Autowired
    private MetodoPagoService metodoPagoService;

    @GetMapping
    public List<MetodoPago> getAll() {
        return metodoPagoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetodoPago> getById(@PathVariable Long id) {
        return metodoPagoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public MetodoPago create(@RequestBody MetodoPago metodoPago) {
        return metodoPagoService.save(metodoPago);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetodoPago> update(@PathVariable Long id, @RequestBody MetodoPago metodoPago) {
        return metodoPagoService.findById(id)
                .map(existing -> {
                	metodoPago.setIdMetodoPago(existing.getIdMetodoPago());
                    return ResponseEntity.ok(metodoPagoService.save(metodoPago));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        if (metodoPagoService.findById(id).isPresent()) {
        	metodoPagoService.disableById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}

