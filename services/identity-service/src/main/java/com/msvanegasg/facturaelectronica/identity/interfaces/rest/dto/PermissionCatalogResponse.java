package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionScope;

public record PermissionCatalogResponse(PermissionCode code, PermissionScope scope, String module, String description,
        boolean active) {
}
