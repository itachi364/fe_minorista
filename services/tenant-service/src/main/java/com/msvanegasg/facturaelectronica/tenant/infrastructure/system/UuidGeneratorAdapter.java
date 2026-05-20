package com.msvanegasg.facturaelectronica.tenant.infrastructure.system;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.tenant.application.port.out.IdGeneratorPort;

@Component
public class UuidGeneratorAdapter implements IdGeneratorPort {

    @Override
    public UUID nextId() {
        return UUID.randomUUID();
    }
}
