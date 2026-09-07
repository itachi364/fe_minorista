package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CalculateWithholdingsCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CompanyTaxProfileCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.ThirdPartyFiscalProfileCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCalculationItemResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCalculationResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.in.CalculateWithholdingsUseCase;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.ThirdPartyFiscalProfilePort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingCalculationSnapshotRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.CompanyTaxProfile;
import com.msvanegasg.facturaelectronica.accounting.domain.model.ThirdPartyFiscalProfile;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingCalculationSnapshot;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingDecision;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingRule;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

public class WithholdingCalculationService implements CalculateWithholdingsUseCase {

    private static final String SIMPLE_EXCLUSION_REASON =
            "El tercero pertenece al regimen SIMPLE; no aplica retencion de renta ni ICA segun regla vigente.";
    private static final String NO_VAT_REASON =
            "No aplica ReteIVA porque no hay IVA causado o el tercero no es responsable de IVA para esta operacion.";
    private static final String NO_RULE_REASON = "No existe una regla activa aplicable para este concepto y perfil fiscal.";

    private final WithholdingRuleRepositoryPort ruleRepository;
    private final WithholdingCalculationSnapshotRepositoryPort snapshotRepository;
    private final ThirdPartyFiscalProfilePort thirdPartyFiscalProfilePort;
    private final IdGeneratorPort idGenerator;
    private final Clock clock;

    public WithholdingCalculationService(WithholdingRuleRepositoryPort ruleRepository,
            WithholdingCalculationSnapshotRepositoryPort snapshotRepository,
            ThirdPartyFiscalProfilePort thirdPartyFiscalProfilePort,
            IdGeneratorPort idGenerator,
            Clock clock) {
        this.ruleRepository = Objects.requireNonNull(ruleRepository);
        this.snapshotRepository = Objects.requireNonNull(snapshotRepository);
        this.thirdPartyFiscalProfilePort = Objects.requireNonNull(thirdPartyFiscalProfilePort);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public WithholdingCalculationResult calculate(CalculateWithholdingsCommand command) {
        validate(command);
        CompanyTaxProfile companyProfile = toCompanyProfile(command.companyProfile());
        ThirdPartyFiscalProfile thirdPartyProfile = resolveThirdPartyProfile(command);
        BigDecimal taxableBase = money(command.taxableBaseAmount());
        BigDecimal taxAmount = money(command.taxAmount());
        List<WithholdingRule> rules = ruleRepository.findActiveRules(command.companyId(), command.operationType(),
                command.operationDate()).stream()
                .filter(rule -> rule.appliesTo(command.operationType(), command.conceptCode(), command.operationDate(),
                        companyProfile, thirdPartyProfile))
                .sorted(Comparator.comparingInt(WithholdingRule::priority))
                .toList();
        List<WithholdingCalculationItemResult> items = calculateItems(command, thirdPartyProfile, taxableBase,
                taxAmount, rules);
        persistSnapshots(command, items);
        BigDecimal withholdingTotal = items.stream()
                .filter(item -> item.decision() == WithholdingDecision.APPLIED)
                .filter(item -> item.withholdingType() != WithholdingType.AUTORETENCION)
                .map(WithholdingCalculationItemResult::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal grossAmount = taxableBase.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
        return new WithholdingCalculationResult(command.companyId(), thirdPartyProfile.thirdPartyId(),
                toCommand(thirdPartyProfile), items, grossAmount, taxAmount, withholdingTotal,
                grossAmount.subtract(withholdingTotal).setScale(2, RoundingMode.HALF_UP));
    }

    private List<WithholdingCalculationItemResult> calculateItems(CalculateWithholdingsCommand command,
            ThirdPartyFiscalProfile thirdPartyProfile, BigDecimal taxableBase, BigDecimal taxAmount,
            List<WithholdingRule> rules) {
        List<WithholdingCalculationItemResult> calculated = rules.stream()
                .map(rule -> calculateRule(command.conceptCode(), taxableBase, taxAmount, rule))
                .toList();
        if (!calculated.isEmpty()) {
            return calculated;
        }
        if (thirdPartyProfile.isSimpleRegime()) {
            return List.of(
                    notApplied(WithholdingType.RETEFUENTE, command.conceptCode(), taxableBase, SIMPLE_EXCLUSION_REASON),
                    notApplied(WithholdingType.RETEICA, command.conceptCode(), taxableBase, SIMPLE_EXCLUSION_REASON));
        }
        if (taxAmount.signum() == 0 || thirdPartyProfile.isNoResponsibleOrNotApplicable()) {
            return List.of(notApplied(WithholdingType.RETEIVA, command.conceptCode(), taxAmount, NO_VAT_REASON));
        }
        return List.of(notApplied(WithholdingType.RETEFUENTE, command.conceptCode(), taxableBase, NO_RULE_REASON));
    }

    private WithholdingCalculationItemResult calculateRule(String conceptCode, BigDecimal taxableBase,
            BigDecimal taxAmount, WithholdingRule rule) {
        BigDecimal baseAmount = rule.withholdingType() == WithholdingType.RETEIVA ? taxAmount : taxableBase;
        if (baseAmount.compareTo(rule.baseMinAmount()) < 0) {
            return new WithholdingCalculationItemResult(rule.withholdingType(), conceptCode, baseAmount, rule.rate(),
                    BigDecimal.ZERO.setScale(2), rule.ruleSetVersion(), WithholdingDecision.NOT_APPLIED,
                    "La base de la operacion no supera la base minima de la regla.");
        }
        BigDecimal amount = baseAmount.multiply(rule.rate()).setScale(2, RoundingMode.HALF_UP);
        return new WithholdingCalculationItemResult(rule.withholdingType(), conceptCode, baseAmount, rule.rate(),
                amount, rule.ruleSetVersion(), WithholdingDecision.APPLIED, "Regla fiscal aplicada.");
    }

    private WithholdingCalculationItemResult notApplied(WithholdingType type, String conceptCode, BigDecimal baseAmount,
            String reason) {
        return new WithholdingCalculationItemResult(type, conceptCode, baseAmount.setScale(2, RoundingMode.HALF_UP),
                BigDecimal.ZERO.setScale(6), BigDecimal.ZERO.setScale(2), null, WithholdingDecision.NOT_APPLIED,
                reason);
    }

    private void persistSnapshots(CalculateWithholdingsCommand command, List<WithholdingCalculationItemResult> items) {
        if (command.sourceType() == null || command.sourceId() == null) {
            return;
        }
        Instant now = clock.instant();
        snapshotRepository.saveAll(items.stream()
                .map(item -> new WithholdingCalculationSnapshot(idGenerator.newId(), command.companyId(),
                        command.sourceType(), command.sourceId(), command.thirdPartyId(), command.operationDate(),
                        item.withholdingType(), item.baseAmount(), item.rate(), item.amount(), item.ruleVersion(),
                        item.decision(), item.reason(), now))
                .toList());
    }

    private ThirdPartyFiscalProfile resolveThirdPartyProfile(CalculateWithholdingsCommand command) {
        if (command.thirdPartyProfile() != null) {
            ThirdPartyFiscalProfile profile = toThirdPartyProfile(command.thirdPartyProfile(), command.thirdPartyId());
            if (!profile.active()) {
                throw new IllegalStateException("El tercero fiscal esta inactivo.");
            }
            return profile;
        }
        return thirdPartyFiscalProfilePort.findByCompanyIdAndId(command.companyId(), command.thirdPartyId())
                .filter(ThirdPartyFiscalProfile::active)
                .orElseThrow(() -> new IllegalStateException(
                        "No fue posible resolver el perfil fiscal del tercero para calcular retenciones."));
    }

    private static CompanyTaxProfile toCompanyProfile(CompanyTaxProfileCommand command) {
        return new CompanyTaxProfile(command.taxRegime(), command.rutResponsibilities(), command.vatResponsible(),
                command.withholdingAgent(), command.largeTaxpayer(), command.selfWithholding(), command.simpleRegime(),
                command.icaMunicipalityCode(), command.ciiuCodes());
    }

    private static ThirdPartyFiscalProfile toThirdPartyProfile(ThirdPartyFiscalProfileCommand command,
            UUID fallbackThirdPartyId) {
        UUID thirdPartyId = command.thirdPartyId() == null ? fallbackThirdPartyId : command.thirdPartyId();
        return new ThirdPartyFiscalProfile(thirdPartyId, command.taxRegime(), command.taxResponsibilities(),
                command.municipalityCode(), command.ciiuCode(), command.active());
    }

    private static ThirdPartyFiscalProfileCommand toCommand(ThirdPartyFiscalProfile profile) {
        return new ThirdPartyFiscalProfileCommand(profile.thirdPartyId(), profile.taxRegime(),
                profile.taxResponsibilities(), profile.municipalityCode(), profile.ciiuCode(), profile.active());
    }

    private static void validate(CalculateWithholdingsCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.operationType(), "operationType is required");
        Objects.requireNonNull(command.thirdPartyId(), "thirdPartyId is required");
        Objects.requireNonNull(command.operationDate(), "operationDate is required");
        Objects.requireNonNull(command.taxableBaseAmount(), "taxableBaseAmount is required");
        Objects.requireNonNull(command.companyProfile(), "companyProfile is required");
        if (command.taxableBaseAmount().signum() < 0 || money(command.taxAmount()).signum() < 0) {
            throw new IllegalArgumentException("withholding amounts cannot be negative");
        }
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, RoundingMode.HALF_UP);
    }
}
