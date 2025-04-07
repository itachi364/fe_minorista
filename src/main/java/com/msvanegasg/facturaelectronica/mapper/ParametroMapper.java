package com.msvanegasg.facturaelectronica.mapper;

import com.msvanegasg.facturaelectronica.DTO.ParametroDTO;
import com.msvanegasg.facturaelectronica.models.Parametro;

public class ParametroMapper {

    public static ParametroDTO toDTO(Parametro parametro) {
        if (parametro == null) return null;

        return ParametroDTO.builder()
                .clave(parametro.getClave())
                .valor(parametro.getValor())
                .descripcion(parametro.getDescripcion())
                .build();
    }

    public static Parametro toEntity(ParametroDTO dto) {
        if (dto == null) return null;

        return Parametro.builder()
                .clave(dto.getClave())
                .valor(dto.getValor())
                .descripcion(dto.getDescripcion())
                .activo(true)
                .build();
    }
}
