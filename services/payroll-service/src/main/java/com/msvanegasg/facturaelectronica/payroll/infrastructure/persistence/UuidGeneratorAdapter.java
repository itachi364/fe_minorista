package com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.payroll.application.port.out.IdGeneratorPort;

@Component
public class UuidGeneratorAdapter implements IdGeneratorPort {
    @Override
    public UUID newId() {
        return UUID.randomUUID();
    }
}
