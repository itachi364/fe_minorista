package com.msvanegasg.facturaelectronica.catalog.interfaces.rest;

import com.msvanegasg.facturaelectronica.catalog.application.dto.ParameterCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Parameter;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ParameterRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.ParameterResponse;

public final class ParameterRestMapper {

    private ParameterRestMapper() {
    }

    public static ParameterCommand toCommand(ParameterRequest dto) {
        return new ParameterCommand(dto.getClave(), dto.getValor(), dto.getDescripcion());
    }

    public static ParameterResponse toResponse(Parameter parameter) {
        return ParameterResponse.builder()
                .idParametro(parameter.id())
                .clave(parameter.key())
                .valor(parameter.value())
                .descripcion(parameter.description())
                .activo(parameter.active())
                .build();
    }

    public static ParameterRequest toRequest(Parameter parameter) {
        return ParameterRequest.builder()
                .clave(parameter.key())
                .valor(parameter.value())
                .descripcion(parameter.description())
                .build();
    }
}
