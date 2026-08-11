package com.msvanegasg.facturaelectronica.payroll.infrastructure.persistence;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.payroll.application.port.out.ClockPort;

@Component
public class SystemClockAdapter implements ClockPort {
    @Override
    public Instant now() {
        return Instant.now();
    }
}
