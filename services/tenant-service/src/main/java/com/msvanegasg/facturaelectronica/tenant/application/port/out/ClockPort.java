package com.msvanegasg.facturaelectronica.tenant.application.port.out;

import java.time.Instant;

public interface ClockPort {

    Instant now();
}
