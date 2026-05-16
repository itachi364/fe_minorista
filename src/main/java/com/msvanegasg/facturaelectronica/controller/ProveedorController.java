package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.thirdparty.application.port.in.ManageSupplierUseCase;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.Supplier;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.SupplierRestMapper;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto.SupplierRequest;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto.SupplierResponse;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
public class ProveedorController {

	private final ManageSupplierUseCase manageSupplierUseCase;

	public ProveedorController(ManageSupplierUseCase manageSupplierUseCase) {
		this.manageSupplierUseCase = manageSupplierUseCase;
	}

	@GetMapping
	public List<SupplierResponse> findAll() {
		return manageSupplierUseCase.findAll().stream()
				.map(SupplierRestMapper::toResponse)
				.toList();
	}

	@GetMapping("/active")
	public List<SupplierResponse> findActiveTrue() {
		return manageSupplierUseCase.findActive().stream()
				.map(SupplierRestMapper::toResponse)
				.toList();
	}

	@GetMapping("/inactive")
	public List<SupplierResponse> findActiveFalse() {
		return manageSupplierUseCase.findInactive().stream()
				.map(SupplierRestMapper::toResponse)
				.toList();
	}

	@GetMapping("/{id}")
	public ResponseEntity<SupplierResponse> findById(@PathVariable("id") Long id) {
		Supplier supplier = manageSupplierUseCase.findById(id);
		return ResponseEntity.ok(SupplierRestMapper.toResponse(supplier));
	}

	@GetMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}")
	public ResponseEntity<SupplierResponse> findByNumeroDocumento(
			@PathVariable("numeroDocumento") Long numeroDocumento, @PathVariable("tipoDocumento") Long tipoDocumento) {
		Supplier supplier = manageSupplierUseCase.findByDocument(tipoDocumento, numeroDocumento);
		return ResponseEntity.ok(SupplierRestMapper.toResponse(supplier));
	}
	
	@GetMapping("/nombre/{nombre}")
	public ResponseEntity<SupplierResponse> findByNombre(@PathVariable("nombre") String nombre) {
		Supplier supplier = manageSupplierUseCase.findByName(nombre);
		return ResponseEntity.ok(SupplierRestMapper.toResponse(supplier));
	}

	@PostMapping
	public ResponseEntity<SupplierRequest> create(@Valid @RequestBody SupplierRequest dto) {
		Supplier saved = manageSupplierUseCase.create(SupplierRestMapper.toCommand(dto));
		return ResponseEntity.ok(SupplierRestMapper.toRequestResponse(saved));
	}

	@PutMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}")
	public ResponseEntity<SupplierRequest> update(@PathVariable("numeroDocumento") Long numeroDocumento,
			@PathVariable("tipoDocumento") Long tipoDocumentoCodigo, @Valid @RequestBody SupplierRequest proveedorDTO) {
		Supplier updated = manageSupplierUseCase.update(tipoDocumentoCodigo, numeroDocumento,
				SupplierRestMapper.toCommand(proveedorDTO));
		return ResponseEntity.ok(SupplierRestMapper.toRequestResponse(updated));
	}

	@DeleteMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}")
	public ResponseEntity<Void> disable(@PathVariable("numeroDocumento") Long numeroDocumento,
			@PathVariable("tipoDocumento") Long tipoDocumentoCodigo) {
		manageSupplierUseCase.disable(tipoDocumentoCodigo, numeroDocumento);
		return ResponseEntity.noContent().build();
	}

	@PutMapping("/documento/{numeroDocumento}/tipo/{tipoDocumento}/activar")
	public ResponseEntity<Void> activarProveedor(@PathVariable("numeroDocumento") Long numeroDocumento,
			@PathVariable("tipoDocumento") Long tipoDocumentoCodigo) {
		manageSupplierUseCase.enable(tipoDocumentoCodigo, numeroDocumento);
		return ResponseEntity.noContent().build();
	}

}
