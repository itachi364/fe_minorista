package com.msvanegasg.facturaelectronica.mapper;

import com.msvanegasg.facturaelectronica.DTO.MetodoPagoDTO;
import com.msvanegasg.facturaelectronica.models.MetodoPago;

public class MetodoPagoMapper {

    public static MetodoPagoDTO toDTO(MetodoPago metodoPago) {
        if (metodoPago == null) {
            return null;
        }

        return MetodoPagoDTO.builder()
                .nombre(metodoPago.getNombre())
                .descripcion(metodoPago.getDescripcion())
                .build();
    }

    public static MetodoPago toEntity(MetodoPagoDTO dto) {
        if (dto == null) {
            return null;
        }

        return MetodoPago.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .activo(true)
                .build();
    }
}
