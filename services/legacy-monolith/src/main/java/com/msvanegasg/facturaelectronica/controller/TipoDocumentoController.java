package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageDocumentTypeUseCase;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.DocumentTypeRestMapper;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.DocumentTypeRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.DocumentTypeResponse;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-documento")
public class TipoDocumentoController {

    private final ManageDocumentTypeUseCase manageDocumentTypeUseCase;

    public TipoDocumentoController(ManageDocumentTypeUseCase manageDocumentTypeUseCase) {
        this.manageDocumentTypeUseCase = manageDocumentTypeUseCase;
    }

    @GetMapping
    public ResponseEntity<List<DocumentTypeResponse>> findAll() {
    	List<DocumentTypeResponse> all = manageDocumentTypeUseCase.findAll().stream()
                .map(DocumentTypeRestMapper::toResponse)
                .toList();
        return ResponseEntity.ok(all);
    }
    
    
    @GetMapping("/active")
    public ResponseEntity<List<DocumentTypeResponse>> findActiveTrue() {
    	List<DocumentTypeResponse> active = manageDocumentTypeUseCase.findActive().stream()
                .map(DocumentTypeRestMapper::toResponse)
                .toList();
    	return ResponseEntity.ok(active);
    }
    
    @GetMapping("/inactive")
    public ResponseEntity<List<DocumentTypeResponse>> findActiveFalse() {
    	List<DocumentTypeResponse> inactivo = manageDocumentTypeUseCase.findInactive().stream()
                .map(DocumentTypeRestMapper::toResponse)
                .toList();
    	return ResponseEntity.ok(inactivo);
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<DocumentTypeResponse> findById(@PathVariable("codigo") Long codigo) {
        return ResponseEntity.ok(DocumentTypeRestMapper.toResponse(manageDocumentTypeUseCase.findByCode(codigo)));
    }

    @PostMapping
    public ResponseEntity<DocumentTypeRequest> create(@Valid @RequestBody DocumentTypeRequest dto) {
        return ResponseEntity.ok(DocumentTypeRestMapper.toRequest(
                manageDocumentTypeUseCase.create(DocumentTypeRestMapper.toCommand(dto))));
    }

    @PutMapping("/{codigo}")
    public ResponseEntity<DocumentTypeRequest> update(@PathVariable("codigo") Long codigo, @Valid @RequestBody DocumentTypeRequest dto) {
        return ResponseEntity.ok(DocumentTypeRestMapper.toRequest(
                manageDocumentTypeUseCase.update(codigo, DocumentTypeRestMapper.toCommand(dto))));
    }

    @DeleteMapping("/{codigo}")
    public ResponseEntity<Void> disable(@PathVariable("codigo") Long codigo) {
        manageDocumentTypeUseCase.disable(codigo);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{codigo}/activar")
    public ResponseEntity<Void> activarTipoDocumento(@PathVariable("codigo") Long codigo) {
    	manageDocumentTypeUseCase.enable(codigo);
        return ResponseEntity.noContent().build();
    }
}
