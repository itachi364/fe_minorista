package com.msvanegasg.facturaelectronica.identity.application.port.out;

import java.util.UUID;

public interface IdGeneratorPort {

    UUID nextId();
}
