package com.msvanegasg.facturaelectronica.controller;

import com.msvanegasg.facturaelectronica.DTO.ProductoDTO;
import com.msvanegasg.facturaelectronica.DTO.request.AumentarStockRequestDTO;
import com.msvanegasg.facturaelectronica.DTO.response.ProductoResponseDTO;
import com.msvanegasg.facturaelectronica.service.ProductoService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

	private final ProductoService productoService;

	@PostMapping
	public ResponseEntity<ProductoResponseDTO> crearProducto(@Valid @RequestBody ProductoDTO productoDTO) {
		ProductoResponseDTO creado = productoService.crearProducto(productoDTO);
		return ResponseEntity.ok(creado);
	}

	@GetMapping("/{id}")
	public ResponseEntity<ProductoResponseDTO> obtenerProducto(@PathVariable("id") Long id) {
		ProductoResponseDTO producto = productoService.obtenerProductoPorId(id);
		return ResponseEntity.ok(producto);
	}

	@PutMapping("/{id}")
	public ResponseEntity<ProductoResponseDTO> actualizarProducto(@PathVariable("id") Long id,
			@Valid @RequestBody ProductoDTO productoDTO) {
		ProductoResponseDTO actualizado = productoService.actualizarProducto(id, productoDTO);
		return ResponseEntity.ok(actualizado);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> eliminarProducto(@PathVariable("id") Long id) {
		productoService.eliminarProducto(id);
		return ResponseEntity.noContent().build();
	}
	
	@GetMapping
	public ResponseEntity<List<ProductoResponseDTO>> listarTodo() {
		List<ProductoResponseDTO> productos = productoService.listarTodo();
		return ResponseEntity.ok(productos);
	}

	@GetMapping("/active")
	public ResponseEntity<List<ProductoResponseDTO>> listarProductosActivos() {
		List<ProductoResponseDTO> productos = productoService.listarProductosActivos();
		return ResponseEntity.ok(productos);
	}
	
	@GetMapping("/inactive")
	public ResponseEntity<List<ProductoResponseDTO>> listarProductosInactivos() {
		List<ProductoResponseDTO> productos = productoService.listarProductosInactivos();
		return ResponseEntity.ok(productos);
	}

	@GetMapping("/nombre/{nombre}")
	public ResponseEntity<List<ProductoResponseDTO>> listarProductosNombre(@PathVariable("nombre") String nombre) {
		List<ProductoResponseDTO> productos = productoService.listarProductosNombre(nombre);
		return ResponseEntity.ok(productos);
	}
	
	@GetMapping("/codigo/{codigoBarras}")
	public ResponseEntity<ProductoResponseDTO> obtenerProductoPorCodigo(@PathVariable("codigoBarras") Long codigoBarras) {
	    ProductoResponseDTO producto = productoService.obtenerPorCodigo(codigoBarras);
	    return ResponseEntity.ok(producto);
	}
	
	@PutMapping("/activar/{id}")
    public ResponseEntity<Void> enable(@PathVariable("id") Long id) {
		productoService.activarProducto(id);
        return ResponseEntity.noContent().build();
    }
	
	@PutMapping("/aumentar-stock")
	public ResponseEntity<ProductoResponseDTO> aumentarStock(@Valid @RequestBody AumentarStockRequestDTO request) {
		System.out.print(request);
	    ProductoResponseDTO actualizado = productoService.aumentarStock(request);
	    return ResponseEntity.ok(actualizado);
	}

}
