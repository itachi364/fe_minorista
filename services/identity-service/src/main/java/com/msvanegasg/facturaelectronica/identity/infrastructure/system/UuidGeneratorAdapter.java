package com.msvanegasg.facturaelectronica.identity.infrastructure.system;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.identity.application.port.out.IdGeneratorPort;

@Component
public class UuidGeneratorAdapter implements IdGeneratorPort {

    @Override
    public UUID nextId() {
        return UUID.randomUUID();
    }
}
