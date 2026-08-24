package com.msvanegasg.facturaelectronica.dianprovider.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

public record DianCompanyConfiguration(
        UUID id,
        UUID companyId,
        DianConnectionMode mode,
        DianEnvironment environment,
        String softwareId,
        String softwarePinSecretRef,
        String technicalKeySecretRef,
        String certificateSecretRef,
        String certificateAlias,
        String certificateFingerprint,
        Instant certificateExpiresAt,
        String serviceBaseUrl,
        String testSetId,
        boolean acceptedResponsibility,
        DianConfigurationStatus status,
        DianTestStatus lastTestStatus,
        Instant lastTestAt,
        String lastTestMessage,
        UUID updatedBy,
        Instant createdAt,
        Instant updatedAt) {

    public DianCompanyConfiguration {
        Objects.requireNonNull(id, "id is required");
        Objects.requireNonNull(companyId, "companyId is required");
        mode = mode == null ? DianConnectionMode.MOCK : mode;
        environment = environment == null ? DianEnvironment.TEST : environment;
        status = status == null ? DianConfigurationStatus.DRAFT : status;
        lastTestStatus = lastTestStatus == null ? DianTestStatus.NOT_TESTED : lastTestStatus;
        softwareId = normalize(softwareId);
        softwarePinSecretRef = normalize(softwarePinSecretRef);
        technicalKeySecretRef = normalize(technicalKeySecretRef);
        certificateSecretRef = normalize(certificateSecretRef);
        certificateAlias = normalize(certificateAlias);
        certificateFingerprint = normalize(certificateFingerprint);
        serviceBaseUrl = normalize(serviceBaseUrl);
        testSetId = normalize(testSetId);
        lastTestMessage = normalize(lastTestMessage);
        Objects.requireNonNull(createdAt, "createdAt is required");
        Objects.requireNonNull(updatedAt, "updatedAt is required");
    }

    public boolean isRealModeComplete(Instant now) {
        return mode == DianConnectionMode.REAL
                && acceptedResponsibility
                && hasText(softwareId)
                && hasText(softwarePinSecretRef)
                && hasText(technicalKeySecretRef)
                && hasText(certificateSecretRef)
                && hasText(certificateFingerprint)
                && certificateExpiresAt != null
                && certificateExpiresAt.isAfter(now);
    }

    public boolean hasExpiredCertificate(Instant now) {
        return certificateExpiresAt != null && !certificateExpiresAt.isAfter(now);
    }

    public DianCompanyConfiguration withTestResult(DianTestStatus testStatus, String message, Instant testedAt) {
        DianConfigurationStatus nextStatus = testStatus == DianTestStatus.SUCCESS
                ? DianConfigurationStatus.TESTED
                : DianConfigurationStatus.READY_FOR_TEST;
        return new DianCompanyConfiguration(id, companyId, mode, environment, softwareId, softwarePinSecretRef,
                technicalKeySecretRef, certificateSecretRef, certificateAlias, certificateFingerprint,
                certificateExpiresAt, serviceBaseUrl, testSetId, acceptedResponsibility, nextStatus, testStatus,
                testedAt, message, updatedBy, createdAt, testedAt);
    }

    public DianCompanyConfiguration activated(Instant now) {
        return new DianCompanyConfiguration(id, companyId, mode, environment, softwareId, softwarePinSecretRef,
                technicalKeySecretRef, certificateSecretRef, certificateAlias, certificateFingerprint,
                certificateExpiresAt, serviceBaseUrl, testSetId, acceptedResponsibility, DianConfigurationStatus.ACTIVE,
                lastTestStatus, lastTestAt, lastTestMessage, updatedBy, createdAt, now);
    }

    public DianCompanyConfiguration deactivated(Instant now) {
        return new DianCompanyConfiguration(id, companyId, mode, environment, softwareId, softwarePinSecretRef,
                technicalKeySecretRef, certificateSecretRef, certificateAlias, certificateFingerprint,
                certificateExpiresAt, serviceBaseUrl, testSetId, acceptedResponsibility,
                DianConfigurationStatus.INACTIVE, lastTestStatus, lastTestAt, lastTestMessage, updatedBy, createdAt,
                now);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }
}
