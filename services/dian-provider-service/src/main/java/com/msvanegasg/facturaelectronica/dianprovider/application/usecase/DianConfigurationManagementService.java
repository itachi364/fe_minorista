package com.msvanegasg.facturaelectronica.dianprovider.application.usecase;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianConfigurationCommand;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianConfigurationResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.in.ManageDianConfigurationUseCase;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianConfigurationRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.SecretVaultPort;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianConfigurationStatus;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianConnectionMode;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianEnvironment;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianTestStatus;

public class DianConfigurationManagementService implements ManageDianConfigurationUseCase {

    private final DianConfigurationRepositoryPort repository;
    private final SecretVaultPort secretVault;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public DianConfigurationManagementService(DianConfigurationRepositoryPort repository, SecretVaultPort secretVault,
            IdGeneratorPort idGenerator, ClockPort clock) {
        this.repository = repository;
        this.secretVault = secretVault;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Override
    public Optional<DianConfigurationResult> findByCompanyId(UUID companyId) {
        return repository.findByCompanyId(companyId).map(DianConfigurationMapper::toResult);
    }

    @Override
    public DianConfigurationResult save(DianConfigurationCommand command) {
        if (command.companyId() == null) {
            throw new IllegalArgumentException("companyId is required");
        }
        Instant now = clock.now();
        DianCompanyConfiguration current = repository.findByCompanyId(command.companyId()).orElse(null);
        UUID id = current == null ? idGenerator.generate() : current.id();
        Instant createdAt = current == null ? now : current.createdAt();
        String pinRef = secretRef(current == null ? null : current.softwarePinSecretRef(), command.companyId(),
                "dian/software-pin", command.softwarePin());
        String technicalKeyRef = secretRef(current == null ? null : current.technicalKeySecretRef(),
                command.companyId(), "dian/technical-key", command.technicalKey());
        String certificateRef = secretRef(current == null ? null : current.certificateSecretRef(),
                command.companyId(), "dian/certificate", command.certificatePayload());
        if (hasText(command.certificatePassword())) {
            secretVault.storeCompanySecret(command.companyId(), "dian/certificate-password",
                    command.certificatePassword());
        }
        DianCompanyConfiguration configuration = new DianCompanyConfiguration(id, command.companyId(),
                command.mode() == null ? DianConnectionMode.MOCK : command.mode(),
                command.environment() == null ? DianEnvironment.TEST : command.environment(), command.softwareId(),
                pinRef, technicalKeyRef, certificateRef, command.certificateAlias(), command.certificateFingerprint(),
                command.certificateExpiresAt(), command.serviceBaseUrl(), command.testSetId(),
                command.acceptedResponsibility(), inferStatus(command.mode(), command.acceptedResponsibility(), pinRef,
                        technicalKeyRef, certificateRef, command.softwareId(), command.certificateFingerprint(),
                        command.certificateExpiresAt(), now),
                current == null ? DianTestStatus.NOT_TESTED : current.lastTestStatus(),
                current == null ? null : current.lastTestAt(), current == null ? null : current.lastTestMessage(),
                command.updatedBy(), createdAt, now);
        return DianConfigurationMapper.toResult(repository.save(configuration));
    }

    @Override
    public DianConfigurationResult testConnection(UUID companyId, UUID userId) {
        DianCompanyConfiguration configuration = configuration(companyId);
        ensureTestable(configuration);
        Instant now = clock.now();
        String message = configuration.mode() == DianConnectionMode.MOCK
                ? "Conector DIAN mock validado para pruebas internas."
                : "Configuracion real lista para pruebas controladas DIAN.";
        return DianConfigurationMapper.toResult(repository.save(configuration.withTestResult(DianTestStatus.SUCCESS,
                message, now)));
    }

    @Override
    public DianConfigurationResult activate(UUID companyId, UUID userId) {
        DianCompanyConfiguration configuration = configuration(companyId);
        ensureTestable(configuration);
        if (configuration.lastTestStatus() != DianTestStatus.SUCCESS) {
            throw new DianConfigurationIncompleteException("Debe ejecutar una prueba exitosa antes de activar DIAN.");
        }
        return DianConfigurationMapper.toResult(repository.save(configuration.activated(clock.now())));
    }

    @Override
    public DianConfigurationResult deactivate(UUID companyId, UUID userId) {
        return DianConfigurationMapper.toResult(repository.save(configuration(companyId).deactivated(clock.now())));
    }

    private DianCompanyConfiguration configuration(UUID companyId) {
        return repository.findByCompanyId(companyId).orElseThrow(() -> new DianConfigurationNotFoundException(companyId));
    }

    private void ensureTestable(DianCompanyConfiguration configuration) {
        Instant now = clock.now();
        if (configuration.mode() == DianConnectionMode.MOCK) {
            return;
        }
        if (configuration.hasExpiredCertificate(now)) {
            throw new DianCertificateExpiredException("El certificado DIAN configurado esta vencido.");
        }
        if (!configuration.isRealModeComplete(now)) {
            throw new DianConfigurationIncompleteException(
                    "La configuracion DIAN real esta incompleta o no tiene responsabilidad empresarial aceptada.");
        }
    }

    private String secretRef(String currentRef, UUID companyId, String secretName, String value) {
        if (!hasText(value)) {
            return currentRef;
        }
        return secretVault.storeCompanySecret(companyId, secretName, value);
    }

    private static DianConfigurationStatus inferStatus(DianConnectionMode mode, boolean acceptedResponsibility,
            String pinRef, String technicalKeyRef, String certificateRef, String softwareId, String fingerprint,
            Instant certificateExpiresAt, Instant now) {
        if (mode == DianConnectionMode.MOCK) {
            return DianConfigurationStatus.READY_FOR_TEST;
        }
        boolean complete = acceptedResponsibility && hasText(pinRef) && hasText(technicalKeyRef)
                && hasText(certificateRef) && hasText(softwareId) && hasText(fingerprint)
                && certificateExpiresAt != null && certificateExpiresAt.isAfter(now);
        return complete ? DianConfigurationStatus.READY_FOR_TEST : DianConfigurationStatus.DRAFT;
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
