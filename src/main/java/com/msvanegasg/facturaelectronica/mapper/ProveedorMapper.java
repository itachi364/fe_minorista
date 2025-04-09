package com.msvanegasg.facturaelectronica.mapper;

import java.util.Optional;

import com.msvanegasg.facturaelectronica.DTO.ProveedorDTO;
import com.msvanegasg.facturaelectronica.DTO.response.ProveedorResponseDTO;
import com.msvanegasg.facturaelectronica.DTO.response.TipoDocumentoResponseDTO;
import com.msvanegasg.facturaelectronica.models.Proveedor;
import com.msvanegasg.facturaelectronica.models.TipoDocumento;

public class ProveedorMapper {

    public static ProveedorDTO toDTO(Proveedor proveedor) {
        if (proveedor == null || proveedor.getTipoDocumento() == null) return null;

        return ProveedorDTO.builder()
                .idTipoDocumento(proveedor.getTipoDocumento().getCodigo())
                .numeroDocumento(proveedor.getNumeroDocumento())
                .digitoVerificacion(Optional.ofNullable(proveedor.getDigitoVerificacion()))
                .nombre(proveedor.getNombre())
                .telefono(proveedor.getTelefono())
                .direccion(proveedor.getDireccion())
                .correo(proveedor.getCorreoElectronico())
                .build();
    }

    public static Proveedor toEntity(ProveedorDTO dto, TipoDocumento tipoDocumento) {
        if (dto == null || tipoDocumento == null) return null;

        return Proveedor.builder()
                .tipoDocumento(tipoDocumento)
                .numeroDocumento(dto.getNumeroDocumento())
                .digitoVerificacion(dto.getDigitoVerificacion().orElse(null))
                .nombre(dto.getNombre())
                .telefono(dto.getTelefono())
                .direccion(dto.getDireccion())
                .correoElectronico(dto.getCorreo())
                .activo(true)
                .build();
    }
    
    public static ProveedorResponseDTO toResponseDTO(Proveedor proveedor) {
    	TipoDocumentoResponseDTO tipoDocumentoDTO = TipoDocumentoResponseDTO.builder()
    			.id(proveedor.getTipoDocumento().getCodigo())
                .nombre(proveedor.getTipoDocumento().getNombre())
                .build();
        return ProveedorResponseDTO.builder()
                .idProveedor(proveedor.getIdProveedor())
                .nombre(proveedor.getNombre())
                .tipoDocumento(tipoDocumentoDTO)
                .numeroDocumento(proveedor.getNumeroDocumento())
                .digitoVerificacion(proveedor.getDigitoVerificacion())
                .direccion(proveedor.getDireccion())
                .telefono(proveedor.getTelefono())
                .correoElectronico(proveedor.getCorreoElectronico())
                .activo(proveedor.getActivo())
                .build();
    }
    
}
