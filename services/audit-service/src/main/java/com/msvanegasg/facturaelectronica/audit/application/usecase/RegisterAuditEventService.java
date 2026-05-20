package com.msvanegasg.facturaelectronica.audit.application.usecase;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.audit.application.dto.AuditEventResult;
import com.msvanegasg.facturaelectronica.audit.application.dto.RegisterAuditEventCommand;
import com.msvanegasg.facturaelectronica.audit.application.port.in.RegisterAuditEventUseCase;
import com.msvanegasg.facturaelectronica.audit.application.port.out.AuditEventRepositoryPort;
import com.msvanegasg.facturaelectronica.audit.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.audit.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.audit.domain.model.AuditEvent;

public class RegisterAuditEventService implements RegisterAuditEventUseCase {

    private final AuditEventRepositoryPort repository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public RegisterAuditEventService(AuditEventRepositoryPort repository, IdGeneratorPort idGenerator,
            ClockPort clock) {
        this.repository = Objects.requireNonNull(repository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public AuditEventResult register(RegisterAuditEventCommand command) {
        Objects.requireNonNull(command, "command is required");
        AuditEvent event = AuditEvent.register(idGenerator.newId(), command.companyId(), command.userId(),
                command.eventType(), command.resourceType(), command.resourceId(), command.action(), command.result(),
                command.detail(), clock.now());
        return AuditEventResultMapper.toResult(repository.save(event));
    }
}
