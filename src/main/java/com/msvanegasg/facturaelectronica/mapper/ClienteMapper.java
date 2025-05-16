package com.msvanegasg.facturaelectronica.mapper;

import java.util.Optional;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.DTO.ClienteDTO;
import com.msvanegasg.facturaelectronica.DTO.response.ClienteResponseDTO;
import com.msvanegasg.facturaelectronica.enums.TipoClienteEnum;
import com.msvanegasg.facturaelectronica.models.Cliente;

@Component
public class ClienteMapper {

    public Cliente toEntity(ClienteDTO dto, TipoClienteEnum tipoCliente) {
        return Cliente.builder()
                .nombre(dto.getNombre())
                .numeroDocumento(dto.getNumeroDocumento())
                .digitoVerificacion(dto.getDigitoVerificacion().orElse(null))
                .direccion(dto.getDireccion())
                .telefono(dto.getTelefono())
                .correoElectronico(dto.getCorreoElectronico())
                .tipoCliente(tipoCliente)
                .activo(true)
                .build();
    }

    public ClienteDTO toDTO(Cliente cliente, Long idTipoDocumento) {
        return ClienteDTO.builder()
                .nombre(cliente.getNombre())
                .idTipoDocumento(idTipoDocumento)
                .numeroDocumento(cliente.getNumeroDocumento())
                .digitoVerificacion(Optional.ofNullable(cliente.getDigitoVerificacion()))
                .direccion(cliente.getDireccion())
                .telefono(cliente.getTelefono())
                .correoElectronico(cliente.getCorreoElectronico())
                .build();
    }

    public ClienteResponseDTO toResponseDTO(Cliente cliente, String codigoTipoDocumento, String descripcionTipoDocumento) {
        return ClienteResponseDTO.builder()
                .idCliente(cliente.getIdCliente())
                .nombre(cliente.getNombre())
                .idTipoDocumento(cliente.getIdTipoDocumento())
                .codigoTipoDocumento(codigoTipoDocumento)
                .descripcionTipoDocumento(descripcionTipoDocumento)
                .numeroDocumento(cliente.getNumeroDocumento())
                .digitoVerificacion(cliente.getDigitoVerificacion())
                .direccion(cliente.getDireccion())
                .telefono(cliente.getTelefono())
                .correoElectronico(cliente.getCorreoElectronico())
                .tipoCliente(cliente.getTipoCliente().toString())
                .activo(cliente.getActivo())
                .build();
    }
    
    public void actualizarEntidadDesdeDTO(Cliente cliente, ClienteDTO dto, TipoClienteEnum tipoCliente) {
        cliente.setNombre(dto.getNombre());
        cliente.setDireccion(dto.getDireccion());
        cliente.setTelefono(dto.getTelefono());
        cliente.setCorreoElectronico(dto.getCorreoElectronico());
        cliente.setTipoCliente(tipoCliente);
        cliente.setDigitoVerificacion(dto.getDigitoVerificacion().orElse(null));
    }

}
