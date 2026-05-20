package com.msvanegasg.facturaelectronica.billing.application.port.out;

import java.time.Instant;

public interface ClockPort {

    Instant now();
}
