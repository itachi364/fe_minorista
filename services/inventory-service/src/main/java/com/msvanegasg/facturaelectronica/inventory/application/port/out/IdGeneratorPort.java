package com.msvanegasg.facturaelectronica.inventory.application.port.out;

import java.util.UUID;

public interface IdGeneratorPort {
    UUID newId();
}
