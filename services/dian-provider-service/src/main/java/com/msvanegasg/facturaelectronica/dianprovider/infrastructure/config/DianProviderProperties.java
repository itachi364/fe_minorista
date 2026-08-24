package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmissionStatus;

@ConfigurationProperties(prefix = "dian-provider")
public record DianProviderProperties(
        String mode,
        ProviderSubmissionStatus mockDefaultStatus,
        String mockErrorCode,
        String mockErrorMessage,
        String realTransportMode,
        ProviderSubmissionStatus realDefaultStatus,
        String artifactStorageRoot,
        String technicalArtifactsRoot,
        String ublInvoiceXsdPath,
        String ublCreditNoteXsdPath,
        String ublDebitNoteXsdPath,
        String dianModelSchematronPath,
        String dianCompiledXslPath,
        String codeListSchematronPath) {
}
