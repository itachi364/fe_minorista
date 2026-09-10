package com.msvanegasg.facturaelectronica.accounting.infrastructure.persistence;

import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CreateMunicipalFiscalPackageCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.MunicipalReteicaRuleCommand;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.MunicipalFiscalPackageRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalLegalSource;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalRuleSetStatus;
import com.msvanegasg.facturaelectronica.accounting.domain.model.MunicipalFiscalRulePackage;
import com.msvanegasg.facturaelectronica.accounting.domain.model.MunicipalityReference;

@Component
public class MunicipalFiscalPackageJdbcAdapter implements MunicipalFiscalPackageRepositoryPort {
    private static final String PACKAGE_SELECT = "SELECT rs.id, rs.code, rs.version, rs.municipality_code, "
            + "rs.municipality_name, rs.legal_source_id, rs.status, rs.valid_from, rs.valid_to, rs.reviewed_at, "
            + "rs.published_at, rs.published_by, rs.import_id, rs.created_at, rs.created_by, COUNT(wr.id) AS rule_count "
            + "FROM fiscal_rule_set rs LEFT JOIN withholding_rule wr ON wr.fiscal_rule_set_id = rs.id "
            + "WHERE rs.scope = 'MUNICIPAL' ";
    private static final String PACKAGE_GROUP = " GROUP BY rs.id, rs.code, rs.version, rs.municipality_code, "
            + "rs.municipality_name, rs.legal_source_id, rs.status, rs.valid_from, rs.valid_to, rs.reviewed_at, "
            + "rs.published_at, rs.published_by, rs.import_id, rs.created_at, rs.created_by";

    private final JdbcTemplate jdbcTemplate;

    public MunicipalFiscalPackageJdbcAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MunicipalFiscalRulePackage> findAll() {
        return jdbcTemplate.query(PACKAGE_SELECT + PACKAGE_GROUP + " ORDER BY rs.created_at DESC",
                MunicipalFiscalPackageJdbcAdapter::mapPackage);
    }

    @Override
    public Optional<MunicipalFiscalRulePackage> findById(UUID packageId) {
        return jdbcTemplate.query(PACKAGE_SELECT + "AND rs.id = ?" + PACKAGE_GROUP,
                MunicipalFiscalPackageJdbcAdapter::mapPackage, packageId).stream().findFirst();
    }

    @Override
    public FiscalLegalSource findOrCreateSource(CreateMunicipalFiscalPackageCommand command, UUID sourceId) {
        List<FiscalLegalSource> existing = jdbcTemplate.query(
                "SELECT id, code, title, authority, official_url, issued_on, review_due_on, active, created_at, created_by "
                        + "FROM fiscal_legal_source WHERE official_url = ? ORDER BY created_at LIMIT 1",
                FiscalLegalSourceJdbcAdapterAccessor::mapSource, command.officialSourceUrl());
        if (!existing.isEmpty()) {
            return existing.get(0);
        }
        Instant now = Instant.now();
        String sourceCode = "RETEICA-" + command.municipalityCode() + "-" + command.packageCode() + "-"
                + command.version();
        jdbcTemplate.update("INSERT INTO fiscal_legal_source "
                        + "(id, code, title, authority, official_url, issued_on, review_due_on, active, created_at, created_by) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, true, ?, ?)",
                sourceId, sourceCode, command.legalReference(), "Municipio " + command.municipalityCode(),
                command.officialSourceUrl(), command.validFrom(), command.validTo(), now, command.userId());
        UUID eventId = UUID.nameUUIDFromBytes((sourceId + "-effective").getBytes(StandardCharsets.UTF_8));
        jdbcTemplate.update("INSERT INTO fiscal_legal_source_event "
                        + "(id, source_id, event_type, effective_from, effective_to, reference, official_url, notes, created_at, created_by) "
                        + "VALUES (?, ?, 'EFFECTIVE', ?, ?, ?, ?, ?, ?, ?)",
                eventId, sourceId, command.validFrom(), command.validTo(), command.legalReference(),
                command.officialSourceUrl(), "Fuente creada durante el cargue municipal ReteICA.", now,
                command.userId());
        return new FiscalLegalSource(sourceId, sourceCode, command.legalReference(),
                "Municipio " + command.municipalityCode(), command.officialSourceUrl(), command.validFrom(),
                command.validTo(), true, now, command.userId());
    }

    @Override
    public MunicipalFiscalRulePackage createDraft(UUID packageId, CreateMunicipalFiscalPackageCommand command,
            MunicipalityReference municipality, UUID legalSourceId, UUID importId, List<UUID> ruleIds) {
        Instant now = Instant.now();
        jdbcTemplate.update("INSERT INTO fiscal_rule_set "
                        + "(id, code, version, scope, municipality_code, municipality_name, legal_source_id, status, "
                        + "valid_from, valid_to, import_id, created_at, created_by) "
                        + "VALUES (?, ?, ?, 'MUNICIPAL', ?, ?, ?, 'DRAFT', ?, ?, ?, ?, ?)",
                packageId, command.packageCode(), command.version(), municipality.code(), municipality.name(),
                legalSourceId, command.validFrom(), command.validTo(), importId, now, command.userId());
        for (int index = 0; index < command.rules().size(); index++) {
            insertRule(ruleIds.get(index), packageId, command, command.rules().get(index), legalSourceId);
        }
        return findById(packageId).orElseThrow();
    }

    private void insertRule(UUID ruleId, UUID packageId, CreateMunicipalFiscalPackageCommand command,
            MunicipalReteicaRuleCommand rule, UUID legalSourceId) {
        jdbcTemplate.update("INSERT INTO withholding_rule "
                        + "(id, company_id, rule_set_version, operation_type, concept_code, withholding_type, "
                        + "base_min_amount, rate, requires_company_withholding_agent, requires_company_vat_responsible, "
                        + "required_third_party_tax_regime, required_third_party_responsibility, municipality_code, "
                        + "ciiu_code, valid_from, valid_to, priority, active, threshold_unit, threshold_value, "
                        + "threshold_operator, calculation_base, threshold_treatment, decision, "
                        + "requires_company_vat_withholding_agent, requires_company_ica_withholding_agent, "
                        + "legal_reference, source_url, specificity, published, target_third_party_id, "
                        + "evidence_reference, legal_source_id, fiscal_rule_set_id) "
                        + "VALUES (?, NULL, ?, ?, ?, 'RETEICA', 0, ?, false, false, NULL, NULL, ?, ?, ?, ?, 100, true, "
                        + "?, ?, ?, ?, 'FULL_AMOUNT', 'APPLIED', false, true, ?, ?, 40, false, NULL, NULL, ?, ?)",
                ruleId, command.packageCode() + "-" + command.version(), rule.operationType().name(),
                rule.conceptCode(), rule.rate(), command.municipalityCode(), rule.ciiuCode(), command.validFrom(),
                command.validTo(), rule.thresholdUnit().name(), rule.thresholdValue(), rule.thresholdOperator().name(),
                rule.calculationBase().name(), command.legalReference(), command.officialSourceUrl(), legalSourceId,
                packageId);
    }

    @Override
    public MunicipalFiscalRulePackage publish(UUID packageId, UUID userId) {
        Instant now = Instant.now();
        int updated = jdbcTemplate.update("UPDATE fiscal_rule_set SET status = 'PUBLISHED', reviewed_at = ?, "
                        + "published_at = ?, published_by = ? WHERE id = ? AND status = 'DRAFT'",
                now, now, userId, packageId);
        if (updated != 1) {
            throw new IllegalStateException("El paquete ReteICA no esta disponible para publicar.");
        }
        jdbcTemplate.update("UPDATE withholding_rule SET published = true WHERE fiscal_rule_set_id = ?", packageId);
        return findById(packageId).orElseThrow();
    }

    @Override
    public boolean hasPublishedOverlap(MunicipalFiscalRulePackage candidate) {
        String baseQuery = "SELECT COUNT(*) FROM fiscal_rule_set "
                + "WHERE scope = 'MUNICIPAL' AND status = 'PUBLISHED' AND municipality_code = ? AND id <> ? "
                + "AND (valid_to IS NULL OR valid_to >= ?)";
        Integer count = candidate.validTo() == null
                ? jdbcTemplate.queryForObject(baseQuery, Integer.class, candidate.municipalityCode(), candidate.id(),
                        candidate.validFrom())
                : jdbcTemplate.queryForObject(baseQuery + " AND valid_from <= ?", Integer.class,
                        candidate.municipalityCode(), candidate.id(), candidate.validFrom(), candidate.validTo());
        return count != null && count > 0;
    }

    @Override
    public UUID saveImport(String fileName, String sha256, int rowCount, int packageCount, String status,
            String errorDetail, UUID userId) {
        UUID id = UUID.randomUUID();
        jdbcTemplate.update("INSERT INTO fiscal_rule_import "
                        + "(id, file_name, file_sha256, row_count, package_count, status, error_detail, created_by) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                id, fileName, sha256, rowCount, packageCount, status, errorDetail, userId);
        return id;
    }

    private static MunicipalFiscalRulePackage mapPackage(ResultSet rs, int rowNumber) throws SQLException {
        Date validTo = rs.getDate("valid_to");
        return new MunicipalFiscalRulePackage(rs.getObject("id", UUID.class), rs.getString("code"),
                rs.getString("version"), rs.getString("municipality_code"), rs.getString("municipality_name"),
                rs.getObject("legal_source_id", UUID.class), FiscalRuleSetStatus.valueOf(rs.getString("status")),
                rs.getDate("valid_from").toLocalDate(), validTo == null ? null : validTo.toLocalDate(),
                instant(rs.getTimestamp("reviewed_at")), instant(rs.getTimestamp("published_at")),
                rs.getObject("published_by", UUID.class), rs.getObject("import_id", UUID.class),
                instant(rs.getTimestamp("created_at")), rs.getObject("created_by", UUID.class),
                rs.getInt("rule_count"));
    }

    private static Instant instant(Timestamp value) {
        return value == null ? null : value.toInstant();
    }

    private static final class FiscalLegalSourceJdbcAdapterAccessor {
        private static FiscalLegalSource mapSource(ResultSet rs, int rowNumber) throws SQLException {
            Date issued = rs.getDate("issued_on");
            Date review = rs.getDate("review_due_on");
            return new FiscalLegalSource(rs.getObject("id", UUID.class), rs.getString("code"), rs.getString("title"),
                    rs.getString("authority"), rs.getString("official_url"),
                    issued == null ? null : issued.toLocalDate(), review == null ? null : review.toLocalDate(),
                    rs.getBoolean("active"), rs.getTimestamp("created_at").toInstant(),
                    rs.getObject("created_by", UUID.class));
        }
    }
}
