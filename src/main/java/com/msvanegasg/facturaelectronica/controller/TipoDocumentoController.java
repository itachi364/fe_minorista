package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.TipoDocumentoDTO;
import com.msvanegasg.facturaelectronica.mapper.TipoDocumentoMapper;
import com.msvanegasg.facturaelectronica.models.Impuesto;
import com.msvanegasg.facturaelectronica.models.TipoDocumento;
import com.msvanegasg.facturaelectronica.service.TipoDocumentoService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tipos-documento")
public class TipoDocumentoController {

    @Autowired
    private TipoDocumentoService tipoDocumentoService;

    @GetMapping
    public ResponseEntity<List<TipoDocumento>> findAll() {
    	List<TipoDocumento> all = tipoDocumentoService.findAll();
        return ResponseEntity.ok(all);
    }
    
    
    @GetMapping("/active")
    public ResponseEntity<List<TipoDocumento>> findActiveTrue() {
    	List<TipoDocumento> active = tipoDocumentoService.findActive();
    	return ResponseEntity.ok(active);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoDocumentoDTO> findById(@PathVariable("id") Long id) {
        TipoDocumento tipo = tipoDocumentoService.findById(id);
        return ResponseEntity.ok(TipoDocumentoMapper.toDTO(tipo));
    }

    @PostMapping
    public ResponseEntity<TipoDocumentoDTO> create(@Valid @RequestBody TipoDocumentoDTO dto) {
        TipoDocumento tipoDocumento = TipoDocumentoMapper.toEntity(dto);
        tipoDocumento.setActivo(true);
        TipoDocumento saved = tipoDocumentoService.save(tipoDocumento);
        return ResponseEntity.ok(TipoDocumentoMapper.toDTO(saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoDocumentoDTO> update(@PathVariable("id") Long id, @Valid @RequestBody TipoDocumentoDTO dto) {
        TipoDocumento existing = tipoDocumentoService.findById(id);

        TipoDocumento updatedTipo = TipoDocumentoMapper.toEntity(dto);
        updatedTipo.setCodigo(existing.getCodigo());
        updatedTipo.setActivo(existing.getActivo());

        TipoDocumento updated = tipoDocumentoService.save(updatedTipo);
        return ResponseEntity.ok(TipoDocumentoMapper.toDTO(updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disable(@PathVariable("id") Long id) {
        tipoDocumentoService.disableById(id);
        return ResponseEntity.noContent().build();
    }
}
