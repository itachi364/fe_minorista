package com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.tenant.application.port.out.InvoicingObligationRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyInvoicingObligationSnapshot;
import com.msvanegasg.facturaelectronica.tenant.domain.model.InvoicingObligationInput;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.entity.CompanyInvoicingObligationJpaEntity;
import com.msvanegasg.facturaelectronica.tenant.infrastructure.persistence.repository.CompanyInvoicingObligationJpaRepository;

@Component
public class InvoicingObligationPersistenceAdapter implements InvoicingObligationRepositoryPort {
    private final CompanyInvoicingObligationJpaRepository repository;
    private final JdbcTemplate jdbcTemplate;

    public InvoicingObligationPersistenceAdapter(CompanyInvoicingObligationJpaRepository repository,
            JdbcTemplate jdbcTemplate) {
        this.repository = repository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public CompanyInvoicingObligationSnapshot saveAsCurrent(CompanyInvoicingObligationSnapshot snapshot) {
        repository.clearCurrent(snapshot.companyId());
        return toDomain(repository.save(toEntity(snapshot)));
    }

    @Override public Optional<CompanyInvoicingObligationSnapshot> findCurrent(UUID companyId) {
        return repository.findByCompanyIdAndCurrentTrue(companyId).map(this::toDomain);
    }

    @Override public List<CompanyInvoicingObligationSnapshot> findHistory(UUID companyId) {
        return repository.findByCompanyIdOrderByVersionDesc(companyId).stream().map(this::toDomain).toList();
    }

    @Override public long nextVersion(UUID companyId) { return repository.maxVersion(companyId) + 1; }

    @Override
    public BigDecimal findUvtValue(LocalDate operationDate) {
        List<BigDecimal> values = jdbcTemplate.query(
                "select numeric_value from tenant.invoicing_rule_parameter where parameter_code = ? and ? between valid_from and valid_to order by valid_from desc limit 1",
                (rs, row) -> rs.getBigDecimal(1), "UVT", Date.valueOf(operationDate));
        if (values.isEmpty()) {
            throw new IllegalStateException("No existe UVT vigente para evaluar la obligacion de facturar.");
        }
        return values.get(0);
    }

    private static CompanyInvoicingObligationJpaEntity toEntity(CompanyInvoicingObligationSnapshot value) {
        var input = value.input();
        var entity = new CompanyInvoicingObligationJpaEntity();
        entity.setId(value.id()); entity.setCompanyId(value.companyId()); entity.setVersion(value.version());
        entity.setCurrent(value.current()); entity.setStatus(value.status()); entity.setDecisionCode(value.decisionCode());
        entity.setDecisionReasons(value.decisionReasons()); entity.setPersonType(input.personType());
        entity.setTaxRegime(input.taxRegime()); entity.setRutGeneratedAt(input.rutGeneratedAt());
        entity.setRutResponsibilityCodes(input.rutResponsibilityCodes()); entity.setCiiuCodes(input.ciiuCodes());
        entity.setEconomicOperationTypes(input.economicOperationTypes()); entity.setCustomsUser(input.customsUser());
        entity.setEstablishmentCount(input.establishmentCount()); entity.setExploitsIntangibles(input.exploitsIntangibles());
        entity.setOnlyExcludedOrUntaxedOperations(input.onlyExcludedOrUntaxedOperations());
        entity.setPreviousYearGrossActivityIncome(input.previousYearGrossActivityIncome());
        entity.setCurrentYearGrossActivityIncome(input.currentYearGrossActivityIncome());
        entity.setPreviousYearTaxedActivityFinancialOperations(input.previousYearTaxedActivityFinancialOperations());
        entity.setCurrentYearTaxedActivityFinancialOperations(input.currentYearTaxedActivityFinancialOperations());
        entity.setLargestPreviousYearTaxedContract(input.largestPreviousYearTaxedContract());
        entity.setLargestCurrentYearTaxedContract(input.largestCurrentYearTaxedContract());
        entity.setLargestSameCustomerAggregate(input.largestSameCustomerAggregate());
        entity.setVoluntaryElectronicInvoicer(input.voluntaryElectronicInvoicer());
        entity.setSpecialExceptionType(input.specialExceptionType()); entity.setSpecialExceptionScope(input.specialExceptionScope());
        entity.setRutAssetId(input.rutAssetId()); entity.setUvtValue(value.uvtValue());
        entity.setNormativeRuleSetVersion(value.normativeRuleSetVersion()); entity.setEvaluatedBy(value.evaluatedBy());
        entity.setEvaluatedAt(value.evaluatedAt());
        return entity;
    }

    private CompanyInvoicingObligationSnapshot toDomain(CompanyInvoicingObligationJpaEntity entity) {
        var input = new InvoicingObligationInput(entity.getPersonType(), entity.getTaxRegime(),
                entity.getRutGeneratedAt(), entity.getRutResponsibilityCodes(), entity.getCiiuCodes(),
                entity.getEconomicOperationTypes(), entity.getCustomsUser(), entity.getEstablishmentCount(),
                entity.getExploitsIntangibles(), entity.getOnlyExcludedOrUntaxedOperations(),
                entity.getPreviousYearGrossActivityIncome(), entity.getCurrentYearGrossActivityIncome(),
                entity.getPreviousYearTaxedActivityFinancialOperations(), entity.getCurrentYearTaxedActivityFinancialOperations(),
                entity.getLargestPreviousYearTaxedContract(), entity.getLargestCurrentYearTaxedContract(),
                entity.getLargestSameCustomerAggregate(), entity.getVoluntaryElectronicInvoicer(),
                entity.getSpecialExceptionType(), entity.getSpecialExceptionScope(), entity.getRutAssetId());
        return new CompanyInvoicingObligationSnapshot(entity.getId(), entity.getCompanyId(), entity.getVersion(),
                entity.isCurrent(), entity.getStatus(), entity.getDecisionCode(), List.copyOf(entity.getDecisionReasons()),
                input, entity.getUvtValue(), entity.getNormativeRuleSetVersion(), entity.getEvaluatedBy(), entity.getEvaluatedAt());
    }
}
