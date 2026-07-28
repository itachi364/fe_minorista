package com.msvanegasg.facturaelectronica.identity.application.usecase;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.identity.application.dto.AssignRolesCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.CompanyAccessResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.CreateUserCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.LoginCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.LoginResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.MembershipResult;
import com.msvanegasg.facturaelectronica.identity.application.dto.UpdateMembershipRolesCommand;
import com.msvanegasg.facturaelectronica.identity.application.dto.UserResult;
import com.msvanegasg.facturaelectronica.identity.application.port.in.ManageIdentityUseCase;
import com.msvanegasg.facturaelectronica.identity.application.port.out.AccessAuditRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.CompanyMembershipRepositoryPort;
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
import com.msvanegasg.facturaelectronica.identity.domain.model.LicenseAction;
import com.msvanegasg.facturaelectronica.identity.domain.model.PermissionCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.RoleCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.UserAccount;
import com.msvanegasg.facturaelectronica.identity.domain.model.UserSession;

public class IdentityManagementService implements ManageIdentityUseCase {

    private static final String BEARER_PREFIX = "Bearer ";

    private final UserAccountRepositoryPort userRepository;
    private final CompanyMembershipRepositoryPort membershipRepository;
    private final UserSessionRepositoryPort sessionRepository;
    private final AccessAuditRepositoryPort auditRepository;
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
        this(userRepository, membershipRepository, sessionRepository, auditRepository, (companyId, action) -> {
        }, passwordHasher, tokenGenerator, tokenHash, idGenerator, clock, sessionDuration);
    }

    public IdentityManagementService(UserAccountRepositoryPort userRepository,
            CompanyMembershipRepositoryPort membershipRepository,
            UserSessionRepositoryPort sessionRepository,
            AccessAuditRepositoryPort auditRepository,
            LicenseValidationPort licenseValidationPort,
            PasswordHasherPort passwordHasher,
            TokenGeneratorPort tokenGenerator,
            TokenHashPort tokenHash,
            IdGeneratorPort idGenerator,
            ClockPort clock,
            Duration sessionDuration) {
        this.userRepository = Objects.requireNonNull(userRepository);
        this.membershipRepository = Objects.requireNonNull(membershipRepository);
        this.sessionRepository = Objects.requireNonNull(sessionRepository);
        this.auditRepository = Objects.requireNonNull(auditRepository);
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
        return new LoginResult(user.id(), user.email(), user.fullName(), rawToken, expiresAt);
    }

    @Override
    public UserResult currentUser(String authorizationHeader) {
        return UserResult.from(authenticate(authorizationHeader));
    }

    @Override
    public List<CompanyAccessResult> currentCompanies(String authorizationHeader) {
        UserAccount user = authenticate(authorizationHeader);
        return membershipRepository.findByUserId(user.id()).stream()
                .filter(CompanyMembership::active)
                .map(IdentityManagementService::toCompanyAccess)
                .toList();
    }

    @Override
    public MembershipResult assignRoles(AssignRolesCommand command) {
        Objects.requireNonNull(command, "command is required");
        validateRoles(command.roles());
        UserAccount target = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));
        licenseValidationPort.ensureAllowed(command.companyId(), LicenseAction.CREATE_USER);
        ensureCanManageRoles(command.companyId(), command.authorizationHeader(), command.roles());
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
        ensureCanManageRoles(command.companyId(), command.authorizationHeader(), command.roles());
        CompanyMembership membership = membershipRepository.findByIdAndCompanyId(command.membershipId(),
                command.companyId()).orElseThrow(() -> new MembershipNotFoundException(command.membershipId()));
        CompanyMembership saved = membershipRepository.save(membership.replaceRoles(command.roles(), clock.now()));
        audit(command.companyId(), saved.userId(), "UPDATE_ROLES", "MEMBERSHIP", saved.id().toString(),
                AccessAuditResult.SUCCESS, saved.roles().toString());
        return MembershipResult.from(saved);
    }

    @Override
    public CompanyAccessResult permissions(UUID companyId, UUID userId) {
        CompanyMembership membership = membershipRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new MembershipNotFoundException(userId));
        return toCompanyAccess(membership);
    }

    private void ensureCanManageRoles(UUID companyId, String authorizationHeader, Set<RoleCode> requestedRoles) {
        boolean hasMemberships = membershipRepository.existsByCompanyId(companyId);
        if (!hasMemberships && requestedRoles.contains(RoleCode.OWNER)) {
            return;
        }
        UserAccount actor = authenticate(authorizationHeader);
        CompanyMembership membership = membershipRepository.findByCompanyIdAndUserId(companyId, actor.id())
                .orElseThrow(() -> new AccessDeniedException(companyId, PermissionCode.ROLES_MANAGE));
        if (!membership.hasPermission(PermissionCode.ROLES_MANAGE)) {
            audit(companyId, actor.id(), "ASSIGN_ROLES", "MEMBERSHIP", companyId.toString(),
                    AccessAuditResult.FAILURE, "FORBIDDEN");
            throw new AccessDeniedException(companyId, PermissionCode.ROLES_MANAGE);
        }
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

    private static CompanyAccessResult toCompanyAccess(CompanyMembership membership) {
        return new CompanyAccessResult(membership.companyId(), membership.roles(), membership.permissions());
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
}
