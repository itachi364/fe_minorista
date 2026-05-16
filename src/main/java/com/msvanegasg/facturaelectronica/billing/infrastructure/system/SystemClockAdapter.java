package com.msvanegasg.facturaelectronica.billing.infrastructure.system;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;

@Component
public class SystemClockAdapter implements ClockPort {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
