package com.msvanegasg.facturaelectronica.billing.application.usecase;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

import com.msvanegasg.facturaelectronica.billing.application.dto.DianProviderRequest;
import com.msvanegasg.facturaelectronica.billing.application.dto.DianProviderResponse;
import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.application.dto.SubmitElectronicDocumentToProviderCommand;
import com.msvanegasg.facturaelectronica.billing.application.port.in.SubmitElectronicDocumentToProviderUseCase;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ClockPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.DianProviderPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.IdGeneratorPort;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ProviderSubmissionRecordRepositoryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionRecord;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionStatus;

public class SubmitElectronicDocumentToProviderService implements SubmitElectronicDocumentToProviderUseCase {

    private static final String PROVIDER_ERROR_CODE = "PROVIDER_ERROR";
    private static final String PROVIDER_ERROR_MESSAGE = "provider submission failed";

    private final DianProviderPort dianProvider;
    private final ProviderSubmissionRecordRepositoryPort submissionRecordRepository;
    private final IdGeneratorPort idGenerator;
    private final ClockPort clock;

    public SubmitElectronicDocumentToProviderService(
            DianProviderPort dianProvider,
            ProviderSubmissionRecordRepositoryPort submissionRecordRepository,
            IdGeneratorPort idGenerator,
            ClockPort clock) {
        this.dianProvider = Objects.requireNonNull(dianProvider);
        this.submissionRecordRepository = Objects.requireNonNull(submissionRecordRepository);
        this.idGenerator = Objects.requireNonNull(idGenerator);
        this.clock = Objects.requireNonNull(clock);
    }

    @Override
    public ProviderSubmissionResult submit(SubmitElectronicDocumentToProviderCommand command) {
        validate(command);

        DianProviderRequest request = new DianProviderRequest(
                command.companyId(),
                command.documentId(),
                command.documentType(),
                command.prefix(),
                command.number(),
                command.subtotal(),
                command.taxTotal(),
                command.total(),
                command.payloadXml(),
                command.idempotencyKey());

        DianProviderResponse response = submitSafely(request);
        ProviderSubmissionRecord record = submissionRecordRepository.save(toRecord(command, response));

        return new ProviderSubmissionResult(
                record.id(),
                record.documentId(),
                record.status(),
                record.providerSubmissionId(),
                record.cufeCude(),
                record.qrContent(),
                record.xmlContent(),
                record.graphicRepresentationContent(),
                record.errorCode(),
                record.errorMessage());
    }

    private DianProviderResponse submitSafely(DianProviderRequest request) {
        try {
            DianProviderResponse response = dianProvider.submit(request);
            if (response == null) {
                return providerFailureResponse();
            }
            return response;
        } catch (RuntimeException exception) {
            return providerFailureResponse();
        }
    }

    private static DianProviderResponse providerFailureResponse() {
        return new DianProviderResponse(
                ProviderSubmissionStatus.FAILED,
                null,
                null,
                null,
                null,
                null,
                PROVIDER_ERROR_CODE,
                PROVIDER_ERROR_MESSAGE);
    }

    private ProviderSubmissionRecord toRecord(
            SubmitElectronicDocumentToProviderCommand command,
            DianProviderResponse response) {
        return new ProviderSubmissionRecord(
                idGenerator.newId(),
                command.companyId(),
                command.documentId(),
                command.documentType(),
                command.idempotencyKey(),
                sha256(command.payloadXml()),
                response.status(),
                response.providerSubmissionId(),
                response.cufeCude(),
                response.qrContent(),
                response.xmlContent(),
                response.graphicRepresentationContent(),
                response.errorCode(),
                response.errorMessage(),
                clock.now());
    }

    private static void validate(SubmitElectronicDocumentToProviderCommand command) {
        Objects.requireNonNull(command, "command is required");
        Objects.requireNonNull(command.companyId(), "companyId is required");
        Objects.requireNonNull(command.documentId(), "documentId is required");
        Objects.requireNonNull(command.documentType(), "documentType is required");
        requirePositive(command.number(), "number");
        requireNonNegative(command.subtotal(), "subtotal");
        requireNonNegative(command.taxTotal(), "taxTotal");
        requireNonNegative(command.total(), "total");
        requireNonBlank(command.payloadXml(), "payloadXml");
        requireNonBlank(command.idempotencyKey(), "idempotencyKey");
    }

    private static void requirePositive(long value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }

    private static void requireNonNegative(BigDecimal value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
        if (value.signum() < 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than or equal to zero");
        }
    }

    private static void requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required");
        }
    }

    private static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}
