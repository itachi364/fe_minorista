package com.msvanegasg.facturaelectronica.providerretry;

import java.time.Instant;

public class SystemClockAdapter implements ClockPort {

    @Override
    public Instant now() {
        return Instant.now();
    }
}