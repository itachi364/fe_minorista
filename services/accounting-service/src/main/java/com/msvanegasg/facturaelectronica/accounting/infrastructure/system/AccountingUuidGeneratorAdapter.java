package com.msvanegasg.facturaelectronica.accounting.infrastructure.system;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.IdGeneratorPort;

@Component
public class AccountingUuidGeneratorAdapter implements IdGeneratorPort {

    @Override
    public UUID newId() {
        return UUID.randomUUID();
    }
}
