package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import java.util.Set;

import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record CompanyRoleRequest(@NotBlank String name, String description,
        @NotEmpty Set<PermissionCode> permissionCodes) {
}
