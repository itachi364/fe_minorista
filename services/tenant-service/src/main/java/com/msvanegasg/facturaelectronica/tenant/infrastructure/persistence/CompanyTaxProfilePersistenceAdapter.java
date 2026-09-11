package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyTaxProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyTaxProfile;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity.CompanyTaxProfileJpaEntity;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.repository.CompanyTaxProfileJpaRepository;

@Component
public class CompanyTaxProfilePersistenceAdapter implements CompanyTaxProfileRepositoryPort {
    private final CompanyTaxProfileJpaRepository repository;
    private final JdbcTemplate jdbcTemplate;

    public CompanyTaxProfilePersistenceAdapter(CompanyTaxProfileJpaRepository repository,
            JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<CompanyTaxProfile> findByCompanyId(UUID companyId) {
        return repository.findById(companyId).map(this::toDomain);
    }

    @Override
    public Optional<CompanyTaxProfile> findEffective(UUID companyId, LocalDate effectiveOn) {
        return jdbcTemplate.query("SELECT * FROM tenant.company_tax_profile_history "
                        + "WHERE company_id = ? AND effective_from < ? "
                        + "AND (effective_to IS NULL OR effective_to >= ?) "
                        + "ORDER BY effective_from DESC LIMIT 1",
                (rs, rowNumber) -> historyToDomain(rs), companyId,
                effectiveOn.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC),
                effectiveOn.atStartOfDay().toInstant(ZoneOffset.UTC)).stream().findFirst();
    }

    @Override
    @Transactional
    public CompanyTaxProfile save(CompanyTaxProfile profile) {
        CompanyTaxProfile saved = toDomain(repository.saveAndFlush(toEntity(profile)));
        jdbcTemplate.update("UPDATE tenant.company_tax_profile_history SET effective_to = ? "
                + "WHERE company_id = ? AND effective_to IS NULL", saved.updatedAt(), saved.companyId());
        jdbcTemplate.update("INSERT INTO tenant.company_tax_profile_history (id, company_id, company_size, "
                        + "financial_reporting_group, tax_regime, rut_responsibilities, vat_responsible, "
                        + "withholding_agent, vat_withholding_agent, ica_withholding_agent, large_taxpayer, "
                        + "self_withholding, simple_regime, ica_municipality_code, ciiu_codes, updated_by, "
                        + "tax_residency, income_tax_status, self_withholding_scopes, fiscal_evidence_reference, "
                        + "effective_from) SELECT ?, profile.company_id, profile.company_size, "
                        + "profile.financial_reporting_group, profile.tax_regime, "
                        + "COALESCE((SELECT array_agg(item.responsibility_code ORDER BY item.responsibility_code) "
                        + "FROM tenant.company_tax_profile_responsibility item WHERE item.company_id = profile.company_id), "
                        + "ARRAY[]::VARCHAR(30)[]), profile.vat_responsible, profile.withholding_agent, "
                        + "profile.vat_withholding_agent, profile.ica_withholding_agent, profile.large_taxpayer, "
                        + "profile.self_withholding, profile.simple_regime, profile.ica_municipality_code, "
                        + "COALESCE((SELECT array_agg(item.ciiu_code ORDER BY item.ciiu_code) "
                        + "FROM tenant.company_tax_profile_ciiu item WHERE item.company_id = profile.company_id), "
                        + "ARRAY[]::VARCHAR(10)[]), profile.updated_by, "
                        + "profile.tax_residency, profile.income_tax_status, profile.self_withholding_scopes, "
                        + "profile.fiscal_evidence_reference, profile.updated_at "
                        + "FROM tenant.company_tax_profile profile WHERE profile.company_id = ?",
                UUID.randomUUID(), saved.companyId());
        return saved;
    }

    private static CompanyTaxProfile historyToDomain(ResultSet rs) throws SQLException {
        return new CompanyTaxProfile(rs.getObject("company_id", UUID.class), rs.getString("company_size"),
                rs.getString("financial_reporting_group"), rs.getString("tax_regime"),
                stringSet(rs.getArray("rut_responsibilities")), rs.getBoolean("vat_responsible"),
                rs.getBoolean("withholding_agent"), rs.getBoolean("vat_withholding_agent"),
                rs.getBoolean("ica_withholding_agent"), rs.getBoolean("large_taxpayer"),
                rs.getBoolean("self_withholding"), rs.getBoolean("simple_regime"),
                rs.getString("ica_municipality_code"), stringSet(rs.getArray("ciiu_codes")),
                rs.getString("tax_residency"), rs.getString("income_tax_status"),
                stringSet(rs.getArray("self_withholding_scopes")), rs.getString("fiscal_evidence_reference"),
                rs.getObject("updated_by", UUID.class), rs.getTimestamp("effective_from").toInstant());
    }

    private static Set<String> stringSet(Array array) throws SQLException {
        if (array == null) {
            return Set.of();
        }
        Object raw = array.getArray();
        return raw instanceof String[] values ? Set.copyOf(Arrays.asList(values)) : Set.of();
    }

    private CompanyTaxProfileJpaEntity toEntity(CompanyTaxProfile profile) {
        return new CompanyTaxProfileJpaEntity(profile.companyId(), profile.companySize(),
                profile.financialReportingGroup(), profile.taxRegime(), profile.rutResponsibilities(),
                profile.vatResponsible(), profile.withholdingAgent(), profile.vatWithholdingAgent(),
                profile.icaWithholdingAgent(), profile.largeTaxpayer(), profile.selfWithholding(),
                profile.simpleRegime(), profile.icaMunicipalityCode(), profile.ciiuCodes(), profile.taxResidency(),
                profile.incomeTaxStatus(), profile.selfWithholdingScopes(), profile.fiscalEvidenceReference(),
                profile.updatedBy(), profile.updatedAt());
    }

    private CompanyTaxProfile toDomain(CompanyTaxProfileJpaEntity entity) {
        return new CompanyTaxProfile(entity.getCompanyId(), entity.getCompanySize(),
                entity.getFinancialReportingGroup(), entity.getTaxRegime(), entity.getRutResponsibilities(),
                entity.isVatResponsible(), entity.isWithholdingAgent(), entity.isVatWithholdingAgent(),
                entity.isIcaWithholdingAgent(), entity.isLargeTaxpayer(), entity.isSelfWithholding(),
                entity.isSimpleRegime(), entity.getIcaMunicipalityCode(), entity.getCiiuCodes(),
                entity.getTaxResidency(), entity.getIncomeTaxStatus(), entity.getSelfWithholdingScopes(),
                entity.getFiscalEvidenceReference(), entity.getUpdatedBy(), entity.getUpdatedAt());
    }
}
