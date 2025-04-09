package com.msvanegasg.facturaelectronica.mapper;

import com.msvanegasg.facturaelectronica.DTO.GastoDTO;
import com.msvanegasg.facturaelectronica.DTO.response.GastoResponseDTO;
import com.msvanegasg.facturaelectronica.DTO.response.MetodoPagoResponseDTO;
import com.msvanegasg.facturaelectronica.DTO.response.TipoGastoResponseDTO;
import com.msvanegasg.facturaelectronica.models.Gasto;
import com.msvanegasg.facturaelectronica.models.MetodoPago;
import com.msvanegasg.facturaelectronica.models.TipoGasto;

public class GastoMapper {

    public static Gasto toEntity(GastoDTO dto, TipoGasto tipoGasto, MetodoPago metodoPago) {
        return Gasto.builder()
                .fecha(dto.getFecha())
                .monto(dto.getMonto())
                .descripcion(dto.getDescripcion())
                .tipoGasto(tipoGasto)
                .metodoPago(metodoPago)
                .urlEvidencia(dto.getUrlEvidencia())
                .estado(dto.getEstado())
                .activo(true)
                .build();
    }

    public static GastoDTO toDTO(Gasto gasto) {
        return GastoDTO.builder()
                .fecha(gasto.getFecha())
                .monto(gasto.getMonto())
                .descripcion(gasto.getDescripcion())
                .idTipoGasto(gasto.getTipoGasto().getIdTipoGasto())
                .idMetodoPago(gasto.getMetodoPago().getIdMetodoPago())
                .urlEvidencia(gasto.getUrlEvidencia())
                .estado(gasto.getEstado())
                .build();
    }
    
    public static GastoResponseDTO toResponseDTO(Gasto gasto) {
        TipoGastoResponseDTO tipoGastoDTO = TipoGastoResponseDTO.builder()
                .id(gasto.getTipoGasto().getIdTipoGasto())
                .nombre(gasto.getTipoGasto().getNombre())
                .descripcion(gasto.getTipoGasto().getDescripcion())
                .build();

        MetodoPagoResponseDTO metodoPagoDTO = MetodoPagoResponseDTO.builder()
                .id(gasto.getMetodoPago().getIdMetodoPago())
                .nombre(gasto.getMetodoPago().getNombre())
                .descripcion(gasto.getMetodoPago().getDescripcion())
                .build();

        return GastoResponseDTO.builder()
                .fecha(gasto.getFecha())
                .monto(gasto.getMonto())
                .descripcion(gasto.getDescripcion())
                .tipoGasto(tipoGastoDTO)
                .metodoPago(metodoPagoDTO)
                .urlEvidencia(gasto.getUrlEvidencia())
                .estado(gasto.getEstado())
                .build();
    }
}
