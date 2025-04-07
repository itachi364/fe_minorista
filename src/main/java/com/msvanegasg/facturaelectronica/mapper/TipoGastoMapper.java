package com.msvanegasg.facturaelectronica.mapper;

import com.msvanegasg.facturaelectronica.DTO.TipoGastoDTO;
import com.msvanegasg.facturaelectronica.models.TipoGasto;

public class TipoGastoMapper {

    public static TipoGastoDTO toDTO(TipoGasto tipo) {
        if (tipo == null) return null;

        return TipoGastoDTO.builder()
                .nombre(tipo.getNombre())
                .descripcion(tipo.getDescripcion())
                .build();
    }

    public static TipoGasto toEntity(TipoGastoDTO dto) {
        if (dto == null) return null;

        return TipoGasto.builder()
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .build();
    }
}
