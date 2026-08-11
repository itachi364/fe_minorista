package com.msvanegasg.facturaelectronica.identity.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.msvanegasg.facturaelectronica.identity.application.dto.AssignCompanyRolesCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.AssignRolesCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.CreateCompanyRoleCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.CreateUserCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.LicensePolicy;
import com.msvanegasg.facturaelectronica.identity.application.dto.LoginCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.UpdateUserCommand;
import com.msvanegasg.facturaelectronica.identity.application.port.out.AccessAuditRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.CompanyMembershipRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.CompanyRoleRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.GlobalUserRoleRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.LicenseValidationPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.PasswordHasherPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.TokenGeneratorPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.TokenHashPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.UserAccountRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.UserSessionRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.domain.model.AccessAuditEvent;
import com.msvanegasg.facturaelectronica.identity.domain.model.CompanyMembership;
import com.msvanegasg.facturaelectronica.identity.domain.model.CompanyRole;
import com.msvanegasg.facturaelectronica.identity.domain.model.GlobalRoleCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionDescriptor;
import com.msvanegasg.facturaelectronica.identity.domain.model.RoleCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.UserAccount;
import com.msvanegasg.facturaelectronica.identity.domain.model.UserSession;

class IdentityManagementServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID SESSION_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MEMBERSHIP_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID COMPANY_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final Instant NOW = Instant.parse("2026-07-16T10:00:00Z");

    private InMemoryUsers users;
    private InMemoryMemberships memberships;
    private InMemorySessions sessions;
    private InMemoryAudit audit;
    private IdentityManagementService service;

    @BeforeEach
    void setUp() {
        users = new InMemoryUsers();
        memberships = new InMemoryMemberships();
        sessions = new InMemorySessions();
        audit = new InMemoryAudit();
        service = new IdentityManagementService(users, memberships, sessions, audit, new FixedPasswordHasher(),
                () -> "plain-token", token -> "hash-" + token, new SequentialIds(USER_ID, SESSION_ID, MEMBERSHIP_ID,
                        UUID.fromString("55555555-5555-5555-5555-555555555555"),
                        UUID.fromString("66666666-6666-6666-6666-666666666666"),
                        UUID.fromString("77777777-7777-7777-7777-777777777777"),
                        UUID.fromString("88888888-8888-8888-8888-888888888888"),
                        UUID.fromString("99999999-9999-9999-9999-999999999999"),
                        UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
                () -> NOW, Duration.ofHours(12));
    }

    @Test
    void createsUserWithHashedPasswordAndAudit() {
        var result = service.createUser(new CreateUserCommand("OWNER@EXAMPLE.COM", "Owner User", "secret123"));

        assertThat(result.id()).isEqualTo(USER_ID);
        assertThat(result.email()).isEqualTo("owner@example.com");
        assertThat(users.byEmail.get("owner@example.com").passwordHash()).isEqualTo("hashed-secret123");
        assertThat(audit.events).extracting(AccessAuditEvent::action).contains("CREATE_USER");
    }

    @Test
    void loginIssuesOpaqueTokenAndPersistsHashedSession() {
        service.createUser(new CreateUserCommand("owner@example.com", "Owner User", "secret123"));

        var result = service.login(new LoginCommand("owner@example.com", "secret123"));

        assertThat(result.accessToken()).isEqualTo("plain-token");
        assertThat(result.expiresAt()).isEqualTo(NOW.plus(Duration.ofHours(12)));
        assertThat(sessions.byHash).containsKey("hash-plain-token");
        assertThat(audit.events).extracting(AccessAuditEvent::action).contains("LOGIN");
    }

    @Test
    void bootstrapsFirstOwnerWithoutExistingMemberships() {
        service.createUser(new CreateUserCommand("owner@example.com", "Owner User", "secret123"));

        var membership = service.assignRoles(new AssignRolesCommand(COMPANY_ID, USER_ID, Set.of(RoleCode.OWNER), null));

        assertThat(membership.roles()).containsExactly(RoleCode.OWNER);
        assertThat(service.permissions(COMPANY_ID, USER_ID).permissions()).contains(PermissionCode.ROLES_MANAGE);
    }

    @Test
    void deniesRoleAssignmentWhenActorDoesNotHavePermission() {
        service.createUser(new CreateUserCommand("owner@example.com", "Owner User", "secret123"));
        service.assignRoles(new AssignRolesCommand(COMPANY_ID, USER_ID, Set.of(RoleCode.OWNER), null));
        UUID cashierId = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
        users.save(UserAccount.create(cashierId, "cashier@example.com", "Cashier", "hashed-secret123", NOW));
        memberships.save(CompanyMembership.create(UUID.fromString("88888888-8888-8888-8888-888888888888"), COMPANY_ID,
                cashierId, Set.of(RoleCode.CASHIER), NOW));
        var login = service.login(new LoginCommand("cashier@example.com", "secret123"));

        assertThatThrownBy(() -> service.assignRoles(new AssignRolesCommand(COMPANY_ID, USER_ID,
                Set.of(RoleCode.ADMIN), "Bearer " + login.accessToken())))
                .isInstanceOf(AccessDeniedException.class);
    }


    @Test
    void rootCanAssignInitialCompanyAdministratorWithoutCompanyLicenseOrMembership() {
        InMemoryGlobalRoles globalRoles = new InMemoryGlobalRoles();
        LicenseValidationPort failingLicenseValidation = (companyId, action) -> {
            throw new AssertionError("ROOT bootstrap must not require company license validation");
        };
        IdentityManagementService rootAwareService = new IdentityManagementService(users, memberships, sessions, audit,
                globalRoles, failingLicenseValidation, new FixedPasswordHasher(), () -> "root-token",
                token -> "hash-" + token, new SequentialIds(USER_ID, SESSION_ID, MEMBERSHIP_ID,
                        UUID.fromString("55555555-5555-5555-5555-555555555555"),
                        UUID.fromString("66666666-6666-6666-6666-666666666666"),
                        UUID.fromString("77777777-7777-7777-7777-777777777777"),
                        UUID.fromString("88888888-8888-8888-8888-888888888888"),
                        UUID.fromString("99999999-9999-9999-9999-999999999999"),
                        UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa")),
                () -> NOW, Duration.ofHours(12));

        var root = rootAwareService.createUser(new CreateUserCommand("root@example.com", "Root User", "secret123"));
        globalRoles.assignRole(root.id(), GlobalRoleCode.ROOT);
        var login = rootAwareService.login(new LoginCommand("root@example.com", "secret123"));
        var admin = rootAwareService.createUser(new CreateUserCommand("admin@example.com", "Admin User", "secret123"));

        var membership = rootAwareService.assignRoles(new AssignRolesCommand(COMPANY_ID, admin.id(),
                Set.of(RoleCode.OWNER), "Bearer " + login.accessToken()));

        assertThat(membership.companyId()).isEqualTo(COMPANY_ID);
        assertThat(membership.userId()).isEqualTo(admin.id());
        assertThat(membership.roles()).containsExactly(RoleCode.OWNER);
    }

    @Test
    void ownerCanCreateLowerCompanyRoleAndAssignIt() {
        InMemoryCompanyRoles companyRoles = new InMemoryCompanyRoles();
        IdentityManagementService rbacService = serviceWithCompanyRoles(companyRoles);
        var owner = rbacService.createUser(new CreateUserCommand("owner@example.com", "Owner User", "secret123"));
        rbacService.assignRoles(new AssignRolesCommand(COMPANY_ID, owner.id(), Set.of(RoleCode.OWNER), null));
        var login = rbacService.login(new LoginCommand("owner@example.com", "secret123"));
        var cashier = rbacService.createUser(new CreateUserCommand("cashier@example.com", "Cashier User", "secret123"));

        var role = rbacService.createCompanyRole(new CreateCompanyRoleCommand(COMPANY_ID, "Vendedor POS",
                "Puede vender", Set.of(PermissionCode.SALES_CREATE), "Bearer " + login.accessToken()));
        var access = rbacService.assignCompanyRoles(new AssignCompanyRolesCommand(COMPANY_ID, cashier.id(),
                Set.of(role.id()), "Bearer " + login.accessToken()));

        assertThat(access.permissions()).contains(PermissionCode.SALES_CREATE);
        assertThat(companyRoles.findActiveAssignedRoles(COMPANY_ID, cashier.id())).hasSize(1);
    }

    @Test
    void ownerCanActivateCompanyRoleAgain() {
        InMemoryCompanyRoles companyRoles = new InMemoryCompanyRoles();
        IdentityManagementService rbacService = serviceWithCompanyRoles(companyRoles);
        var owner = rbacService.createUser(new CreateUserCommand("owner@example.com", "Owner User", "secret123"));
        rbacService.assignRoles(new AssignRolesCommand(COMPANY_ID, owner.id(), Set.of(RoleCode.OWNER), null));
        var login = rbacService.login(new LoginCommand("owner@example.com", "secret123"));
        var role = rbacService.createCompanyRole(new CreateCompanyRoleCommand(COMPANY_ID, "Vendedor POS",
                "Puede vender", Set.of(PermissionCode.SALES_CREATE), "Bearer " + login.accessToken()));

        rbacService.deactivateCompanyRole(COMPANY_ID, role.id(), "Bearer " + login.accessToken());
        var activated = rbacService.activateCompanyRole(COMPANY_ID, role.id(), "Bearer " + login.accessToken());

        assertThat(activated.active()).isTrue();
        assertThat(audit.events).extracting(AccessAuditEvent::action).contains("ACTIVATE_ROLE");
    }

    @Test
    void ownerCanUpdateAndDeactivateCompanyUser() {
        InMemoryCompanyRoles companyRoles = new InMemoryCompanyRoles();
        IdentityManagementService rbacService = serviceWithCompanyRoles(companyRoles);
        var owner = rbacService.createUser(new CreateUserCommand("owner@example.com", "Owner User", "secret123"));
        rbacService.assignRoles(new AssignRolesCommand(COMPANY_ID, owner.id(), Set.of(RoleCode.OWNER), null));
        var login = rbacService.login(new LoginCommand("owner@example.com", "secret123"));
        var cashier = rbacService.createUser(new CreateUserCommand("cashier@example.com", "Cashier User", "secret123"));
        var role = rbacService.createCompanyRole(new CreateCompanyRoleCommand(COMPANY_ID, "Vendedor POS",
                "Puede vender", Set.of(PermissionCode.SALES_CREATE), "Bearer " + login.accessToken()));
        rbacService.assignCompanyRoles(new AssignCompanyRolesCommand(COMPANY_ID, cashier.id(),
                Set.of(role.id()), "Bearer " + login.accessToken()));

        var updated = rbacService.updateCompanyUser(new UpdateUserCommand(COMPANY_ID, cashier.id(),
                "seller@example.com", "Seller User", "Bearer " + login.accessToken()));
        var deactivated = rbacService.deactivateCompanyUser(COMPANY_ID, cashier.id(), "Bearer " + login.accessToken());

        assertThat(updated.email()).isEqualTo("seller@example.com");
        assertThat(updated.fullName()).isEqualTo("Seller User");
        assertThat(deactivated.status().name()).isEqualTo("INACTIVE");
        assertThatThrownBy(() -> rbacService.login(new LoginCommand("seller@example.com", "secret123")))
                .isInstanceOf(AuthenticationFailedException.class);
    }

    @Test
    void blocksNewCompanyAccessWhenLicenseUserLimitIsReached() {
        InMemoryCompanyRoles companyRoles = new InMemoryCompanyRoles();
        LicenseValidationPort limitedLicense = new LicenseValidationPort() {
            @Override
            public void ensureAllowed(UUID companyId,
                    com.msvanegasg.facturaelectronica.identity.domain.model.LicenseAction action) {
            }

            @Override
            public LicensePolicy policy(UUID companyId,
                    com.msvanegasg.facturaelectronica.identity.domain.model.LicenseAction action) {
                return new LicensePolicy(1, null);
            }
        };
        IdentityManagementService rbacService = serviceWithCompanyRoles(companyRoles, limitedLicense);
        var owner = rbacService.createUser(new CreateUserCommand("owner@example.com", "Owner User", "secret123"));
        rbacService.assignRoles(new AssignRolesCommand(COMPANY_ID, owner.id(), Set.of(RoleCode.OWNER), null));
        users.companyUserCount = 1;
        var login = rbacService.login(new LoginCommand("owner@example.com", "secret123"));
        var cashier = rbacService.createUser(new CreateUserCommand("cashier@example.com", "Cashier User", "secret123"));
        var role = rbacService.createCompanyRole(new CreateCompanyRoleCommand(COMPANY_ID, "Vendedor POS",
                "Puede vender", Set.of(PermissionCode.SALES_CREATE), "Bearer " + login.accessToken()));

        assertThatThrownBy(() -> rbacService.assignCompanyRoles(new AssignCompanyRolesCommand(COMPANY_ID,
                cashier.id(), Set.of(role.id()), "Bearer " + login.accessToken())))
                .isInstanceOf(LicenseBlockedException.class)
                .hasMessageContaining("maximo 1 usuarios");
    }

    @Test
    void deniesCompanyRoleCreationWhenPermissionsAreEqualToActor() {
        InMemoryCompanyRoles companyRoles = new InMemoryCompanyRoles();
        IdentityManagementService rbacService = serviceWithCompanyRoles(companyRoles);
        var owner = rbacService.createUser(new CreateUserCommand("owner@example.com", "Owner User", "secret123"));
        rbacService.assignRoles(new AssignRolesCommand(COMPANY_ID, owner.id(), Set.of(RoleCode.OWNER), null));
        var login = rbacService.login(new LoginCommand("owner@example.com", "secret123"));

        assertThatThrownBy(() -> rbacService.createCompanyRole(new CreateCompanyRoleCommand(COMPANY_ID,
                "Otro administrador", "Permisos iguales", RoleCode.OWNER.permissions(),
                "Bearer " + login.accessToken())))
                .isInstanceOf(AccessDeniedException.class);
    }
    private IdentityManagementService serviceWithCompanyRoles(InMemoryCompanyRoles companyRoles) {
        return serviceWithCompanyRoles(companyRoles, (companyId, action) -> { });
    }

    private IdentityManagementService serviceWithCompanyRoles(InMemoryCompanyRoles companyRoles,
            LicenseValidationPort licenseValidationPort) {
        return new IdentityManagementService(users, memberships, companyRoles, sessions, audit, new InMemoryGlobalRoles(),
                licenseValidationPort, new FixedPasswordHasher(), () -> "plain-token", token -> "hash-" + token,
                UUID::randomUUID, () -> NOW, Duration.ofHours(12));
    }

    private static final class InMemoryUsers implements UserAccountRepositoryPort {
        private final Map<UUID, UserAccount> byId = new HashMap<>();
        private final Map<String, UserAccount> byEmail = new HashMap<>();
        private long companyUserCount;

        @Override
        public UserAccount save(UserAccount user) {
            byId.put(user.id(), user);
            byEmail.put(user.email(), user);
            return user;
        }

        @Override
        public Optional<UserAccount> findById(UUID id) { return Optional.ofNullable(byId.get(id)); }
        @Override
        public Optional<UserAccount> findByEmail(String email) { return Optional.ofNullable(byEmail.get(email)); }
        @Override
        public List<UserAccount> findByCompanyIdAndEmailContaining(UUID companyId, String email) {
            String normalizedEmail = email == null ? "" : email.toLowerCase();
            return byId.values().stream()
                    .filter(user -> user.email().contains(normalizedEmail))
                    .toList();
        }
        @Override
        public long countByCompanyId(UUID companyId) { return companyUserCount; }
        @Override
        public boolean existsByEmail(String email) { return byEmail.containsKey(email); }

        @Override
        public boolean existsByEmailAndIdNot(String email, UUID id) {
            return Optional.ofNullable(byEmail.get(email)).map(user -> !user.id().equals(id)).orElse(false);
        }
    }

    private static final class InMemoryMemberships implements CompanyMembershipRepositoryPort {
        private final Map<UUID, CompanyMembership> byId = new HashMap<>();

        @Override
        public CompanyMembership save(CompanyMembership membership) {
            byId.put(membership.id(), membership);
            return membership;
        }

        @Override
        public Optional<CompanyMembership> findByIdAndCompanyId(UUID membershipId, UUID companyId) {
            return Optional.ofNullable(byId.get(membershipId)).filter(m -> m.companyId().equals(companyId));
        }

        @Override
        public Optional<CompanyMembership> findByCompanyIdAndUserId(UUID companyId, UUID userId) {
            return byId.values().stream().filter(m -> m.companyId().equals(companyId) && m.userId().equals(userId))
                    .findFirst();
        }

        @Override
        public List<CompanyMembership> findByUserId(UUID userId) {
            return byId.values().stream().filter(m -> m.userId().equals(userId)).toList();
        }

        @Override
        public boolean existsByCompanyId(UUID companyId) {
            return byId.values().stream().anyMatch(m -> m.companyId().equals(companyId));
        }
    }



    private static final class InMemoryCompanyRoles implements CompanyRoleRepositoryPort {
        private final Map<UUID, CompanyRole> roles = new HashMap<>();
        private final Map<UUID, Map<UUID, Set<UUID>>> assignments = new HashMap<>();

        @Override
        public List<PermissionDescriptor> listActivePermissions() {
            return java.util.Arrays.stream(PermissionCode.values()).map(PermissionDescriptor::from).toList();
        }

        @Override
        public CompanyRole save(CompanyRole role) {
            roles.put(role.id(), role);
            return role;
        }

        @Override
        public Optional<CompanyRole> findByIdAndCompanyId(UUID roleId, UUID companyId) {
            return Optional.ofNullable(roles.get(roleId)).filter(role -> role.companyId().equals(companyId));
        }

        @Override
        public List<CompanyRole> findByCompanyId(UUID companyId) {
            return roles.values().stream().filter(role -> role.companyId().equals(companyId)).toList();
        }

        @Override
        public List<CompanyRole> findActiveAssignedRoles(UUID companyId, UUID userId) {
            return assignments.getOrDefault(companyId, Map.of()).getOrDefault(userId, Set.of()).stream()
                    .map(roles::get)
                    .filter(java.util.Objects::nonNull)
                    .filter(CompanyRole::active)
                    .toList();
        }

        @Override
        public List<UUID> findAssignedCompanyIds(UUID userId) {
            return assignments.entrySet().stream()
                    .filter(entry -> entry.getValue().containsKey(userId))
                    .map(Map.Entry::getKey)
                    .toList();
        }

        @Override
        public void replaceUserRoleAssignments(UUID companyId, UUID userId, Set<UUID> roleIds, UUID assignedBy,
                Instant assignedAt) {
            assignments.computeIfAbsent(companyId, ignored -> new HashMap<>()).put(userId, Set.copyOf(roleIds));
        }

        @Override
        public void revokeUserRoleAssignment(UUID companyId, UUID userId, UUID roleId, UUID revokedBy,
                Instant revokedAt) {
            Set<UUID> roleIds = new java.util.HashSet<>(assignments.getOrDefault(companyId, Map.of())
                    .getOrDefault(userId, Set.of()));
            roleIds.remove(roleId);
            assignments.computeIfAbsent(companyId, ignored -> new HashMap<>()).put(userId, roleIds);
        }
    }
    private static final class InMemoryGlobalRoles implements GlobalUserRoleRepositoryPort {
        private final Map<UUID, Set<GlobalRoleCode>> roles = new HashMap<>();

        @Override
        public Set<GlobalRoleCode> findByUserId(UUID userId) {
            return roles.getOrDefault(userId, Set.of());
        }

        @Override
        public boolean hasRole(UUID userId, GlobalRoleCode roleCode) {
            return findByUserId(userId).contains(roleCode);
        }

        @Override
        public void assignRole(UUID userId, GlobalRoleCode roleCode) {
            roles.put(userId, Set.of(roleCode));
        }
    }
    private static final class InMemorySessions implements UserSessionRepositoryPort {
        private final Map<String, UserSession> byHash = new HashMap<>();
        @Override
        public UserSession save(UserSession session) { byHash.put(session.tokenHash(), session); return session; }
        @Override
        public Optional<UserSession> findByTokenHash(String tokenHash) { return Optional.ofNullable(byHash.get(tokenHash)); }
    }

    private static final class InMemoryAudit implements AccessAuditRepositoryPort {
        private final List<AccessAuditEvent> events = new ArrayList<>();
        @Override
        public AccessAuditEvent save(AccessAuditEvent event) { events.add(event); return event; }
    }

    private static final class FixedPasswordHasher implements PasswordHasherPort {
        @Override
        public String hash(String rawPassword) { return "hashed-" + rawPassword; }
        @Override
        public boolean matches(String rawPassword, String encodedPassword) { return encodedPassword.equals(hash(rawPassword)); }
    }

    private static final class SequentialIds implements IdGeneratorPort {
        private final ArrayDeque<UUID> ids;
        private SequentialIds(UUID... ids) { this.ids = new ArrayDeque<>(List.of(ids)); }
        @Override
        public UUID nextId() { return ids.removeFirst(); }
    }
}
