package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.system;

import java.time.Instant;

import org.springframework.stereotype.Component;

import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ClockPort;

@Component
public class SystemClockAdapter implements ClockPort {

    @Override
    public Instant now() {
        return Instant.now();
    }
}
