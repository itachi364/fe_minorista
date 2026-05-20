package com.msvanegasg.facturaelectronica.billing.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalAuditEventResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.RegisterFiscalAuditEventCommand;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalAuditEventRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalAuditEvent;

class RegisterFiscalAuditEventServiceTest {

    private static final UUID AUDIT_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID COMPANY_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
    private static final UUID RESOURCE_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
    private static final UUID USER_ID = UUID.fromString("dddddddd-dddd-dddd-dddd-dddddddddddd");
    private static final Instant NOW = Instant.parse("2026-05-19T16:00:00Z");

    @Test
    void registersFiscalAuditEventWithUserDateActionResourceAndResult() {
        CapturingAuditRepository repository = new CapturingAuditRepository();
        RegisterFiscalAuditEventService service = new RegisterFiscalAuditEventService(
                repository,
                () -> AUDIT_ID,
                () -> NOW);

        FiscalAuditEventResult result = service.register(new RegisterFiscalAuditEventCommand(
                COMPANY_ID,
                RESOURCE_ID,
                " ELECTRONIC_DOCUMENT ",
                " PROVIDER_ACCEPTED ",
                " VALIDATED ",
                USER_ID,
                " provider accepted document "));

        assertThat(result.id()).isEqualTo(AUDIT_ID);
        assertThat(result.companyId()).isEqualTo(COMPANY_ID);
        assertThat(result.resourceId()).isEqualTo(RESOURCE_ID);
        assertThat(result.resourceType()).isEqualTo("ELECTRONIC_DOCUMENT");
        assertThat(result.action()).isEqualTo("PROVIDER_ACCEPTED");
        assertThat(result.result()).isEqualTo("VALIDATED");
        assertThat(result.userId()).isEqualTo(USER_ID);
        assertThat(result.occurredAt()).isEqualTo(NOW);
        assertThat(result.detail()).isEqualTo("provider accepted document");
        assertThat(repository.savedEvent()).isNotNull();
        assertThat(repository.savedEvent().userId()).isEqualTo(USER_ID);
    }

    @Test
    void registersFiscalAuditEventWithoutUserWhenAuthenticationIsNotAvailableYet() {
        CapturingAuditRepository repository = new CapturingAuditRepository();
        RegisterFiscalAuditEventService service = new RegisterFiscalAuditEventService(
                repository,
                () -> AUDIT_ID,
                () -> NOW);

        FiscalAuditEventResult result = service.register(new RegisterFiscalAuditEventCommand(
                COMPANY_ID,
                RESOURCE_ID,
                "ELECTRONIC_DOCUMENT",
                "PROVIDER_REJECTED",
                "REJECTED",
                null,
                null));

        assertThat(result.userId()).isNull();
        assertThat(result.detail()).isNull();
        assertThat(repository.savedEvent().occurredAt()).isEqualTo(NOW);
    }

    @Test
    void rejectsMissingRequiredFields() {
        RegisterFiscalAuditEventService service = new RegisterFiscalAuditEventService(
                new CapturingAuditRepository(),
                () -> AUDIT_ID,
                () -> NOW);

        assertThatThrownBy(() -> service.register(new RegisterFiscalAuditEventCommand(
                COMPANY_ID,
                RESOURCE_ID,
                " ",
                "PROVIDER_ACCEPTED",
                "VALIDATED",
                USER_ID,
                null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("resourceType is required");
    }

    private static final class CapturingAuditRepository implements FiscalAuditEventRepositoryPort {

        private FiscalAuditEvent savedEvent;

        @Override
        public FiscalAuditEvent save(FiscalAuditEvent event) {
            savedEvent = event;
            return event;
        }

        FiscalAuditEvent savedEvent() {
            return savedEvent;
        }
    }
}
