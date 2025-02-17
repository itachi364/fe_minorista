package com.msvanegasg.facturaelectronica.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.msvanegasg.facturaelectronica.DTO.TipoDocumentoDTO;
import com.msvanegasg.facturaelectronica.models.TipoDocumento;
import com.msvanegasg.facturaelectronica.service.TipoDocumentoService;

import java.util.List;

@RestController
@RequestMapping("/api/tipo-documento")
public class TipoDocumentoController {

    @Autowired
    private TipoDocumentoService tipoDocumentoService;

    @GetMapping
    public List<TipoDocumento> getAll() {
        return tipoDocumentoService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TipoDocumento> getById(@PathVariable Long id) {
        return tipoDocumentoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<TipoDocumento> create(@RequestBody TipoDocumentoDTO tipoDocumentoDTO) {
        // Convertir el DTO a la entidad
        TipoDocumento tipoDocumento = TipoDocumento.builder()
                .codigo(tipoDocumentoDTO.getCodigo())
                .nombre(tipoDocumentoDTO.getNombre())
                .descripcion(tipoDocumentoDTO.getDescripcion())
                .activo(true)
                .build();

        TipoDocumento savedTipoDocumento = tipoDocumentoService.save(tipoDocumento);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTipoDocumento);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TipoDocumento> update(@PathVariable Long id, @RequestBody TipoDocumentoDTO tipoDocumentoDTO) {
        return tipoDocumentoService.findById(id)
                .map(existing -> {
                    existing.setCodigo(tipoDocumentoDTO.getCodigo());
                    existing.setNombre(tipoDocumentoDTO.getNombre());
                    existing.setDescripcion(tipoDocumentoDTO.getDescripcion());
                    existing.setActivo(true);
                    TipoDocumento updatedTipoDocumento = tipoDocumentoService.save(existing);
                    return ResponseEntity.ok(updatedTipoDocumento);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        if (tipoDocumentoService.findById(id).isPresent()) {
            tipoDocumentoService.disableById(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
}