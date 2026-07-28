package com.msvanegasg.facturaelectronica.identity.interfaces.rest;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.identity.application.port.in.ManageIdentityUseCase;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.CompanyAccessResponse;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.CreateUserRequest;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.LoginRequest;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.LoginResponse;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.MembershipRequest;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.MembershipResponse;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.MembershipRolesRequest;
import com.msvanegasg.facturaelectronica.identity.interfaces.rest.dto.UserResponse;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1")
public class IdentityController {

    private final ManageIdentityUseCase manageIdentityUseCase;

    public IdentityController(ManageIdentityUseCase manageIdentityUseCase) {
        this.manageIdentityUseCase = manageIdentityUseCase;
    }

    @PostMapping("/users")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = IdentityRestMapper.toResponse(
                manageIdentityUseCase.createUser(IdentityRestMapper.toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/auth/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return IdentityRestMapper.toResponse(manageIdentityUseCase.login(IdentityRestMapper.toCommand(request)));
    }

    @GetMapping("/me")
    public UserResponse me(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return IdentityRestMapper.toResponse(manageIdentityUseCase.currentUser(authorizationHeader));
    }

    @GetMapping("/me/companies")
    public List<CompanyAccessResponse> myCompanies(@RequestHeader(HttpHeaders.AUTHORIZATION) String authorizationHeader) {
        return manageIdentityUseCase.currentCompanies(authorizationHeader).stream()
                .map(IdentityRestMapper::toResponse)
                .toList();
    }

    @PostMapping("/companies/{companyId}/memberships")
    public ResponseEntity<MembershipResponse> assignMembership(@PathVariable UUID companyId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody MembershipRequest request) {
        MembershipResponse response = IdentityRestMapper.toResponse(manageIdentityUseCase.assignRoles(
                IdentityRestMapper.toCommand(companyId, request, authorizationHeader)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/companies/{companyId}/users/{userId}/roles")
    public ResponseEntity<MembershipResponse> assignUserRoles(@PathVariable UUID companyId, @PathVariable UUID userId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody MembershipRolesRequest request) {
        MembershipResponse response = IdentityRestMapper.toResponse(manageIdentityUseCase.assignRoles(
                new com.msvanegasg.facturaelectronica.identity.application.dto.AssignRolesCommand(companyId, userId,
                        request.roles(), authorizationHeader)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/companies/{companyId}/memberships/{membershipId}/roles")
    public MembershipResponse updateMembershipRoles(@PathVariable UUID companyId, @PathVariable UUID membershipId,
            @RequestHeader(value = HttpHeaders.AUTHORIZATION, required = false) String authorizationHeader,
            @Valid @RequestBody MembershipRolesRequest request) {
        return IdentityRestMapper.toResponse(manageIdentityUseCase.updateMembershipRoles(
                IdentityRestMapper.toCommand(companyId, membershipId, request, authorizationHeader)));
    }

    @GetMapping("/companies/{companyId}/permissions")
    public CompanyAccessResponse permissions(@PathVariable UUID companyId, @RequestParam UUID userId) {
        return IdentityRestMapper.toResponse(manageIdentityUseCase.permissions(companyId, userId));
    }
}
