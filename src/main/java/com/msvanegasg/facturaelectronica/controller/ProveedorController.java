package com.msvanegasg.facturaelectronica.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.msvanegasg.facturaelectronica.models.Proveedor;
import com.msvanegasg.facturaelectronica.service.ProveedorService;

import java.util.List;

@RestController
@RequestMapping("/api/proveedor")
public class ProveedorController {

	@Autowired
	private ProveedorService proveedorService;

	@GetMapping
	public List<Proveedor> getAll() {
		return proveedorService.findAll();
	}

	@GetMapping("/{id}")
	public ResponseEntity<Proveedor> getById(@PathVariable Long id) {
		return proveedorService.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
	}

	@PostMapping
	public Proveedor create(@RequestBody Proveedor proveedor) {
		return proveedorService.save(proveedor);
	}

	@PutMapping("/{id}")
	public ResponseEntity<Proveedor> update(@PathVariable Long id, @RequestBody Proveedor proveedor) {
		return proveedorService.findById(id).map(existing -> {
			proveedor.setIdProveedor(existing.getIdProveedor());
			return ResponseEntity.ok(proveedorService.save(proveedor));
		}).orElse(ResponseEntity.notFound().build());
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> disable(@PathVariable Long id) {
		if (proveedorService.findById(id).isPresent()) {
			proveedorService.disableById(id);
			return ResponseEntity.noContent().build();
		}
		return ResponseEntity.notFound().build();
	}
}
