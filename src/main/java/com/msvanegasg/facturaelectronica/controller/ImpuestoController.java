package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.ImpuestoDTO;
import com.msvanegasg.facturaelectronica.exception.ImpuestoNotFoundException;
import com.msvanegasg.facturaelectronica.mapper.ImpuestoMapper;
import com.msvanegasg.facturaelectronica.models.Impuesto;
import com.msvanegasg.facturaelectronica.models.Pais;
import com.msvanegasg.facturaelectronica.service.ImpuestoService;
import com.msvanegasg.facturaelectronica.service.PaisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

	@GetMapping("/{id}")
	public ResponseEntity<Impuesto> getById(@PathVariable("id") Long id) {
			Impuesto impuesto = impuestoService.findById(id);
			return ResponseEntity.ok(impuesto);
	}

	@PostMapping
	public ResponseEntity<Impuesto> create(@RequestBody ImpuestoDTO dto) {
		Pais pais = paisService.findById(dto.getCodPais());
		Impuesto impuesto = ImpuestoMapper.toEntity(dto, pais);
		return ResponseEntity.ok(impuesto);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ImpuestoDTO> update(@PathVariable("id") Long id, @RequestBody ImpuestoDTO dto) {
		try {
			Impuesto existing = impuestoService.findById(id);
			Pais pais = paisService.findById(dto.getCodPais());

			Impuesto impuestoToUpdate = ImpuestoMapper.toEntity(dto, pais);
			impuestoToUpdate.setIdImpuesto(existing.getIdImpuesto());
			impuestoToUpdate.setActivo(existing.getActivo());

			Impuesto actualizado = impuestoService.save(impuestoToUpdate);
			return ResponseEntity.ok(ImpuestoMapper.toDTO(actualizado));
		} catch (ImpuestoNotFoundException e) {
			return ResponseEntity.notFound().build();
		}
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

}
