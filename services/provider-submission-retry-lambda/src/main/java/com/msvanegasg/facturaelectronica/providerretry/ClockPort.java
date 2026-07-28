package com.msvanegasg.facturaelectronica.providerretry;

import java.time.Instant;

public interface ClockPort {

    Instant now();
}