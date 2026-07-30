package com.msvanegasg.facturaelectronica.identity.interfaces.rest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.identity.application.dto.CompanyAccessResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.LoginResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.MembershipResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.UserResult;
import com.msvanegasg.facturaelectronica.identity.application.port.in.ManageIdentityUseCase;
import com.msvanegasg.facturaelectronica.identity.application.usecase.AuthenticationFailedException;
import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.RoleCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.UserStatus;
import com.msvanegasg.facturaelectronica.identity.exception.IdentityExceptionHandler;
import com.msvanegasg.facturaelectronica.identity.observability.CorrelationId;
import com.msvanegasg.facturaelectronica.identity.observability.CorrelationIdFilter;

@ExtendWith(MockitoExtension.class)
class IdentityControllerTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID COMPANY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MEMBERSHIP_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final Instant NOW = Instant.parse("2026-07-16T10:00:00Z");

    private MockMvc mockMvc;

    @Mock
    private ManageIdentityUseCase manageIdentityUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new IdentityController(manageIdentityUseCase))
                .setControllerAdvice(new IdentityExceptionHandler())
                .addFilters(new CorrelationIdFilter())
                .build();
    }

    @Test
    void createsUserAndLogsIn() throws Exception {
        org.mockito.Mockito.when(manageIdentityUseCase.createUser(any())).thenReturn(user());
        org.mockito.Mockito.when(manageIdentityUseCase.login(any()))
                .thenReturn(new LoginResult(USER_ID, "owner@example.com", "Owner User", "token", NOW.plusSeconds(3600),
                        Set.of()));

        mockMvc.perform(post("/api/v1/users")
                .header(CorrelationId.HEADER_NAME, "corr-id")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string(CorrelationId.HEADER_NAME, "corr-id"))
                .andExpect(jsonPath("$.email").value("owner@example.com"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.accessToken").value("token"))
                .andExpect(jsonPath("$.globalRoles").isArray());
    }

    @Test
    void returnsCurrentUserAndCompanies() throws Exception {
        org.mockito.Mockito.when(manageIdentityUseCase.currentUser(eq("Bearer token"))).thenReturn(user());
        org.mockito.Mockito.when(manageIdentityUseCase.currentCompanies(eq("Bearer token")))
                .thenReturn(List.of(access()));

        mockMvc.perform(get("/api/v1/me").header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(USER_ID.toString()));

        mockMvc.perform(get("/api/v1/me/companies").header(HttpHeaders.AUTHORIZATION, "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].companyId").value(COMPANY_ID.toString()))
                .andExpect(jsonPath("$[0].permissions[0]").exists());
    }

    @Test
    void assignsRolesAndReturnsPermissions() throws Exception {
        org.mockito.Mockito.when(manageIdentityUseCase.assignRoles(any())).thenReturn(membership());
        org.mockito.Mockito.when(manageIdentityUseCase.permissions(eq(COMPANY_ID), eq(USER_ID))).thenReturn(access());

        mockMvc.perform(post("/api/v1/companies/{companyId}/memberships", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(membershipJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.roles[0]").value("OWNER"));

        mockMvc.perform(get("/api/v1/companies/{companyId}/permissions", COMPANY_ID).param("userId", USER_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyId").value(COMPANY_ID.toString()));
    }

    @Test
    void updatesMembershipRoles() throws Exception {
        org.mockito.Mockito.when(manageIdentityUseCase.updateMembershipRoles(any())).thenReturn(membership());

        mockMvc.perform(put("/api/v1/companies/{companyId}/memberships/{membershipId}/roles", COMPANY_ID,
                MEMBERSHIP_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"roles\":[\"ADMIN\"]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(MEMBERSHIP_ID.toString()));
    }

    @Test
    void returnsUnauthorizedForInvalidSession() throws Exception {
        org.mockito.Mockito.when(manageIdentityUseCase.currentUser(any())).thenThrow(new AuthenticationFailedException());

        mockMvc.perform(get("/api/v1/me").header(HttpHeaders.AUTHORIZATION, "Bearer bad"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
    }

    private static UserResult user() {
        return new UserResult(USER_ID, "owner@example.com", "Owner User", UserStatus.ACTIVE, NOW, NOW);
    }

    private static MembershipResult membership() {
        return new MembershipResult(MEMBERSHIP_ID, COMPANY_ID, USER_ID, Set.of(RoleCode.OWNER), true, NOW, NOW);
    }

    private static CompanyAccessResult access() {
        return new CompanyAccessResult(COMPANY_ID, Set.of(RoleCode.OWNER), Set.of(PermissionCode.ROLES_MANAGE));
    }

    private static String userJson() {
        return """
                {
                  "email": "owner@example.com",
                  "fullName": "Owner User",
                  "password": "secret123"
                }
                """;
    }

    private static String loginJson() {
        return """
                {
                  "email": "owner@example.com",
                  "password": "secret123"
                }
                """;
    }

    private static String membershipJson() {
        return """
                {
                  "userId": "11111111-1111-1111-1111-111111111111",
                  "roles": ["OWNER"]
                }
                """;
    }
}
