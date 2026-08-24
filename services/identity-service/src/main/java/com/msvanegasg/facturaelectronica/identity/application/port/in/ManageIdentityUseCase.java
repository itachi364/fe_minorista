package com.msvanegasg.facturaelectronica.identity.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.application.dto.AssignCompanyRolesCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.AssignRolesCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.CompanyAccessResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.CompanyRoleResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.CognitoSessionCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.CreateCompanyRoleCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.CreateUserCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.LoginCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.LoginResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.MembershipResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.PermissionCatalogResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.RevokeCompanyRoleCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.UpdateCompanyRoleCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.UpdateMembershipRolesCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.UpdateUserCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.UserResult;

public interface ManageIdentityUseCase {

    UserResult createUser(CreateUserCommand command);

    LoginResult login(LoginCommand command);

    LoginResult issueCognitoSession(CognitoSessionCommand command);

    void logout(String authorizationHeader);

    UserResult currentUser(String authorizationHeader);

    List<CompanyAccessResult> currentCompanies(String authorizationHeader);

    MembershipResult assignRoles(AssignRolesCommand command);

    MembershipResult updateMembershipRoles(UpdateMembershipRolesCommand command);

    CompanyAccessResult permissions(UUID companyId, UUID userId);

    List<PermissionCatalogResult> listPermissionCatalog(String authorizationHeader);

    List<UserResult> listCompanyUsers(UUID companyId, String email, String authorizationHeader);

    List<CompanyRoleResult> listCompanyRoles(UUID companyId, String authorizationHeader);

    CompanyRoleResult getCompanyRole(UUID companyId, UUID roleId, String authorizationHeader);

    CompanyRoleResult createCompanyRole(CreateCompanyRoleCommand command);

    CompanyRoleResult updateCompanyRole(UpdateCompanyRoleCommand command);

    CompanyRoleResult deactivateCompanyRole(UUID companyId, UUID roleId, String authorizationHeader);

    CompanyRoleResult activateCompanyRole(UUID companyId, UUID roleId, String authorizationHeader);

    UserResult updateCompanyUser(UpdateUserCommand command);

    UserResult activateCompanyUser(UUID companyId, UUID userId, String authorizationHeader);

    UserResult deactivateCompanyUser(UUID companyId, UUID userId, String authorizationHeader);

    CompanyAccessResult assignCompanyRoles(AssignCompanyRolesCommand command);

    CompanyAccessResult revokeCompanyRole(RevokeCompanyRoleCommand command);

    CompanyAccessResult effectivePermissions(UUID companyId, UUID userId, String authorizationHeader);
}
