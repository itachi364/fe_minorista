package com.msvanegasg.facturaelectronica.thirdparty.application.dto;

public record CustomerCommand(
        String name,
        Long documentTypeId,
        Long documentNumber,
        Integer verificationDigit,
        String address,
        String phone,
        String email) {
}
