package com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest;

import java.util.Optional;

import com.msvanegasg.facturaelectronica.thirdparty.application.dto.CustomerCommand;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.CustomerResult;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto.CustomerRequest;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto.CustomerResponse;

public final class CustomerRestMapper {

    private CustomerRestMapper() {
    }

    public static CustomerCommand toCommand(CustomerRequest dto) {
        return new CustomerCommand(
                dto.getNombre(),
                dto.getIdTipoDocumento(),
                dto.getNumeroDocumento(),
                verificationDigit(dto),
                dto.getDireccion(),
                dto.getTelefono(),
                dto.getCorreoElectronico());
    }

    public static CustomerResponse toResponse(CustomerResult result) {
        return CustomerResponse.builder()
                .idCliente(result.id())
                .nombre(result.name())
                .idTipoDocumento(result.documentTypeId())
                .codigoTipoDocumento(result.documentTypeCode())
                .descripcionTipoDocumento(result.documentTypeDescription())
                .numeroDocumento(result.documentNumber())
                .digitoVerificacion(result.verificationDigit())
                .direccion(result.address())
                .telefono(result.phone())
                .correoElectronico(result.email())
                .tipoCliente(result.customerType())
                .activo(result.active())
                .build();
    }

    private static Integer verificationDigit(CustomerRequest dto) {
        return Optional.ofNullable(dto.getDigitoVerificacion())
                .flatMap(value -> value)
                .orElse(null);
    }
}
