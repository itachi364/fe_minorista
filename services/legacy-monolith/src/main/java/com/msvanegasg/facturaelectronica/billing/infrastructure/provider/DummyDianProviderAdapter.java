package com.msvanegasg.facturaelectronica.billing.infrastructure.provider;

import com.msvanegasg.facturaelectronica.billing.application.dto.DianProviderRequest;
import com.msvanegasg.facturaelectronica.billing.application.dto.DianProviderResponse;
import com.msvanegasg.facturaelectronica.billing.application.port.out.DianProviderPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionStatus;

public class DummyDianProviderAdapter implements DianProviderPort {

    private static final String DEFAULT_REJECTION_CODE = "DUMMY_REJECTED";
    private static final String DEFAULT_REJECTION_MESSAGE = "document rejected by DIAN mock";
    private static final String DEFAULT_FAILURE_CODE = "DUMMY_FAILED";
    private static final String DEFAULT_FAILURE_MESSAGE = "document submission failed in DIAN mock";

    private final ProviderSubmissionStatus defaultStatus;
    private final String errorCode;
    private final String errorMessage;

    public DummyDianProviderAdapter() {
        this(ProviderSubmissionStatus.ACCEPTED, null, null);
    }

    public DummyDianProviderAdapter(
            ProviderSubmissionStatus defaultStatus,
            String errorCode,
            String errorMessage) {
        this.defaultStatus = defaultStatus == null ? ProviderSubmissionStatus.ACCEPTED : defaultStatus;
        this.errorCode = blankToNull(errorCode);
        this.errorMessage = blankToNull(errorMessage);
    }

    @Override
    public DianProviderResponse submit(DianProviderRequest request) {
        String identifier = request.documentId().toString().substring(0, 12).toUpperCase();
        if (defaultStatus == ProviderSubmissionStatus.REJECTED) {
            return rejectedResponse(identifier);
        }
        if (defaultStatus == ProviderSubmissionStatus.FAILED) {
            return failedResponse(identifier);
        }

        return acceptedResponse(request, identifier);
    }

    private DianProviderResponse acceptedResponse(DianProviderRequest request, String identifier) {
        String fiscalIdentifierPrefix = request.documentType() == ElectronicDocumentType.ELECTRONIC_POS
                ? "DUMMY-CUDE-"
                : "DUMMY-CUFE-";

        return new DianProviderResponse(
                ProviderSubmissionStatus.ACCEPTED,
                "DUMMY-SUBMISSION-" + identifier,
                fiscalIdentifierPrefix + identifier,
                "https://dummy-dian.local/documents/" + request.documentId(),
                request.payloadXml(),
                "DUMMY_GRAPHIC_REPRESENTATION",
                null,
                null);
    }

    private DianProviderResponse rejectedResponse(String identifier) {
        return new DianProviderResponse(
                ProviderSubmissionStatus.REJECTED,
                "DUMMY-REJECTION-" + identifier,
                null,
                null,
                null,
                null,
                valueOrDefault(errorCode, DEFAULT_REJECTION_CODE),
                valueOrDefault(errorMessage, DEFAULT_REJECTION_MESSAGE));
    }

    private DianProviderResponse failedResponse(String identifier) {
        return new DianProviderResponse(
                ProviderSubmissionStatus.FAILED,
                "DUMMY-FAILURE-" + identifier,
                null,
                null,
                null,
                null,
                valueOrDefault(errorCode, DEFAULT_FAILURE_CODE),
                valueOrDefault(errorMessage, DEFAULT_FAILURE_MESSAGE));
    }

    private static String valueOrDefault(String value, String defaultValue) {
        return value == null ? defaultValue : value;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
