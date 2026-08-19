package com.msvanegasg.facturaelectronica.dianprovider.application.usecase;

import java.util.Locale;
import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.SubmitProviderDocumentCommand;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.in.FindProviderSubmissionUseCase;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.in.SubmitProviderDocumentUseCase;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.ProviderSubmissionRepositoryPort;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.CudeHashGenerator;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmission;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmissionStatus;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.config.DianProviderProperties;

public class MockProviderSubmissionService implements SubmitProviderDocumentUseCase, FindProviderSubmissionUseCase {

    private final ProviderSubmissionRepositoryPort repository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;
    private final DianProviderProperties properties;

    public MockProviderSubmissionService(ProviderSubmissionRepositoryPort repository, IdGeneratorPort idGenerator,
            ClockPort clock, DianProviderProperties properties) {
        this.repository = repository;
        this.idGenerator = idGenerator;
        this.clock = clock;
        this.properties = properties;
    }

    @Override
    public ProviderSubmissionResult submit(SubmitProviderDocumentCommand command) {
        validateMode();
        validate(command);
        return repository
                .findByIdempotencyKey(command.companyId(), command.documentId(), command.documentType(),
                        command.idempotencyKey())
                .map(ProviderSubmissionResultMapper::toResult)
                .orElseGet(() -> ProviderSubmissionResultMapper.toResult(repository.save(toSubmission(command))));
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

    private ProviderSubmission toSubmission(SubmitProviderDocumentCommand command) {
        ProviderSubmissionStatus status = properties.mockDefaultStatus();
        String trackingId = "mock-" + command.documentType().name().toLowerCase(Locale.ROOT) + "-"
                + command.documentId();
        String source = command.companyId() + "|" + command.documentId() + "|" + command.documentType() + "|"
                + command.idempotencyKey();
        String cude = status == ProviderSubmissionStatus.ACCEPTED ? CudeHashGenerator.generate(source) : null;
        String qr = cude == null ? null : "mock-qr:" + cude;
        String errorCode = errorCode(status);
        String errorMessage = errorMessage(status);
        String rawResponse = safeRawResponse(status, trackingId, cude, errorCode, errorMessage);
        return new ProviderSubmission(idGenerator.generate(), command.companyId(), command.documentId(),
                command.documentType(), command.idempotencyKey(), trackingId, status, cude, qr, errorCode,
                errorMessage, clock.now(), command.payload(), rawResponse);
    }

    private String errorCode(ProviderSubmissionStatus status) {
        if (status == ProviderSubmissionStatus.ACCEPTED) {
            return null;
        }
        if (properties.mockErrorCode() != null && !properties.mockErrorCode().isBlank()) {
            return properties.mockErrorCode();
        }
        return status == ProviderSubmissionStatus.REJECTED ? "MOCK_REJECTED" : "MOCK_FAILED";
    }

    private String errorMessage(ProviderSubmissionStatus status) {
        if (status == ProviderSubmissionStatus.ACCEPTED) {
            return null;
        }
        if (properties.mockErrorMessage() != null && !properties.mockErrorMessage().isBlank()) {
            return properties.mockErrorMessage();
        }
        return status == ProviderSubmissionStatus.REJECTED
                ? "Documento rechazado por conector DIAN mock."
                : "Fallo tecnico simulado por conector DIAN mock.";
    }

    private static String safeRawResponse(ProviderSubmissionStatus status, String trackingId, String cude,
            String errorCode, String errorMessage) {
        return "{\"trackingId\":\"" + trackingId + "\",\"status\":\"" + status + "\",\"cufeCude\":\""
                + (cude == null ? "" : cude) + "\",\"errorCode\":\"" + (errorCode == null ? "" : errorCode)
                + "\",\"errorMessage\":\"" + (errorMessage == null ? "" : errorMessage) + "\"}";
    }

    private void validateMode() {
        if (!"mock".equalsIgnoreCase(properties.mode())) {
            throw new IllegalStateException("Solo DIAN_PROVIDER_MODE=mock esta soportado en esta version.");
        }
    }

    private static void validate(SubmitProviderDocumentCommand command) {
        if (command == null || command.companyId() == null || command.documentId() == null
                || command.documentType() == null) {
            throw new IllegalArgumentException("La solicitud del proveedor tiene campos obligatorios incompletos.");
        }
        if (command.idempotencyKey() == null || command.idempotencyKey().isBlank()) {
            throw new IllegalArgumentException("La clave de idempotencia es obligatoria.");
        }
    }
}
