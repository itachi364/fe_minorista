package com.msvanegasg.facturaelectronica.thirdparty.application.usecase;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.thirdparty.application.dto.ThirdPartyCommand;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.ThirdPartyResult;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.in.ManageThirdPartyUseCase;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.out.ThirdPartyRepositoryPort;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdParty;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdPartyRole;

public class ThirdPartyManagementService implements ManageThirdPartyUseCase {

    private final ThirdPartyRepositoryPort repository;

    public ThirdPartyManagementService(ThirdPartyRepositoryPort repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    @Override
    public ThirdPartyResult create(ThirdPartyCommand command) {
        Objects.requireNonNull(command, "command is required");
        if (repository.existsByCompanyIdAndDocument(command.companyId(), command.identificationTypeCode(),
                command.identificationNumber())) {
            throw new IllegalStateException("third party already exists for company and document");
        }
        ThirdParty thirdParty = ThirdParty.create(command.companyId(), command.personType(),
                command.identificationTypeCode(), command.identificationNumber(), command.fullName(),
                command.businessName(), command.tradeName(), command.email(), command.phone(), command.address(),
                command.municipalityCode(), command.roles());
        return toResult(repository.save(thirdParty));
    }

    @Override
    public ThirdPartyResult update(UUID companyId, UUID id, ThirdPartyCommand command) {
        Objects.requireNonNull(command, "command is required");
        ThirdParty existing = find(companyId, id);
        if (!Objects.equals(existing.identificationTypeCode(), command.identificationTypeCode())
                || !Objects.equals(existing.identificationNumber(), command.identificationNumber())) {
            throw new IllegalArgumentException("third party document cannot be modified");
        }
        ThirdParty updated = existing.update(command.personType(), command.fullName(), command.businessName(),
                command.tradeName(), command.email(), command.phone(), command.address(), command.municipalityCode(),
                command.roles());
        return toResult(repository.save(updated));
    }

    @Override
    public ThirdPartyResult findById(UUID companyId, UUID id) {
        return toResult(find(companyId, id));
    }

    @Override
    public List<ThirdPartyResult> findByRole(UUID companyId, ThirdPartyRole role, Boolean active) {
        Objects.requireNonNull(companyId, "companyId is required");
        return repository.findByCompanyIdAndRole(companyId, role, active).stream()
                .map(ThirdPartyManagementService::toResult)
                .toList();
    }

    @Override
    public ThirdPartyResult findByDocument(UUID companyId, Integer identificationTypeCode, String identificationNumber) {
        Objects.requireNonNull(companyId, "companyId is required");
        return repository.findByCompanyIdAndDocument(companyId, normalizeDocumentType(identificationTypeCode),
                normalizeDocumentNumber(identificationNumber))
                .map(ThirdPartyManagementService::toResult)
                .orElseThrow(() -> new IllegalArgumentException("third party was not found"));
    }

    @Override
    public void activate(UUID companyId, UUID id) {
        repository.save(find(companyId, id).activate());
    }

    @Override
    public void deactivate(UUID companyId, UUID id) {
        repository.save(find(companyId, id).deactivate());
    }

    private ThirdParty find(UUID companyId, UUID id) {
        Objects.requireNonNull(companyId, "companyId is required");
        Objects.requireNonNull(id, "id is required");
        return repository.findByCompanyIdAndId(companyId, id)
                .orElseThrow(() -> new IllegalArgumentException("third party was not found"));
    }

    private static ThirdPartyResult toResult(ThirdParty thirdParty) {
        return new ThirdPartyResult(thirdParty.id(), thirdParty.companyId(), thirdParty.personType(),
                thirdParty.identificationTypeCode(), thirdParty.identificationNumber(),
                thirdParty.verificationDigit(), thirdParty.fullName(), thirdParty.businessName(),
                thirdParty.tradeName(), thirdParty.email(), thirdParty.phone(), thirdParty.address(),
                thirdParty.municipalityCode(), thirdParty.roles(), thirdParty.active());
    }

    private static Integer normalizeDocumentType(Integer value) {
        if (value == null) {
            throw new IllegalArgumentException("identificationTypeCode is required");
        }
        return value;
    }

    private static String normalizeDocumentNumber(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("identificationNumber is required");
        }
        return value.trim();
    }
}
