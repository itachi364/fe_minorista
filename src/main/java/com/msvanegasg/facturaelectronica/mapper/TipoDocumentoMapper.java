package com.msvanegasg.facturaelectronica.mapper;

import com.msvanegasg.facturaelectronica.DTO.TipoDocumentoDTO;
import com.msvanegasg.facturaelectronica.models.TipoDocumento;

public class TipoDocumentoMapper {

    public static TipoDocumentoDTO toDTO(TipoDocumento tipo) {
        if (tipo == null) return null;

        return TipoDocumentoDTO.builder()
        		.codigo(tipo.getCodigo())
                .nombre(tipo.getNombre())
                .descripcion(tipo.getDescripcion())
                .build();
    }

    public static TipoDocumento toEntity(TipoDocumentoDTO dto) {
        if (dto == null) return null;

        return TipoDocumento.builder()
        		.codigo(dto.getCodigo())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .build();
    }
}
