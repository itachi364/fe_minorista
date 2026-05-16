package com.msvanegasg.facturaelectronica.accounting.application.port.out;

import java.util.UUID;

public interface IdGeneratorPort {

    UUID newId();
}
