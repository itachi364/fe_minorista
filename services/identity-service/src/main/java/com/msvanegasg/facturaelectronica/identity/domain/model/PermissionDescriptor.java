package com.msvanegasg.facturaelectronica.identity.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public record PermissionDescriptor(
        PermissionCode code,
        PermissionScope scope,
        String module,
        String description,
        boolean active) {

    public PermissionDescriptor {
        Objects.requireNonNull(code, "code is required");
        Objects.requireNonNull(scope, "scope is required");
        Objects.requireNonNull(module, "module is required");
        Objects.requireNonNull(description, "description is required");
    }

    public static PermissionDescriptor from(PermissionCode code) {
        return new PermissionDescriptor(code, code.scope(), code.module(), code.description(), true);
    }
}
