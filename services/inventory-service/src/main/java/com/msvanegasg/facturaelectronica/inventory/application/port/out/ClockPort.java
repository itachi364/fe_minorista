package com.msvanegasg.facturaelectronica.inventory.application.port.out;

import java.time.Instant;

public interface ClockPort {
    Instant now();
}
