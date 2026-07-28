package com.msvanegasg.facturaelectronica.identity.application.usecase;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;

public class AccessDeniedException extends RuntimeException {

    public AccessDeniedException(UUID companyId, PermissionCode permission) {
        super("Permission " + permission + " denied for company " + companyId);
    }
}
