package com.msvanegasg.facturaelectronica.identity.application.usecase;

public class AuthenticationFailedException extends RuntimeException {

    public AuthenticationFailedException() {
        super("Invalid credentials or expired session.");
    }
}
