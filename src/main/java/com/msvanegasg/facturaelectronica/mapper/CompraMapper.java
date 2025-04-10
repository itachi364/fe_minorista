package com.msvanegasg.facturaelectronica.mapper;

import com.msvanegasg.facturaelectronica.DTO.CompraDTO;
import com.msvanegasg.facturaelectronica.DTO.DetalleCompraDTO;
import com.msvanegasg.facturaelectronica.models.Compra;
import com.msvanegasg.facturaelectronica.models.DetalleCompra;

import java.util.List;
import java.util.stream.Collectors;

public class CompraMapper {

	public static Compra toEntitySinProveedor(CompraDTO dto) {
	    return Compra.builder()
	            .fecha(dto.getFecha())
	            .subtotal(dto.getSubtotal())
	            .ivaTotal(dto.getIvaTotal())
	            .total(dto.getTotal())
	            .urlEvidencia(dto.getUrlEvidencia())
	            .activo(true)
	            .build();
	}



	public static CompraDTO toDTO(Compra compra, List<DetalleCompra> detalles) {
	    List<DetalleCompraDTO> detallesDTO = detalles.stream()
	            .map(DetalleCompraMapper::toDTO)
	            .collect(Collectors.toList());

	    return CompraDTO.builder()
	            .fecha(compra.getFecha())
	            .subtotal(compra.getSubtotal())
	            .ivaTotal(compra.getIvaTotal())
	            .total(compra.getTotal())
	            .urlEvidencia(compra.getUrlEvidencia())
	            .detalles(detallesDTO)
	            .build();
	}


}
