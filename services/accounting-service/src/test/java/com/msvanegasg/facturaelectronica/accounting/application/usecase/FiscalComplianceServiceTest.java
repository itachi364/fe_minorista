package com.msvanegasg.facturaelectronica.accounting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalAccountMappingResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalPeriodSummary;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalReconciliationResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.FiscalReversalResult;
import com.msvanegasg.facturaelectronica.accounting.application.dto.WithholdingCertificateResult;
import com.msvanegasg.facturaelectronica.accounting.application.port.out.FiscalComplianceRepositoryPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.WithholdingType;

class FiscalComplianceServiceTest {
    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    @Test
    void validatesAndDelegatesCompanyAccountMapping() {
        FakeRepository repository = new FakeRepository();
        FiscalComplianceService service = new FiscalComplianceService(repository);

        FiscalAccountMappingResult result = service.saveMapping(COMPANY_ID, WithholdingType.RETEFUENTE, "236540",
                null, LocalDate.of(2026, 1, 1), null, null);

        assertThat(result.payableAccountCode()).isEqualTo("236540");
        assertThat(repository.mappingSaved).isTrue();
        assertThatThrownBy(() -> service.saveMapping(COMPANY_ID, WithholdingType.RETEFUENTE, "23-6540", null,
                LocalDate.of(2026, 1, 1), null, null)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void validatesPeriodBeforeClosing() {
        FiscalComplianceService service = new FiscalComplianceService(new FakeRepository());

        assertThat(service.close(COMPANY_ID, 2026, 9, null).closed()).isTrue();
        assertThatThrownBy(() -> service.close(COMPANY_ID, 2026, 13, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void requiresReasonForImmutableReversal() {
        FiscalComplianceService service = new FiscalComplianceService(new FakeRepository());

        assertThatThrownBy(() -> service.reverse(COMPANY_ID, UUID.randomUUID(), " ", null))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining("reason");
    }

    private static final class FakeRepository implements FiscalComplianceRepositoryPort {
        private boolean mappingSaved;
        @Override public FiscalAccountMappingResult saveMapping(UUID companyId, WithholdingType type,
                String payable, String receivable, LocalDate from, LocalDate to, UUID userId) {
            mappingSaved = true;
            return new FiscalAccountMappingResult(UUID.randomUUID(), companyId, type, payable, receivable, from, to,
                    true);
        }
        @Override public List<FiscalAccountMappingResult> findMappings(UUID companyId) { return List.of(); }
        @Override public FiscalReconciliationResult reconcile(UUID companyId, int year, int month) {
            return new FiscalReconciliationResult(companyId, year, month, BigDecimal.ZERO, BigDecimal.ZERO,
                    BigDecimal.ZERO, "RECONCILED");
        }
        @Override public FiscalReversalResult reverse(UUID companyId, UUID calculationId, String reason, UUID userId) {
            return new FiscalReversalResult(UUID.randomUUID(), companyId, calculationId, reason, userId, Instant.now());
        }
        @Override public FiscalPeriodSummary summarize(UUID companyId, int year, int month) {
            return new FiscalPeriodSummary(companyId, year, month, Map.of(), Map.of(), BigDecimal.ZERO, false);
        }
        @Override public FiscalPeriodSummary close(UUID companyId, int year, int month, UUID userId) {
            return new FiscalPeriodSummary(companyId, year, month, Map.of(), Map.of(), BigDecimal.ZERO, true);
        }
        @Override public WithholdingCertificateResult generateCertificate(UUID companyId, UUID thirdPartyId,
                int year, UUID userId) {
            return new WithholdingCertificateResult(UUID.randomUUID(), companyId, thirdPartyId, year, 1,
                    BigDecimal.ZERO, BigDecimal.ZERO, Instant.now());
        }
        @Override public List<WithholdingCertificateResult> findCertificates(UUID companyId, UUID thirdPartyId,
                int year) { return List.of(); }
        @Override public Optional<String> findCertificateContent(UUID companyId, UUID certificateId) {
            return Optional.empty();
        }
    }
}
