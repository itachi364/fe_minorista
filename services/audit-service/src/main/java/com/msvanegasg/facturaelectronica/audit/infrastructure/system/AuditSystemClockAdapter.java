package com.msvanegasg.facturaelectronica.audit.infrastructure.system;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.audit.application.port.out.ClockPort;

@Component
public class AuditSystemClockAdapter implements ClockPort {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
