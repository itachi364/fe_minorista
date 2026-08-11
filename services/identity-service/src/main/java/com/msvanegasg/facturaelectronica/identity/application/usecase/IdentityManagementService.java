package com.msvanegasg.facturaelectronica.identity.application.usecase;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.msvanegasg.facturaelectronica.identity.application.dto.AssignCompanyRolesCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.AssignRolesCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.CompanyAccessResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.CompanyRoleResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.CreateCompanyRoleCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.CreateUserCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.LoginCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.LoginResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.LicensePolicy;
import com.msvanegasg.facturaelectronica.identity.application.dto.MembershipResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.PermissionCatalogResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.RevokeCompanyRoleCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.UpdateCompanyRoleCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.UpdateMembershipRolesCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.UpdateUserCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.UserResult;
import com.msvanegasg.facturaelectronica.identity.application.port.in.ManageIdentityUseCase;
import com.msvanegasg.facturaelectronica.identity.application.port.out.AccessAuditRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.CompanyMembershipRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.CompanyRoleRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.GlobalUserRoleRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.LicenseValidationPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.PasswordHasherPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.TokenGeneratorPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.TokenHashPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.UserAccountRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.UserSessionRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.domain.model.AccessAuditEvent;
import com.msvanegasg.facturaelectronica.identity.domain.model.AccessAuditResult;
import com.msvanegasg.facturaelectronica.identity.domain.model.CompanyMembership;
import com.msvanegasg.facturaelectronica.identity.domain.model.CompanyRole;
import com.msvanegasg.facturaelectronica.identity.domain.model.GlobalRoleCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.LicenseAction;
import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionDescriptor;
import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionScope;
import com.msvanegasg.facturaelectronica.identity.domain.model.RoleCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.UserAccount;
import com.msvanegasg.facturaelectronica.identity.domain.model.UserSession;

public class IdentityManagementService implements ManageIdentityUseCase {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserAccountRepositoryPort userRepository;
    private final CompanyMembershipRepositoryPort membershipRepository;
    private final CompanyRoleRepositoryPort companyRoleRepository;
    private final UserSessionRepositoryPort sessionRepository;
    private final AccessAuditRepositoryPort auditRepository;
    private final GlobalUserRoleRepositoryPort globalRoleRepository;
    private final LicenseValidationPort licenseValidationPort;
    private final PasswordHasherPort passwordHasher;
    private final TokenGeneratorPort tokenGenerator;
    private final TokenHashPort tokenHash;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;
    private final Duration sessionDuration;

    public IdentityManagementService(UserAccountRepositoryPort userRepository,
            CompanyMembershipRepositoryPort membershipRepository,
            UserSessionRepositoryPort sessionRepository,
            AccessAuditRepositoryPort auditRepository,
            PasswordHasherPort passwordHasher,
            TokenGeneratorPort tokenGenerator,
            TokenHashPort tokenHash,
            IdGeneratorPort idGenerator,
            ClockPort clock,
            Duration sessionDuration) {
        this(userRepository, membershipRepository, new NoopCompanyRoleRepository(), sessionRepository, auditRepository,
                new NoopGlobalUserRoleRepository(), (companyId, action) -> {
                }, passwordHasher, tokenGenerator, tokenHash, idGenerator, clock, sessionDuration);
    }

    public IdentityManagementService(UserAccountRepositoryPort userRepository,
            CompanyMembershipRepositoryPort membershipRepository,
            UserSessionRepositoryPort sessionRepository,
            AccessAuditRepositoryPort auditRepository,
            GlobalUserRoleRepositoryPort globalRoleRepository,
            LicenseValidationPort licenseValidationPort,
            PasswordHasherPort passwordHasher,
            TokenGeneratorPort tokenGenerator,
            TokenHashPort tokenHash,
            IdGeneratorPort idGenerator,
            ClockPort clock,
            Duration sessionDuration) {
        this(userRepository, membershipRepository, new NoopCompanyRoleRepository(), sessionRepository, auditRepository,
                globalRoleRepository, licenseValidationPort, passwordHasher, tokenGenerator, tokenHash, idGenerator,
                clock, sessionDuration);
    }

    public IdentityManagementService(UserAccountRepositoryPort userRepository,
            CompanyMembershipRepositoryPort membershipRepository,
            CompanyRoleRepositoryPort companyRoleRepository,
            UserSessionRepositoryPort sessionRepository,
            AccessAuditRepositoryPort auditRepository,
            GlobalUserRoleRepositoryPort globalRoleRepository,
            LicenseValidationPort licenseValidationPort,
            PasswordHasherPort passwordHasher,
            TokenGeneratorPort tokenGenerator,
            TokenHashPort tokenHash,
            IdGeneratorPort idGenerator,
            ClockPort clock,
            Duration sessionDuration) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.membershipRepository = Objects.requireNonNull(membershipRepository);
        this.companyRoleRepository = Objects.requireNonNull(companyRoleRepository);
        this.sessionRepository = Objects.requireNonNull(sessionRepository);
        this.auditRepository = Objects.requireNonNull(auditRepository);
        this.globalRoleRepository = Objects.requireNonNull(globalRoleRepository);
        this.licenseValidationPort = Objects.requireNonNull(licenseValidationPort);
        this.passwordHasher = Objects.requireNonNull(passwordHasher);
        this.tokenGenerator = Objects.requireNonNull(tokenGenerator);
        this.tokenHash = Objects.requireNonNull(tokenHash);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
        this.sessionDuration = Objects.requireNonNull(sessionDuration);
    }

    @Override
    public UserResult createUser(CreateUserCommand command) {
        Objects.requireNonNull(command, "command is required");
        String email = normalizeEmail(command.email());
        validatePassword(command.password());
        if (userRepository.existsByEmail(email)) {
            audit(null, null, "CREATE_USER", "USER", email, AccessAuditResult.FAILURE, "DUPLICATE_EMAIL");
            throw new UserAlreadyExistsException(email);
        }
        UserAccount user = UserAccount.create(idGenerator.nextId(), email, required(command.fullName(), "fullName"),
                passwordHasher.hash(command.password()), clock.now());
        UserAccount saved = userRepository.save(user);
        audit(null, saved.id(), "CREATE_USER", "USER", saved.id().toString(), AccessAuditResult.SUCCESS, null);
        return UserResult.from(saved);
    }

    @Override
    public LoginResult login(LoginCommand command) {
        Objects.requireNonNull(command, "command is required");
        String email = normalizeEmail(command.email());
        UserAccount user = userRepository.findByEmail(email).orElse(null);
        if (user == null || !user.isActive() || !passwordHasher.matches(command.password(), user.passwordHash())) {
            audit(null, user == null ? null : user.id(), "LOGIN", "USER", email, AccessAuditResult.FAILURE,
                    "INVALID_CREDENTIALS");
            throw new AuthenticationFailedException();
        }
        String rawToken = tokenGenerator.generate();
        Instant now = clock.now();
        Instant expiresAt = now.plus(sessionDuration);
        sessionRepository.save(UserSession.create(idGenerator.nextId(), user.id(), tokenHash.hash(rawToken), expiresAt,
                now));
        audit(null, user.id(), "LOGIN", "USER", user.id().toString(), AccessAuditResult.SUCCESS, null);
        return new LoginResult(user.id(), user.email(), user.fullName(), rawToken, expiresAt,
                globalRoleRepository.findByUserId(user.id()));
    }

    @Override
    public UserResult currentUser(String authorizationHeader) {
        return UserResult.from(authenticate(authorizationHeader));
    }

    @Override
    public List<CompanyAccessResult> currentCompanies(String authorizationHeader) {
        UserAccount user = authenticate(authorizationHeader);
        java.util.Map<UUID, CompanyAccessResult> accesses = new java.util.LinkedHashMap<>();
        membershipRepository.findByUserId(user.id()).stream()
                .filter(CompanyMembership::active)
                .forEach(membership -> accesses.put(membership.companyId(),
                        toCompanyAccess(membership, effectivePermissions(membership.companyId(), user.id()))));
        companyRoleRepository.findAssignedCompanyIds(user.id()).forEach(companyId -> accesses.putIfAbsent(companyId,
                new CompanyAccessResult(companyId, Set.of(), effectivePermissions(companyId, user.id()))));
        return List.copyOf(accesses.values());
    }

    @Override
    public MembershipResult assignRoles(AssignRolesCommand command) {
        Objects.requireNonNull(command, "command is required");
        validateRoles(command.roles());
        UserAccount target = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        UserAccount actor = authenticateIfPresent(command.authorizationHeader());
        boolean rootActor = isRoot(actor);
        LicensePolicy licensePolicy = LicensePolicy.unlimited();
        if (!rootActor) {
            licensePolicy = licenseValidationPort.policy(command.companyId(), LicenseAction.CREATE_USER);
        }
        ensureCanManageLegacyRoles(command.companyId(), command.authorizationHeader(), command.roles(), actor, rootActor);
        ensureUserQuotaAvailable(command.companyId(), target.id(), licensePolicy, "ASSIGN_ROLES");
        CompanyMembership membership = membershipRepository.findByCompanyIdAndUserId(command.companyId(), target.id())
                .map(existing -> existing.replaceRoles(command.roles(), clock.now()))
                .orElseGet(() -> CompanyMembership.create(idGenerator.nextId(), command.companyId(), target.id(),
                        command.roles(), clock.now()));
        CompanyMembership saved = membershipRepository.save(membership);
        audit(command.companyId(), target.id(), "ASSIGN_ROLES", "MEMBERSHIP", saved.id().toString(),
                AccessAuditResult.SUCCESS, saved.roles().toString());
        return MembershipResult.from(saved);
    }

    @Override
    public MembershipResult updateMembershipRoles(UpdateMembershipRolesCommand command) {
        Objects.requireNonNull(command, "command is required");
        validateRoles(command.roles());
        UserAccount actor = authenticateIfPresent(command.authorizationHeader());
        boolean rootActor = isRoot(actor);
        ensureCanManageLegacyRoles(command.companyId(), command.authorizationHeader(), command.roles(), actor, rootActor);
        CompanyMembership membership = membershipRepository.findByIdAndCompanyId(command.membershipId(),
                command.companyId()).orElseThrow(() -> new MembershipNotFoundException(command.membershipId()));
        CompanyMembership saved = membershipRepository.save(membership.replaceRoles(command.roles(), clock.now()));
        audit(command.companyId(), saved.userId(), "UPDATE_ROLES", "MEMBERSHIP", saved.id().toString(),
                AccessAuditResult.SUCCESS, saved.roles().toString());
        return MembershipResult.from(saved);
    }

    @Override
    public CompanyAccessResult permissions(UUID companyId, UUID userId) {
        Set<PermissionCode> permissions = effectivePermissions(companyId, userId);
        return membershipRepository.findByCompanyIdAndUserId(companyId, userId)
                .map(membership -> toCompanyAccess(membership, permissions))
                .orElseGet(() -> {
                    if (permissions.isEmpty()) {
                        throw new MembershipNotFoundException(userId);
                    }
                    return new CompanyAccessResult(companyId, Set.of(), permissions);
                });
    }

    @Override
    public List<PermissionCatalogResult> listPermissionCatalog(String authorizationHeader) {
        UserAccount actor = authenticate(authorizationHeader);
        boolean rootActor = isRoot(actor);
        return companyRoleRepository.listActivePermissions().stream()
                .filter(permission -> rootActor || permission.scope() == PermissionScope.COMPANY)
                .map(PermissionCatalogResult::from)
                .toList();
    }

    @Override
    public List<UserResult> listCompanyUsers(UUID companyId, String email, String authorizationHeader) {
        UserAccount actor = authenticate(authorizationHeader);
        if (!isRoot(actor)) {
            ensureCanListCompanyUsers(companyId, actor);
        }
        return userRepository.findByCompanyIdAndEmailContaining(companyId, email).stream()
                .map(UserResult::from)
                .toList();
    }
    @Override
    public List<CompanyRoleResult> listCompanyRoles(UUID companyId, String authorizationHeader) {
        ensureCanViewCompany(companyId, authorizationHeader);
        return companyRoleRepository.findByCompanyId(companyId).stream()
                .map(CompanyRoleResult::from)
                .toList();
    }

    @Override
    public CompanyRoleResult getCompanyRole(UUID companyId, UUID roleId, String authorizationHeader) {
        ensureCanViewCompany(companyId, authorizationHeader);
        return CompanyRoleResult.from(findRole(companyId, roleId));
    }

    @Override
    public CompanyRoleResult createCompanyRole(CreateCompanyRoleCommand command) {
        Objects.requireNonNull(command, "command is required");
        Set<PermissionCode> requestedPermissions = validateCompanyPermissions(command.permissionCodes());
        UserAccount actor = authenticate(command.authorizationHeader());
        if (!isRoot(actor)) {
            licenseValidationPort.ensureAllowed(command.companyId(), LicenseAction.CREATE_USER);
            ensureCanDelegate(command.companyId(), actor, requestedPermissions, PermissionCode.COMPANY_ROLES_MANAGE,
                    "CREATE_ROLE");
        }
        CompanyRole role = CompanyRole.create(idGenerator.nextId(), command.companyId(), command.name(),
                command.description(), requestedPermissions, false, actor.id(), clock.now());
        CompanyRole saved = companyRoleRepository.save(role);
        audit(command.companyId(), actor.id(), "CREATE_ROLE", "COMPANY_ROLE", saved.id().toString(),
                AccessAuditResult.SUCCESS, saved.permissionCodes().toString());
        return CompanyRoleResult.from(saved);
    }

    @Override
    public CompanyRoleResult updateCompanyRole(UpdateCompanyRoleCommand command) {
        Objects.requireNonNull(command, "command is required");
        Set<PermissionCode> requestedPermissions = validateCompanyPermissions(command.permissionCodes());
        UserAccount actor = authenticate(command.authorizationHeader());
        CompanyRole existing = findRole(command.companyId(), command.roleId());
        if (!isRoot(actor)) {
            ensureCanDelegate(command.companyId(), actor, requestedPermissions, PermissionCode.COMPANY_ROLES_MANAGE,
                    "UPDATE_ROLE");
        }
        CompanyRole saved = companyRoleRepository.save(existing.update(command.name(), command.description(),
                requestedPermissions, clock.now()));
        audit(command.companyId(), actor.id(), "UPDATE_ROLE", "COMPANY_ROLE", saved.id().toString(),
                AccessAuditResult.SUCCESS, saved.permissionCodes().toString());
        return CompanyRoleResult.from(saved);
    }

    @Override
    public CompanyRoleResult deactivateCompanyRole(UUID companyId, UUID roleId, String authorizationHeader) {
        UserAccount actor = authenticate(authorizationHeader);
        CompanyRole existing = findRole(companyId, roleId);
        if (!isRoot(actor)) {
            ensureHasPermission(companyId, actor.id(), PermissionCode.COMPANY_ROLES_MANAGE, "DEACTIVATE_ROLE");
        }
        CompanyRole saved = companyRoleRepository.save(existing.deactivate(clock.now()));
        audit(companyId, actor.id(), "DEACTIVATE_ROLE", "COMPANY_ROLE", saved.id().toString(),
                AccessAuditResult.SUCCESS, null);
        return CompanyRoleResult.from(saved);
    }

    @Override
    public CompanyRoleResult activateCompanyRole(UUID companyId, UUID roleId, String authorizationHeader) {
        UserAccount actor = authenticate(authorizationHeader);
        CompanyRole existing = findRole(companyId, roleId);
        if (!isRoot(actor)) {
            ensureHasPermission(companyId, actor.id(), PermissionCode.COMPANY_ROLES_MANAGE, "ACTIVATE_ROLE");
        }
        CompanyRole saved = companyRoleRepository.save(existing.activate(clock.now()));
        audit(companyId, actor.id(), "ACTIVATE_ROLE", "COMPANY_ROLE", saved.id().toString(),
                AccessAuditResult.SUCCESS, null);
        return CompanyRoleResult.from(saved);
    }

    @Override
    public UserResult updateCompanyUser(UpdateUserCommand command) {
        Objects.requireNonNull(command, "command is required");
        UserAccount actor = authenticate(command.authorizationHeader());
        ensureCanManageCompanyUsers(command.companyId(), actor, "UPDATE_COMPANY_USER");
        UserAccount target = findCompanyUser(command.companyId(), command.userId());
        String email = normalizeEmail(command.email());
        if (userRepository.existsByEmailAndIdNot(email, target.id())) {
            audit(command.companyId(), actor.id(), "UPDATE_COMPANY_USER", "USER", target.id().toString(),
                    AccessAuditResult.FAILURE, "DUPLICATE_EMAIL");
            throw new UserAlreadyExistsException(email);
        }
        UserAccount saved = userRepository.save(target.update(email, required(command.fullName(), "fullName"),
                clock.now()));
        audit(command.companyId(), actor.id(), "UPDATE_COMPANY_USER", "USER", saved.id().toString(),
                AccessAuditResult.SUCCESS, null);
        return UserResult.from(saved);
    }

    @Override
    public UserResult activateCompanyUser(UUID companyId, UUID userId, String authorizationHeader) {
        UserAccount actor = authenticate(authorizationHeader);
        ensureCanManageCompanyUsers(companyId, actor, "ACTIVATE_COMPANY_USER");
        UserAccount target = findCompanyUser(companyId, userId);
        UserAccount saved = userRepository.save(target.activate(clock.now()));
        audit(companyId, actor.id(), "ACTIVATE_COMPANY_USER", "USER", saved.id().toString(),
                AccessAuditResult.SUCCESS, null);
        return UserResult.from(saved);
    }

    @Override
    public UserResult deactivateCompanyUser(UUID companyId, UUID userId, String authorizationHeader) {
        UserAccount actor = authenticate(authorizationHeader);
        ensureCanManageCompanyUsers(companyId, actor, "DEACTIVATE_COMPANY_USER");
        UserAccount target = findCompanyUser(companyId, userId);
        if (actor.id().equals(target.id())) {
            audit(companyId, actor.id(), "DEACTIVATE_COMPANY_USER", "USER", target.id().toString(),
                    AccessAuditResult.FAILURE, "SELF_DEACTIVATION_NOT_ALLOWED");
            throw new AccessDeniedException(companyId, PermissionCode.COMPANY_USERS_MANAGE);
        }
        UserAccount saved = userRepository.save(target.deactivate(clock.now()));
        audit(companyId, actor.id(), "DEACTIVATE_COMPANY_USER", "USER", saved.id().toString(),
                AccessAuditResult.SUCCESS, null);
        return UserResult.from(saved);
    }

    @Override
    public CompanyAccessResult assignCompanyRoles(AssignCompanyRolesCommand command) {
        Objects.requireNonNull(command, "command is required");
        if (command.roleIds() == null || command.roleIds().isEmpty()) {
            throw new IllegalArgumentException("roleIds are required");
        }
        UserAccount actor = authenticate(command.authorizationHeader());
        UserAccount target = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        List<CompanyRole> roles = command.roleIds().stream()
                .map(roleId -> findRole(command.companyId(), roleId))
                .filter(CompanyRole::active)
                .toList();
        if (roles.size() != command.roleIds().size()) {
            throw new CompanyRoleNotFoundException(command.roleIds().iterator().next());
        }
        Set<PermissionCode> requestedPermissions = roles.stream()
                .flatMap(role -> role.permissionCodes().stream())
                .collect(Collectors.toUnmodifiableSet());
        LicensePolicy licensePolicy = LicensePolicy.unlimited();
        if (!isRoot(actor)) {
            licensePolicy = licenseValidationPort.policy(command.companyId(), LicenseAction.CREATE_USER);
            ensureCanDelegate(command.companyId(), actor, requestedPermissions, PermissionCode.COMPANY_ROLES_MANAGE,
                    "ASSIGN_COMPANY_ROLES");
        }
        ensureUserQuotaAvailable(command.companyId(), target.id(), licensePolicy, "ASSIGN_COMPANY_ROLES");
        companyRoleRepository.replaceUserRoleAssignments(command.companyId(), target.id(), command.roleIds(), actor.id(),
                clock.now());
        audit(command.companyId(), actor.id(), "ASSIGN_COMPANY_ROLES", "USER", target.id().toString(),
                AccessAuditResult.SUCCESS, command.roleIds().toString());
        return permissions(command.companyId(), target.id());
    }

    @Override
    public CompanyAccessResult revokeCompanyRole(RevokeCompanyRoleCommand command) {
        Objects.requireNonNull(command, "command is required");
        UserAccount actor = authenticate(command.authorizationHeader());
        if (!isRoot(actor)) {
            ensureHasPermission(command.companyId(), actor.id(), PermissionCode.COMPANY_ROLES_MANAGE,
                    "REVOKE_COMPANY_ROLE");
        }
        companyRoleRepository.revokeUserRoleAssignment(command.companyId(), command.userId(), command.roleId(), actor.id(),
                clock.now());
        audit(command.companyId(), actor.id(), "REVOKE_COMPANY_ROLE", "USER", command.userId().toString(),
                AccessAuditResult.SUCCESS, command.roleId().toString());
        return permissions(command.companyId(), command.userId());
    }

    @Override
    public CompanyAccessResult effectivePermissions(UUID companyId, UUID userId, String authorizationHeader) {
        UserAccount actor = authenticate(authorizationHeader);
        if (!isRoot(actor) && !actor.id().equals(userId)) {
            ensureHasPermission(companyId, actor.id(), PermissionCode.COMPANY_USERS_MANAGE, "VIEW_EFFECTIVE_PERMISSIONS");
        }
        return permissions(companyId, userId);
    }

    private void ensureCanManageLegacyRoles(UUID companyId, String authorizationHeader, Set<RoleCode> requestedRoles,
            UserAccount authenticatedActor, boolean rootActor) {
        if (rootActor) {
            return;
        }
        boolean hasMemberships = membershipRepository.existsByCompanyId(companyId);
        if (!hasMemberships && requestedRoles.contains(RoleCode.OWNER)) {
            return;
        }
        UserAccount actor = authenticatedActor == null ? authenticate(authorizationHeader) : authenticatedActor;
        Set<PermissionCode> requestedPermissions = requestedRoles.stream()
                .flatMap(role -> role.permissions().stream())
                .collect(Collectors.toUnmodifiableSet());
        ensureCanDelegate(companyId, actor, requestedPermissions, PermissionCode.COMPANY_ROLES_MANAGE, "ASSIGN_ROLES");
    }

    private void ensureCanListCompanyUsers(UUID companyId, UserAccount actor) {
        Set<PermissionCode> permissions = effectivePermissions(companyId, actor.id());
        if (!permissions.contains(PermissionCode.COMPANY_USERS_MANAGE)
                && !permissions.contains(PermissionCode.COMPANY_ROLES_MANAGE)
                && !permissions.contains(PermissionCode.USERS_MANAGE)
                && !permissions.contains(PermissionCode.ROLES_MANAGE)) {
            audit(companyId, actor.id(), "LIST_COMPANY_USERS", "USER", companyId.toString(),
                    AccessAuditResult.FAILURE, "FORBIDDEN");
            throw new AccessDeniedException(companyId, PermissionCode.COMPANY_USERS_MANAGE);
        }
    }

    private void ensureCanManageCompanyUsers(UUID companyId, UserAccount actor, String action) {
        if (isRoot(actor)) {
            return;
        }
        Set<PermissionCode> permissions = effectivePermissions(companyId, actor.id());
        if (!permissions.contains(PermissionCode.COMPANY_USERS_MANAGE)
                && !permissions.contains(PermissionCode.USERS_MANAGE)) {
            audit(companyId, actor.id(), action, "USER", companyId.toString(), AccessAuditResult.FAILURE,
                    "FORBIDDEN");
            throw new AccessDeniedException(companyId, PermissionCode.COMPANY_USERS_MANAGE);
        }
    }

    private UserAccount findCompanyUser(UUID companyId, UUID userId) {
        UserAccount user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        if (!hasCompanyAccess(companyId, user.id())) {
            throw new UserNotFoundException(userId);
        }
        return user;
    }

    private void ensureCanViewCompany(UUID companyId, String authorizationHeader) {
        UserAccount actor = authenticate(authorizationHeader);
        if (isRoot(actor)) {
            return;
        }
        membershipRepository.findByCompanyIdAndUserId(companyId, actor.id())
                .filter(CompanyMembership::active)
                .orElseThrow(() -> new AccessDeniedException(companyId, PermissionCode.COMPANY_USERS_MANAGE));
    }

    private void ensureCanDelegate(UUID companyId, UserAccount actor, Set<PermissionCode> requestedPermissions,
            PermissionCode managementPermission, String action) {
        Set<PermissionCode> actorPermissions = effectivePermissions(companyId, actor.id());
        if (!actorPermissions.contains(managementPermission) && !actorPermissions.contains(PermissionCode.ROLES_MANAGE)) {
            audit(companyId, actor.id(), action, "COMPANY_ROLE", companyId.toString(), AccessAuditResult.FAILURE,
                    "FORBIDDEN");
            throw new AccessDeniedException(companyId, managementPermission);
        }
        if (!actorPermissions.containsAll(requestedPermissions) || requestedPermissions.containsAll(actorPermissions)) {
            audit(companyId, actor.id(), action, "COMPANY_ROLE", companyId.toString(), AccessAuditResult.FAILURE,
                    "DELEGATION_NOT_STRICTLY_LOWER");
            throw new AccessDeniedException(companyId, managementPermission);
        }
    }

    private void ensureHasPermission(UUID companyId, UUID userId, PermissionCode permission, String action) {
        Set<PermissionCode> permissions = effectivePermissions(companyId, userId);
        if (!permissions.contains(permission) && !permissions.contains(PermissionCode.ROLES_MANAGE)) {
            audit(companyId, userId, action, "COMPANY_ROLE", companyId.toString(), AccessAuditResult.FAILURE,
                    "FORBIDDEN");
            throw new AccessDeniedException(companyId, permission);
        }
    }

    private void ensureUserQuotaAvailable(UUID companyId, UUID userId, LicensePolicy policy, String action) {
        Integer maxUsers = policy.maxUsers();
        if (maxUsers == null) {
            return;
        }
        if (hasCompanyAccess(companyId, userId)) {
            return;
        }
        long activeUsers = userRepository.countByCompanyId(companyId);
        if (activeUsers >= maxUsers.longValue()) {
            audit(companyId, userId, action, "LICENSE", companyId.toString(), AccessAuditResult.FAILURE,
                    "MAX_USERS_EXCEEDED");
            throw new LicenseBlockedException("La licencia permite maximo " + maxUsers
                    + " usuarios activos para la empresa.");
        }
    }

    private boolean hasCompanyAccess(UUID companyId, UUID userId) {
        boolean hasMembership = membershipRepository.findByCompanyIdAndUserId(companyId, userId)
                .filter(CompanyMembership::active)
                .isPresent();
        return hasMembership || companyRoleRepository.findAssignedCompanyIds(userId).contains(companyId);
    }


    private CompanyRole findRole(UUID companyId, UUID roleId) {
        return companyRoleRepository.findByIdAndCompanyId(roleId, companyId)
                .orElseThrow(() -> new CompanyRoleNotFoundException(roleId));
    }

    private Set<PermissionCode> effectivePermissions(UUID companyId, UUID userId) {
        EnumSet<PermissionCode> permissions = EnumSet.noneOf(PermissionCode.class);
        membershipRepository.findByCompanyIdAndUserId(companyId, userId)
                .filter(CompanyMembership::active)
                .ifPresent(membership -> permissions.addAll(membership.permissions()));
        permissions.addAll(companyRoleRepository.findEffectivePermissions(companyId, userId));
        return Set.copyOf(permissions);
    }

    private boolean isRoot(UserAccount actor) {
        return actor != null && globalRoleRepository.hasRole(actor.id(), GlobalRoleCode.ROOT);
    }

    private UserAccount authenticateIfPresent(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return null;
        }
        return authenticate(authorizationHeader);
    }

    private UserAccount authenticate(String authorizationHeader) {
        String token = extractBearerToken(authorizationHeader);
        UserSession session = sessionRepository.findByTokenHash(tokenHash.hash(token))
                .orElseThrow(AuthenticationFailedException::new);
        if (!session.isValidAt(clock.now())) {
            throw new AuthenticationFailedException();
        }
        UserAccount user = userRepository.findById(session.userId())
                .orElseThrow(() -> new UserNotFoundException(session.userId()));
        if (!user.isActive()) {
            throw new AuthenticationFailedException();
        }
        return user;
    }

    private static CompanyAccessResult toCompanyAccess(CompanyMembership membership, Set<PermissionCode> permissions) {
        return new CompanyAccessResult(membership.companyId(), membership.roles(), permissions);
    }

    private void audit(UUID companyId, UUID userId, String action, String resourceType, String resourceId,
            AccessAuditResult result, String detail) {
        auditRepository.save(AccessAuditEvent.register(idGenerator.nextId(), companyId, userId, action, resourceType,
                resourceId, result, detail, clock.now()));
    }

    private static String extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new AuthenticationFailedException();
        }
        String token = authorizationHeader.substring(BEARER_PREFIX.length()).trim();
        if (token.isBlank()) {
            throw new AuthenticationFailedException();
        }
        return token;
    }

    private static String normalizeEmail(String email) {
        return required(email, "email").toLowerCase(Locale.ROOT);
    }

    private static String required(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value.trim();
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("password must have at least 8 characters");
        }
    }

    private static void validateRoles(Set<RoleCode> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new IllegalArgumentException("roles are required");
        }
    }

    private static Set<PermissionCode> validateCompanyPermissions(Set<PermissionCode> permissions) {
        if (permissions == null || permissions.isEmpty()) {
            throw new IllegalArgumentException("permissionCodes are required");
        }
        Set<PermissionCode> normalized = Set.copyOf(permissions);
        if (normalized.stream().anyMatch(permission -> !permission.companyScoped())) {
            throw new IllegalArgumentException("company roles cannot include global permissions");
        }
        return normalized;
    }

    private static final class NoopGlobalUserRoleRepository implements GlobalUserRoleRepositoryPort {
        @Override
        public Set<GlobalRoleCode> findByUserId(UUID userId) { return Set.of(); }
        @Override
        public boolean hasRole(UUID userId, GlobalRoleCode roleCode) { return false; }
        @Override
        public void assignRole(UUID userId, GlobalRoleCode roleCode) { }
    }

    private static final class NoopCompanyRoleRepository implements CompanyRoleRepositoryPort {
        @Override
        public List<PermissionDescriptor> listActivePermissions() {
            return java.util.Arrays.stream(PermissionCode.values())
                    .map(PermissionDescriptor::from)
                    .toList();
        }
        @Override
        public CompanyRole save(CompanyRole role) { return role; }
        @Override
        public java.util.Optional<CompanyRole> findByIdAndCompanyId(UUID roleId, UUID companyId) {
            return java.util.Optional.empty();
        }
        @Override
        public List<CompanyRole> findByCompanyId(UUID companyId) { return List.of(); }
        @Override
        public List<CompanyRole> findActiveAssignedRoles(UUID companyId, UUID userId) { return List.of(); }
        @Override
        public List<UUID> findAssignedCompanyIds(UUID userId) { return List.of(); }
        @Override
        public void replaceUserRoleAssignments(UUID companyId, UUID userId, Set<UUID> roleIds, UUID assignedBy,
                Instant assignedAt) { }
        @Override
        public void revokeUserRoleAssignment(UUID companyId, UUID userId, UUID roleId, UUID revokedBy,
                Instant revokedAt) { }
    }
}
