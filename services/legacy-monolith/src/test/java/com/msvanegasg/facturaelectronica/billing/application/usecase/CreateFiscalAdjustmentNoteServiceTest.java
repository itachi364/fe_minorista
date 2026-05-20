package com.msvanegasg.facturaelectronica.billing.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateFiscalAdjustmentNoteCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalAdjustmentNoteResult;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentLifecycleRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentLifecycle;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalAdjustmentNote;

class CreateFiscalAdjustmentNoteServiceTest {

    private static final UUID NOTE_ID = UUID.fromString("abababab-abab-abab-abab-abababababab");
    private static final UUID COMPANY_ID = UUID.fromString("cdcdcdcd-cdcd-cdcd-cdcd-cdcdcdcdcdcd");
    private static final UUID INVOICE_ID = UUID.fromString("efefefef-efef-efef-efef-efefefefefef");
    private static final UUID USER_ID = UUID.fromString("10101010-1010-1010-1010-101010101010");
    private static final Instant NOW = Instant.parse("2026-05-11T23:30:00Z");

    @Test
    void createCreditNoteReferencesValidatedInvoiceAndStartsInDraft() {
        CapturingNoteRepository noteRepository = new CapturingNoteRepository();
        CreateFiscalAdjustmentNoteService service = service(
                invoice(ElectronicDocumentType.ELECTRONIC_INVOICE, ElectronicDocumentStatus.VALIDATED),
                noteRepository);

        FiscalAdjustmentNoteResult result = service.create(command(ElectronicDocumentType.CREDIT_NOTE));

        assertThat(result.id()).isEqualTo(NOTE_ID);
        assertThat(result.companyId()).isEqualTo(COMPANY_ID);
        assertThat(result.referencedDocumentId()).isEqualTo(INVOICE_ID);
        assertThat(result.documentType()).isEqualTo(ElectronicDocumentType.CREDIT_NOTE);
        assertThat(result.reason()).isEqualTo("customer return");
        assertThat(result.subtotal()).isEqualByComparingTo("1000.00");
        assertThat(result.taxTotal()).isEqualByComparingTo("190.00");
        assertThat(result.total()).isEqualByComparingTo("1190.00");
        assertThat(result.status()).isEqualTo(ElectronicDocumentStatus.DRAFT);
        assertThat(result.createdBy()).isEqualTo(USER_ID);
        assertThat(result.createdAt()).isEqualTo(NOW);
        assertThat(noteRepository.savedNote()).isNotNull();
    }

    @Test
    void createDebitNoteReferencesValidatedInvoiceAndStartsInDraft() {
        CreateFiscalAdjustmentNoteService service = service(
                invoice(ElectronicDocumentType.ELECTRONIC_INVOICE, ElectronicDocumentStatus.VALIDATED),
                new CapturingNoteRepository());

        FiscalAdjustmentNoteResult result = service.create(command(ElectronicDocumentType.DEBIT_NOTE));

        assertThat(result.documentType()).isEqualTo(ElectronicDocumentType.DEBIT_NOTE);
        assertThat(result.status()).isEqualTo(ElectronicDocumentStatus.DRAFT);
    }

    @Test
    void createNoteRejectsInvoiceThatIsNotValidated() {
        CreateFiscalAdjustmentNoteService service = service(
                invoice(ElectronicDocumentType.ELECTRONIC_INVOICE, ElectronicDocumentStatus.SENT_TO_PROVIDER),
                new CapturingNoteRepository());

        assertThatThrownBy(() -> service.create(command(ElectronicDocumentType.CREDIT_NOTE)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("referenced invoice must be validated");
    }

    @Test
    void createNoteRejectsReferencedPosDocument() {
        CreateFiscalAdjustmentNoteService service = service(
                invoice(ElectronicDocumentType.ELECTRONIC_POS, ElectronicDocumentStatus.VALIDATED),
                new CapturingNoteRepository());

        assertThatThrownBy(() -> service.create(command(ElectronicDocumentType.CREDIT_NOTE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("referenced document must be an electronic invoice");
    }

    @Test
    void createNoteRejectsInvalidNoteType() {
        CreateFiscalAdjustmentNoteService service = service(
                invoice(ElectronicDocumentType.ELECTRONIC_INVOICE, ElectronicDocumentStatus.VALIDATED),
                new CapturingNoteRepository());

        assertThatThrownBy(() -> service.create(command(ElectronicDocumentType.ELECTRONIC_INVOICE)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("documentType must be CREDIT_NOTE or DEBIT_NOTE");
    }

    @Test
    void createNoteRejectsInvalidTotal() {
        CreateFiscalAdjustmentNoteService service = service(
                invoice(ElectronicDocumentType.ELECTRONIC_INVOICE, ElectronicDocumentStatus.VALIDATED),
                new CapturingNoteRepository());
        CreateFiscalAdjustmentNoteCommand command = new CreateFiscalAdjustmentNoteCommand(
                COMPANY_ID,
                INVOICE_ID,
                ElectronicDocumentType.CREDIT_NOTE,
                "customer return",
                new BigDecimal("1000.00"),
                new BigDecimal("190.00"),
                new BigDecimal("1000.00"),
                USER_ID);

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("total must be equal to subtotal plus taxTotal");
    }

    @Test
    void createNoteRejectsBlankReason() {
        CreateFiscalAdjustmentNoteService service = service(
                invoice(ElectronicDocumentType.ELECTRONIC_INVOICE, ElectronicDocumentStatus.VALIDATED),
                new CapturingNoteRepository());
        CreateFiscalAdjustmentNoteCommand command = new CreateFiscalAdjustmentNoteCommand(
                COMPANY_ID,
                INVOICE_ID,
                ElectronicDocumentType.CREDIT_NOTE,
                " ",
                new BigDecimal("1000.00"),
                new BigDecimal("190.00"),
                new BigDecimal("1190.00"),
                USER_ID);

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("reason is required");
    }

    @Test
    void createNoteRejectsMissingReferencedDocument() {
        CreateFiscalAdjustmentNoteService service = service(null, new CapturingNoteRepository());

        assertThatThrownBy(() -> service.create(command(ElectronicDocumentType.CREDIT_NOTE)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("referenced document was not found");
    }

    private static CreateFiscalAdjustmentNoteService service(
            ElectronicDocumentLifecycle referencedDocument,
            CapturingNoteRepository noteRepository) {
        return new CreateFiscalAdjustmentNoteService(
                new InMemoryDocumentRepository(referencedDocument),
                noteRepository,
                () -> NOTE_ID,
                () -> NOW);
    }

    private static CreateFiscalAdjustmentNoteCommand command(ElectronicDocumentType documentType) {
        return new CreateFiscalAdjustmentNoteCommand(
                COMPANY_ID,
                INVOICE_ID,
                documentType,
                "customer return",
                new BigDecimal("1000.00"),
                new BigDecimal("190.00"),
                new BigDecimal("1190.00"),
                USER_ID);
    }

    private static ElectronicDocumentLifecycle invoice(
            ElectronicDocumentType documentType,
            ElectronicDocumentStatus status) {
        return ElectronicDocumentLifecycle.restore(
                INVOICE_ID,
                COMPANY_ID,
                documentType,
                status,
                "PT-001",
                "CUFE-001",
                "QR-001",
                "<xml/>",
                "PDF-CONTENT",
                null,
                null,
                NOW);
    }

    private static final class InMemoryDocumentRepository implements ElectronicDocumentLifecycleRepositoryPort {

        private final ElectronicDocumentLifecycle referencedDocument;

        private InMemoryDocumentRepository(ElectronicDocumentLifecycle referencedDocument) {
            this.referencedDocument = referencedDocument;
        }

        @Override
        public Optional<ElectronicDocumentLifecycle> findByCompanyIdAndDocumentId(UUID companyId, UUID documentId) {
            return Optional.ofNullable(referencedDocument)
                    .filter(document -> document.companyId().equals(companyId) && document.id().equals(documentId));
        }

        @Override
        public ElectronicDocumentLifecycle save(ElectronicDocumentLifecycle document) {
            return document;
        }
    }

    private static final class CapturingNoteRepository
            implements com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalAdjustmentNoteRepositoryPort {

        private FiscalAdjustmentNote savedNote;

        @Override
        public FiscalAdjustmentNote save(FiscalAdjustmentNote note) {
            savedNote = note;
            return note;
        }

        FiscalAdjustmentNote savedNote() {
            return savedNote;
        }
    }
}
