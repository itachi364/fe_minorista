package com.msvanegasg.facturaelectronica.catalog.application.dto;

public record MunicipalityResult(
        String code,
        String departmentCode,
        String name,
        boolean active,
        String source,
        String sourceVersion) {
}
