package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.Map;
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
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalParameterRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.CompanyTaxProfilePort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.ThirdPartyFiscalProfilePort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingCalculationSnapshotRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.CompanyTaxProfile;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalCalculationBase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalParameter;
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
    private final FiscalParameterRepositoryPort parameterRepository;
    private final CompanyTaxProfilePort companyTaxProfilePort;

    public WithholdingCalculationService(WithholdingRuleRepositoryPort ruleRepository,
            WithholdingCalculationSnapshotRepositoryPort snapshotRepository,
            ThirdPartyFiscalProfilePort thirdPartyFiscalProfilePort,
            IdGeneratorPort idGenerator,
            Clock clock) {
        this(ruleRepository, snapshotRepository, thirdPartyFiscalProfilePort, idGenerator, clock,
                FiscalParameterRepositoryPortDefaults.forTests(), companyId -> java.util.Optional.empty());
    }

    public WithholdingCalculationService(WithholdingRuleRepositoryPort ruleRepository,
            WithholdingCalculationSnapshotRepositoryPort snapshotRepository,
            ThirdPartyFiscalProfilePort thirdPartyFiscalProfilePort, IdGeneratorPort idGenerator, Clock clock,
            FiscalParameterRepositoryPort parameterRepository) {
        this(ruleRepository, snapshotRepository, thirdPartyFiscalProfilePort, idGenerator, clock, parameterRepository,
                companyId -> java.util.Optional.empty());
    }

    public WithholdingCalculationService(WithholdingRuleRepositoryPort ruleRepository,
            WithholdingCalculationSnapshotRepositoryPort snapshotRepository,
            ThirdPartyFiscalProfilePort thirdPartyFiscalProfilePort, IdGeneratorPort idGenerator, Clock clock,
            FiscalParameterRepositoryPort parameterRepository, CompanyTaxProfilePort companyTaxProfilePort) {
        this.ruleRepository = Objects.requireNonNull(ruleRepository);
        this.snapshotRepository = Objects.requireNonNull(snapshotRepository);
        this.thirdPartyFiscalProfilePort = Objects.requireNonNull(thirdPartyFiscalProfilePort);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
        this.parameterRepository = Objects.requireNonNull(parameterRepository);
        this.companyTaxProfilePort = Objects.requireNonNull(companyTaxProfilePort);
    }

    @Override
    public WithholdingCalculationResult calculate(CalculateWithholdingsCommand command) {
        validate(command);
        CompanyTaxProfile companyProfile = resolveCompanyProfile(command);
        ThirdPartyFiscalProfile thirdPartyProfile = resolveThirdPartyProfile(command);
        BigDecimal taxableBase = money(command.taxableBaseAmount());
        BigDecimal taxAmount = money(command.taxAmount());
        List<WithholdingCalculationSnapshot> existing = existingSnapshots(command);
        if (!existing.isEmpty()) {
            return fromSnapshots(command, thirdPartyProfile, taxableBase, taxAmount, existing);
        }
        List<WithholdingRule> rules = ruleRepository.findActiveRules(command.companyId(), command.operationType(),
                command.operationDate()).stream()
                .filter(rule -> rule.appliesTo(command.operationType(), command.conceptCode(), command.operationDate(),
                        companyProfile, thirdPartyProfile))
                .sorted(rulePrecedence())
                .toList();
        BigDecimal uvtValue = resolveUvt(command, rules);
        String parameterVersion = rules.stream().anyMatch(rule -> rule.thresholdUnit()
                == com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdUnit.UVT)
                        ? parameterRepository.findEffective("UVT", command.operationDate()).map(FiscalParameter::version)
                                .orElse(null)
                        : null;
        List<WithholdingCalculationItemResult> items = calculateItems(command, companyProfile, thirdPartyProfile,
                taxableBase, taxAmount, rules, uvtValue, parameterVersion);
        if (command.sourceType() != null && command.sourceId() != null
                && items.stream().anyMatch(item -> item.decision() == WithholdingDecision.BLOCKED)) {
            String reasons = items.stream().filter(item -> item.decision() == WithholdingDecision.BLOCKED)
                    .map(WithholdingCalculationItemResult::reason).distinct()
                    .collect(java.util.stream.Collectors.joining(" "));
            throw new IllegalStateException(reasons);
        }
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

    private List<WithholdingCalculationSnapshot> existingSnapshots(CalculateWithholdingsCommand command) {
        if (command.sourceType() == null || command.sourceId() == null) {
            return List.of();
        }
        return snapshotRepository.findBySource(command.companyId(), command.sourceType(), command.sourceId());
    }

    private WithholdingCalculationResult fromSnapshots(CalculateWithholdingsCommand command,
            ThirdPartyFiscalProfile profile, BigDecimal taxableBase, BigDecimal taxAmount,
            List<WithholdingCalculationSnapshot> snapshots) {
        List<WithholdingCalculationItemResult> items = snapshots.stream().map(snapshot ->
                new WithholdingCalculationItemResult(snapshot.withholdingType(), command.conceptCode(),
                        snapshot.baseAmount(), snapshot.rate(), snapshot.amount(), snapshot.ruleVersion(),
                        snapshot.decision(), snapshot.reason(), snapshot.ruleId(), snapshot.parameterVersion(),
                        snapshot.legalReference(), snapshot.sourceUrl())).toList();
        BigDecimal withholdingTotal = items.stream()
                .filter(item -> item.decision() == WithholdingDecision.APPLIED)
                .filter(item -> item.withholdingType() != WithholdingType.AUTORETENCION)
                .map(WithholdingCalculationItemResult::amount).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal grossAmount = taxableBase.add(taxAmount).setScale(2, RoundingMode.HALF_UP);
        return new WithholdingCalculationResult(command.companyId(), profile.thirdPartyId(), toCommand(profile),
                items, grossAmount, taxAmount, withholdingTotal,
                grossAmount.subtract(withholdingTotal).setScale(2, RoundingMode.HALF_UP));
    }

    private List<WithholdingCalculationItemResult> calculateItems(CalculateWithholdingsCommand command,
            CompanyTaxProfile companyProfile, ThirdPartyFiscalProfile thirdPartyProfile, BigDecimal taxableBase,
            BigDecimal taxAmount, List<WithholdingRule> rules, BigDecimal uvtValue, String parameterVersion) {
        Map<WithholdingType, WithholdingRule> selected = new EnumMap<>(WithholdingType.class);
        rules.forEach(rule -> selected.putIfAbsent(rule.withholdingType(), rule));
        List<WithholdingCalculationItemResult> calculated = new ArrayList<>(selected.values().stream()
                .map(rule -> calculateRule(command.conceptCode(), taxableBase, taxAmount, uvtValue,
                        parameterVersion, rule))
                .toList());
        if (companyProfile.icaWithholdingAgent() && selected.get(WithholdingType.RETEICA) == null) {
            calculated.add(thirdPartyProfile.isSimpleRegime()
                    ? notApplied(WithholdingType.RETEICA, command.conceptCode(), taxableBase, SIMPLE_EXCLUSION_REASON)
                    : blocked(WithholdingType.RETEICA, command.conceptCode(), taxableBase,
                            "Falta un catalogo ReteICA publicado para el municipio y la fecha de la operacion."));
        }
        if (companyProfile.selfWithholding() && command.operationType()
                == com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType.RECEIPT
                && selected.get(WithholdingType.AUTORETENCION) == null) {
            calculated.add(blocked(WithholdingType.AUTORETENCION, command.conceptCode(), taxableBase,
                    "No existe tarifa de autorretencion vigente para el CIIU propio de la empresa."));
        }
        if (!calculated.isEmpty()) {
            return List.copyOf(calculated);
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
            BigDecimal taxAmount, BigDecimal uvtValue, String parameterVersion, WithholdingRule rule) {
        BigDecimal evaluatedBase = rule.calculationBase() == FiscalCalculationBase.VAT_AMOUNT ? taxAmount : taxableBase;
        if (!rule.thresholdReached(evaluatedBase, uvtValue)) {
            return new WithholdingCalculationItemResult(rule.withholdingType(), conceptCode,
                    evaluatedBase.setScale(2, RoundingMode.HALF_UP), rule.rate(),
                    BigDecimal.ZERO.setScale(2), rule.ruleSetVersion(), WithholdingDecision.NOT_APPLIED,
                    "La base de la operacion no supera el umbral de la regla.", rule.id(), parameterVersion,
                    rule.legalReference(), rule.sourceUrl());
        }
        BigDecimal baseAmount = rule.taxableAmount(evaluatedBase, uvtValue).setScale(2, RoundingMode.HALF_UP);
        BigDecimal amount = rule.decision() == WithholdingDecision.APPLIED
                ? baseAmount.multiply(rule.rate()).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO.setScale(2);
        return new WithholdingCalculationItemResult(rule.withholdingType(), conceptCode, baseAmount, rule.rate(),
                amount, rule.ruleSetVersion(), rule.decision(), decisionReason(rule), rule.id(), parameterVersion,
                rule.legalReference(), rule.sourceUrl());
    }

    private WithholdingCalculationItemResult blocked(WithholdingType type, String conceptCode, BigDecimal base,
            String reason) {
        return new WithholdingCalculationItemResult(type, conceptCode, money(base), BigDecimal.ZERO.setScale(6),
                BigDecimal.ZERO.setScale(2), null, WithholdingDecision.BLOCKED, reason);
    }

    private BigDecimal resolveUvt(CalculateWithholdingsCommand command, List<WithholdingRule> rules) {
        boolean required = rules.stream().anyMatch(rule -> rule.thresholdUnit()
                == com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdUnit.UVT);
        if (!required) {
            return BigDecimal.ZERO;
        }
        return parameterRepository.findEffective("UVT", command.operationDate()).map(FiscalParameter::value)
                .orElseThrow(() -> new IllegalStateException("No existe una UVT publicada para la fecha de operacion."));
    }

    private static Comparator<WithholdingRule> rulePrecedence() {
        return Comparator.comparingInt((WithholdingRule rule) -> decisionRank(rule.decision()))
                .thenComparing(Comparator.comparingInt(WithholdingRule::effectiveSpecificity).reversed())
                .thenComparingInt(WithholdingRule::priority)
                .thenComparing(WithholdingRule::id);
    }

    private static int decisionRank(WithholdingDecision decision) {
        return switch (decision) {
            case EXEMPT -> 0;
            case NOT_APPLIED -> 1;
            case BLOCKED -> 2;
            case APPLIED -> 3;
        };
    }

    private static String decisionReason(WithholdingRule rule) {
        return switch (rule.decision()) {
            case APPLIED -> "Regla fiscal aplicada.";
            case EXEMPT -> "Exencion fiscal vigente aplicada.";
            case NOT_APPLIED -> "Regla de exclusion fiscal vigente aplicada.";
            case BLOCKED -> "La regla fiscal bloquea la operacion hasta completar su configuracion.";
        };
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
                        item.decision(), item.reason(), now, item.ruleId(), item.parameterVersion(),
                        item.legalReference(), item.sourceUrl()))
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

    private CompanyTaxProfile resolveCompanyProfile(CalculateWithholdingsCommand command) {
        if (command.companyProfile() != null) {
            return toCompanyProfile(command.companyProfile());
        }
        return companyTaxProfilePort.findByCompanyId(command.companyId())
                .orElseThrow(() -> new IllegalStateException(
                        "La empresa no tiene un perfil fiscal configurado para calcular retenciones."));
    }

    private static CompanyTaxProfile toCompanyProfile(CompanyTaxProfileCommand command) {
        return new CompanyTaxProfile(command.taxRegime(), command.rutResponsibilities(), command.vatResponsible(),
                command.withholdingAgent(), command.largeTaxpayer(), command.selfWithholding(), command.simpleRegime(),
                command.icaMunicipalityCode(), command.ciiuCodes(), command.vatWithholdingAgent(),
                command.icaWithholdingAgent());
    }

    private static ThirdPartyFiscalProfile toThirdPartyProfile(ThirdPartyFiscalProfileCommand command,
            UUID fallbackThirdPartyId) {
        UUID thirdPartyId = command.thirdPartyId() == null ? fallbackThirdPartyId : command.thirdPartyId();
        return new ThirdPartyFiscalProfile(thirdPartyId, command.taxRegime(), command.taxResponsibilities(),
                command.municipalityCode(), command.ciiuCodes(), command.active());
    }

    private static ThirdPartyFiscalProfileCommand toCommand(ThirdPartyFiscalProfile profile) {
        return new ThirdPartyFiscalProfileCommand(profile.thirdPartyId(), profile.taxRegime(),
                profile.taxResponsibilities(), profile.municipalityCode(), profile.ciiuCode(), profile.ciiuCodes(),
                profile.active());
    }

    private static void validate(CalculateWithholdingsCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.operationType(), "operationType is required");
        Objects.requireNonNull(command.thirdPartyId(), "thirdPartyId is required");
        Objects.requireNonNull(command.operationDate(), "operationDate is required");
        Objects.requireNonNull(command.taxableBaseAmount(), "taxableBaseAmount is required");
        if (command.taxableBaseAmount().signum() < 0 || money(command.taxAmount()).signum() < 0) {
            throw new IllegalArgumentException("withholding amounts cannot be negative");
        }
    }

    private static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2) : value.setScale(2, RoundingMode.HALF_UP);
    }

    private static final class FiscalParameterRepositoryPortDefaults {
        private FiscalParameterRepositoryPortDefaults() {
        }

        static FiscalParameterRepositoryPort forTests() {
            return new FiscalParameterRepositoryPort() {
                @Override
                public java.util.Optional<FiscalParameter> findEffective(String code, java.time.LocalDate date) {
                    return java.util.Optional.of(new FiscalParameter(new UUID(0, 1), "UVT", "TEST-2026",
                            new BigDecimal("52374"), java.time.LocalDate.of(2026, 1, 1), null, "TEST", "TEST", true));
                }

                @Override
                public List<FiscalParameter> findAll() {
                    return List.of();
                }
            };
        }
    }
}
