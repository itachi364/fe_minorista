package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.accounting.application.dto.CalculateFiscalDocumentCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalDocumentCalculationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalDocumentLineCommand;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCalculationItemResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCalculationResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalDocumentCalculationRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.AccountingSourceType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.CompanyTaxProfile;
import com.msvanegasg.facturaelectronica.accounting.domain.model.FiscalOperationType;
import com.msvanegasg.facturaelectronica.accounting.domain.model.ThirdPartyFiscalProfile;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingDecision;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

class FiscalDocumentCalculationServiceTest {
    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID THIRD_PARTY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SOURCE_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    void calculatesMixedDocumentByLineAndUsesOperationMunicipality() {
        InMemoryRepository repository = new InMemoryRepository();
        FiscalDocumentCalculationService service = service(repository);

        FiscalDocumentCalculationResult result = service.calculate(command(null, null, "11001", List.of(
                line("SERVICE", "100000"), line("GOODS", "50000"))));

        assertThat(result.lines()).hasSize(2);
        assertThat(result.municipalityCode()).isEqualTo("11001");
        assertThat(result.withholdingTotal()).isEqualByComparingTo("10000.00");
        assertThat(result.netPayable()).isEqualByComparingTo("140000.00");
        assertThat(result.status()).isEqualTo("APPLIED");
        assertThat(repository.saved).isEmpty();
    }

    @Test
    void persistsConfirmedCalculationAndReturnsItIdempotently() {
        InMemoryRepository repository = new InMemoryRepository();
        FiscalDocumentCalculationService service = service(repository);
        CalculateFiscalDocumentCommand command = command(AccountingSourceType.PURCHASE, SOURCE_ID, null,
                List.of(line("SERVICE", "100000")));

        FiscalDocumentCalculationResult first = service.calculate(command);
        FiscalDocumentCalculationResult replay = service.calculate(command);

        assertThat(repository.saved).hasSize(1);
        assertThat(repository.accumulationsSaved).isEqualTo(1);
        assertThat(replay).isEqualTo(first);
        assertThat(first.municipalityCode()).isEqualTo("11001");
        assertThat(first.profileEvidence()).containsEntry("companyWithholdingAgent", true);
    }

    @Test
    void rejectsReplayWhenConfirmedInputChanges() {
        InMemoryRepository repository = new InMemoryRepository();
        FiscalDocumentCalculationService service = service(repository);
        service.calculate(command(AccountingSourceType.PURCHASE, SOURCE_ID, "11001",
                List.of(line("SERVICE", "100000"))));

        assertThatThrownBy(() -> service.calculate(command(AccountingSourceType.PURCHASE, SOURCE_ID, "11001",
                List.of(line("SERVICE", "200000")))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("datos diferentes");
    }

    @Test
    void blocksConfirmationButReturnsBlockedPreview() {
        InMemoryRepository repository = new InMemoryRepository();
        FiscalDocumentCalculationService service = service(repository, true);

        assertThat(service.calculate(command(null, null, "11001", List.of(line("SERVICE", "100")))).status())
                .isEqualTo("BLOCKED");
        assertThatThrownBy(() -> service.calculate(command(AccountingSourceType.PURCHASE, SOURCE_ID, "11001",
                List.of(line("SERVICE", "100")))))
                .isInstanceOf(IllegalStateException.class).hasMessageContaining("catalogo");
    }

    @Test
    void rejectsConfirmationInClosedFiscalPeriod() {
        InMemoryRepository repository = new InMemoryRepository();
        repository.periodClosed = true;
        FiscalDocumentCalculationService service = service(repository);

        assertThatThrownBy(() -> service.calculate(command(AccountingSourceType.PURCHASE, SOURCE_ID, "11001",
                List.of(line("SERVICE", "100000")))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("periodo fiscal");
        assertThat(repository.saved).isEmpty();
        assertThat(repository.accumulationsSaved).isZero();
    }

    private static FiscalDocumentCalculationService service(InMemoryRepository repository) {
        return service(repository, false);
    }

    private static FiscalDocumentCalculationService service(InMemoryRepository repository, boolean blocked) {
        AtomicInteger ids = new AtomicInteger();
        return new FiscalDocumentCalculationService(command -> {
            boolean applies = "SERVICE".equals(command.conceptCode());
            WithholdingDecision decision = blocked ? WithholdingDecision.BLOCKED
                    : applies ? WithholdingDecision.APPLIED : WithholdingDecision.NOT_APPLIED;
            BigDecimal amount = applies && !blocked ? new BigDecimal("10000.00") : BigDecimal.ZERO.setScale(2);
            var item = new WithholdingCalculationItemResult(WithholdingType.RETEFUENTE, command.conceptCode(),
                    command.taxableBaseAmount(), new BigDecimal("0.100000"), amount, "RULE-1", decision,
                    blocked ? "Falta catalogo municipal." : applies ? "Regla aplicada." : "No aplica.");
            return new WithholdingCalculationResult(command.companyId(), command.thirdPartyId(),
                    command.thirdPartyProfile(), List.of(item), command.taxableBaseAmount().add(command.taxAmount()),
                    command.taxAmount(), amount, command.taxableBaseAmount().subtract(amount));
        }, repository,
                companyId -> Optional.of(new CompanyTaxProfile("ORDINARIO", Set.of("O-07"), true, true, false,
                        false, false, "11001", Set.of("6201"), false, true)),
                (companyId, thirdPartyId) -> Optional.of(new ThirdPartyFiscalProfile(thirdPartyId, "ORDINARIO",
                        Set.of("O-48"), "05001", Set.of("6201"), true)),
                () -> UUID.nameUUIDFromBytes(("calculation-" + ids.incrementAndGet()).getBytes()),
                Clock.fixed(Instant.parse("2026-09-10T12:00:00Z"), ZoneOffset.UTC));
    }

    private static CalculateFiscalDocumentCommand command(AccountingSourceType sourceType, UUID sourceId,
            String municipality, List<FiscalDocumentLineCommand> lines) {
        return new CalculateFiscalDocumentCommand(COMPANY_ID, FiscalOperationType.PURCHASE, THIRD_PARTY_ID,
                LocalDate.of(2026, 9, 10), municipality, sourceType, sourceId, lines);
    }

    private static FiscalDocumentLineCommand line(String concept, String amount) {
        return new FiscalDocumentLineCommand(UUID.nameUUIDFromBytes((concept + amount).getBytes()), concept, "6201",
                new BigDecimal(amount), BigDecimal.ZERO);
    }

    private static final class InMemoryRepository implements FiscalDocumentCalculationRepositoryPort {
        private final List<FiscalDocumentCalculationResult> saved = new ArrayList<>();
        private int accumulationsSaved;
        private boolean periodClosed;
        @Override public Optional<FiscalDocumentCalculationResult> findBySource(UUID companyId,
                AccountingSourceType sourceType, UUID sourceId) {
            return saved.stream().filter(item -> item.companyId().equals(companyId)
                    && item.sourceType() == sourceType && item.sourceId().equals(sourceId)).findFirst();
        }
        @Override public FiscalDocumentCalculationResult save(FiscalDocumentCalculationResult result) {
            saved.add(result); return result;
        }
        @Override public void saveAccumulations(FiscalDocumentCalculationResult result) {
            accumulationsSaved++;
        }
        @Override public boolean isPeriodClosed(UUID companyId, LocalDate operationDate) {
            return periodClosed;
        }
    }
}
