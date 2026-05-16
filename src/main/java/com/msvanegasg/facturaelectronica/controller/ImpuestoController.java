package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageTaxUseCase;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Tax;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.TaxRestMapper;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.TaxRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.TaxResponse;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.TaxUpdateResponse;
import com.msvanegasg.facturaelectronica.exception.impuesto.ImpuestoNotFoundException;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/impuesto")
public class ImpuestoController {

	private final ManageTaxUseCase manageTaxUseCase;

	public ImpuestoController(ManageTaxUseCase manageTaxUseCase) {
		this.manageTaxUseCase = manageTaxUseCase;
	}

	@GetMapping
    public ResponseEntity<List<TaxResponse>> findAll() {
    	List<TaxResponse> all = manageTaxUseCase.findAll().stream()
				.map(TaxRestMapper::toResponse)
				.toList();
        return ResponseEntity.ok(all);
    }
	
	@GetMapping("/activos")
    public ResponseEntity<TaxResponse> findActivos() {
        return ResponseEntity.ok(TaxRestMapper.toResponse(manageTaxUseCase.findActive()));
    }
	
	@GetMapping("/inactivos")
    public ResponseEntity<TaxResponse> findInactivos() {
        return ResponseEntity.ok(TaxRestMapper.toResponse(manageTaxUseCase.findInactive()));
    }

	@GetMapping("/{id}")
	public ResponseEntity<TaxResponse> getById(@PathVariable("id") Long id) {
			return ResponseEntity.ok(TaxRestMapper.toResponse(manageTaxUseCase.findById(id)));
	}
	
	@GetMapping("/porcentaje/{porcentaje}")
	public ResponseEntity<TaxResponse> findByPorcentaje(@PathVariable("porcentaje") BigDecimal porcentaje) {
			return ResponseEntity.ok(TaxRestMapper.toResponse(manageTaxUseCase.findByPercentage(porcentaje)));
	}
	
	@GetMapping("/tipo/{tipo}")
	public ResponseEntity<TaxResponse> findByTipo(@PathVariable("tipo") String tipo) {
			return ResponseEntity.ok(TaxRestMapper.toResponse(manageTaxUseCase.findByType(tipo)));
	}

	@PostMapping
	public ResponseEntity<TaxResponse> create(@RequestBody TaxRequest dto) {
		Tax tax = manageTaxUseCase.create(TaxRestMapper.toCommand(dto));
		return ResponseEntity.ok(TaxRestMapper.toResponse(tax));
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<TaxUpdateResponse> update(@PathVariable("id") Long id, @Valid @RequestBody TaxRequest dto) {
			Tax updated = manageTaxUseCase.update(id, TaxRestMapper.toCommand(dto));
			return ResponseEntity.ok(TaxRestMapper.toUpdateResponse(updated));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> disable(@PathVariable("id") Long id) {
		try {
			manageTaxUseCase.disable(id);
			return ResponseEntity.noContent().build();
		} catch (ImpuestoNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
	}
	
	@PutMapping("/{id}/activar")
    public ResponseEntity<Void> activarImpuesto(@PathVariable("id") Long id) {
		manageTaxUseCase.enable(id);
        return ResponseEntity.noContent().build();
    }
}
