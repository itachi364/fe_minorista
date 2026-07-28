package com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto;

import java.util.Set;

import com.msvanegasg.facturaelectronica.identity.domain.model.RoleCode;

import jakarta.validation.constraints.NotEmpty;

public record MembershipRolesRequest(@NotEmpty Set<RoleCode> roles) {
}
