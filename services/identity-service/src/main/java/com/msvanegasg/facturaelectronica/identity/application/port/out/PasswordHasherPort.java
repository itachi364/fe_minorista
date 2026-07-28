package com.msvanegasg.facturaelectronica.identity.application.port.out;

public interface PasswordHasherPort {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
