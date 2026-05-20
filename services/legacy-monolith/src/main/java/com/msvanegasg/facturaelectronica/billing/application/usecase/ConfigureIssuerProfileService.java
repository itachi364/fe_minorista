package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;

import com.msvanegasg.facturaelectronica.billing.application.dto.ConfigureIssuerProfileCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.IssuerProfileResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ConfigureIssuerProfileUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IssuerProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.IssuerProfile;

public class ConfigureIssuerProfileService implements ConfigureIssuerProfileUseCase {

    private final IssuerProfileRepositoryPort issuerProfileRepository;
    private final IdGeneratorPort idGenerator;

    public ConfigureIssuerProfileService(
            IssuerProfileRepositoryPort issuerProfileRepository,
            IdGeneratorPort idGenerator) {
        this.issuerProfileRepository = Objects.requireNonNull(issuerProfileRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public IssuerProfileResult configure(ConfigureIssuerProfileCommand command) {
        Objects.requireNonNull(command, "command is required");

        IssuerProfile issuerProfile = IssuerProfile.configure(
                idGenerator.newId(),
                command.companyId(),
                command.legalName(),
                command.nit(),
                command.verificationDigit(),
                command.taxResponsibilities(),
                command.municipalityCode(),
                command.address());

        IssuerProfile savedIssuerProfile = issuerProfileRepository.save(issuerProfile);

        return new IssuerProfileResult(
                savedIssuerProfile.id(),
                savedIssuerProfile.companyId(),
                savedIssuerProfile.legalName(),
                savedIssuerProfile.nit(),
                savedIssuerProfile.verificationDigit(),
                savedIssuerProfile.taxResponsibilities(),
                savedIssuerProfile.municipalityCode(),
                savedIssuerProfile.address(),
                savedIssuerProfile.active());
    }
}
