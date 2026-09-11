package com.msvanegasg.facturaelectronica.accounting.application.dto;

public record WithholdingCertificateIdentity(String certificateCity, String issuerIdentification,
        String issuerName, String issuerAddress, String beneficiaryIdentification,
        String beneficiaryName) {
}
