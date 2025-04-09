package com.msvanegasg.facturaelectronica.mapper;

import com.msvanegasg.facturaelectronica.DTO.DetalleCompraDTO;
import com.msvanegasg.facturaelectronica.models.DetalleCompra;
import com.msvanegasg.facturaelectronica.models.Producto;

public class DetalleCompraMapper {

    public static DetalleCompra toEntity(DetalleCompraDTO dto) {
        return DetalleCompra.builder()
                .producto(Producto.builder().idProducto(dto.getIdProducto()).build())
                .cantidad(dto.getCantidad())
                .precioUnitario(dto.getPrecioUnitario())
                .subtotal(dto.getSubtotal())
                .iva(dto.getIva())
                .totalLinea(dto.getTotalLinea())
                .build();
    }

    public static DetalleCompraDTO toDTO(DetalleCompra detalle) {
        return DetalleCompraDTO.builder()
                .idProducto(detalle.getProducto().getIdProducto())
                .cantidad(detalle.getCantidad())
                .precioUnitario(detalle.getPrecioUnitario())
                .subtotal(detalle.getSubtotal())
                .iva(detalle.getIva())
                .totalLinea(detalle.getTotalLinea())
                .build();
    }
}
