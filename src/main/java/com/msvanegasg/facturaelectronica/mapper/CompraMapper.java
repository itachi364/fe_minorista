package com.msvanegasg.facturaelectronica.mapper;

import com.msvanegasg.facturaelectronica.DTO.CompraDTO;
import com.msvanegasg.facturaelectronica.DTO.DetalleCompraDTO;
import com.msvanegasg.facturaelectronica.models.Compra;
import com.msvanegasg.facturaelectronica.models.DetalleCompra;
import com.msvanegasg.facturaelectronica.models.Proveedor;

import java.util.List;
import java.util.stream.Collectors;

public class CompraMapper {

    public static Compra toEntity(CompraDTO dto) {
        Compra compra = Compra.builder()
                .proveedor(Proveedor.builder().idProveedor(dto.getIdProveedor()).build())
                .fecha(dto.getFecha())
                .subtotal(dto.getSubtotal())
                .ivaTotal(dto.getIvaTotal())
                .total(dto.getTotal())
                .urlEvidencia(dto.getUrlEvidencia())
                .estado(dto.getEstado())
                .activo(true)
                .build();

        return compra;
    }

    public static CompraDTO toDTO(Compra compra, List<DetalleCompra> detalles) {
        List<DetalleCompraDTO> detallesDTO = detalles.stream()
                .map(DetalleCompraMapper::toDTO)
                .collect(Collectors.toList());

        return CompraDTO.builder()
                .idProveedor(compra.getProveedor().getIdProveedor())
                .fecha(compra.getFecha())
                .subtotal(compra.getSubtotal())
                .ivaTotal(compra.getIvaTotal())
                .total(compra.getTotal())
                .urlEvidencia(compra.getUrlEvidencia())
                .estado(compra.getEstado())
                .detalles(detallesDTO)
                .build();
    }
}
