package com.msvanegasg.facturaelectronica.thirdparty.application.dto;

public record SupplierCommand(
        Long documentTypeId,
        Long documentNumber,
        Integer verificationDigit,
        String name,
        String phone,
        String address,
        String email) {
}
