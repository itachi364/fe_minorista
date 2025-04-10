package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.DTO.ProductoDTO;
import com.msvanegasg.facturaelectronica.DTO.request.AumentarStockRequestDTO;
import com.msvanegasg.facturaelectronica.DTO.response.ProductoResponseDTO;
import com.msvanegasg.facturaelectronica.exception.CategoriaNotFoundException;
import com.msvanegasg.facturaelectronica.exception.producto.*;
import com.msvanegasg.facturaelectronica.mapper.ProductoMapper;
import com.msvanegasg.facturaelectronica.models.Categoria;
import com.msvanegasg.facturaelectronica.models.Producto;
import com.msvanegasg.facturaelectronica.repository.CategoriaRepository;
import com.msvanegasg.facturaelectronica.repository.ProductoRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductoService {

	private final ProductoRepository productoRepository;
	private final CategoriaRepository categoriaRepository;
	private final ProductoMapper productoMapper;

	public List<ProductoResponseDTO> listarTodo() {
		return productoRepository.findAll().stream().map(productoMapper::toResponseDTO).collect(Collectors.toList());
	}

	public ProductoResponseDTO obtenerPorCodigo(Long codigoBarras) {
	    return productoRepository.findByCodigoBarras(codigoBarras)
	            .map(productoMapper::toResponseDTO)
	            .orElseThrow(() -> new ProductoCodigoNotFoundException(codigoBarras));
	}

	public ProductoResponseDTO crearProducto(ProductoDTO productoDTO) {
		Categoria categoria = categoriaRepository.findById(productoDTO.getIdCategoria())
				.orElseThrow(() -> new CategoriaNotFoundException(productoDTO.getIdCategoria()));

		Producto producto = productoMapper.toEntity(productoDTO, categoria);
		producto.setActivo(true);

		return productoMapper.toResponseDTO(productoRepository.save(producto));
	}

	public ProductoResponseDTO actualizarProducto(Long idProducto, ProductoDTO productoDTO) {
		Producto productoExistente = productoRepository.findById(idProducto)
				.orElseThrow(() -> new ProductoIdNotFoundException(idProducto));

		Categoria categoria = categoriaRepository.findById(productoDTO.getIdCategoria())
				.orElseThrow(() -> new CategoriaNotFoundException(productoDTO.getIdCategoria()));

		productoExistente.setNombre(productoDTO.getNombre());
		productoExistente.setDescripcion(productoDTO.getDescripcion());
		productoExistente.setPrecioBase(productoDTO.getPrecioBase());
		productoExistente.setCantidadStock(productoDTO.getCantidadStock());
		productoExistente.setCategoria(categoria);

		return productoMapper.toResponseDTO(productoRepository.save(productoExistente));
	}

	public ProductoResponseDTO obtenerProductoPorId(Long idProducto) {
		Producto producto = productoRepository.findById(idProducto)
				.orElseThrow(() -> new ProductoIdNotFoundException(idProducto));

		return productoMapper.toResponseDTO(producto);
	}

	public List<ProductoResponseDTO> listarProductosActivos() {
		return productoRepository.findByActivoTrue().stream().map(productoMapper::toResponseDTO)
				.collect(Collectors.toList());
	}

	public List<ProductoResponseDTO> listarProductosInactivos() {
		return productoRepository.findByActivoFalse().stream().map(productoMapper::toResponseDTO)
				.collect(Collectors.toList());
	}

	public List<ProductoResponseDTO> listarProductosNombre(String nombre) {
		return productoRepository.findByNombreContainingIgnoreCase(nombre).stream().map(productoMapper::toResponseDTO)
				.collect(Collectors.toList());
	}

	public void eliminarProducto(Long idProducto) {
		Producto producto = productoRepository.findById(idProducto)
				.orElseThrow(() -> new ProductoIdNotFoundException(idProducto));

		producto.setActivo(false);
		productoRepository.save(producto);
	}
	
	public void activarProducto(Long idProducto) {
		Producto producto = productoRepository.findById(idProducto)
				.orElseThrow(() -> new ProductoIdNotFoundException(idProducto));

		producto.setActivo(true);
		productoRepository.save(producto);
    }
	
	public ProductoResponseDTO aumentarStock(AumentarStockRequestDTO request) {
	    Producto producto = productoRepository.findByCodigoBarras(request.getCodigoBarras())
	        .stream()
	        .findFirst()
	        .orElseThrow(() -> new ProductoCodigoNotFoundException(request.getCodigoBarras()));

	    Integer stockActual = producto.getCantidadStock();
	    producto.setCantidadStock(stockActual + request.getCantidadASumar());

	    return productoMapper.toResponseDTO(productoRepository.save(producto));
	}

}
