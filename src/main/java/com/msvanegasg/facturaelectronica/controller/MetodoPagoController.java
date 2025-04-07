package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.MetodoPagoDTO;
import com.msvanegasg.facturaelectronica.mapper.MetodoPagoMapper;
import com.msvanegasg.facturaelectronica.models.MetodoPago;
import com.msvanegasg.facturaelectronica.service.MetodoPagoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/metodopago")
public class MetodoPagoController {

    @Autowired
    private MetodoPagoService metodoPagoService;

    @GetMapping
    public ResponseEntity<List<MetodoPago>> findAll() {
    	List<MetodoPago> all = metodoPagoService.findAll();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MetodoPagoDTO> getById(@PathVariable("id") Long id) {
        MetodoPago metodoPago = metodoPagoService.findById(id);
        return ResponseEntity.ok(MetodoPagoMapper.toDTO(metodoPago));
    }

    @PostMapping
    public ResponseEntity<MetodoPagoDTO> create(@Valid @RequestBody MetodoPagoDTO dto) {
        MetodoPago nuevoMetodo = MetodoPagoMapper.toEntity(dto);
        nuevoMetodo.setActivo(true);
        MetodoPago guardado = metodoPagoService.save(nuevoMetodo);
        return ResponseEntity.ok(MetodoPagoMapper.toDTO(guardado));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MetodoPagoDTO> update(@PathVariable("id") Long id, @Valid @RequestBody MetodoPagoDTO dto) {
        MetodoPago existente = metodoPagoService.findById(id);
        MetodoPago actualizado = MetodoPagoMapper.toEntity(dto);
        actualizado.setIdMetodoPago(existente.getIdMetodoPago());
        actualizado.setActivo(existente.getActivo());
        MetodoPago guardado = metodoPagoService.save(actualizado);
        return ResponseEntity.ok(MetodoPagoMapper.toDTO(guardado));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disable(@PathVariable("id") Long id) {
        metodoPagoService.disableById(id);
        return ResponseEntity.noContent().build();
    }
}
