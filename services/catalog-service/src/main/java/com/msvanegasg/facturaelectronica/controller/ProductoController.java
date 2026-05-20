package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageProductUseCase;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.ProductRestMapper;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ProductRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ProductResponse;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ProductStockIncreaseRequest;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

	private final ManageProductUseCase manageProductUseCase;

	@PostMapping
	public ResponseEntity<ProductResponse> crearProducto(@Valid @RequestBody ProductRequest productoDTO) {
		return ResponseEntity.ok(ProductRestMapper.toResponse(
				manageProductUseCase.create(ProductRestMapper.toCommand(productoDTO))));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductResponse> obtenerProducto(@PathVariable("id") Long id) {
		return ResponseEntity.ok(ProductRestMapper.toResponse(manageProductUseCase.findById(id)));
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProductResponse> actualizarProducto(@PathVariable("id") Long id,
			@Valid @RequestBody ProductRequest productoDTO) {
		return ResponseEntity.ok(ProductRestMapper.toResponse(
				manageProductUseCase.update(id, ProductRestMapper.toCommand(productoDTO))));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminarProducto(@PathVariable("id") Long id) {
		manageProductUseCase.disable(id);
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping
	public ResponseEntity<List<ProductResponse>> listarTodo() {
		List<ProductResponse> productos = manageProductUseCase.findAll().stream()
				.map(ProductRestMapper::toResponse)
				.toList();
		return ResponseEntity.ok(productos);
	}

	@GetMapping("/active")
	public ResponseEntity<List<ProductResponse>> listarProductosActivos() {
		List<ProductResponse> productos = manageProductUseCase.findActive().stream()
				.map(ProductRestMapper::toResponse)
				.toList();
		return ResponseEntity.ok(productos);
	}
	
	@GetMapping("/inactive")
	public ResponseEntity<List<ProductResponse>> listarProductosInactivos() {
		List<ProductResponse> productos = manageProductUseCase.findInactive().stream()
				.map(ProductRestMapper::toResponse)
				.toList();
		return ResponseEntity.ok(productos);
	}

	@GetMapping("/nombre/{nombre}")
	public ResponseEntity<List<ProductResponse>> listarProductosNombre(@PathVariable("nombre") String nombre) {
		List<ProductResponse> productos = manageProductUseCase.findByName(nombre).stream()
				.map(ProductRestMapper::toResponse)
				.toList();
		return ResponseEntity.ok(productos);
	}
	
	@GetMapping("/codigo/{codigoBarras}")
	public ResponseEntity<ProductResponse> obtenerProductoPorCodigo(@PathVariable("codigoBarras") Long codigoBarras) {
	    return ResponseEntity.ok(ProductRestMapper.toResponse(manageProductUseCase.findByBarcode(codigoBarras)));
	}
	
	@PutMapping("/activar/{id}")
    public ResponseEntity<Void> enable(@PathVariable("id") Long id) {
		manageProductUseCase.enable(id);
        return ResponseEntity.noContent().build();
	}
	
	@PutMapping("/aumentar-stock")
	public ResponseEntity<ProductResponse> aumentarStock(@Valid @RequestBody ProductStockIncreaseRequest request) {
	    return ResponseEntity.ok(ProductRestMapper.toResponse(
	    		manageProductUseCase.increaseStock(ProductRestMapper.toIncreaseStockCommand(request))));
	}

}
