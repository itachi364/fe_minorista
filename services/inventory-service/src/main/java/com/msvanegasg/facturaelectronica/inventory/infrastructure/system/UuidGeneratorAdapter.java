package com.msvanegasg.facturaelectronica.inventory.infrastructure.system;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.inventory.application.port.out.IdGeneratorPort;

@Component
public class UuidGeneratorAdapter implements IdGeneratorPort {

    @Override
    public UUID newId() {
        return UUID.randomUUID();
    }
}
