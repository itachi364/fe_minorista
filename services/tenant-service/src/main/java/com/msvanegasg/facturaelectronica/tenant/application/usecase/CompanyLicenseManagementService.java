package com.msvanegasg.facturaelectronica.tenant.application.usecase;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyLicenseCommand;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyLicenseResult;
import com.msvanegasg.facturaelectronica.tenant.application.dto.CompanyLicenseValidationResult;
import com.msvanegasg.facturaelectronica.tenant.application.port.in.ManageCompanyLicenseUseCase;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyLicenseRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.CompanyRepositoryPort;
import com.msvanegasg.facturaelectronica.tenant.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicense;
import com.msvanegasg.facturaelectronica.tenant.domain.model.CompanyLicenseStatus;
import com.msvanegasg.facturaelectronica.tenant.domain.model.LicenseAction;

public class CompanyLicenseManagementService implements ManageCompanyLicenseUseCase {

    private static final String LICENSE_ACTIVE = "LICENSE_ACTIVE";
    private static final String LICENSE_SUSPENDED = "LICENSE_SUSPENDED";
    private static final String LICENSE_EXPIRED = "LICENSE_EXPIRED";
    private static final String LICENSE_CANCELLED = "LICENSE_CANCELLED";

    private final CompanyRepositoryPort companyRepository;
    private final CompanyLicenseRepositoryPort licenseRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public CompanyLicenseManagementService(
            CompanyRepositoryPort companyRepository,
            CompanyLicenseRepositoryPort licenseRepository,
            IdGeneratorPort idGenerator,
            ClockPort clock) {
        this.companyRepository = companyRepository;
        this.licenseRepository = licenseRepository;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Override
    public CompanyLicenseResult save(UUID companyId, CompanyLicenseCommand command) {
        ensureCompanyExists(companyId);
        CompanyLicense license = licenseRepository.findByCompanyId(companyId)
                .map(existing -> existing.update(command.planCode(), command.validFrom(), command.validTo(),
                        command.maxUsers(), command.maxMonthlyDocuments(), clock.now()))
                .orElseGet(() -> CompanyLicense.create(idGenerator.nextId(), companyId, command.planCode(),
                        command.validFrom(), command.validTo(), command.maxUsers(), command.maxMonthlyDocuments(),
                        clock.now()));
        return CompanyLicenseResult.from(licenseRepository.save(license));
    }

    @Override
    public CompanyLicenseResult findByCompanyId(UUID companyId) {
        ensureCompanyExists(companyId);
        return CompanyLicenseResult.from(findLicense(companyId));
    }

    @Override
    public CompanyLicenseResult activate(UUID companyId) {
        ensureCompanyExists(companyId);
        return CompanyLicenseResult.from(licenseRepository.save(findLicense(companyId).activate(clock.now())));
    }

    @Override
    public CompanyLicenseResult suspend(UUID companyId) {
        ensureCompanyExists(companyId);
        return CompanyLicenseResult.from(licenseRepository.save(findLicense(companyId).suspend(clock.now())));
    }

    @Override
    public CompanyLicenseValidationResult validate(UUID companyId, LicenseAction action) {
        ensureCompanyExists(companyId);
        CompanyLicense license = findLicense(companyId);
        LocalDate today = LocalDate.ofInstant(clock.now(), ZoneOffset.UTC);
        CompanyLicenseStatus effectiveStatus = license.effectiveStatus(today);
        boolean allowed = license.allows(action, today);
        return new CompanyLicenseValidationResult(
                companyId,
                action,
                allowed,
                effectiveStatus,
                reasonCode(effectiveStatus),
                message(allowed, effectiveStatus));
    }

    private void ensureCompanyExists(UUID companyId) {
        companyRepository.findById(companyId)
                .orElseThrow(() -> new CompanyNotFoundException(companyId));
    }

    private CompanyLicense findLicense(UUID companyId) {
        return licenseRepository.findByCompanyId(companyId)
                .orElseThrow(() -> new CompanyLicenseNotFoundException(companyId));
    }

    private static String reasonCode(CompanyLicenseStatus status) {
        return switch (status) {
            case ACTIVE -> LICENSE_ACTIVE;
            case SUSPENDED -> LICENSE_SUSPENDED;
            case EXPIRED -> LICENSE_EXPIRED;
            case CANCELLED -> LICENSE_CANCELLED;
        };
    }

    private static String message(boolean allowed, CompanyLicenseStatus status) {
        if (allowed) {
            return "La licencia permite ejecutar la accion solicitada.";
        }
        return switch (status) {
            case SUSPENDED -> "La licencia de la empresa esta suspendida.";
            case EXPIRED -> "La licencia de la empresa esta vencida.";
            case CANCELLED -> "La licencia de la empresa esta cancelada.";
            case ACTIVE -> "La licencia no permite ejecutar la accion solicitada.";
        };
    }
}
