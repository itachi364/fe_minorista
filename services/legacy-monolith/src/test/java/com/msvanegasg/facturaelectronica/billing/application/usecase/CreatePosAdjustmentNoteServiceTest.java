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

import com.msvanegasg.facturaelectronica.billing.application.dto.AssignFiscalNumberCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.CreatePosAdjustmentNoteCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalNumberResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.PosAdjustmentNoteResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.AssignFiscalNumberUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicPosDocumentRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.PosAdjustmentNoteRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.BuyerInformation;
import com.msvanegasg.facturaelectronica.billing.domain.model.CalculatedElectronicDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicPosDocument;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalEnvironment;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalNumberAssignment;
import com.msvanegasg.facturaelectronica.billing.domain.model.PosAdjustmentNote;
import com.msvanegasg.facturaelectronica.billing.domain.model.PosAdjustmentType;

class CreatePosAdjustmentNoteServiceTest {

    private static final UUID NOTE_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID COMPANY_ID = UUID.fromString("24682468-2468-2468-2468-246824682468");
    private static final UUID POS_ID = UUID.fromString("13571357-1357-1357-1357-135713571357");
    private static final UUID SALE_ID = UUID.fromString("abcdefab-cdef-abcd-efab-cdefabcdefab");
    private static final UUID USER_ID = UUID.fromString("99999999-8888-7777-6666-555555555555");
    private static final UUID RESOLUTION_ID = UUID.fromString("11112222-3333-4444-5555-666677778888");
    private static final Instant NOW = Instant.parse("2026-05-11T23:50:00Z");
    private static final LocalDate DOCUMENT_DATE = LocalDate.of(2026, 5, 11);

    @Test
    void createCancellationAdjustmentAssignsOwnNumberAndStartsNumberAssigned() {
        CapturingAssignFiscalNumberUseCase assignFiscalNumber = new CapturingAssignFiscalNumberUseCase(
                new FiscalNumberResult(RESOLUTION_ID, "18760000002", "NCPOS", 500));
        CapturingNoteRepository noteRepository = new CapturingNoteRepository();
        CreatePosAdjustmentNoteService service = service(
                issuedPosDocument("POS", 123),
                noteRepository,
                assignFiscalNumber);

        PosAdjustmentNoteResult result = service.create(command(PosAdjustmentType.CANCELLATION));

        assertThat(result.id()).isEqualTo(NOTE_ID);
        assertThat(result.companyId()).isEqualTo(COMPANY_ID);
        assertThat(result.referencedDocumentId()).isEqualTo(POS_ID);
        assertThat(result.adjustmentType()).isEqualTo(PosAdjustmentType.CANCELLATION);
        assertThat(result.reason()).isEqualTo("Anulacion por devolucion total");
        assertThat(result.prefix()).isEqualTo("NCPOS");
        assertThat(result.number()).isEqualTo(500);
        assertThat(result.status()).isEqualTo(ElectronicDocumentStatus.NUMBER_ASSIGNED);
        assertThat(result.createdBy()).isEqualTo(USER_ID);
        assertThat(result.createdAt()).isEqualTo(NOW);
        assertThat(noteRepository.savedNote()).isNotNull();
        assertThat(assignFiscalNumber.lastCommand().documentType()).isEqualTo(ElectronicDocumentType.POS_ADJUSTMENT_NOTE);
    }

    @Test
    void createCorrectionAdjustmentReferencesIssuedPos() {
        CreatePosAdjustmentNoteService service = service(
                issuedPosDocument("POS", 123),
                new CapturingNoteRepository(),
                command -> new FiscalNumberResult(RESOLUTION_ID, "18760000002", "AJP", 501));

        PosAdjustmentNoteResult result = service.create(command(PosAdjustmentType.CORRECTION));

        assertThat(result.adjustmentType()).isEqualTo(PosAdjustmentType.CORRECTION);
        assertThat(result.subtotal()).isEqualByComparingTo("10000.00");
        assertThat(result.taxTotal()).isEqualByComparingTo("1900.00");
        assertThat(result.total()).isEqualByComparingTo("11900.00");
    }

    @Test
    void createAdjustmentRejectsReusedOriginalFiscalNumber() {
        CreatePosAdjustmentNoteService service = service(
                issuedPosDocument("POS", 123),
                new CapturingNoteRepository(),
                command -> new FiscalNumberResult(RESOLUTION_ID, "18760000002", "POS", 123));

        assertThatThrownBy(() -> service.create(command(PosAdjustmentType.CANCELLATION)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("adjustment note must not reuse referenced POS fiscal number");
    }

    @Test
    void createAdjustmentRejectsMissingReferencedPos() {
        CreatePosAdjustmentNoteService service = service(
                null,
                new CapturingNoteRepository(),
                command -> new FiscalNumberResult(RESOLUTION_ID, "18760000002", "AJP", 501));

        assertThatThrownBy(() -> service.create(command(PosAdjustmentType.CANCELLATION)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("referenced POS document was not found");
    }

    @Test
    void createAdjustmentRejectsInvalidTotals() {
        CreatePosAdjustmentNoteService service = service(
                issuedPosDocument("POS", 123),
                new CapturingNoteRepository(),
                command -> new FiscalNumberResult(RESOLUTION_ID, "18760000002", "AJP", 501));
        CreatePosAdjustmentNoteCommand command = new CreatePosAdjustmentNoteCommand(
                COMPANY_ID,
                POS_ID,
                PosAdjustmentType.CORRECTION,
                "Correccion de valor",
                new BigDecimal("10000.00"),
                new BigDecimal("1900.00"),
                new BigDecimal("12000.00"),
                DOCUMENT_DATE,
                FiscalEnvironment.TEST,
                USER_ID);

        assertThatThrownBy(() -> service.create(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("total must be equal to subtotal plus taxTotal");
    }

    private static CreatePosAdjustmentNoteService service(
            ElectronicPosDocument referencedPos,
            PosAdjustmentNoteRepositoryPort noteRepository,
            AssignFiscalNumberUseCase assignFiscalNumberUseCase) {
        return new CreatePosAdjustmentNoteService(
                new InMemoryPosRepository(referencedPos),
                noteRepository,
                assignFiscalNumberUseCase,
                () -> NOTE_ID,
                () -> NOW);
    }

    private static CreatePosAdjustmentNoteCommand command(PosAdjustmentType adjustmentType) {
        return new CreatePosAdjustmentNoteCommand(
                COMPANY_ID,
                POS_ID,
                adjustmentType,
                "Anulacion por devolucion total",
                new BigDecimal("10000.00"),
                new BigDecimal("1900.00"),
                new BigDecimal("11900.00"),
                DOCUMENT_DATE,
                FiscalEnvironment.TEST,
                USER_ID);
    }

    private static ElectronicPosDocument issuedPosDocument(String prefix, long number) {
        return ElectronicPosDocument.issue(
                POS_ID,
                COMPANY_ID,
                SALE_ID,
                new BuyerInformation("Cliente POS", "CC", "123456789"),
                new FiscalNumberAssignment(RESOLUTION_ID, "18760000001", prefix, number),
                new CalculatedElectronicDocument(
                        List.of(),
                        new BigDecimal("10000.00"),
                        BigDecimal.ZERO,
                        new BigDecimal("10000.00"),
                        new BigDecimal("1900.00"),
                        new BigDecimal("11900.00")),
                NOW);
    }

    private static final class InMemoryPosRepository implements ElectronicPosDocumentRepositoryPort {

        private final ElectronicPosDocument referencedPos;

        private InMemoryPosRepository(ElectronicPosDocument referencedPos) {
            this.referencedPos = referencedPos;
        }

        @Override
        public ElectronicPosDocument save(ElectronicPosDocument document) {
            return document;
        }

        @Override
        public Optional<ElectronicPosDocument> findByCompanyIdAndDocumentId(UUID companyId, UUID documentId) {
            if (referencedPos != null
                    && referencedPos.companyId().equals(companyId)
                    && referencedPos.id().equals(documentId)) {
                return Optional.of(referencedPos);
            }
            return Optional.empty();
        }
    }

    private static final class CapturingNoteRepository implements PosAdjustmentNoteRepositoryPort {

        private PosAdjustmentNote savedNote;

        @Override
        public PosAdjustmentNote save(PosAdjustmentNote note) {
            savedNote = note;
            return note;
        }

        private PosAdjustmentNote savedNote() {
            return savedNote;
        }
    }

    private static final class CapturingAssignFiscalNumberUseCase implements AssignFiscalNumberUseCase {

        private final FiscalNumberResult result;
        private AssignFiscalNumberCommand lastCommand;

        private CapturingAssignFiscalNumberUseCase(FiscalNumberResult result) {
            this.result = result;
        }

        @Override
        public FiscalNumberResult assign(AssignFiscalNumberCommand command) {
            lastCommand = command;
            return result;
        }

        private AssignFiscalNumberCommand lastCommand() {
            return lastCommand;
        }
    }
}
