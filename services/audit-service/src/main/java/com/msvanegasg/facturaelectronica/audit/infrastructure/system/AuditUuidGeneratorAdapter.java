package com.msvanegasg.facturaelectronica.audit.infrastructure.system;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.audit.application.port.out.IdGeneratorPort;

@Component
public class AuditUuidGeneratorAdapter implements IdGeneratorPort {

    @Override
    public UUID newId() {
        return UUID.randomUUID();
    }
}
