package com.msvanegasg.facturaelectronica.payroll.application.port.out;

import java.util.UUID;

public interface IdGeneratorPort {
    UUID newId();
}
