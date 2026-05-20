package com.msvanegasg.facturaelectronica.audit.application.port.out;

import java.util.UUID;

public interface IdGeneratorPort {

    UUID newId();
}
