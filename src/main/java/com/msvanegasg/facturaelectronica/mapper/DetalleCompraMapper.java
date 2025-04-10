package com.msvanegasg.facturaelectronica.mapper;

import com.msvanegasg.facturaelectronica.DTO.DetalleCompraDTO;
import com.msvanegasg.facturaelectronica.models.DetalleCompra;

public class DetalleCompraMapper {

	public static DetalleCompraDTO toDTO(DetalleCompra detalle) {
		return DetalleCompraDTO.builder()
				.codigoBarras(detalle.getProducto())
				.cantidad(detalle.getCantidad())
				.precioUnitario(detalle.getPrecioUnitario())
				.subtotal(detalle.getSubtotal())
				.iva(detalle.getIva())
				.totalLinea(detalle.getTotalLinea())
				.build();
	}

}