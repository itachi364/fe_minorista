package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.TipoGastoDTO;
import com.msvanegasg.facturaelectronica.mapper.TipoGastoMapper;
import com.msvanegasg.facturaelectronica.models.TipoDocumento;
import com.msvanegasg.facturaelectronica.models.TipoGasto;
import com.msvanegasg.facturaelectronica.service.TipoGastoService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tipogasto")
public class TipoGastoController {

    @Autowired
    private TipoGastoService tipoGastoService;

    @GetMapping
    public ResponseEntity<List<TipoGasto>> findAll() {
    	List<TipoGasto> all = tipoGastoService.findAll();
        return ResponseEntity.ok(all);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoGastoDTO> getById(@PathVariable("id") Long id) {
        TipoGasto tipo = tipoGastoService.findById(id);
        return ResponseEntity.ok(TipoGastoMapper.toDTO(tipo));
    }

    @PostMapping
    public ResponseEntity<TipoGastoDTO> create(@Valid @RequestBody TipoGastoDTO dto) {
        TipoGasto tipoGasto = TipoGastoMapper.toEntity(dto);
        tipoGasto.setActivo(true);
        TipoGasto saved = tipoGastoService.save(tipoGasto);
        return ResponseEntity.created(URI.create("/api/tipogasto/" + saved.getIdTipoGasto()))
                .body(TipoGastoMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoGastoDTO> update(@PathVariable("id") Long id, @Valid @RequestBody TipoGastoDTO dto) {
        TipoGasto existing = tipoGastoService.findById(id);
        existing.setNombre(dto.getNombre());
        existing.setDescripcion(dto.getDescripcion());
        TipoGasto updated = tipoGastoService.save(existing);
        return ResponseEntity.ok(TipoGastoMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        tipoGastoService.disableById(id);
        return ResponseEntity.noContent().build();
    }
}
