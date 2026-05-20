package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.system;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.IdGeneratorPort;

@Component
public class UuidGeneratorAdapter implements IdGeneratorPort {

    @Override
    public UUID generate() {
        return UUID.randomUUID();
    }
}
