package com.msvanegasg.facturaelectronica.identity.application.usecase;

import java.util.UUID;

public class CompanyRoleNotFoundException extends RuntimeException {

    public CompanyRoleNotFoundException(UUID roleId) {
        super("Company role not found: " + roleId);
    }
}
