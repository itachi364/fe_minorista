package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalAccountMappingResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.AccountPresentationMappingResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalAuxiliaryResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.NationalFiscalConceptResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalPeriodSummary;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalReconciliationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalReversalResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCertificateResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCertificateIdentity;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalComplianceRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

@Component
public class FiscalComplianceJdbcAdapter implements FiscalComplianceRepositoryPort {
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    public FiscalComplianceJdbcAdapter(JdbcTemplate jdbcTemplate, ObjectMapper objectMapper) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public FiscalAccountMappingResult saveMapping(UUID companyId, WithholdingType type, String payableAccountCode,
            String receivableAccountCode, LocalDate validFrom, LocalDate validTo, UUID userId) {
        assertAccount(companyId, payableAccountCode);
        if (receivableAccountCode != null) assertAccount(companyId, receivableAccountCode);
        jdbcTemplate.update("UPDATE fiscal_account_mapping SET active = false, valid_to = ? "
                        + "WHERE company_id = ? AND withholding_type = ? AND active = true AND valid_from < ?",
                validFrom.minusDays(1), companyId, type.name(), validFrom);
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO fiscal_account_mapping (id, company_id, withholding_type, "
                        + "payable_account_code, receivable_account_code, valid_from, valid_to, active, updated_by, "
                        + "updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, true, ?, ?) "
                        + "ON CONFLICT (company_id, withholding_type, valid_from) DO UPDATE SET "
                        + "payable_account_code = EXCLUDED.payable_account_code, "
                        + "receivable_account_code = EXCLUDED.receivable_account_code, valid_to = EXCLUDED.valid_to, "
                        + "active = true, updated_by = EXCLUDED.updated_by, updated_at = EXCLUDED.updated_at",
                id, companyId, type.name(), payableAccountCode, receivableAccountCode, validFrom, validTo, userId,
                Instant.now());
        return findMappings(companyId).stream()
                .filter(item -> item.withholdingType() == type && item.validFrom().equals(validFrom))
                .findFirst().orElseThrow();
    }

    @Override
    public List<FiscalAccountMappingResult> findMappings(UUID companyId) {
        return jdbcTemplate.query("SELECT * FROM fiscal_account_mapping WHERE company_id = ? "
                        + "ORDER BY withholding_type, valid_from DESC",
                (rs, row) -> new FiscalAccountMappingResult(rs.getObject("id", UUID.class), companyId,
                        WithholdingType.valueOf(rs.getString("withholding_type")),
                        rs.getString("payable_account_code"), rs.getString("receivable_account_code"),
                        rs.getDate("valid_from").toLocalDate(), date(rs.getDate("valid_to")),
                        rs.getBoolean("active")), companyId);
    }

    @Override
    public FiscalReconciliationResult reconcile(UUID companyId, int year, int month) {
        Period period = period(year, month);
        BigDecimal fiscal = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(line.amount), 0) "
                        + "FROM fiscal_line_calculation_snapshot line "
                        + "JOIN fiscal_document_calculation calculation ON calculation.id = line.calculation_id "
                        + "LEFT JOIN fiscal_calculation_reversal reversal ON reversal.calculation_id = calculation.id "
                        + "WHERE calculation.company_id = ? AND calculation.operation_date BETWEEN ? AND ? "
                        + "AND line.decision = 'APPLIED' AND line.withholding_type <> 'AUTORETENCION' "
                        + "AND reversal.id IS NULL",
                BigDecimal.class, companyId, period.from(), period.to());
        BigDecimal accounting = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(line.credit_amount - line.debit_amount), 0) "
                        + "FROM accounting_entry_line line JOIN accounting_entry entry ON entry.id = line.entry_id "
                        + "WHERE entry.company_id = ? AND entry.status = 'POSTED' AND entry.entry_date BETWEEN ? AND ? "
                        + "AND line.account_code IN (SELECT payable_account_code FROM fiscal_account_mapping "
                        + "WHERE company_id = ? AND valid_from <= ? AND (valid_to IS NULL OR valid_to >= ?))",
                BigDecimal.class, companyId, period.from(), period.to(), companyId, period.to(), period.from());
        BigDecimal normalizedFiscal = money(fiscal);
        BigDecimal normalizedAccounting = money(accounting);
        BigDecimal difference = normalizedAccounting.subtract(normalizedFiscal).setScale(2, RoundingMode.HALF_UP);
        return new FiscalReconciliationResult(companyId, year, month, normalizedFiscal, normalizedAccounting,
                difference, difference.signum() == 0 ? "RECONCILED" : "DIFFERENCE");
    }

    @Override
    public FiscalReversalResult reverse(UUID companyId, UUID calculationId, String reason, UUID userId) {
        List<FiscalReversalResult> existing = findReversal(companyId, calculationId);
        if (!existing.isEmpty()) return existing.get(0);
        LocalDate operationDate = jdbcTemplate.query("SELECT operation_date FROM fiscal_document_calculation "
                        + "WHERE id = ? AND company_id = ?",
                rs -> rs.next() ? rs.getDate(1).toLocalDate() : null, calculationId, companyId);
        if (operationDate == null) throw new IllegalArgumentException("fiscal calculation was not found");
        if (isClosed(companyId, operationDate.getYear(), operationDate.getMonthValue())) {
            throw new IllegalStateException("El periodo fiscal se encuentra cerrado.");
        }
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        int inserted = jdbcTemplate.update("INSERT INTO fiscal_calculation_reversal "
                        + "(id, company_id, calculation_id, reason, reversed_by, reversed_at) VALUES (?, ?, ?, ?, ?, ?) "
                        + "ON CONFLICT (company_id, calculation_id) DO NOTHING",
                id, companyId, calculationId, reason, userId, now);
        if (inserted == 0) return findReversal(companyId, calculationId).get(0);
        UUID compensatingEntryId = createCompensatingEntry(companyId, calculationId, id, operationDate, reason);
        if (compensatingEntryId != null) {
            jdbcTemplate.update("UPDATE fiscal_calculation_reversal SET compensating_entry_id = ? WHERE id = ?",
                    compensatingEntryId, id);
        }
        jdbcTemplate.update("UPDATE fiscal_accumulation_line SET reversed_at = ? "
                + "WHERE calculation_id = ? AND reversed_at IS NULL", now, calculationId);
        return new FiscalReversalResult(id, companyId, calculationId, reason, userId, now, compensatingEntryId);
    }

    @Override
    public FiscalPeriodSummary summarize(UUID companyId, int year, int month) {
        Period period = period(year, month);
        Map<String, BigDecimal> bases = new LinkedHashMap<>();
        Map<String, BigDecimal> amounts = new LinkedHashMap<>();
        jdbcTemplate.query("SELECT line.withholding_type, COALESCE(SUM(line.base_amount), 0) base_amount, "
                        + "COALESCE(SUM(line.amount), 0) amount FROM fiscal_line_calculation_snapshot line "
                        + "JOIN fiscal_document_calculation calculation ON calculation.id = line.calculation_id "
                        + "LEFT JOIN fiscal_calculation_reversal reversal ON reversal.calculation_id = calculation.id "
                        + "WHERE calculation.company_id = ? AND calculation.operation_date BETWEEN ? AND ? "
                        + "AND line.decision = 'APPLIED' AND reversal.id IS NULL GROUP BY line.withholding_type "
                        + "ORDER BY line.withholding_type",
                rs -> {
                    bases.put(rs.getString("withholding_type"), money(rs.getBigDecimal("base_amount")));
                    amounts.put(rs.getString("withholding_type"), money(rs.getBigDecimal("amount")));
                }, companyId, period.from(), period.to());
        BigDecimal total = amounts.entrySet().stream()
                .filter(item -> !WithholdingType.AUTORETENCION.name().equals(item.getKey()))
                .map(Map.Entry::getValue).reduce(BigDecimal.ZERO, BigDecimal::add).setScale(2);
        return new FiscalPeriodSummary(companyId, year, month, Map.copyOf(bases), Map.copyOf(amounts), total,
                isClosed(companyId, year, month));
    }

    @Override
    public FiscalPeriodSummary close(UUID companyId, int year, int month, UUID userId) {
        FiscalPeriodSummary summary = summarize(companyId, year, month);
        if (!summary.closed()) {
            jdbcTemplate.update("INSERT INTO fiscal_period_close "
                            + "(id, company_id, fiscal_year, fiscal_month, snapshot_payload, closed_by, closed_at) "
                            + "VALUES (?, ?, ?, ?, CAST(? AS jsonb), ?, ?) "
                            + "ON CONFLICT (company_id, fiscal_year, fiscal_month) DO NOTHING",
                    UUID.randomUUID(), companyId, year, month, write(summary), userId, Instant.now());
        }
        return new FiscalPeriodSummary(summary.companyId(), year, month, summary.bases(), summary.amounts(),
                summary.totalWithheld(), true);
    }

    @Override
    public WithholdingCertificateResult generateCertificate(UUID companyId, UUID thirdPartyId, int year,
            UUID userId) {
        return generateCertificate(companyId, thirdPartyId, year,
                new WithholdingCertificateIdentity("NO_INFORMADA", companyId.toString(), companyId.toString(),
                        "NO_INFORMADA", thirdPartyId.toString(), thirdPartyId.toString()), userId);
    }

    @Override
    public WithholdingCertificateResult generateCertificate(UUID companyId, UUID thirdPartyId, int year,
            WithholdingCertificateIdentity identity, UUID userId) {
        Period period = period(year, 1, 12);
        jdbcTemplate.query("SELECT pg_advisory_xact_lock(hashtextextended(?, 0))", rs -> { },
                companyId + ":" + thirdPartyId + ":" + year);
        List<CertificateLine> lines = jdbcTemplate.query("SELECT calculation.operation_date, "
                        + "line.withholding_type, line.concept_code, line.base_amount, line.rate, line.amount, "
                        + "calculation.source_type, calculation.source_id FROM fiscal_line_calculation_snapshot line "
                        + "JOIN fiscal_document_calculation calculation ON calculation.id = line.calculation_id "
                        + "LEFT JOIN fiscal_calculation_reversal reversal ON reversal.calculation_id = calculation.id "
                        + "WHERE calculation.company_id = ? AND calculation.third_party_id = ? "
                        + "AND calculation.operation_date BETWEEN ? AND ? AND line.decision = 'APPLIED' "
                        + "AND line.withholding_type <> 'AUTORETENCION' AND reversal.id IS NULL "
                        + "ORDER BY calculation.operation_date, calculation.source_id, line.item_index",
                (rs, row) -> new CertificateLine(rs.getDate(1).toLocalDate(), rs.getString(2), rs.getString(3),
                        rs.getBigDecimal(4), rs.getBigDecimal(5), rs.getBigDecimal(6), rs.getString(7),
                        rs.getObject(8, UUID.class)), companyId, thirdPartyId, period.from(), period.to());
        BigDecimal totalBase = lines.stream().map(CertificateLine::base).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalWithheld = lines.stream().map(CertificateLine::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        Integer version = jdbcTemplate.queryForObject("SELECT COALESCE(MAX(version), 0) + 1 "
                        + "FROM withholding_certificate WHERE company_id = ? AND third_party_id = ? AND fiscal_year = ?",
                Integer.class, companyId, thirdPartyId, year);
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();
        jdbcTemplate.update("INSERT INTO withholding_certificate (id, company_id, third_party_id, fiscal_year, "
                        + "version, total_base, total_withheld, content_csv, generated_by, generated_at, "
                        + "certificate_city, issuer_identification, issuer_name, issuer_address, "
                        + "beneficiary_identification, beneficiary_name, private_storage_key, notification_status) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'PENDING')", id, companyId, thirdPartyId,
                year, version, money(totalBase), money(totalWithheld), csv(identity, year, lines), userId, now,
                identity.certificateCity(), identity.issuerIdentification(), identity.issuerName(),
                identity.issuerAddress(), identity.beneficiaryIdentification(), identity.beneficiaryName(),
                "accounting/withholding-certificates/" + companyId + "/" + id + ".csv");
        return new WithholdingCertificateResult(id, companyId, thirdPartyId, year, version, money(totalBase),
                money(totalWithheld), now);
    }

    @Override
    public List<WithholdingCertificateResult> findCertificates(UUID companyId, UUID thirdPartyId, int year) {
        return jdbcTemplate.query("SELECT * FROM withholding_certificate WHERE company_id = ? "
                        + "AND third_party_id = ? AND fiscal_year = ? ORDER BY version DESC",
                (rs, row) -> new WithholdingCertificateResult(rs.getObject("id", UUID.class), companyId,
                        thirdPartyId, year, rs.getInt("version"), rs.getBigDecimal("total_base"),
                        rs.getBigDecimal("total_withheld"), rs.getTimestamp("generated_at").toInstant()),
                companyId, thirdPartyId, year);
    }

    @Override
    public Optional<String> findCertificateContent(UUID companyId, UUID certificateId) {
        return jdbcTemplate.query("SELECT content_csv FROM withholding_certificate WHERE company_id = ? AND id = ?",
                (rs, row) -> rs.getString(1), companyId, certificateId).stream().findFirst();
    }

    @Override
    public AccountPresentationMappingResult savePresentationMapping(UUID companyId, UUID accountId,
            String financialReportingGroup, String statementSection, String presentationConcept,
            LocalDate validFrom, LocalDate validTo, String evidenceReference, UUID userId) {
        Integer accountCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accounting_account "
                + "WHERE id = ? AND company_id = ? AND active = true", Integer.class, accountId, companyId);
        if (accountCount == null || accountCount == 0) {
            throw new IllegalStateException("La cuenta contable no existe o esta inactiva para la empresa.");
        }
        jdbcTemplate.update("UPDATE account_presentation_mapping SET active = false, valid_to = ? "
                        + "WHERE company_id = ? AND account_id = ? AND financial_reporting_group = ? "
                        + "AND active = true AND valid_from < ?",
                validFrom.minusDays(1), companyId, accountId, financialReportingGroup, validFrom);
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO account_presentation_mapping (id, company_id, account_id, "
                        + "financial_reporting_group, statement_section, presentation_concept, valid_from, valid_to, "
                        + "evidence_reference, active, updated_by, updated_at) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, true, ?, ?) "
                        + "ON CONFLICT (company_id, account_id, financial_reporting_group, valid_from) DO UPDATE SET "
                        + "statement_section = EXCLUDED.statement_section, presentation_concept = EXCLUDED.presentation_concept, "
                        + "valid_to = EXCLUDED.valid_to, evidence_reference = EXCLUDED.evidence_reference, active = true, "
                        + "updated_by = EXCLUDED.updated_by, updated_at = EXCLUDED.updated_at",
                id, companyId, accountId, financialReportingGroup, statementSection, presentationConcept,
                validFrom, validTo, evidenceReference, userId, Instant.now());
        return findPresentationMappings(companyId).stream()
                .filter(item -> item.accountId().equals(accountId)
                        && item.financialReportingGroup().equals(financialReportingGroup)
                        && item.validFrom().equals(validFrom))
                .findFirst().orElseThrow();
    }

    @Override
    public List<AccountPresentationMappingResult> findPresentationMappings(UUID companyId) {
        return jdbcTemplate.query("SELECT mapping.*, account.code account_code FROM account_presentation_mapping mapping "
                        + "JOIN accounting_account account ON account.id = mapping.account_id "
                        + "WHERE mapping.company_id = ? ORDER BY mapping.financial_reporting_group, "
                        + "mapping.statement_section, account.code, mapping.valid_from DESC",
                (rs, row) -> new AccountPresentationMappingResult(rs.getObject("id", UUID.class), companyId,
                        rs.getObject("account_id", UUID.class), rs.getString("account_code"),
                        rs.getString("financial_reporting_group"), rs.getString("statement_section"),
                        rs.getString("presentation_concept"), rs.getDate("valid_from").toLocalDate(),
                        date(rs.getDate("valid_to")), rs.getString("evidence_reference"), rs.getBoolean("active")),
                companyId);
    }

    @Override
    public List<FiscalAuxiliaryResult> findForm350Auxiliary(UUID companyId, int year, int month) {
        return jdbcTemplate.query("SELECT * FROM fiscal_form_350_auxiliary WHERE company_id = ? "
                        + "AND fiscal_year = ? AND fiscal_month = ? ORDER BY form_350_section, concept_code, withholding_type",
                (rs, row) -> new FiscalAuxiliaryResult(companyId, year, month, rs.getString("concept_code"),
                        rs.getString("form_350_section"), rs.getString("withholding_type"),
                        money(rs.getBigDecimal("base_amount")), money(rs.getBigDecimal("withheld_amount")),
                        rs.getLong("document_count")), companyId, year, month);
    }

    @Override
    public List<NationalFiscalConceptResult> findNationalConcepts() {
        return jdbcTemplate.query("SELECT * FROM national_fiscal_concept_catalog ORDER BY withholding_type, code",
                (rs, row) -> new NationalFiscalConceptResult(rs.getString("code"), rs.getString("description"),
                        rs.getString("withholding_type"), rs.getString("form_350_section"),
                        rs.getString("operational_status"), rs.getString("legal_reference"),
                        rs.getString("source_url")));
    }

    private List<FiscalReversalResult> findReversal(UUID companyId, UUID calculationId) {
        return jdbcTemplate.query("SELECT * FROM fiscal_calculation_reversal WHERE company_id = ? AND calculation_id = ?",
                (rs, row) -> new FiscalReversalResult(rs.getObject("id", UUID.class), companyId, calculationId,
                        rs.getString("reason"), rs.getObject("reversed_by", UUID.class),
                        rs.getTimestamp("reversed_at").toInstant(),
                        rs.getObject("compensating_entry_id", UUID.class)), companyId, calculationId);
    }

    private UUID createCompensatingEntry(UUID companyId, UUID calculationId, UUID reversalId,
            LocalDate operationDate, String reason) {
        List<UUID> originalIds = jdbcTemplate.query("SELECT entry.id FROM accounting_entry entry "
                        + "JOIN fiscal_document_calculation calculation ON calculation.company_id = entry.company_id "
                        + "AND calculation.source_type = entry.source_type AND calculation.source_id = entry.source_id "
                        + "WHERE calculation.id = ? AND calculation.company_id = ? AND entry.status = 'POSTED'",
                (rs, row) -> rs.getObject(1, UUID.class), calculationId, companyId);
        if (originalIds.isEmpty()) return null;
        UUID originalId = originalIds.get(0);
        UUID entryId = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO accounting_entry (id, company_id, entry_date, description, source_type, "
                        + "source_id, status, debit_total, credit_total) SELECT ?, company_id, ?, ?, 'ADJUSTMENT', ?, "
                        + "'POSTED', credit_total, debit_total FROM accounting_entry WHERE id = ? AND company_id = ?",
                entryId, operationDate, "Reverso fiscal: " + reason, reversalId, originalId, companyId);
        List<EntryLine> lines = jdbcTemplate.query("SELECT line_order, account_id, account_code, account_name, "
                        + "thirdparty_id, debit_amount, credit_amount, description FROM accounting_entry_line "
                        + "WHERE entry_id = ? ORDER BY line_order",
                (rs, row) -> new EntryLine(rs.getInt(1), rs.getObject(2, UUID.class), rs.getString(3),
                        rs.getString(4), rs.getObject(5, UUID.class), rs.getBigDecimal(6), rs.getBigDecimal(7),
                        rs.getString(8)), originalId);
        lines.forEach(line -> jdbcTemplate.update("INSERT INTO accounting_entry_line (id, entry_id, line_order, "
                        + "account_id, account_code, account_name, thirdparty_id, debit_amount, credit_amount, "
                        + "description) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                UUID.randomUUID(), entryId, line.order(), line.accountId(), line.accountCode(), line.accountName(),
                line.thirdPartyId(), line.credit(), line.debit(), "Reverso: " + escapeDescription(line.description())));
        return entryId;
    }

    private static String escapeDescription(String value) {
        String normalized = value == null || value.isBlank() ? "movimiento fiscal" : value.trim();
        return normalized.length() > 240 ? normalized.substring(0, 240) : normalized;
    }

    private boolean isClosed(UUID companyId, int year, int month) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM fiscal_period_close "
                + "WHERE company_id = ? AND fiscal_year = ? AND fiscal_month = ?", Integer.class,
                companyId, year, month);
        return count != null && count > 0;
    }

    private void assertAccount(UUID companyId, String accountCode) {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM accounting_account "
                + "WHERE company_id = ? AND code = ? AND active = true", Integer.class, companyId, accountCode);
        if (count == null || count == 0) {
            throw new IllegalStateException("La cuenta contable no existe o esta inactiva para la empresa.");
        }
    }

    private String write(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("No fue posible serializar el cierre fiscal.", exception);
        }
    }

    private static String csv(WithholdingCertificateIdentity identity, int year, List<CertificateLine> lines) {
        StringBuilder value = new StringBuilder();
        value.append("campo,valor\n")
                .append("anoGravable,").append(year).append('\n')
                .append("ciudadExpedicion,").append(escape(identity.certificateCity())).append('\n')
                .append("nitAgenteRetenedor,").append(escape(identity.issuerIdentification())).append('\n')
                .append("nombreAgenteRetenedor,").append(escape(identity.issuerName())).append('\n')
                .append("direccionAgenteRetenedor,").append(escape(identity.issuerAddress())).append('\n')
                .append("identificacionBeneficiario,").append(escape(identity.beneficiaryIdentification())).append('\n')
                .append("nombreBeneficiario,").append(escape(identity.beneficiaryName())).append('\n')
                .append("firmaResponsable,USUARIO_AUTENTICADO\n\n")
                .append("fecha,tipoRetencion,concepto,base,tarifa,valor,tipoDocumento,idDocumento\n");
        lines.forEach(line -> value.append(line.date()).append(',').append(line.type()).append(',')
                .append(escape(line.concept())).append(',').append(line.base()).append(',').append(line.rate())
                .append(',').append(line.amount()).append(',').append(line.sourceType()).append(',')
                .append(line.sourceId()).append('\n'));
        return value.toString();
    }

    private static String escape(String value) {
        String safe = value == null ? "" : value;
        return safe.contains(",") || safe.contains("\"") ? "\"" + safe.replace("\"", "\"\"") + "\"" : safe;
    }

    private static Period period(int year, int month) {
        YearMonth value = YearMonth.of(year, month);
        return new Period(value.atDay(1), value.atEndOfMonth());
    }

    private static Period period(int year, int fromMonth, int toMonth) {
        return new Period(YearMonth.of(year, fromMonth).atDay(1), YearMonth.of(year, toMonth).atEndOfMonth());
    }

    private static BigDecimal money(BigDecimal value) {
        return (value == null ? BigDecimal.ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private static LocalDate date(Date value) { return value == null ? null : value.toLocalDate(); }
    private record Period(LocalDate from, LocalDate to) { }
    private record CertificateLine(LocalDate date, String type, String concept, BigDecimal base, BigDecimal rate,
            BigDecimal amount, String sourceType, UUID sourceId) { }
    private record EntryLine(int order, UUID accountId, String accountCode, String accountName, UUID thirdPartyId,
            BigDecimal debit, BigDecimal credit, String description) { }
}
