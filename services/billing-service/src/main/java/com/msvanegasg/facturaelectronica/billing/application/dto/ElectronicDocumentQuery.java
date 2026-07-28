package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

public record ElectronicDocumentQuery(UUID companyId, ElectronicDocumentType documentType,
        ElectronicDocumentStatus status, UUID customerId, LocalDate from, LocalDate to, String prefix, Long number,
        String cufeCude) {
}