package com.msvanegasg.facturaelectronica.identity.application.port.out;

public interface TokenHashPort {

    String hash(String token);
}
