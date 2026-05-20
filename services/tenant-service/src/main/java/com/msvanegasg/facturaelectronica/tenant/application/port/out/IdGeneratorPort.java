package com.msvanegasg.facturaelectronica.tenant.application.port.out;

import java.util.UUID;

public interface IdGeneratorPort {

    UUID nextId();
}
