package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CalculateWithholdingsCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.CompanyTaxProfileCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.ThirdPartyFiscalProfileCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCalculationResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalParameterRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.ThirdPartyFiscalProfilePort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingCalculationSnapshotRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.CompanyTaxProfile;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalCalculationBase;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdOperator;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalParameter;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdTreatment;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalThresholdUnit;
import com.msvanegasg.facturaelectronica.accounting.domain.model.ThirdPartyFiscalProfile;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingCalculationSnapshot;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingDecision;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingRule;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

class WithholdingCalculationServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID THIRD_PARTY_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID SOURCE_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final LocalDate OPERATION_DATE = LocalDate.of(2026, 9, 7);

    @Test
    void calculatesReteivaOverTaxAmountForSimpleSupplier() {
        TestContext context = new TestContext();
        context.rules.rules.add(reteivaSimpleRule());

        WithholdingCalculationResult result = context.service().calculate(command(simpleSupplier()));

        assertThat(result.grossAmount()).isEqualByComparingTo("1190000.00");
        assertThat(result.withholdingTotal()).isEqualByComparingTo("28500.00");
        assertThat(result.netPayable()).isEqualByComparingTo("1161500.00");
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).withholdingType()).isEqualTo(WithholdingType.RETEIVA);
        assertThat(result.items().get(0).baseAmount()).isEqualByComparingTo("190000.00");
        assertThat(result.items().get(0).amount()).isEqualByComparingTo("28500.00");
        assertThat(result.items().get(0).decision()).isEqualTo(WithholdingDecision.APPLIED);
    }

    @Test
    void simpleSupplierWithoutApplicableRuleReturnsRentaAndIcaNotApplied() {
        TestContext context = new TestContext();

        WithholdingCalculationResult result = context.service().calculate(command(simpleSupplier()));

        assertThat(result.items()).hasSize(2);
        assertThat(result.items()).extracting("withholdingType")
                .containsExactly(WithholdingType.RETEFUENTE, WithholdingType.RETEICA);
        assertThat(result.withholdingTotal()).isEqualByComparingTo("0.00");
        assertThat(result.netPayable()).isEqualByComparingTo("1190000.00");
    }

    @Test
    void noResponsibleSupplierWithoutTaxReturnsReteivaNotApplied() {
        TestContext context = new TestContext();

        WithholdingCalculationResult result = context.service().calculate(new CalculateWithholdingsCommand(
                COMPANY_ID, FiscalOperationType.EXPENSE, THIRD_PARTY_ID, "OPERATING_EXPENSE", OPERATION_DATE,
                money("500000"), BigDecimal.ZERO, "11001", null, null, companyProfile(),
                new ThirdPartyFiscalProfileCommand(THIRD_PARTY_ID, "NO_RESPONSABLE_IVA", Set.of("R-99-PN"),
                        "11001", "6201", true)));

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).withholdingType()).isEqualTo(WithholdingType.RETEIVA);
        assertThat(result.items().get(0).decision()).isEqualTo(WithholdingDecision.NOT_APPLIED);
    }

    @Test
    void persistsSnapshotWhenSourceIsProvided() {
        TestContext context = new TestContext();
        context.rules.rules.add(reteivaSimpleRule());

        context.service().calculate(new CalculateWithholdingsCommand(COMPANY_ID, FiscalOperationType.PURCHASE,
                THIRD_PARTY_ID, "PURCHASE_GENERAL", OPERATION_DATE, money("1000000"), money("190000"),
                "11001", AccountingSourceType.PURCHASE, SOURCE_ID, companyProfile(), simpleSupplier()));

        assertThat(context.snapshots.snapshots).hasSize(1);
        assertThat(context.snapshots.snapshots.get(0).sourceId()).isEqualTo(SOURCE_ID);
        assertThat(context.snapshots.snapshots.get(0).withholdingType()).isEqualTo(WithholdingType.RETEIVA);
    }

    @Test
    void reusesPersistedSnapshotForSameDocument() {
        TestContext context = new TestContext();
        context.rules.rules.add(reteivaSimpleRule());
        CalculateWithholdingsCommand command = new CalculateWithholdingsCommand(COMPANY_ID,
                FiscalOperationType.PURCHASE, THIRD_PARTY_ID, "PURCHASE_GENERAL", OPERATION_DATE,
                money("1000000"), money("190000"), "11001", AccountingSourceType.PURCHASE, SOURCE_ID,
                companyProfile(), simpleSupplier());

        WithholdingCalculationResult first = context.service().calculate(command);
        context.rules.rules.clear();
        WithholdingCalculationResult second = context.service().calculate(command);

        assertThat(context.snapshots.snapshots).hasSize(1);
        assertThat(second.withholdingTotal()).isEqualByComparingTo(first.withholdingTotal());
        assertThat(second.items()).extracting("decision").containsExactly(WithholdingDecision.APPLIED);
    }

    @Test
    void resolvesPersistedCompanyProfileWhenRequestDoesNotSendIt() {
        TestContext context = new TestContext();
        context.rules.rules.add(reteivaSimpleRule());

        WithholdingCalculationResult result = context.serviceWithCompanyProfile().calculate(
                new CalculateWithholdingsCommand(COMPANY_ID, FiscalOperationType.PURCHASE, THIRD_PARTY_ID,
                        "PURCHASE_GENERAL", OPERATION_DATE, money("1000000"), money("190000"), "11001",
                        null, null, null, simpleSupplier()));

        assertThat(result.items()).singleElement().satisfies(item ->
                assertThat(item.amount()).isEqualByComparingTo("28500.00"));
    }

    @Test
    void blocksDocumentConfirmationWithoutPersistingSnapshot() {
        TestContext context = new TestContext();
        CompanyTaxProfileCommand profile = new CompanyTaxProfileCommand("ORDINARIO", Set.of("O-13"), true, true,
                false, false, false, "11001", Set.of("6201"), false, true);
        CalculateWithholdingsCommand command = new CalculateWithholdingsCommand(COMPANY_ID,
                FiscalOperationType.PURCHASE, THIRD_PARTY_ID, "PURCHASE_GENERAL", OPERATION_DATE,
                money("1000000"), money("190000"), "11001", AccountingSourceType.PURCHASE, SOURCE_ID, profile,
                ordinarySupplier());

        assertThatThrownBy(() -> context.service().calculate(command))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("ReteICA");
        assertThat(context.snapshots.snapshots).isEmpty();
    }

    @Test
    void resolvesUvtThresholdAndKeepsNormativeEvidence() {
        TestContext context = new TestContext();
        UUID ruleId = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
        context.rules.rules.add(rule(ruleId, FiscalOperationType.EXPENSE, "SERVICE", WithholdingType.RETEFUENTE,
                "0.040000", FiscalThresholdUnit.UVT, "2", WithholdingDecision.APPLIED, null, null, 10));

        WithholdingCalculationResult result = context.service().calculate(new CalculateWithholdingsCommand(
                COMPANY_ID, FiscalOperationType.EXPENSE, THIRD_PARTY_ID, "SERVICE", OPERATION_DATE,
                money("104748"), BigDecimal.ZERO, "11001", null, null, companyProfile(), ordinarySupplier()));

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).amount()).isEqualByComparingTo("4189.92");
        assertThat(result.items().get(0).ruleId()).isEqualTo(ruleId);
        assertThat(result.items().get(0).parameterVersion()).isEqualTo("TEST-2026");
        assertThat(result.items().get(0).legalReference()).isEqualTo("NORMA");
    }

    @Test
    void exemptionWinsOverApplicableRateForSameType() {
        TestContext context = new TestContext();
        context.rules.rules.add(rule(UUID.randomUUID(), FiscalOperationType.PURCHASE, "PURCHASE_GENERAL",
                WithholdingType.RETEFUENTE, "0.025000", FiscalThresholdUnit.COP, "0",
                WithholdingDecision.APPLIED, null, null, 100));
        context.rules.rules.add(rule(UUID.randomUUID(), FiscalOperationType.PURCHASE, "PURCHASE_GENERAL",
                WithholdingType.RETEFUENTE, "0", FiscalThresholdUnit.COP, "0",
                WithholdingDecision.EXEMPT, "ORDINARIO", null, 1));

        WithholdingCalculationResult result = context.service().calculate(command(ordinarySupplier()));

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).decision()).isEqualTo(WithholdingDecision.EXEMPT);
        assertThat(result.withholdingTotal()).isEqualByComparingTo("0.00");
    }

    @Test
    void documentaryExemptionOnlyAppliesToItsTargetThirdParty() {
        TestContext context = new TestContext();
        context.rules.rules.add(rule(UUID.randomUUID(), FiscalOperationType.PURCHASE, "PURCHASE_GENERAL",
                WithholdingType.RETEFUENTE, "0.025000", FiscalThresholdUnit.COP, "0",
                WithholdingDecision.APPLIED, null, null, 100));
        context.rules.rules.add(rule(UUID.randomUUID(), FiscalOperationType.PURCHASE, "PURCHASE_GENERAL",
                WithholdingType.RETEFUENTE, "0", FiscalThresholdUnit.COP, "0",
                WithholdingDecision.EXEMPT, null, null, 1, THIRD_PARTY_ID, "Certificado vigente 2026"));

        WithholdingCalculationResult exempt = context.service().calculate(command(ordinarySupplier()));
        ThirdPartyFiscalProfileCommand other = new ThirdPartyFiscalProfileCommand(UUID.randomUUID(), "ORDINARIO",
                Set.of("O-05"), "11001", "7490", true);
        WithholdingCalculationResult applied = context.service().calculate(new CalculateWithholdingsCommand(
                COMPANY_ID, FiscalOperationType.PURCHASE, other.thirdPartyId(), "PURCHASE_GENERAL", OPERATION_DATE,
                money("1000000"), money("190000"), "11001", null, null, companyProfile(), other));

        assertThat(exempt.items()).singleElement().satisfies(item ->
                assertThat(item.decision()).isEqualTo(WithholdingDecision.EXEMPT));
        assertThat(applied.items()).singleElement().satisfies(item ->
                assertThat(item.decision()).isEqualTo(WithholdingDecision.APPLIED));
    }

    @Test
    void blocksReteicaWhenCompanyIsAgentAndMunicipalCatalogIsMissing() {
        TestContext context = new TestContext();
        CompanyTaxProfileCommand profile = new CompanyTaxProfileCommand("ORDINARIO", Set.of("O-13"), true, true,
                false, false, false, "11001", Set.of("6201"), false, true);

        WithholdingCalculationResult result = context.service().calculate(new CalculateWithholdingsCommand(
                COMPANY_ID, FiscalOperationType.PURCHASE, THIRD_PARTY_ID, "PURCHASE_GENERAL", OPERATION_DATE,
                money("1000000"), money("190000"), "11001", null, null, profile, ordinarySupplier()));

        assertThat(result.items()).singleElement().satisfies(item -> {
            assertThat(item.withholdingType()).isEqualTo(WithholdingType.RETEICA);
            assertThat(item.decision()).isEqualTo(WithholdingDecision.BLOCKED);
        });
    }

    @Test
    void selfWithholdingUsesCompanyCiiuAndDoesNotReduceNetPayable() {
        TestContext context = new TestContext();
        context.rules.rules.add(rule(UUID.randomUUID(), FiscalOperationType.RECEIPT, "ANY",
                WithholdingType.AUTORETENCION, "0.011000", FiscalThresholdUnit.COP, "0",
                WithholdingDecision.APPLIED, null, "6201", 10));
        CompanyTaxProfileCommand profile = new CompanyTaxProfileCommand("ORDINARIO", Set.of(), true, false,
                false, true, false, "11001", Set.of("6201"), false, false);

        WithholdingCalculationResult result = context.service().calculate(new CalculateWithholdingsCommand(
                COMPANY_ID, FiscalOperationType.RECEIPT, THIRD_PARTY_ID, "ANY", OPERATION_DATE,
                money("1000000"), BigDecimal.ZERO, "11001", null, null, profile, ordinarySupplier()));

        assertThat(result.items()).singleElement().satisfies(item ->
                assertThat(item.amount()).isEqualByComparingTo("11000.00"));
        assertThat(result.withholdingTotal()).isEqualByComparingTo("0.00");
        assertThat(result.netPayable()).isEqualByComparingTo("1000000.00");
    }

    @Test
    void matchesRuleAgainstAnyThirdPartyCiiuActivity() {
        TestContext context = new TestContext();
        context.rules.rules.add(rule(UUID.randomUUID(), FiscalOperationType.PURCHASE, "PURCHASE_GENERAL",
                WithholdingType.RETEFUENTE, "0.025000", FiscalThresholdUnit.COP, "0",
                WithholdingDecision.APPLIED, null, "6201", 10));
        ThirdPartyFiscalProfileCommand supplier = new ThirdPartyFiscalProfileCommand(THIRD_PARTY_ID, "ORDINARIO",
                Set.of("O-05"), "11001", null, Set.of("4711", "6201"), true);

        WithholdingCalculationResult result = context.service().calculate(command(supplier));

        assertThat(result.items()).singleElement().satisfies(item -> {
            assertThat(item.decision()).isEqualTo(WithholdingDecision.APPLIED);
            assertThat(item.amount()).isEqualByComparingTo("25000.00");
        });
    }

    @Test
    void appliesDailyAccumulationWhenCurrentLineCrossesThreshold() {
        TestContext context = new TestContext();
        context.rules.rules.add(rule(UUID.randomUUID(), FiscalOperationType.PURCHASE, "PURCHASE_GENERAL",
                WithholdingType.RETEFUENTE, "0.100000", FiscalThresholdUnit.COP, "100",
                WithholdingDecision.APPLIED, null, null, 10));

        WithholdingCalculationResult result = context.service().calculate(new CalculateWithholdingsCommand(
                COMPANY_ID, FiscalOperationType.PURCHASE, THIRD_PARTY_ID, "PURCHASE_GENERAL", OPERATION_DATE,
                money("30"), BigDecimal.ZERO, "11001", null, null, companyProfile(), ordinarySupplier(),
                money("80"), BigDecimal.ZERO, Map.of()));

        assertThat(result.items()).singleElement().satisfies(item -> {
            assertThat(item.previousAccumulatedBase()).isEqualByComparingTo("80.00");
            assertThat(item.cumulativeBase()).isEqualByComparingTo("110.00");
            assertThat(item.amount()).isEqualByComparingTo("11.00");
        });
    }

    private static CalculateWithholdingsCommand command(ThirdPartyFiscalProfileCommand thirdPartyProfile) {
        return new CalculateWithholdingsCommand(COMPANY_ID, FiscalOperationType.PURCHASE, THIRD_PARTY_ID,
                "PURCHASE_GENERAL", OPERATION_DATE, money("1000000"), money("190000"), "11001", null, null,
                companyProfile(), thirdPartyProfile);
    }

    private static CompanyTaxProfileCommand companyProfile() {
        return new CompanyTaxProfileCommand("ORDINARIO", Set.of("O-13", "O-23"), true, true, false, false,
                false, "11001", Set.of("6201"));
    }

    private static ThirdPartyFiscalProfileCommand simpleSupplier() {
        return new ThirdPartyFiscalProfileCommand(THIRD_PARTY_ID, "SIMPLE", Set.of("O-47"), "11001", "6201",
                true);
    }

    private static ThirdPartyFiscalProfileCommand ordinarySupplier() {
        return new ThirdPartyFiscalProfileCommand(THIRD_PARTY_ID, "ORDINARIO", Set.of("O-05"), "11001", "7490",
                true);
    }

    private static WithholdingRule rule(UUID id, FiscalOperationType operationType, String concept,
            WithholdingType type, String rate, FiscalThresholdUnit thresholdUnit, String threshold,
            WithholdingDecision decision, String regime, String ciiu, int priority) {
        return rule(id, operationType, concept, type, rate, thresholdUnit, threshold, decision, regime, ciiu,
                priority, null, null);
    }

    private static WithholdingRule rule(UUID id, FiscalOperationType operationType, String concept,
            WithholdingType type, String rate, FiscalThresholdUnit thresholdUnit, String threshold,
            WithholdingDecision decision, String regime, String ciiu, int priority, UUID targetThirdPartyId,
            String evidenceReference) {
        return new WithholdingRule(id, null, "TEST-RULE", operationType, concept, type, BigDecimal.ZERO,
                new BigDecimal(rate), type == WithholdingType.RETEFUENTE, false, regime, null, null, ciiu,
                LocalDate.of(2025, 6, 1), null, priority, true, thresholdUnit, new BigDecimal(threshold),
                FiscalThresholdOperator.GTE,
                type == WithholdingType.AUTORETENCION ? FiscalCalculationBase.COMPANY_INCOME
                        : FiscalCalculationBase.TAXABLE_BASE,
                FiscalThresholdTreatment.FULL_AMOUNT, decision, false, false, "NORMA", "https://example.test", 10,
                true, targetThirdPartyId, evidenceReference);
    }

    private static WithholdingRule reteivaSimpleRule() {
        return new WithholdingRule(UUID.randomUUID(), null, "CO-DIAN-2026-RETEIVA-SIMPLE",
                FiscalOperationType.PURCHASE, "ANY", WithholdingType.RETEIVA, BigDecimal.ZERO,
                new BigDecimal("0.150000"), false, true, "SIMPLE", null, null, null,
                LocalDate.of(2026, 1, 1), null, 100, true);
    }

    private static BigDecimal money(String value) {
        return new BigDecimal(value);
    }

    private static final class TestContext {

        private final InMemoryWithholdingRuleRepository rules = new InMemoryWithholdingRuleRepository();
        private final InMemorySnapshotRepository snapshots = new InMemorySnapshotRepository();
        private final IdGeneratorPort idGenerator = new QueueIdGenerator();

        WithholdingCalculationService service() {
            return new WithholdingCalculationService(rules, snapshots, (companyId, thirdPartyId) -> Optional.empty(),
                    idGenerator, Clock.fixed(Instant.parse("2026-09-07T10:00:00Z"), ZoneOffset.UTC));
        }

        WithholdingCalculationService serviceWithCompanyProfile() {
            return new WithholdingCalculationService(rules, snapshots,
                    (companyId, thirdPartyId) -> Optional.empty(), idGenerator,
                    Clock.fixed(Instant.parse("2026-09-07T10:00:00Z"), ZoneOffset.UTC),
                    emptyFiscalParameters(), companyId -> Optional.of(
                            new CompanyTaxProfile("ORDINARIO", Set.of("O-13", "O-23"), true, true, false,
                                    false, false, "11001", Set.of("6201"), true, false)));
        }

        private static FiscalParameterRepositoryPort emptyFiscalParameters() {
            return new FiscalParameterRepositoryPort() {
                @Override
                public Optional<FiscalParameter> findEffective(String code, LocalDate date) {
                    return Optional.empty();
                }

                @Override
                public List<FiscalParameter> findAll() {
                    return List.of();
                }
            };
        }
    }

    private static final class InMemoryWithholdingRuleRepository implements WithholdingRuleRepositoryPort {

        private final List<WithholdingRule> rules = new ArrayList<>();

        @Override
        public List<WithholdingRule> findActiveRules(UUID companyId, FiscalOperationType operationType,
                LocalDate operationDate) {
            return rules.stream()
                    .filter(rule -> rule.companyId() == null || rule.companyId().equals(companyId))
                    .filter(rule -> rule.operationType() == operationType)
                    .toList();
        }
    }

    private static final class InMemorySnapshotRepository implements WithholdingCalculationSnapshotRepositoryPort {

        private final List<WithholdingCalculationSnapshot> snapshots = new ArrayList<>();

        @Override
        public void saveAll(List<WithholdingCalculationSnapshot> snapshots) {
            this.snapshots.addAll(snapshots);
        }

        @Override
        public List<WithholdingCalculationSnapshot> findBySource(UUID companyId, AccountingSourceType sourceType,
                UUID sourceId) {
            return snapshots.stream()
                    .filter(snapshot -> snapshot.companyId().equals(companyId))
                    .filter(snapshot -> snapshot.sourceType() == sourceType)
                    .filter(snapshot -> snapshot.sourceId().equals(sourceId))
                    .toList();
        }
    }

    private static final class QueueIdGenerator implements IdGeneratorPort {

        private final Queue<UUID> ids = new ArrayDeque<>();

        QueueIdGenerator() {
            for (int index = 0; index < 50; index++) {
                ids.add(UUID.nameUUIDFromBytes(("withholding-test-id-" + index).getBytes()));
            }
        }

        @Override
        public UUID newId() {
            return ids.remove();
        }
    }
}
