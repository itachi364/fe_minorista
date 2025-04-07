package com.msvanegasg.facturaelectronica.mapper;

import com.msvanegasg.facturaelectronica.DTO.ClienteDTO;
import com.msvanegasg.facturaelectronica.models.Cliente;
import com.msvanegasg.facturaelectronica.models.TipoDocumento;

public class ClienteMapper {

    public static Cliente toEntity(ClienteDTO dto, TipoDocumento tipoDocumento) {
        return Cliente.builder()
                .nombre(dto.getNombre())
                .tipoDocumento(tipoDocumento)
                .numeroDocumento(dto.getNumeroDocumento())
                .direccion(dto.getDireccion())
                .telefono(dto.getTelefono())
                .correoElectronico(dto.getCorreoElectronico())
                .tipoCliente(Cliente.TipoCliente.valueOf(dto.getTipoCliente().toUpperCase()))
                .activo(true)
                .build();
    }

    public static ClienteDTO toDTO(Cliente cliente) {
        return ClienteDTO.builder()
                .nombre(cliente.getNombre())
                .idTipoDocumento(cliente.getTipoDocumento().getCodigo())
                .numeroDocumento(cliente.getNumeroDocumento())
                .direccion(cliente.getDireccion())
                .telefono(cliente.getTelefono())
                .correoElectronico(cliente.getCorreoElectronico())
                .tipoCliente(cliente.getTipoCliente().name())
                .build();
    }
}
