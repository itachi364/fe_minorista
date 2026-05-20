package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.billing.application.dto.FiscalAuditEventResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.RegisterFiscalAuditEventCommand;
import com.msvanegasg.facturaelectronica.billing.application.port.in.RegisterFiscalAuditEventUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalAuditEventRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.FiscalAuditEvent;

public class RegisterFiscalAuditEventService implements RegisterFiscalAuditEventUseCase {

    private final FiscalAuditEventRepositoryPort auditEventRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public RegisterFiscalAuditEventService(
            FiscalAuditEventRepositoryPort auditEventRepository,
            IdGeneratorPort idGenerator,
            ClockPort clock) {
        this.auditEventRepository = Objects.requireNonNull(auditEventRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public FiscalAuditEventResult register(RegisterFiscalAuditEventCommand command) {
        validate(command);

        FiscalAuditEvent savedEvent = auditEventRepository.save(new FiscalAuditEvent(
                idGenerator.newId(),
                command.companyId(),
                command.resourceId(),
                normalizeRequired(command.resourceType(), "resourceType"),
                normalizeRequired(command.action(), "action"),
                normalizeRequired(command.result(), "result"),
                command.userId(),
                clock.now(),
                normalizeOptional(command.detail())));

        return toResult(savedEvent);
    }

    private static FiscalAuditEventResult toResult(FiscalAuditEvent event) {
        return new FiscalAuditEventResult(
                event.id(),
                event.companyId(),
                event.resourceId(),
                event.resourceType(),
                event.action(),
                event.result(),
                event.userId(),
                event.occurredAt(),
                event.detail());
    }

    private static void validate(RegisterFiscalAuditEventCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.resourceId(), "resourceId is required");
        normalizeRequired(command.resourceType(), "resourceType");
        normalizeRequired(command.action(), "action");
        normalizeRequired(command.result(), "result");
    }

    private static String normalizeRequired(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
        return value.trim();
    }

    private static String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
