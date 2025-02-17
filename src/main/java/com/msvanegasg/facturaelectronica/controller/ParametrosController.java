package com.msvanegasg.facturaelectronica.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.msvanegasg.facturaelectronica.models.Parametros;
import com.msvanegasg.facturaelectronica.service.ParametrosService;

import java.util.List;

@RestController
@RequestMapping("/api/parametros")
public class ParametrosController {

	@Autowired
	private ParametrosService parametrosService;

	@GetMapping
	public List<Parametros> getAll() {
		return parametrosService.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Parametros> getById(@PathVariable Long id) {
		return parametrosService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public Parametros create(@RequestBody Parametros parametros) {
		return parametrosService.save(parametros);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Parametros> update(@PathVariable Long id, @RequestBody Parametros parametros) {
		return parametrosService.findById(id).map(existing -> {
			parametros.setIdParametro(existing.getIdParametro());
			return ResponseEntity.ok(parametrosService.save(parametros));
		}).orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> disable(@PathVariable Long id) {
		if (parametrosService.findById(id).isPresent()) {
			parametrosService.disableById(id);
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}
}
