package com.msvanegasg.facturaelectronica.billing.application.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.domain.model.SaleStatus;

public record SaleQuery(UUID companyId, SaleStatus status, LocalDate from, LocalDate to) {
}