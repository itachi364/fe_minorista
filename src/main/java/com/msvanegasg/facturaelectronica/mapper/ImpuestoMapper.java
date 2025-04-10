package com.msvanegasg.facturaelectronica.mapper;

import com.msvanegasg.facturaelectronica.DTO.ImpuestoDTO;
import com.msvanegasg.facturaelectronica.DTO.response.ImpuestoResponseDTO;
import com.msvanegasg.facturaelectronica.models.Impuesto;
import com.msvanegasg.facturaelectronica.models.Pais;

public class ImpuestoMapper {

    public static ImpuestoDTO toDTO(Impuesto impuesto) {
        if (impuesto == null) return null;
        return new ImpuestoDTO(
                impuesto.getNombre(),
                impuesto.getPorcentaje(),
                impuesto.getTipo(),
                impuesto.getDescripcion(),
                impuesto.getPais().getCodigoPais()
        );
    }

    public static Impuesto toEntity(ImpuestoDTO dto, Pais pais) {
        return Impuesto.builder()
                .nombre(dto.getNombre())
                .porcentaje(dto.getPorcentaje())
                .tipo(dto.getTipo())
                .descripcion(dto.getDescripcion())
                .pais(pais)
                .activo(true)
                .build();
    }
    
    public static ImpuestoResponseDTO toResponseDTO(Impuesto impuesto, Pais pais) {
        
        return ImpuestoResponseDTO.builder()
        		.id(impuesto.getIdImpuesto())
        		.nombre(impuesto.getNombre())
        		.descripcion(impuesto.getDescripcion())
        		.porcentaje(impuesto.getPorcentaje())
        		.tipo(impuesto.getTipo())
        		.codigoPais(pais)
                .build();
    }
}
