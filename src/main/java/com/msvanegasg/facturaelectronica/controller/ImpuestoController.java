package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.ImpuestoDTO;
import com.msvanegasg.facturaelectronica.DTO.response.ImpuestoResponseDTO;
import com.msvanegasg.facturaelectronica.exception.impuesto.ImpuestoNotFoundException;
import com.msvanegasg.facturaelectronica.mapper.ImpuestoMapper;
import com.msvanegasg.facturaelectronica.models.Impuesto;
import com.msvanegasg.facturaelectronica.models.Pais;
import com.msvanegasg.facturaelectronica.service.ImpuestoService;
import com.msvanegasg.facturaelectronica.service.PaisService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/impuesto")
public class ImpuestoController {

	@Autowired
	private ImpuestoService impuestoService;

	@Autowired
	private PaisService paisService;

	@GetMapping
    public ResponseEntity<List<Impuesto>> findAll() {
    	List<Impuesto> all = impuestoService.findAll();
        return ResponseEntity.ok(all);
    }
	
	@GetMapping("/activos")
    public ResponseEntity<Impuesto> findActivos() {
        Impuesto activos = impuestoService.findActiveTrue();
        return ResponseEntity.ok(activos);
    }
	
	@GetMapping("/inactivos")
    public ResponseEntity<Impuesto> findInactivos() {
        Impuesto inactivos = impuestoService.findActiveFalse();
        return ResponseEntity.ok(inactivos);
    }

	@GetMapping("/{id}")
	public ResponseEntity<Impuesto> getById(@PathVariable("id") Long id) {
			Impuesto impuesto = impuestoService.findById(id);
			return ResponseEntity.ok(impuesto);
	}
	
	@GetMapping("/porcentaje/{porcentaje}")
	public ResponseEntity<Impuesto> findByPorcentaje(@PathVariable("porcentaje") BigDecimal porcentaje) {
			Impuesto impuesto = impuestoService.findByPorcentaje(porcentaje);
			return ResponseEntity.ok(impuesto);
	}
	
	@GetMapping("/tipo/{tipo}")
	public ResponseEntity<Impuesto> findByTipo(@PathVariable("tipo") String tipo) {
			Impuesto impuesto = impuestoService.findByTipo(tipo);
			return ResponseEntity.ok(impuesto);
	}

	@PostMapping
	public ResponseEntity<Impuesto> create(@RequestBody ImpuestoDTO dto) {
		Pais pais = paisService.findById(dto.getCodPais());
		Impuesto impuesto = impuestoService.save(dto,pais);
		return ResponseEntity.ok(impuesto);
	}
	
	@PutMapping("/{id}")
	public ResponseEntity<ImpuestoResponseDTO> update(@PathVariable("id") Long id, @Valid @RequestBody ImpuestoDTO dto) {
			Pais pais = paisService.findById(dto.getCodPais());

			Impuesto actualizado = impuestoService.actualizarImpuesto(id, dto, pais);
			return ResponseEntity.ok(ImpuestoMapper.toResponseDTO(actualizado, pais));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> disable(@PathVariable("id") Long id) {
		try {
			impuestoService.findById(id);
			impuestoService.disableById(id);
			return ResponseEntity.noContent().build();
		} catch (ImpuestoNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
	}
	
	@PutMapping("/{id}/activar")
    public ResponseEntity<Void> activarImpuesto(@PathVariable("id") Long id) {
		impuestoService.activarImpuesto(id);
        return ResponseEntity.noContent().build();
    }
}
