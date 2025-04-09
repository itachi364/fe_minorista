package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.TipoDocumentoDTO;
import com.msvanegasg.facturaelectronica.mapper.TipoDocumentoMapper;
import com.msvanegasg.facturaelectronica.models.TipoDocumento;
import com.msvanegasg.facturaelectronica.service.TipoDocumentoService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    
    @GetMapping("/inactive")
    public ResponseEntity<List<TipoDocumento>> findActiveFalse() {
    	List<TipoDocumento> inactivo = tipoDocumentoService.findActiveFalse();
    	return ResponseEntity.ok(inactivo);
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<TipoDocumento> findById(@PathVariable("codigo") Long codigo) {
        TipoDocumento tipo = tipoDocumentoService.findById(codigo);
        return ResponseEntity.ok(tipo);
    }

    @PostMapping
    public ResponseEntity<TipoDocumentoDTO> create(@Valid @RequestBody TipoDocumentoDTO dto) {
        TipoDocumento tipoDocumento = TipoDocumentoMapper.toEntity(dto);
        tipoDocumento.setActivo(true);
        TipoDocumento saved = tipoDocumentoService.save(tipoDocumento);
        return ResponseEntity.ok(TipoDocumentoMapper.toDTO(saved));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<TipoDocumentoDTO> update(@PathVariable("codigo") Long codigo, @Valid @RequestBody TipoDocumentoDTO dto) {
        TipoDocumento existing = tipoDocumentoService.findById(codigo);

        TipoDocumento updatedTipo = TipoDocumentoMapper.toEntity(dto);
        updatedTipo.setCodigo(existing.getCodigo());
        updatedTipo.setActivo(existing.getActivo());

        TipoDocumento updated = tipoDocumentoService.save(updatedTipo);
        return ResponseEntity.ok(TipoDocumentoMapper.toDTO(updated));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> disable(@PathVariable("codigo") Long codigo) {
        tipoDocumentoService.disableByCodigo(codigo);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{codigo}/activar")
    public ResponseEntity<Void> activarTipoDocumento(@PathVariable("codigo") Long codigo) {
    	tipoDocumentoService.activarTipoDocumento(codigo);
        return ResponseEntity.noContent().build();
    }
}
