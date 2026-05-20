package com.msvanegasg.facturaelectronica.billing.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.billing.application.dto.CreateElectronicDocumentDraftCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.ElectronicDocumentDraftResult;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentDraftRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentDraft;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;

class CreateElectronicDocumentDraftServiceTest {

    private static final UUID DOCUMENT_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID COMPANY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final Instant NOW = Instant.parse("2026-05-11T22:00:00Z");

    @Test
    void createDraftPersistsDocumentWithDraftStatus() {
        ElectronicDocumentDraftRepositoryPort repository = draft -> draft;
        IdGeneratorPort idGenerator = () -> DOCUMENT_ID;
        ClockPort clock = () -> NOW;
        CreateElectronicDocumentDraftService service = new CreateElectronicDocumentDraftService(
                repository,
                idGenerator,
                clock);
        CreateElectronicDocumentDraftCommand command = new CreateElectronicDocumentDraftCommand(
                COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_POS,
                "idem-001");

        ElectronicDocumentDraftResult result = service.createDraft(command);

        assertThat(result.id()).isEqualTo(DOCUMENT_ID);
        assertThat(result.companyId()).isEqualTo(COMPANY_ID);
        assertThat(result.documentType()).isEqualTo(ElectronicDocumentType.ELECTRONIC_POS);
        assertThat(result.status()).isEqualTo(ElectronicDocumentStatus.DRAFT);
        assertThat(result.createdAt()).isEqualTo(NOW);
    }

    @Test
    void createDraftRejectsBlankIdempotencyKey() {
        CreateElectronicDocumentDraftService service = new CreateElectronicDocumentDraftService(
                draft -> draft,
                () -> DOCUMENT_ID,
                () -> NOW);
        CreateElectronicDocumentDraftCommand command = new CreateElectronicDocumentDraftCommand(
                COMPANY_ID,
                ElectronicDocumentType.ELECTRONIC_POS,
                " ");

        assertThatThrownBy(() -> service.createDraft(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("idempotencyKey is required");
    }

    @Test
    void createFactoryRejectsMissingCompanyId() {
        assertThatThrownBy(() -> ElectronicDocumentDraft.create(
                DOCUMENT_ID,
                null,
                ElectronicDocumentType.ELECTRONIC_POS,
                "idem-001",
                NOW))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("companyId is required");
    }
}
