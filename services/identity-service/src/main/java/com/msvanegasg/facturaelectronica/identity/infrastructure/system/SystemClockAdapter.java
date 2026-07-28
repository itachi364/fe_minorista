package com.msvanegasg.facturaelectronica.identity.infrastructure.system;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.identity.application.port.out.ClockPort;

@Component
public class SystemClockAdapter implements ClockPort {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
