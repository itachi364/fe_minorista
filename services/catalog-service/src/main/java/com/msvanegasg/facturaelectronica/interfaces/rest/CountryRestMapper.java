package com.msvanegasg.facturaelectronica.catalog.interfaces.rest;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CountryCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Country;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CountryRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CountryResponse;

public final class CountryRestMapper {

    private CountryRestMapper() {
    }

    public static CountryCommand toCommand(CountryRequest dto) {
        return new CountryCommand(dto.getCodigoPais(), dto.getNombre(), dto.getMoneda());
    }

    public static CountryResponse toResponse(Country country) {
        return CountryResponse.builder()
                .codigoPais(country.code())
                .nombre(country.name())
                .moneda(country.currency())
                .activo(country.active())
                .build();
    }

    public static CountryRequest toRequest(Country country) {
        return new CountryRequest(country.code(), country.name(), country.currency());
    }
}
