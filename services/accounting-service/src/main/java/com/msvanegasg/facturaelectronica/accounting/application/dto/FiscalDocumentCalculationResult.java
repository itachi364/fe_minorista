package com.msvanegasg.facturaelectronica.accounting.application.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;

public record FiscalDocumentCalculationResult(
        UUID calculationId,
        UUID companyId,
        FiscalOperationType operationType,
        UUID thirdPartyId,
        LocalDate operationDate,
        String municipalityCode,
        AccountingSourceType sourceType,
        UUID sourceId,
        String requestHash,
        String status,
        List<FiscalDocumentLineResult> lines,
        BigDecimal grossAmount,
        BigDecimal withholdingTotal,
        BigDecimal netPayable,
        Map<String, Object> profileEvidence,
        Instant calculatedAt,
        UUID contractId,
        UUID paymentId) {
    public FiscalDocumentCalculationResult {
        lines = List.copyOf(lines);
        profileEvidence = Map.copyOf(profileEvidence);
    }

    public FiscalDocumentCalculationResult(UUID calculationId, UUID companyId, FiscalOperationType operationType,
            UUID thirdPartyId, LocalDate operationDate, String municipalityCode, AccountingSourceType sourceType,
            UUID sourceId, String requestHash, String status, List<FiscalDocumentLineResult> lines,
            BigDecimal grossAmount, BigDecimal withholdingTotal, BigDecimal netPayable,
            Map<String, Object> profileEvidence, Instant calculatedAt) {
        this(calculationId, companyId, operationType, thirdPartyId, operationDate, municipalityCode, sourceType,
                sourceId, requestHash, status, lines, grossAmount, withholdingTotal, netPayable, profileEvidence,
                calculatedAt, null, null);
    }
}
