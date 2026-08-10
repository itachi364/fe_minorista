package com.msvanegasg.facturaelectronica.catalog.interfaces.rest.dto;

public record MunicipalityResponse(
        String code,
        String departmentCode,
        String name,
        boolean active,
        String source,
        String sourceVersion) {
}
