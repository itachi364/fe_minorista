package com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence;

import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.ThirdPartyRepositoryPort;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.PersonType;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.IncomeTaxStatus;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.TaxResidency;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdParty;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdPartyRole;
import com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.entity.ThirdPartyJpaEntity;
import com.msvanegasg.facturaelectronica.thirdparty.infrastructure.persistence.repository.ThirdPartyJpaRepository;

@Component
public class ThirdPartyPersistenceAdapter implements ThirdPartyRepositoryPort {

    private final ThirdPartyJpaRepository repository;
    private final JdbcTemplate jdbcTemplate;

    public ThirdPartyPersistenceAdapter(ThirdPartyJpaRepository repository, JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public ThirdParty save(ThirdParty thirdParty) {
        ThirdPartyJpaEntity saved = repository.save(toEntity(thirdParty));
        repository.flush();
        Instant now = Instant.now();
        jdbcTemplate.update("UPDATE thirdparty.third_party_fiscal_history SET effective_to = ? "
                + "WHERE company_id = ? AND third_party_id = ? AND effective_to IS NULL",
                now, saved.getCompanyId(), saved.getId());
        jdbcTemplate.update("INSERT INTO thirdparty.third_party_fiscal_history (id, third_party_id, company_id, "
                        + "person_type, identification_type_code, identification_number, verification_digit, "
                        + "full_name, business_name, trade_name, email, phone, address, municipality_code, "
                        + "ciiu_codes, tax_responsibilities, tax_regime, roles, active, tax_residency, "
                        + "income_tax_status, self_withholding_scopes, fiscal_evidence_reference, effective_from) "
                        + "SELECT ?, party.id, party.company_id, party.person_type, party.identification_type_code, "
                        + "party.identification_number, party.verification_digit, party.full_name, party.business_name, "
                        + "party.trade_name, party.email, party.phone, party.address, party.municipality_code, "
                        + "COALESCE((SELECT array_agg(item.ciiu_code ORDER BY item.ciiu_code) "
                        + "FROM thirdparty.third_party_ciiu item WHERE item.third_party_id = party.id), "
                        + "ARRAY[]::VARCHAR(10)[]), COALESCE((SELECT array_agg(item.tax_responsibility_code "
                        + "ORDER BY item.tax_responsibility_code) FROM thirdparty.third_party_tax_responsibility item "
                        + "WHERE item.third_party_id = party.id), ARRAY[]::VARCHAR(20)[]), party.tax_regime, "
                        + "COALESCE((SELECT array_agg(item.role ORDER BY item.role) "
                        + "FROM thirdparty.third_party_role item WHERE item.third_party_id = party.id), "
                        + "ARRAY[]::VARCHAR(30)[]), party.active, party.tax_residency, party.income_tax_status, "
                        + "party.self_withholding_scopes, party.fiscal_evidence_reference, ? "
                        + "FROM thirdparty.third_party party WHERE party.id = ?",
                UUID.randomUUID(), now, saved.getId());
        return toDomain(saved);
    }

    @Override
    public Optional<ThirdParty> findByCompanyIdAndId(UUID companyId, UUID id) {
        return repository.findByCompanyIdAndId(companyId, id).map(ThirdPartyPersistenceAdapter::toDomain);
    }

    @Override
    public Optional<ThirdParty> findEffective(UUID companyId, UUID id, LocalDate effectiveOn) {
        return jdbcTemplate.query("SELECT * FROM thirdparty.third_party_fiscal_history "
                        + "WHERE company_id = ? AND third_party_id = ? AND effective_from < ? "
                        + "AND (effective_to IS NULL OR effective_to >= ?) "
                        + "ORDER BY effective_from DESC LIMIT 1",
                (rs, rowNumber) -> historyToDomain(rs), companyId, id,
                effectiveOn.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC),
                effectiveOn.atStartOfDay().toInstant(ZoneOffset.UTC)).stream().findFirst();
    }

    private static ThirdParty historyToDomain(ResultSet rs) throws SQLException {
        return ThirdParty.restore(rs.getObject("third_party_id", UUID.class), rs.getObject("company_id", UUID.class),
                PersonType.valueOf(rs.getString("person_type")), rs.getInt("identification_type_code"),
                rs.getString("identification_number"), (Integer) rs.getObject("verification_digit"),
                rs.getString("full_name"), rs.getString("business_name"), rs.getString("trade_name"),
                rs.getString("email"), rs.getString("phone"), rs.getString("address"),
                rs.getString("municipality_code"), stringSet(rs.getArray("ciiu_codes")),
                stringSet(rs.getArray("tax_responsibilities")),
                rs.getString("tax_regime") == null ? null : com.msvanegasg.facturaelectronica.thirdparty.domain.model.TaxRegime.valueOf(rs.getString("tax_regime")),
                TaxResidency.valueOf(rs.getString("tax_residency")),
                IncomeTaxStatus.valueOf(rs.getString("income_tax_status")),
                stringSet(rs.getArray("self_withholding_scopes")), rs.getString("fiscal_evidence_reference"),
                enumSet(rs.getArray("roles")), rs.getBoolean("active"));
    }

    private static Set<String> stringSet(Array array) throws SQLException {
        if (array == null) return Set.of();
        Object raw = array.getArray();
        return raw instanceof String[] values ? Set.copyOf(Arrays.asList(values)) : Set.of();
    }

    private static Set<ThirdPartyRole> enumSet(Array array) throws SQLException {
        return stringSet(array).stream().map(ThirdPartyRole::valueOf)
                .collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    @Override
    public Optional<ThirdParty> findByCompanyIdAndDocument(UUID companyId, Integer identificationTypeCode,
            String identificationNumber) {
        return repository.findByCompanyIdAndIdentificationTypeCodeAndIdentificationNumber(companyId,
                identificationTypeCode, identificationNumber).map(ThirdPartyPersistenceAdapter::toDomain);
    }

    @Override
    public List<ThirdParty> findByCompanyIdAndRole(UUID companyId, ThirdPartyRole role, Boolean active) {
        return repository.findByCompanyIdAndRole(companyId, role, active).stream()
                .map(ThirdPartyPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public List<ThirdParty> findByCompanyIdAndRoleAndIdentificationNumberPrefix(UUID companyId, ThirdPartyRole role,
            Boolean active, String identificationNumberPrefix) {
        return repository.findByCompanyIdAndRoleAndIdentificationNumberPrefix(companyId, role, active,
                identificationNumberPrefix).stream()
                .map(ThirdPartyPersistenceAdapter::toDomain)
                .toList();
    }

    @Override
    public boolean existsByCompanyIdAndDocument(UUID companyId, Integer identificationTypeCode,
            String identificationNumber) {
        return repository.existsByCompanyIdAndIdentificationTypeCodeAndIdentificationNumber(companyId,
                identificationTypeCode, identificationNumber);
    }

    private static ThirdParty toDomain(ThirdPartyJpaEntity entity) {
        return ThirdParty.restore(entity.getId(), entity.getCompanyId(), entity.getPersonType(),
                entity.getIdentificationTypeCode(), entity.getIdentificationNumber(), entity.getVerificationDigit(),
                entity.getFullName(), entity.getBusinessName(), entity.getTradeName(), entity.getEmail(),
                entity.getPhone(), entity.getAddress(), entity.getMunicipalityCode(),
                mergeCiiuCodes(entity.getCiiuCodes(), entity.getCiiuCode()),
                entity.getTaxResponsibilities(), entity.getTaxRegime(), entity.getTaxResidency(),
                entity.getIncomeTaxStatus(), Set.copyOf(Arrays.asList(entity.getSelfWithholdingScopes())),
                entity.getFiscalEvidenceReference(), entity.getRoles(),
                Boolean.TRUE.equals(entity.getActive()));
    }

    private static ThirdPartyJpaEntity toEntity(ThirdParty thirdParty) {
        UUID id = thirdParty.id() == null ? UUID.randomUUID() : thirdParty.id();
        return ThirdPartyJpaEntity.builder()
                .id(id)
                .companyId(thirdParty.companyId())
                .personType(thirdParty.personType())
                .identificationTypeCode(thirdParty.identificationTypeCode())
                .identificationNumber(thirdParty.identificationNumber())
                .verificationDigit(thirdParty.verificationDigit())
                .fullName(thirdParty.fullName())
                .businessName(thirdParty.businessName())
                .tradeName(thirdParty.tradeName())
                .email(thirdParty.email())
                .phone(thirdParty.phone())
                .address(thirdParty.address())
                .municipalityCode(thirdParty.municipalityCode())
                .ciiuCode(thirdParty.ciiuCode())
                .ciiuCodes(new LinkedHashSet<>(thirdParty.ciiuCodes()))
                .taxResponsibilities(new LinkedHashSet<>(thirdParty.taxResponsibilities()))
                .taxRegime(thirdParty.taxRegime())
                .taxResidency(thirdParty.taxResidency())
                .incomeTaxStatus(thirdParty.incomeTaxStatus())
                .selfWithholdingScopes(thirdParty.selfWithholdingScopes().toArray(String[]::new))
                .fiscalEvidenceReference(thirdParty.fiscalEvidenceReference())
                .roles(new LinkedHashSet<>(thirdParty.roles()))
                .active(thirdParty.active())
                .build();
    }

    private static Set<String> mergeCiiuCodes(Set<String> ciiuCodes, String legacyCiiuCode) {
        LinkedHashSet<String> merged = new LinkedHashSet<>();
        if (ciiuCodes != null) {
            merged.addAll(ciiuCodes);
        }
        if (legacyCiiuCode != null && !legacyCiiuCode.isBlank()) {
            merged.add(legacyCiiuCode);
        }
        return merged;
    }
}
