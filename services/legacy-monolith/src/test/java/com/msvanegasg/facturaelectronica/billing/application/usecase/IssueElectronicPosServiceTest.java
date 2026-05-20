package com.msvanegasg.facturaelectronica.billing.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentLineCalculationCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicPosDocumentResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNumberResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.IssueElectronicPosCommand;
import com.msvanegasg.facturaelectronica.billing.application.port.in.AssignFiscalNumberUseCase;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicPosDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;

class IssueElectronicPosServiceTest {

    private static final UUID DOCUMENT_ID = UUID.fromString("13571357-1357-1357-1357-135713571357");
    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");
    private static final UUID SALE_ID = UUID.fromString("abcdefab-cdef-abcd-efab-cdefabcdefab");
    private static final UUID PRODUCT_ID = UUID.fromString("99998888-7777-6666-5555-444433332222");
    private static final UUID RESOLUTION_ID = UUID.fromString("11112222-3333-4444-5555-666677778888");
    private static final Instant NOW = Instant.parse("2026-05-11T23:45:00Z");
    private static final LocalDate DOCUMENT_DATE = LocalDate.of(2026, 5, 11);

    @Test
    void issueElectronicPosAssignsNumberCalculatesTotalsAndGeneratesCude() {
        CapturingPosRepository repository = new CapturingPosRepository();
        IssueElectronicPosService service = service(command -> new FiscalNumberResult(
                RESOLUTION_ID,
                "18760000001",
                "POS",
                123), repository);

        ElectronicPosDocumentResult result = service.issue(validCommand());

        assertThat(result.id()).isEqualTo(DOCUMENT_ID);
        assertThat(result.companyId()).isEqualTo(COMPANY_ID);
        assertThat(result.saleId()).isEqualTo(SALE_ID);
        assertThat(result.buyerName()).isEqualTo("Cliente POS");
        assertThat(result.buyerDocumentType()).isEqualTo("CC");
        assertThat(result.buyerDocumentNumber()).isEqualTo("123456789");
        assertThat(result.prefix()).isEqualTo("POS");
        assertThat(result.number()).isEqualTo(123);
        assertThat(result.subtotal()).isEqualByComparingTo("29000.00");
        assertThat(result.taxTotal()).isEqualByComparingTo("5510.00");
        assertThat(result.total()).isEqualByComparingTo("34510.00");
        assertThat(result.status()).isEqualTo(ElectronicDocumentStatus.NUMBER_ASSIGNED);
        assertThat(result.issueAt()).isEqualTo(NOW);
        assertThat(result.cude()).hasSize(96);
        assertThat(result.cude()).isEqualTo(repository.savedDocument().cude());
    }

    @Test
    void issueElectronicPosGeneratesDeterministicCudeForSameFiscalData() {
        IssueElectronicPosService firstService = service(command -> new FiscalNumberResult(
                RESOLUTION_ID,
                "18760000001",
                "POS",
                123), new CapturingPosRepository());
        IssueElectronicPosService secondService = service(command -> new FiscalNumberResult(
                RESOLUTION_ID,
                "18760000001",
                "POS",
                123), new CapturingPosRepository());

        ElectronicPosDocumentResult first = firstService.issue(validCommand());
        ElectronicPosDocumentResult second = secondService.issue(validCommand());

        assertThat(first.cude()).isEqualTo(second.cude());
    }

    @Test
    void issueElectronicPosAllowsMissingBuyerInformation() {
        IssueElectronicPosService service = service(command -> new FiscalNumberResult(
                RESOLUTION_ID,
                "18760000001",
                "POS",
                124), new CapturingPosRepository());
        IssueElectronicPosCommand command = new IssueElectronicPosCommand(
                COMPANY_ID,
                SALE_ID,
                null,
                null,
                null,
                DOCUMENT_DATE,
                FiscalEnvironment.TEST,
                lines());

        ElectronicPosDocumentResult result = service.issue(command);

        assertThat(result.buyerName()).isNull();
        assertThat(result.buyerDocumentType()).isNull();
        assertThat(result.buyerDocumentNumber()).isNull();
    }

    @Test
    void issueElectronicPosRejectsIncompleteBuyerInformation() {
        IssueElectronicPosService service = service(command -> new FiscalNumberResult(
                RESOLUTION_ID,
                "18760000001",
                "POS",
                125), new CapturingPosRepository());
        IssueElectronicPosCommand command = new IssueElectronicPosCommand(
                COMPANY_ID,
                SALE_ID,
                "Cliente POS",
                "CC",
                null,
                DOCUMENT_DATE,
                FiscalEnvironment.TEST,
                lines());

        assertThatThrownBy(() -> service.issue(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("buyer name, documentType and documentNumber must be provided together");
    }

    @Test
    void issueElectronicPosPropagatesNumberingFailure() {
        IssueElectronicPosService service = service(command -> {
            throw new IllegalStateException("active numbering resolution is required");
        }, new CapturingPosRepository());

        assertThatThrownBy(() -> service.issue(validCommand()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("active numbering resolution is required");
    }

    private static IssueElectronicPosService service(
            AssignFiscalNumberUseCase assignFiscalNumberUseCase,
            CapturingPosRepository repository) {
        return new IssueElectronicPosService(
                assignFiscalNumberUseCase,
                repository,
                () -> DOCUMENT_ID,
                () -> NOW);
    }

    private static IssueElectronicPosCommand validCommand() {
        return new IssueElectronicPosCommand(
                COMPANY_ID,
                SALE_ID,
                "Cliente POS",
                "CC",
                "123456789",
                DOCUMENT_DATE,
                FiscalEnvironment.TEST,
                lines());
    }

    private static List<ElectronicDocumentLineCalculationCommand> lines() {
        return List.of(new ElectronicDocumentLineCalculationCommand(
                PRODUCT_ID,
                new BigDecimal("2"),
                new BigDecimal("15000"),
                new BigDecimal("1000"),
                "IVA_19",
                new BigDecimal("19")));
    }

    private static final class CapturingPosRepository
            implements com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicPosDocumentRepositoryPort {

        private ElectronicPosDocument savedDocument;

        @Override
        public ElectronicPosDocument save(ElectronicPosDocument document) {
            savedDocument = document;
            return document;
        }

        @Override
        public Optional<ElectronicPosDocument> findByCompanyIdAndDocumentId(UUID companyId, UUID documentId) {
            if (savedDocument != null
                    && savedDocument.companyId().equals(companyId)
                    && savedDocument.id().equals(documentId)) {
                return Optional.of(savedDocument);
            }
            return Optional.empty();
        }

        ElectronicPosDocument savedDocument() {
            return savedDocument;
        }
    }
}
