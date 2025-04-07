package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.ProveedorDTO;
import com.msvanegasg.facturaelectronica.DTO.ProveedorResponseDTO;
import com.msvanegasg.facturaelectronica.mapper.ProveedorMapper;
import com.msvanegasg.facturaelectronica.models.Proveedor;
import com.msvanegasg.facturaelectronica.models.TipoDocumento;
import com.msvanegasg.facturaelectronica.service.ProveedorService;
import com.msvanegasg.facturaelectronica.service.TipoDocumentoService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

    @Autowired
    private ProveedorService proveedorService;

    @Autowired
    private TipoDocumentoService tipoDocumentoService;

    @GetMapping
    public List<ProveedorResponseDTO> findAll() {
        return proveedorService.findAll()
                .stream()
                .map(ProveedorMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProveedorResponseDTO> findById(@PathVariable("id") Long id) {
        Proveedor proveedor = proveedorService.findById(id);
        return ResponseEntity.ok(ProveedorMapper.toResponseDTO(proveedor));
    }
    
    @GetMapping("/documento/{numeroDocumento}")
    public ResponseEntity<ProveedorResponseDTO> findByNumeroDocumento(@PathVariable("numeroDocumento") Long numeroDocumento) {
        Proveedor proveedor = proveedorService.findByNumeroDocumento(numeroDocumento);
        return ResponseEntity.ok(ProveedorMapper.toResponseDTO(proveedor));
    }


    @PostMapping
    public ResponseEntity<ProveedorDTO> create(@Valid @RequestBody ProveedorDTO dto) {
        TipoDocumento tipoDocumento = tipoDocumentoService.findById(dto.getIdTipoDocumento());
        Proveedor proveedor = ProveedorMapper.toEntity(dto, tipoDocumento);
        proveedor.setActivo(true);
        Proveedor saved = proveedorService.save(proveedor);
        return ResponseEntity.ok(ProveedorMapper.toDTO(saved));
    }

    @PutMapping("/{numero_documento}")
    public ResponseEntity<ProveedorDTO> update(@PathVariable("numero_documento") Long id, @Valid @RequestBody ProveedorDTO proveedorDTO) {
        TipoDocumento tipoDocumento = tipoDocumentoService.findById(proveedorDTO.getIdTipoDocumento());
        Proveedor proveedorActualizado = ProveedorMapper.toEntity(proveedorDTO, tipoDocumento);
        Proveedor actualizado = proveedorService.update(id, proveedorActualizado);
        return ResponseEntity.ok(ProveedorMapper.toDTO(actualizado));
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> disable(@PathVariable("id") Long id) {
        proveedorService.disableById(id);
        return ResponseEntity.noContent().build();
    }
    
    @PutMapping("/{numero_documento}/activar")
    public ResponseEntity<Void> activarProveedor(@PathVariable("numero_documento") Long numeroDocumento) {
        proveedorService.activarProveedor(numeroDocumento);
        return ResponseEntity.noContent().build();
    }

}
