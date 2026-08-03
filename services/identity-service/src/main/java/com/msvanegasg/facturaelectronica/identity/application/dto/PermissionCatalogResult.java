package com.msvanegasg.facturaelectronica.identity.application.dto;

import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionDescriptor;
import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionScope;

public record PermissionCatalogResult(PermissionCode code, PermissionScope scope, String module, String description,
        boolean active) {

    public static PermissionCatalogResult from(PermissionDescriptor descriptor) {
        return new PermissionCatalogResult(descriptor.code(), descriptor.scope(), descriptor.module(),
                descriptor.description(), descriptor.active());
    }
}
