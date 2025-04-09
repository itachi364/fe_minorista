package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.ProveedorDTO;
import com.msvanegasg.facturaelectronica.DTO.response.ClienteResponseDTO;
import com.msvanegasg.facturaelectronica.DTO.response.ProveedorResponseDTO;
import com.msvanegasg.facturaelectronica.mapper.ClienteMapper;
import com.msvanegasg.facturaelectronica.mapper.ProveedorMapper;
import com.msvanegasg.facturaelectronica.models.Cliente;
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
		return proveedorService.findAll().stream().map(ProveedorMapper::toResponseDTO).collect(Collectors.toList());
	}

	@GetMapping("/active")
	public List<ProveedorResponseDTO> findActiveTrue() {
		return proveedorService.findActive().stream().map(ProveedorMapper::toResponseDTO).collect(Collectors.toList());
	}

	@GetMapping("/inactive")
	public List<ProveedorResponseDTO> findActiveFalse() {
		return proveedorService.findActiveFalse().stream().map(ProveedorMapper::toResponseDTO)
				.collect(Collectors.toList());
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProveedorResponseDTO> findById(@PathVariable("id") Long id) {
		Proveedor proveedor = proveedorService.findById(id);
		return ResponseEntity.ok(ProveedorMapper.toResponseDTO(proveedor));
	}

	@GetMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}")
	public ResponseEntity<ProveedorResponseDTO> findByNumeroDocumento(
			@PathVariable("numeroDocumento") Long numeroDocumento, @PathVariable("tipoDocumento") Long tipoDocumento) {
		Proveedor proveedor = proveedorService.findByNumeroDocumento(numeroDocumento, tipoDocumento);
		return ResponseEntity.ok(ProveedorMapper.toResponseDTO(proveedor));
	}
	
	@GetMapping("/nombre/{nombre}")
	public ResponseEntity<ProveedorResponseDTO> findByNombre(@PathVariable("nombre") String nombre) {
		Proveedor proveedor = proveedorService.findByNombre(nombre);
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

	@PutMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}")
	public ResponseEntity<ProveedorDTO> update(@PathVariable("numeroDocumento") Long numeroDocumento,
			@PathVariable("tipoDocumento") Long tipoDocumentoCodigo, @Valid @RequestBody ProveedorDTO proveedorDTO) {
		TipoDocumento tipoDocumento = tipoDocumentoService.findById(proveedorDTO.getIdTipoDocumento());
		Proveedor proveedorActualizado = ProveedorMapper.toEntity(proveedorDTO, tipoDocumento);
		Proveedor actualizado = proveedorService.update(numeroDocumento, tipoDocumentoCodigo, proveedorActualizado);
		return ResponseEntity.ok(ProveedorMapper.toDTO(actualizado));
	}

	@DeleteMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}")
	public ResponseEntity<Void> disable(@PathVariable("numeroDocumento") Long numeroDocumento,
			@PathVariable("tipoDocumento") Long tipoDocumentoCodigo) {
		proveedorService.disableByNumero(numeroDocumento, tipoDocumentoCodigo);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}/activar")
	public ResponseEntity<Void> activarProveedor(@PathVariable("numeroDocumento") Long numeroDocumento,
			@PathVariable("tipoDocumento") Long tipoDocumentoCodigo) {
		proveedorService.activarProveedor(numeroDocumento, tipoDocumentoCodigo);
		return ResponseEntity.noContent().build();
	}

}
