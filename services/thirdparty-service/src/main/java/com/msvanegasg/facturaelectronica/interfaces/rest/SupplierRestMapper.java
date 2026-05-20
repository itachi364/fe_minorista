package com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest;

import java.util.Optional;

import com.msvanegasg.facturaelectronica.thirdparty.application.dto.SupplierCommand;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.Supplier;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto.SupplierRequest;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto.SupplierResponse;

public final class SupplierRestMapper {

    private SupplierRestMapper() {
    }

    public static SupplierCommand toCommand(SupplierRequest dto) {
        return new SupplierCommand(
                dto.getIdTipoDocumento(),
                dto.getNumeroDocumento(),
                verificationDigit(dto),
                dto.getNombre(),
                dto.getTelefono(),
                dto.getDireccion(),
                dto.getCorreo());
    }

    public static SupplierRequest toRequestResponse(Supplier supplier) {
        return SupplierRequest.builder()
                .idTipoDocumento(supplier.documentType().id())
                .numeroDocumento(supplier.documentNumber())
                .digitoVerificacion(Optional.ofNullable(supplier.verificationDigit()))
                .nombre(supplier.name())
                .telefono(supplier.phone())
                .direccion(supplier.address())
                .correo(supplier.email())
                .build();
    }

    public static SupplierResponse toResponse(Supplier supplier) {
        return SupplierResponse.builder()
                .idProveedor(supplier.id())
                .nombre(supplier.name())
                .tipoDocumento(SupplierResponse.DocumentTypeResponse.builder()
                        .id(supplier.documentType().id())
                        .nombre(supplier.documentType().name())
                        .build())
                .numeroDocumento(supplier.documentNumber())
                .digitoVerificacion(supplier.verificationDigit())
                .direccion(supplier.address())
                .telefono(supplier.phone())
                .correoElectronico(supplier.email())
                .activo(supplier.active())
                .build();
    }

    private static Integer verificationDigit(SupplierRequest dto) {
        return Optional.ofNullable(dto.getDigitoVerificacion())
                .flatMap(value -> value)
                .orElse(null);
    }
}
