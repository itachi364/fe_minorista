package com.msvanegasg.facturaelectronica.identity.interfaces.rest;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.application.dto.AssignRolesCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.CompanyAccessResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.CreateUserCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.LoginCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.LoginResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.MembershipResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.UpdateMembershipRolesCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.UserResult;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.CompanyAccessResponse;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.CreateUserRequest;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.LoginRequest;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.LoginResponse;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.MembershipRequest;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.MembershipResponse;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.MembershipRolesRequest;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.UserResponse;

public final class IdentityRestMapper {

    private IdentityRestMapper() {
    }

    public static CreateUserCommand toCommand(CreateUserRequest request) {
        return new CreateUserCommand(request.email(), request.fullName(), request.password());
    }

    public static LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(request.email(), request.password());
    }

    public static AssignRolesCommand toCommand(UUID companyId, MembershipRequest request, String authorizationHeader) {
        return new AssignRolesCommand(companyId, request.userId(), request.roles(), authorizationHeader);
    }

    public static UpdateMembershipRolesCommand toCommand(UUID companyId, UUID membershipId,
            MembershipRolesRequest request, String authorizationHeader) {
        return new UpdateMembershipRolesCommand(companyId, membershipId, request.roles(), authorizationHeader);
    }

    public static UserResponse toResponse(UserResult result) {
        return new UserResponse(result.id(), result.email(), result.fullName(), result.status(), result.createdAt(),
                result.updatedAt());
    }

    public static LoginResponse toResponse(LoginResult result) {
        return new LoginResponse(result.userId(), result.email(), result.fullName(), "Bearer", result.accessToken(),
                result.expiresAt());
    }

    public static MembershipResponse toResponse(MembershipResult result) {
        return new MembershipResponse(result.id(), result.companyId(), result.userId(), result.roles(), result.active(),
                result.createdAt(), result.updatedAt());
    }

    public static CompanyAccessResponse toResponse(CompanyAccessResult result) {
        return new CompanyAccessResponse(result.companyId(), result.roles(), result.permissions());
    }
}
