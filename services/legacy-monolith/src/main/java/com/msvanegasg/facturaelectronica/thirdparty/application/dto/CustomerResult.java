package com.msvanegasg.facturaelectronica.thirdparty.application.dto;

public record CustomerResult(
        Long id,
        String name,
        Long documentTypeId,
        String documentTypeCode,
        String documentTypeDescription,
        Long documentNumber,
        Integer verificationDigit,
        String address,
        String phone,
        String email,
        String customerType,
        boolean active) {
}
