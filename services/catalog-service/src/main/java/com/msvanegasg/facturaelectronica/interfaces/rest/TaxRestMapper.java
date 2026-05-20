package com.msvanegasg.facturaelectronica.catalog.interfaces.rest;

import com.msvanegasg.facturaelectronica.catalog.application.dto.TaxCommand;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Tax;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.CountryResponse;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.TaxRequest;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.TaxResponse;
import com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto.TaxUpdateResponse;

public final class TaxRestMapper {

    private TaxRestMapper() {
    }

    public static TaxCommand toCommand(TaxRequest dto) {
        return new TaxCommand(dto.getNombre(), dto.getPorcentaje(), dto.getTipo(), dto.getCodPais(),
                dto.getDescripcion());
    }

    public static TaxResponse toResponse(Tax tax) {
        return TaxResponse.builder()
                .idImpuesto(tax.id())
                .nombre(tax.name())
                .porcentaje(tax.percentage())
                .tipo(tax.type())
                .pais(toCountryResponse(tax))
                .activo(tax.active())
                .descripcion(tax.description())
                .build();
    }

    public static TaxUpdateResponse toUpdateResponse(Tax tax) {
        return TaxUpdateResponse.builder()
                .id(tax.id())
                .nombre(tax.name())
                .porcentaje(tax.percentage())
                .tipo(tax.type())
                .descripcion(tax.description())
                .codigoPais(toCountryResponse(tax))
                .build();
    }

    private static CountryResponse toCountryResponse(Tax tax) {
        return CountryResponse.builder()
                .codigoPais(tax.country().code())
                .nombre(tax.country().name())
                .moneda(tax.country().currency())
                .activo(tax.country().active())
                .build();
    }
}
