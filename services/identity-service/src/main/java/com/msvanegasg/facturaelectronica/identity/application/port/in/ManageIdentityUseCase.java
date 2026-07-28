package com.msvanegasg.facturaelectronica.identity.application.port.in;

import java.util.List;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.application.dto.AssignRolesCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.CompanyAccessResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.CreateUserCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.LoginCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.LoginResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.MembershipResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.UpdateMembershipRolesCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.UserResult;

public interface ManageIdentityUseCase {

    UserResult createUser(CreateUserCommand command);

    LoginResult login(LoginCommand command);

    UserResult currentUser(String authorizationHeader);

    List<CompanyAccessResult> currentCompanies(String authorizationHeader);

    MembershipResult assignRoles(AssignRolesCommand command);

    MembershipResult updateMembershipRoles(UpdateMembershipRolesCommand command);

    CompanyAccessResult permissions(UUID companyId, UUID userId);
}
