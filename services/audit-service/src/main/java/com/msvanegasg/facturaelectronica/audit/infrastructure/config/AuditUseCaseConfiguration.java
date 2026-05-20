package com.msvanegasg.facturaelectronica.audit.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.audit.application.port.in.QueryAuditEventsUseCase;
import com.msvanegasg.facturaelectronica.audit.application.port.in.RegisterAuditEventUseCase;
import com.msvanegasg.facturaelectronica.audit.application.port.out.AuditEventRepositoryPort;
import com.msvanegasg.facturaelectronica.audit.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.audit.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.audit.application.usecase.QueryAuditEventsService;
import com.msvanegasg.facturaelectronica.audit.application.usecase.RegisterAuditEventService;

@Configuration
public class AuditUseCaseConfiguration {

    @Bean
    RegisterAuditEventUseCase registerAuditEventUseCase(AuditEventRepositoryPort repository,
            IdGeneratorPort idGenerator, ClockPort clock) {
        return new RegisterAuditEventService(repository, idGenerator, clock);
    }

    @Bean
    QueryAuditEventsUseCase queryAuditEventsUseCase(AuditEventRepositoryPort repository) {
        return new QueryAuditEventsService(repository);
    }
}
