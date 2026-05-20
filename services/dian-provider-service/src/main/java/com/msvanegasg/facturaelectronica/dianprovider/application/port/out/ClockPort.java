package com.msvanegasg.facturaelectronica.dianprovider.application.port.out;

import java.time.Instant;

public interface ClockPort {

    Instant now();
}
