package com.msvanegasg.facturaelectronica.service;

import com.msvanegasg.facturaelectronica.DTO.CompraDTO;
import com.msvanegasg.facturaelectronica.DTO.request.AumentarStockRequestDTO;
import com.msvanegasg.facturaelectronica.DTO.response.ProductoResponseDTO;
import com.msvanegasg.facturaelectronica.client.ProductoClient;
import com.msvanegasg.facturaelectronica.enums.Estado;
import com.msvanegasg.facturaelectronica.exception.compra.*;
import com.msvanegasg.facturaelectronica.models.Compra;
import com.msvanegasg.facturaelectronica.models.DetalleCompra;
import com.msvanegasg.facturaelectronica.repository.CompraRepository;
import com.msvanegasg.facturaelectronica.repository.DetalleCompraRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.msvanegasg.facturaelectronica.client.ProveedorClient;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CompraService {

	private final CompraRepository compraRepository;
	private final DetalleCompraRepository detalleCompraRepository;
	private final ProductoClient productoClient;
	private final ProveedorClient proveedorClient;

	@Transactional
	public Compra crearCompra(CompraDTO compraDTO) {
	    var proveedor = proveedorClient.obtenerProveedorPorDocumento(
	        compraDTO.getNumeroDocumento(), compraDTO.getTipoDocumentoId()
	    );

	    Map<Long, ProductoResponseDTO> productosMap = obtenerProductos(compraDTO);

	    Compra compra = inicializarCompra(proveedor.getIdProveedor(), compraDTO);

	    compra = compraRepository.save(compra);

	    registrarDetallesCompra(compra, compraDTO, productosMap);

	    actualizarStockYEstado(compra, compraDTO);

	    return compraRepository.save(compra);
	}

	@Transactional
	public Compra actualizarCompraSiPendiente(Long idCompra, CompraDTO compraDTO) {
	    Compra compra = compraRepository.findById(idCompra)
	        .orElseThrow(() -> new CompraNotFoundException(idCompra));

	    if (compra.getEstado() != Estado.PENDIENTE) {
	        throw new CompraNoEditableException(idCompra);
	    }

	    var proveedor = proveedorClient.obtenerProveedorPorDocumento(
	        compraDTO.getNumeroDocumento(), compraDTO.getTipoDocumentoId());

	    // Inactivar detalles antiguos
	    detalleCompraRepository.findByCompraIdCompra(idCompra)
	        .forEach(detalle -> {
	            detalle.setActivo(false);
	            detalleCompraRepository.save(detalle);
	        });

	    Map<Long, ProductoResponseDTO> productosMap = obtenerProductos(compraDTO);

	    // Actualizar campos
	    compra.setIdProveedor(proveedor.getIdProveedor());
	    compra.setUrlEvidencia(compraDTO.getUrlEvidencia());
	    compra.setSubtotal(compraDTO.getSubtotal());
	    compra.setIvaTotal(compraDTO.getIvaTotal());
	    compra.setTotal(compraDTO.getTotal());

	    registrarDetallesCompra(compra, compraDTO, productosMap);

	    actualizarStockYEstado(compra, compraDTO);

	    return compraRepository.save(compra);
	}

	@Transactional(readOnly = true)
	public Compra obtenerCompraPorId(Long idCompra) {
		return compraRepository.findById(idCompra).orElseThrow(() -> new CompraNotFoundException(idCompra));
	}

	@Transactional(readOnly = true)
	public List<Compra> listarComprasActivas() {
		return compraRepository.findByActivoTrue();
	}

	@Transactional(readOnly = true)
	public List<DetalleCompra> obtenerDetallesPorCompra(Long idCompra) {
		if (!compraRepository.existsById(idCompra)) {
			throw new CompraNotFoundException(idCompra);
		}
		return detalleCompraRepository.findByCompraIdCompra(idCompra);
	}
	
	
	private Map<Long, ProductoResponseDTO> obtenerProductos(CompraDTO compraDTO) {
	    Map<Long, ProductoResponseDTO> productosMap = new HashMap<>();
	    for (var detalle : compraDTO.getDetalles()) {
	        ProductoResponseDTO producto = productoClient.obtenerProductoPorCodigoBarras(detalle.getCodigoBarras());
	        productosMap.put(detalle.getCodigoBarras(), producto);
	    }
	    return productosMap;
	}

	private Compra inicializarCompra(Long idProveedor, CompraDTO dto) {
	    return Compra.builder()
	        .idProveedor(idProveedor)
	        .fecha(LocalDateTime.now())
	        .estado(Estado.PENDIENTE)
	        .activo(true)
	        .urlEvidencia(dto.getUrlEvidencia())
	        .subtotal(dto.getSubtotal())
	        .ivaTotal(dto.getIvaTotal())
	        .total(dto.getTotal())
	        .build();
	}

	private void registrarDetallesCompra(Compra compra, CompraDTO dto, Map<Long, ProductoResponseDTO> productosMap) {
	    for (var detalleDTO : dto.getDetalles()) {
	        ProductoResponseDTO producto = productosMap.get(detalleDTO.getCodigoBarras());

	        DetalleCompra detalle = DetalleCompra.builder()
	            .compra(compra)
	            .producto(producto.getId())
	            .cantidad(detalleDTO.getCantidad())
	            .precioUnitario(detalleDTO.getPrecioUnitario())
	            .subtotal(detalleDTO.getSubtotal())
	            .iva(detalleDTO.getIva())
	            .totalLinea(detalleDTO.getTotalLinea())
	            .activo(true)
	            .build();

	        detalleCompraRepository.save(detalle);
	    }
	}

	private void actualizarStockYEstado(Compra compra, CompraDTO dto) {
	    for (var detalleDTO : dto.getDetalles()) {
	        AumentarStockRequestDTO stockRequest = new AumentarStockRequestDTO(
	            detalleDTO.getCodigoBarras(), detalleDTO.getCantidad());
	        productoClient.aumentarStock(stockRequest);
	    }
	    compra.setEstado(Estado.PROCESADO);
	}

}
