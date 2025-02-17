package com.msvanegasg.facturaelectronica.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.msvanegasg.facturaelectronica.models.TipoGasto;
import com.msvanegasg.facturaelectronica.service.TipoGastoService;

import java.util.List;

@RestController
@RequestMapping("/api/tipo-gasto")
public class TipoGastoController {

	@Autowired
	private TipoGastoService tipoGastoService;

	@GetMapping
	public List<TipoGasto> getAll() {
		return tipoGastoService.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<TipoGasto> getById(@PathVariable Long id) {
		return tipoGastoService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public TipoGasto create(@RequestBody TipoGasto tipoGasto) {
		return tipoGastoService.save(tipoGasto);
	}

	@PutMapping("/{id}")
	public ResponseEntity<TipoGasto> update(@PathVariable Long id, @RequestBody TipoGasto tipoGasto) {
		return tipoGastoService.findById(id).map(existing -> {
			tipoGasto.setIdTipoGasto(existing.getIdTipoGasto());
			return ResponseEntity.ok(tipoGastoService.save(tipoGasto));
		}).orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> disable(@PathVariable Long id) {
		if (tipoGastoService.findById(id).isPresent()) {
			tipoGastoService.disableById(id);
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}
}
