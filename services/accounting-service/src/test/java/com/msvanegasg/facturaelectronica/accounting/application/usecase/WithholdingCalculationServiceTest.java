package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
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
import com.msvanegasg.facturaelectronica.accounting.application.port.out.ThirdPartyFiscalProfilePort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingCalculationSnapshotRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.WithholdingRuleRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.CompanyTaxProfile;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
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
