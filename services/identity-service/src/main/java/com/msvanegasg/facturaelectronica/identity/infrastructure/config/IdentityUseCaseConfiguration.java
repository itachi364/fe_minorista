package com.msvanegasg.facturaelectronica.identity.infrastructure.config;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
import com.msvanegasg.facturaelectronica.identity.application.usecase.IdentityManagementService;

@Configuration
public class IdentityUseCaseConfiguration {

    @Bean
    ManageIdentityUseCase manageIdentityUseCase(UserAccountRepositoryPort userRepository,
            CompanyMembershipRepositoryPort membershipRepository,
            UserSessionRepositoryPort sessionRepository,
            AccessAuditRepositoryPort auditRepository,
            LicenseValidationPort licenseValidationPort,
            PasswordHasherPort passwordHasher,
            TokenGeneratorPort tokenGenerator,
            TokenHashPort tokenHash,
            IdGeneratorPort idGenerator,
            ClockPort clock,
            @Value("${identity.session.duration-hours:12}") long sessionDurationHours) {
        return new IdentityManagementService(userRepository, membershipRepository, sessionRepository, auditRepository, licenseValidationPort,
                passwordHasher, tokenGenerator, tokenHash, idGenerator, clock, Duration.ofHours(sessionDurationHours));
    }
}
