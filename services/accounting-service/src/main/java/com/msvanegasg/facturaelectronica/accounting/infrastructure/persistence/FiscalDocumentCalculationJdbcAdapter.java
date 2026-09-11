package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalDocumentCalculationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalAccumulationTotals;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalDocumentCalculationRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalAccumulationScope;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingDecision;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

@Component
public class FiscalDocumentCalculationJdbcAdapter implements FiscalDocumentCalculationRepositoryPort {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public FiscalDocumentCalculationJdbcAdapter(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Optional<FiscalDocumentCalculationResult> findBySource(UUID companyId, AccountingSourceType sourceType,
            UUID sourceId) {
        List<String> payloads = jdbcTemplate.query("SELECT result_payload::text FROM fiscal_document_calculation "
                        + "WHERE company_id = ? AND source_type = ? AND source_id = ?",
                (rs, row) -> rs.getString(1), companyId, sourceType.name(), sourceId);
        return payloads.stream().findFirst().map(this::readResult);
    }

    @Override
    public FiscalDocumentCalculationResult save(FiscalDocumentCalculationResult result) {
        jdbcTemplate.update("INSERT INTO fiscal_document_calculation "
                        + "(id, company_id, source_type, source_id, request_hash, operation_date, third_party_id, "
                        + "operation_municipality_code, status, profile_evidence, result_payload, created_at, "
                        + "contract_id, payment_id) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, CAST(? AS jsonb), CAST(? AS jsonb), ?, ?, ?)",
                result.calculationId(), result.companyId(), result.sourceType().name(), result.sourceId(),
                result.requestHash(), result.operationDate(), result.thirdPartyId(), result.municipalityCode(),
                result.status(), write(result.profileEvidence()), write(result), result.calculatedAt(),
                result.contractId(), result.paymentId());
        result.lines().forEach(line -> {
            for (int index = 0; index < line.items().size(); index++) {
                var item = line.items().get(index);
                jdbcTemplate.update("INSERT INTO fiscal_line_calculation_snapshot "
                                + "(id, calculation_id, line_id, item_index, concept_code, withholding_type, decision, "
                                + "base_amount, rate, amount, rule_id, rule_version, parameter_version, legal_reference, "
                                + "source_url, reason, created_at, previous_accumulated_base, cumulative_base) "
                                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                        UUID.randomUUID(), result.calculationId(), line.lineId(), index, line.conceptCode(),
                        item.withholdingType().name(), item.decision().name(), item.baseAmount(), item.rate(),
                        item.amount(), item.ruleId(), item.ruleVersion(), item.parameterVersion(), item.legalReference(),
                        item.sourceUrl(), item.reason(), result.calculatedAt(), item.previousAccumulatedBase(),
                        item.cumulativeBase());
            }
        });
        return result;
    }

    @Override
    public void lockAccumulation(UUID companyId, UUID thirdPartyId, LocalDate operationDate) {
        String key = companyId + ":" + thirdPartyId + ":" + operationDate;
        jdbcTemplate.query("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", rs -> { }, key);
    }

    @Override
    public FiscalAccumulationTotals findDailyAccumulation(UUID companyId, UUID thirdPartyId,
            LocalDate operationDate, String conceptCode) {
        return jdbcTemplate.queryForObject("SELECT COALESCE(SUM(taxable_base), 0) taxable_base, "
                        + "COALESCE(SUM(tax_amount), 0) tax_amount, "
                        + "COALESCE(SUM(retefuente_amount), 0) retefuente_amount, "
                        + "COALESCE(SUM(reteiva_amount), 0) reteiva_amount, "
                        + "COALESCE(SUM(reteica_amount), 0) reteica_amount, "
                        + "COALESCE(SUM(self_withholding_amount), 0) self_withholding_amount, "
                        + "COALESCE(SUM(aiu_amount), 0) aiu_amount, "
                        + "COALESCE(SUM(gross_payment_amount), 0) gross_payment_amount "
                        + "FROM fiscal_accumulation_line WHERE company_id = ? AND third_party_id = ? "
                        + "AND operation_date = ? AND concept_code = ? AND reversed_at IS NULL",
                (rs, row) -> {
                    Map<WithholdingType, BigDecimal> amounts = new EnumMap<>(WithholdingType.class);
                    amounts.put(WithholdingType.RETEFUENTE, rs.getBigDecimal("retefuente_amount"));
                    amounts.put(WithholdingType.RETEIVA, rs.getBigDecimal("reteiva_amount"));
                    amounts.put(WithholdingType.RETEICA, rs.getBigDecimal("reteica_amount"));
                    amounts.put(WithholdingType.AUTORETENCION, rs.getBigDecimal("self_withholding_amount"));
                    return new FiscalAccumulationTotals(rs.getBigDecimal("taxable_base"),
                            rs.getBigDecimal("tax_amount"), rs.getBigDecimal("aiu_amount"),
                            rs.getBigDecimal("gross_payment_amount"), amounts);
                }, companyId, thirdPartyId, operationDate, conceptCode);
    }

    @Override
    public FiscalAccumulationTotals findAccumulation(UUID companyId, UUID thirdPartyId, LocalDate operationDate,
            String conceptCode, FiscalAccumulationScope scope, UUID contractId) {
        if (scope == FiscalAccumulationScope.OPERATION) return FiscalAccumulationTotals.empty();
        LocalDate from;
        LocalDate to;
        if (scope == FiscalAccumulationScope.DAY || scope == FiscalAccumulationScope.CONTRACT) {
            from = scope == FiscalAccumulationScope.CONTRACT ? LocalDate.of(1900, 1, 1) : operationDate;
            to = scope == FiscalAccumulationScope.CONTRACT ? operationDate : operationDate;
        } else if (scope == FiscalAccumulationScope.MONTH) {
            from = operationDate.withDayOfMonth(1);
            to = operationDate.withDayOfMonth(operationDate.lengthOfMonth());
        } else {
            from = operationDate.withDayOfYear(1);
            to = operationDate.withDayOfYear(operationDate.lengthOfYear());
        }
        if (scope == FiscalAccumulationScope.CONTRACT && contractId == null) return FiscalAccumulationTotals.empty();
        String contractFilter = scope == FiscalAccumulationScope.CONTRACT ? " AND contract_id = ?" : "";
        Object[] arguments = scope == FiscalAccumulationScope.CONTRACT
                ? new Object[] { companyId, thirdPartyId, from, to, conceptCode, contractId }
                : new Object[] { companyId, thirdPartyId, from, to, conceptCode };
        return jdbcTemplate.queryForObject("SELECT COALESCE(SUM(taxable_base), 0), COALESCE(SUM(tax_amount), 0), "
                        + "COALESCE(SUM(retefuente_amount), 0), COALESCE(SUM(reteiva_amount), 0), "
                        + "COALESCE(SUM(reteica_amount), 0), COALESCE(SUM(self_withholding_amount), 0), "
                        + "COALESCE(SUM(aiu_amount), 0), COALESCE(SUM(gross_payment_amount), 0) "
                        + "FROM fiscal_accumulation_line WHERE company_id = ? AND third_party_id = ? "
                        + "AND operation_date BETWEEN ? AND ? AND concept_code = ? AND reversed_at IS NULL"
                        + contractFilter,
                (rs, row) -> {
                    Map<WithholdingType, BigDecimal> amounts = new EnumMap<>(WithholdingType.class);
                    amounts.put(WithholdingType.RETEFUENTE, rs.getBigDecimal(3));
                    amounts.put(WithholdingType.RETEIVA, rs.getBigDecimal(4));
                    amounts.put(WithholdingType.RETEICA, rs.getBigDecimal(5));
                    amounts.put(WithholdingType.AUTORETENCION, rs.getBigDecimal(6));
                    return new FiscalAccumulationTotals(rs.getBigDecimal(1), rs.getBigDecimal(2),
                            rs.getBigDecimal(7), rs.getBigDecimal(8), amounts);
                }, arguments);
    }

    @Override
    public void saveAccumulations(FiscalDocumentCalculationResult result) {
        result.lines().forEach(line -> {
            Map<WithholdingType, BigDecimal> amounts = new EnumMap<>(WithholdingType.class);
            line.items().stream().filter(item -> item.decision() == WithholdingDecision.APPLIED)
                    .forEach(item -> amounts.merge(item.withholdingType(), item.amount(), BigDecimal::add));
            jdbcTemplate.update("INSERT INTO fiscal_accumulation_line (id, calculation_id, company_id, "
                            + "third_party_id, operation_date, concept_code, line_id, taxable_base, tax_amount, "
                            + "retefuente_amount, reteiva_amount, reteica_amount, self_withholding_amount, created_at, "
                            + "contract_id, payment_id, aiu_amount, gross_payment_amount) "
                            + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                            + "ON CONFLICT DO NOTHING",
                    UUID.randomUUID(), result.calculationId(), result.companyId(), result.thirdPartyId(),
                    result.operationDate(), line.conceptCode(), line.lineId(), line.taxableBaseAmount(),
                    line.taxAmount(), amount(amounts, WithholdingType.RETEFUENTE),
                    amount(amounts, WithholdingType.RETEIVA), amount(amounts, WithholdingType.RETEICA),
                    amount(amounts, WithholdingType.AUTORETENCION), result.calculatedAt(), result.contractId(),
                    result.paymentId(), line.aiuAmount(), line.grossPaymentAmount());
        });
    }

    @Override
    public boolean isPeriodClosed(UUID companyId, LocalDate operationDate) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM fiscal_period_close "
                        + "WHERE company_id = ? AND fiscal_year = ? AND fiscal_month = ?",
                Integer.class, companyId, operationDate.getYear(), operationDate.getMonthValue());
        return count != null && count > 0;
    }

    private static BigDecimal amount(Map<WithholdingType, BigDecimal> amounts, WithholdingType type) {
        return amounts.getOrDefault(type, BigDecimal.ZERO).setScale(2, java.math.RoundingMode.HALF_UP);
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No fue posible serializar el snapshot fiscal.", exception);
        }
    }

    private FiscalDocumentCalculationResult readResult(String payload) {
        try {
            return objectMapper.readValue(payload, FiscalDocumentCalculationResult.class);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No fue posible leer el snapshot fiscal.", exception);
        }
    }
}
