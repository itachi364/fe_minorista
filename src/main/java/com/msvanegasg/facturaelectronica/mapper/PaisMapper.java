package com.msvanegasg.facturaelectronica.mapper;

import com.msvanegasg.facturaelectronica.DTO.PaisDTO;
import com.msvanegasg.facturaelectronica.models.Pais;

public class PaisMapper {

    public static PaisDTO toDTO(Pais pais) {
        if (pais == null) return null;
        return new PaisDTO(
            pais.getCodigoPais(),
            pais.getNombre(),
            pais.getMoneda()
        );
    }

    public static Pais toEntity(PaisDTO dto) {
        return Pais.builder()
                .codigoPais(dto.getCodigoPais())
                .nombre(dto.getNombre())
                .moneda(dto.getMoneda())
                .activo(true)
                .build();
    }
}
