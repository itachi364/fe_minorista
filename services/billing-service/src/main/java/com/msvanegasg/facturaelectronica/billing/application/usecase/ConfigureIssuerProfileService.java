package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.billing.application.dto.ConfigureIssuerProfileCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.IssuerProfileResult;
import com.msvanegasg.facturaelectronica.billing.application.port.in.ConfigureIssuerProfileUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IssuerProfileRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.IssuerProfile;

public class ConfigureIssuerProfileService implements ConfigureIssuerProfileUseCase {

    private final IssuerProfileRepositoryPort issuerProfileRepository;
    private final IdGeneratorPort idGenerator;

    public ConfigureIssuerProfileService(IssuerProfileRepositoryPort issuerProfileRepository,
            IdGeneratorPort idGenerator) {
        this.issuerProfileRepository = Objects.requireNonNull(issuerProfileRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
    }

    @Override
    public IssuerProfileResult configure(ConfigureIssuerProfileCommand command) {
        Objects.requireNonNull(command, "command is required");
        IssuerProfile saved = issuerProfileRepository.saveAsOnlyActive(IssuerProfile.configure(idGenerator.newId(),
                command.companyId(), command.legalName(), command.nit(), command.verificationDigit(),
                command.taxResponsibilities(), command.municipalityCode(), command.address()));
        return BillingResultMapper.toIssuerProfileResult(saved);
    }

    @Override
    public IssuerProfileResult activate(UUID companyId, UUID issuerId) {
        IssuerProfile issuer = issuerProfileRepository.findByCompanyIdAndId(companyId, issuerId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el emisor fiscal indicado."));
        return BillingResultMapper.toIssuerProfileResult(issuerProfileRepository.saveAsOnlyActive(issuer.activate()));
    }

    @Override
    public IssuerProfileResult deactivate(UUID companyId, UUID issuerId) {
        IssuerProfile issuer = issuerProfileRepository.findByCompanyIdAndId(companyId, issuerId)
                .orElseThrow(() -> new IllegalArgumentException("No existe el emisor fiscal indicado."));
        return BillingResultMapper.toIssuerProfileResult(issuerProfileRepository.save(issuer.deactivate()));
    }
}
