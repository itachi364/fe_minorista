package com.msvanegasg.facturaelectronica.dianprovider.application.usecase;

import java.util.Locale;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianIdentifierResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianSignedDocument;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianTransportResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianValidationReport;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianXmlDocument;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.SubmitProviderDocumentCommand;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.in.FindProviderSubmissionUseCase;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.in.SubmitProviderDocumentUseCase;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianConfigurationRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianIdentifierCalculationPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianSignaturePort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianSubmissionTraceRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianTechnicalArtifactPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianTechnicalValidationPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianTransportPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.FiscalArtifactStoragePort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.FiscalDocumentXmlBuilderPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ProviderSubmissionRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianArtifactType;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianConfigurationStatus;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianConnectionMode;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianSubmissionEvent;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianSubmissionEventStatus;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianSubmissionEventType;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmission;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmissionStatus;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.config.DianProviderProperties;

public class DianProviderSubmissionService implements SubmitProviderDocumentUseCase, FindProviderSubmissionUseCase {

    private final ProviderSubmissionRepositoryPort repository;
    private final DianConfigurationRepositoryPort configurationRepository;
    private final DianTechnicalArtifactPort technicalArtifacts;
    private final FiscalDocumentXmlBuilderPort xmlBuilder;
    private final DianIdentifierCalculationPort identifierCalculator;
    private final DianSignaturePort signature;
    private final DianTechnicalValidationPort technicalValidation;
    private final DianTransportPort transport;
    private final FiscalArtifactStoragePort artifactStorage;
    private final DianSubmissionTraceRepositoryPort traceRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;
    private final DianProviderProperties properties;

    public DianProviderSubmissionService(ProviderSubmissionRepositoryPort repository,
            DianConfigurationRepositoryPort configurationRepository, DianTechnicalArtifactPort technicalArtifacts,
            FiscalDocumentXmlBuilderPort xmlBuilder, DianIdentifierCalculationPort identifierCalculator,
            DianSignaturePort signature, DianTechnicalValidationPort technicalValidation, DianTransportPort transport,
            FiscalArtifactStoragePort artifactStorage, DianSubmissionTraceRepositoryPort traceRepository,
            IdGeneratorPort idGenerator, ClockPort clock, DianProviderProperties properties) {
        this.repository = repository;
        this.configurationRepository = configurationRepository;
        this.technicalArtifacts = technicalArtifacts;
        this.xmlBuilder = xmlBuilder;
        this.identifierCalculator = identifierCalculator;
        this.signature = signature;
        this.technicalValidation = technicalValidation;
        this.transport = transport;
        this.artifactStorage = artifactStorage;
        this.traceRepository = traceRepository;
        this.idGenerator = idGenerator;
        this.clock = clock;
        this.properties = properties;
    }

    @Override
    public ProviderSubmissionResult submit(SubmitProviderDocumentCommand command) {
        validate(command);
        return repository
                .findByIdempotencyKey(command.companyId(), command.documentId(), command.documentType(),
                        command.idempotencyKey())
                .map(ProviderSubmissionResultMapper::toResult)
                .orElseGet(() -> submitNew(command));
    }

    @Override
    public ProviderSubmissionResult findByTrackingId(UUID companyId, String trackingId) {
        if (companyId == null) {
            throw new IllegalArgumentException("La empresa es obligatoria.");
        }
        if (trackingId == null || trackingId.isBlank()) {
            throw new IllegalArgumentException("El tracking ID es obligatorio.");
        }
        return repository.findByCompanyIdAndTrackingId(companyId, trackingId)
                .map(ProviderSubmissionResultMapper::toResult)
                .orElseThrow(() -> new ProviderSubmissionNotFoundException(trackingId));
    }

    private ProviderSubmissionResult submitNew(SubmitProviderDocumentCommand command) {
        if ("mock".equalsIgnoreCase(properties.mode())) {
            return ProviderSubmissionResultMapper.toResult(repository.save(toMockSubmission(command)));
        }
        if ("real".equalsIgnoreCase(properties.mode())) {
            return ProviderSubmissionResultMapper.toResult(submitReal(command));
        }
        throw new IllegalStateException("DIAN_PROVIDER_MODE debe ser mock o real.");
    }

    private ProviderSubmission submitReal(SubmitProviderDocumentCommand command) {
        DianCompanyConfiguration configuration = configurationRepository.findByCompanyId(command.companyId())
                .orElseThrow(() -> new DianConfigurationNotFoundException(command.companyId()));
        validateRealConfiguration(configuration);
        technicalArtifacts.ensureReadyForRealMode();

        UUID submissionId = idGenerator.generate();
        DianXmlDocument xml = xmlBuilder.build(command, configuration);
        saveEvent(command, submissionId, DianSubmissionEventType.XML_BUILT, DianSubmissionEventStatus.SUCCESS, null,
                xml.documentName());
        storeArtifact(command, submissionId, DianArtifactType.UNSIGNED_XML, "application/xml", xml.documentName(),
                xml.xml());

        DianIdentifierResult identifiers = identifierCalculator.calculate(command, configuration, xml.xml());
        saveEvent(command, submissionId, DianSubmissionEventType.IDENTIFIERS_CALCULATED,
                DianSubmissionEventStatus.SUCCESS, null, identifiers.cufeCude());
        storeArtifact(command, submissionId, DianArtifactType.QR, "text/plain", "qr-" + command.documentId() + ".txt",
                identifiers.qrContent());

        DianSignedDocument signedDocument = signature.sign(configuration, xml.xml(), identifiers);
        saveEvent(command, submissionId, DianSubmissionEventType.SIGNED, DianSubmissionEventStatus.SUCCESS, null,
                "XML firmado con referencia empresarial.");
        storeArtifact(command, submissionId, DianArtifactType.SIGNED_XML, "application/xml",
                "signed-" + xml.documentName(), signedDocument.xml());

        DianValidationReport validationReport = technicalValidation.validate(submissionId, command, configuration,
                xml.xml(), signedDocument, identifiers);
        traceRepository.saveValidationResults(validationReport.results());
        if (validationReport.failed()) {
            saveEvent(command, submissionId, DianSubmissionEventType.VALIDATED, DianSubmissionEventStatus.FAILURE,
                    "DIAN_TECHNICAL_VALIDATION_FAILED", validationReport.firstFailureMessage());
            ProviderSubmission failed = buildRealSubmission(command, submissionId, "real-validation-" + command.documentId(),
                    ProviderSubmissionStatus.FAILED, identifiers.cufeCude(), identifiers.qrContent(),
                    "DIAN_TECHNICAL_VALIDATION_FAILED", validationReport.firstFailureMessage(),
                    "{\"validation\":\"FAILED\"}");
            return repository.save(failed);
        }
        saveEvent(command, submissionId, DianSubmissionEventType.VALIDATED, DianSubmissionEventStatus.SUCCESS, null,
                "Validacion tecnica local aprobada.");

        DianTransportResult transportResult = transport.transmit(submissionId, command, configuration, signedDocument);
        saveEvent(command, submissionId, DianSubmissionEventType.TRANSMITTED,
                transportResult.status() == ProviderSubmissionStatus.FAILED ? DianSubmissionEventStatus.FAILURE
                        : DianSubmissionEventStatus.SUCCESS,
                transportResult.dianCode(), transportResult.dianMessage());
        if (transportResult.applicationResponse() != null && !transportResult.applicationResponse().isBlank()) {
            storeArtifact(command, submissionId, DianArtifactType.APPLICATION_RESPONSE, "application/xml",
                    "application-response-" + command.documentId() + ".xml", transportResult.applicationResponse());
        }
        saveEvent(command, submissionId,
                transportResult.status() == ProviderSubmissionStatus.ACCEPTED ? DianSubmissionEventType.ACCEPTED
                        : transportResult.status() == ProviderSubmissionStatus.REJECTED ? DianSubmissionEventType.REJECTED
                                : DianSubmissionEventType.FAILED,
                transportResult.status() == ProviderSubmissionStatus.ACCEPTED ? DianSubmissionEventStatus.SUCCESS
                        : DianSubmissionEventStatus.FAILURE,
                transportResult.dianCode(), transportResult.dianMessage());
        return repository.save(buildRealSubmission(command, submissionId, transportResult.trackingId(),
                transportResult.status(), identifiers.cufeCude(), identifiers.qrContent(),
                transportResult.status() == ProviderSubmissionStatus.ACCEPTED ? null : transportResult.dianCode(),
                transportResult.status() == ProviderSubmissionStatus.ACCEPTED ? null : transportResult.dianMessage(),
                safeRawResponse(transportResult)));
    }

    private ProviderSubmission toMockSubmission(SubmitProviderDocumentCommand command) {
        ProviderSubmissionStatus status = properties.mockDefaultStatus();
        String trackingId = "mock-" + command.documentType().name().toLowerCase(Locale.ROOT) + "-"
                + command.documentId();
        String source = command.companyId() + "|" + command.documentId() + "|" + command.documentType() + "|"
                + command.idempotencyKey();
        String cude = status == ProviderSubmissionStatus.ACCEPTED
                ? com.msvanegasg.facturaelectronica.dianprovider.domain.model.CudeHashGenerator.generate(source)
                : null;
        String qr = cude == null ? null : "mock-qr:" + cude;
        String errorCode = status == ProviderSubmissionStatus.ACCEPTED ? null
                : properties.mockErrorCode() == null || properties.mockErrorCode().isBlank()
                        ? status == ProviderSubmissionStatus.REJECTED ? "MOCK_REJECTED" : "MOCK_FAILED"
                        : properties.mockErrorCode();
        String errorMessage = status == ProviderSubmissionStatus.ACCEPTED ? null
                : properties.mockErrorMessage() == null || properties.mockErrorMessage().isBlank()
                        ? status == ProviderSubmissionStatus.REJECTED
                                ? "Documento rechazado por conector DIAN mock."
                                : "Fallo tecnico simulado por conector DIAN mock."
                        : properties.mockErrorMessage();
        return new ProviderSubmission(idGenerator.generate(), command.companyId(), command.documentId(),
                command.documentType(), command.idempotencyKey(), trackingId, status, cude, qr, errorCode,
                errorMessage, clock.now(), command.payload(), "{\"trackingId\":\"" + trackingId + "\",\"status\":\""
                        + status + "\",\"cufeCude\":\"" + (cude == null ? "" : cude) + "\"}");
    }

    private ProviderSubmission buildRealSubmission(SubmitProviderDocumentCommand command, UUID submissionId,
            String trackingId, ProviderSubmissionStatus status, String cufeCude, String qrContent, String errorCode,
            String errorMessage, String rawResponse) {
        return new ProviderSubmission(submissionId, command.companyId(), command.documentId(), command.documentType(),
                command.idempotencyKey(), trackingId, status, cufeCude, qrContent, errorCode, errorMessage, clock.now(),
                "{\"mode\":\"REAL\",\"documentId\":\"" + command.documentId() + "\",\"documentType\":\""
                        + command.documentType() + "\"}",
                rawResponse);
    }

    private void validateRealConfiguration(DianCompanyConfiguration configuration) {
        if (configuration.mode() != DianConnectionMode.REAL) {
            throw new DianConfigurationIncompleteException("La configuracion DIAN de la empresa no esta en modo real.");
        }
        if (configuration.status() != DianConfigurationStatus.ACTIVE) {
            throw new DianConfigurationIncompleteException("La configuracion DIAN real no esta activa.");
        }
        if (configuration.hasExpiredCertificate(clock.now())) {
            throw new DianCertificateExpiredException("El certificado DIAN configurado esta vencido.");
        }
        if (!configuration.isRealModeComplete(clock.now())) {
            throw new DianConfigurationIncompleteException("La configuracion DIAN real esta incompleta.");
        }
    }

    private void saveEvent(SubmitProviderDocumentCommand command, UUID submissionId, DianSubmissionEventType eventType,
            DianSubmissionEventStatus status, String dianCode, String message) {
        traceRepository.saveEvent(new DianSubmissionEvent(idGenerator.generate(), command.companyId(), submissionId,
                command.documentId(), eventType, status, dianCode, sanitize(message), null, clock.now()));
    }

    private void storeArtifact(SubmitProviderDocumentCommand command, UUID submissionId, DianArtifactType type,
            String contentType, String fileName, String content) {
        traceRepository.saveArtifact(artifactStorage.store(command.companyId(), submissionId, command.documentId(), type,
                contentType, fileName, content, clock.now()));
    }

    private static String safeRawResponse(DianTransportResult result) {
        return "{\"trackingId\":\"" + result.trackingId() + "\",\"status\":\"" + result.status()
                + "\",\"dianCode\":\"" + safe(result.dianCode()) + "\",\"dianMessage\":\""
                + safe(result.dianMessage()) + "\"}";
    }

    private static void validate(SubmitProviderDocumentCommand command) {
        if (command == null || command.companyId() == null || command.documentId() == null
                || command.documentType() == null) {
            throw new IllegalArgumentException("La solicitud del conector DIAN tiene campos obligatorios incompletos.");
        }
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("La clave de idempotencia es obligatoria.");
        }
    }

    private static String sanitize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.replaceAll("(?i)(password|pin|token|certificate|certificado|clave)=[^,\\s]+", "$1=***");
    }

    private static String safe(String value) {
        return value == null ? "" : value.replace("\"", "'");
    }
}
