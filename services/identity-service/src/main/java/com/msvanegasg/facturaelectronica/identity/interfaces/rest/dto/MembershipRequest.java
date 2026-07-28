package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.domain.model.RoleCode;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record MembershipRequest(@NotNull UUID userId, @NotEmpty Set<RoleCode> roles) {
}
