package com.msvanegasg.facturaelectronica.identity.infrastructure.config;

import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.msvanegasg.facturaelectronica.identity.application.port.in.ManageIdentityUseCase;
import com.msvanegasg.facturaelectronica.identity.application.port.out.AccessAuditRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.CompanyMembershipRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.GlobalUserRoleRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.LicenseValidationPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.PasswordHasherPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.TokenGeneratorPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.TokenHashPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.UserAccountRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.port.out.UserSessionRepositoryPort;
import com.msvanegasg.facturaelectronica.identity.application.usecase.IdentityManagementService;
import com.msvanegasg.facturaelectronica.identity.domain.model.GlobalRoleCode;
import com.msvanegasg.facturaelectronica.identity.domain.model.UserAccount;

@Configuration
public class IdentityUseCaseConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(IdentityUseCaseConfiguration.class);

    @Bean
    ManageIdentityUseCase manageIdentityUseCase(UserAccountRepositoryPort userRepository,
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
            @Value("${identity.session.duration-hours:12}") long sessionDurationHours) {
        return new IdentityManagementService(userRepository, membershipRepository, sessionRepository, auditRepository,
                globalRoleRepository, licenseValidationPort, passwordHasher, tokenGenerator, tokenHash, idGenerator,
                clock, Duration.ofHours(sessionDurationHours));
    }

    @Bean
    ApplicationRunner rootUserSeeder(UserAccountRepositoryPort userRepository,
            GlobalUserRoleRepositoryPort globalRoleRepository,
            PasswordHasherPort passwordHasher,
            IdGeneratorPort idGenerator,
            ClockPort clock,
            @Value("${identity.root-user.seed-enabled:false}") boolean seedEnabled,
            @Value("${identity.root-user.email:}") String rootEmail,
            @Value("${identity.root-user.full-name:Root Platform User}") String rootFullName,
            @Value("${identity.root-user.password:}") String rootPassword) {
        return args -> {
            if (!seedEnabled) {
                return;
            }
            String email = rootEmail == null ? "" : rootEmail.trim().toLowerCase(java.util.Locale.ROOT);
            if (email.isBlank() || rootPassword == null || rootPassword.length() < 8) {
                LOGGER.warn("Root user seed is enabled but email/password are not valid; skipping seed.");
                return;
            }
            UserAccount user = userRepository.findByEmail(email)
                    .orElseGet(() -> userRepository.save(UserAccount.create(idGenerator.nextId(), email,
                            rootFullName == null || rootFullName.isBlank() ? "Root Platform User" : rootFullName.trim(),
                            passwordHasher.hash(rootPassword), clock.now())));
            globalRoleRepository.assignRole(user.id(), GlobalRoleCode.ROOT);
            LOGGER.info("Root user seed ensured for {}", email);
        };
    }
}