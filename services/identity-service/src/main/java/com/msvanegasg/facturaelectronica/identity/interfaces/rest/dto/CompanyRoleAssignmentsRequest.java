package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import java.util.Set;
import java.util.UUID;

import jakarta.validation.constraints.NotEmpty;

public record CompanyRoleAssignmentsRequest(@NotEmpty Set<UUID> roleIds) {
}
