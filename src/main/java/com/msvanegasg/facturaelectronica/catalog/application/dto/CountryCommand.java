package com.msvanegasg.facturaelectronica.catalog.application.dto;

public record CountryCommand(
        String code,
        String name,
        String currency) {
}
