package com.msvanegasg.facturaelectronica.reporting.infrastructure;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.reporting.application.port.out.IdGeneratorPort;

@Component
public class UuidIdGeneratorAdapter implements IdGeneratorPort {

    @Override
    public UUID newId() {
        return UUID.randomUUID();
    }
}
