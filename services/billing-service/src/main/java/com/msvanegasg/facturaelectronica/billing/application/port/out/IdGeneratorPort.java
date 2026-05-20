package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.util.UUID;

public interface IdGeneratorPort {

    UUID newId();
}
