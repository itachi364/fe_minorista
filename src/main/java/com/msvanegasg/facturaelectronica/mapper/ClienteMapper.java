package com.msvanegasg.facturaelectronica.mapper;

import java.util.Optional;

import com.msvanegasg.facturaelectronica.DTO.ClienteDTO;
import com.msvanegasg.facturaelectronica.DTO.response.ClienteResponseDTO;
import com.msvanegasg.facturaelectronica.DTO.response.TipoDocumentoResponseDTO;
import com.msvanegasg.facturaelectronica.models.Cliente;
import com.msvanegasg.facturaelectronica.models.TipoDocumento;

public class ClienteMapper {

	public static Cliente toEntity(ClienteDTO dto, TipoDocumento tipoDocumento) {
	    Cliente.TipoCliente tipoCliente;

	    switch (dto.getIdTipoDocumento().intValue()) {
	        case 31, 50 -> tipoCliente = Cliente.TipoCliente.JURIDICO;
	        default -> tipoCliente = Cliente.TipoCliente.NATURAL;
	    }

	    return Cliente.builder()
	            .nombre(dto.getNombre())
	            .numeroDocumento(dto.getNumeroDocumento())
	            .tipoDocumento(tipoDocumento)
	            .digitoVerificacion(dto.getDigitoVerificacion().orElse(null))
                .direccion(dto.getDireccion())
	            .telefono(dto.getTelefono())
	            .correoElectronico(dto.getCorreoElectronico())
	            .tipoCliente(tipoCliente)
	            .activo(true)
	            .build();
	}


    public static ClienteDTO toDTO(Cliente cliente) {
        return ClienteDTO.builder()
                .nombre(cliente.getNombre())
                .idTipoDocumento(cliente.getTipoDocumento().getCodigo())
                .numeroDocumento(cliente.getNumeroDocumento())
                .digitoVerificacion(Optional.ofNullable(cliente.getDigitoVerificacion()))
                .direccion(cliente.getDireccion())
                .telefono(cliente.getTelefono())
                .correoElectronico(cliente.getCorreoElectronico())
                .build();
    }
    
    public static ClienteResponseDTO toResponseDTO(Cliente cliente) {
    	TipoDocumentoResponseDTO tipoDocumentoDTO = TipoDocumentoResponseDTO.builder()
    			.id(cliente.getTipoDocumento().getCodigo())
                .nombre(cliente.getTipoDocumento().getNombre())
                .build();
        return ClienteResponseDTO.builder()
        		.id_cliente(cliente.getIdCliente())
        		.numeroDocumento(cliente.getNumeroDocumento())
        		.digitoVerificacion(cliente.getDigitoVerificacion())
        		.tipoDocumento(tipoDocumentoDTO)
        		.nombre(cliente.getNombre())
        		.correoElectronico(cliente.getCorreoElectronico())
        		.telefono(cliente.getTelefono())
        		.direccion(cliente.getDireccion())
        		.tipoCliente(cliente.getTipoCliente().name())
        		.activo(cliente.getActivo())
                .build();
    }
}
