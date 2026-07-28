package com.msvanegasg.facturaelectronica.identity.application.usecase;

import java.util.UUID;

public class MembershipNotFoundException extends RuntimeException {

    public MembershipNotFoundException(UUID id) {
        super("Membership not found: " + id);
    }
}
