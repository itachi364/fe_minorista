package com.msvanegasg.facturaelectronica.payroll.application.port.out;

import java.time.Instant;

public interface ClockPort {
    Instant now();
}
