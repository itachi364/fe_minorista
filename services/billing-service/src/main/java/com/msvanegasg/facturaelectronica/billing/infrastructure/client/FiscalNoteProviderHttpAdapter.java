package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.application.port.out.FiscalNoteProviderPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;

@Component
public class FiscalNoteProviderHttpAdapter implements FiscalNoteProviderPort {

    private final RestClient restClient;

    public FiscalNoteProviderHttpAdapter(BillingProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.providerServiceUrl()).build();
    }

    @Override
    public ProviderSubmissionResult submit(UUID companyId, UUID noteId, ElectronicDocumentType documentType,
            Map<String, Object> payload, String idempotencyKey) {
        ProviderSubmissionResponse response = restClient.post()
                .uri(providerPath(documentType))
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new ProviderSubmissionRequest(companyId, noteId, payload))
                .retrieve()
                .body(ProviderSubmissionResponse.class);
        if (response == null) {
            throw new IllegalStateException("El conector DIAN mock no retorno respuesta.");
        }
        return new ProviderSubmissionResult(ProviderStatus.valueOf(response.status()), response.trackingId(),
                response.cufeCude(), response.qrContent(), response.errorCode(), response.errorMessage());
    }

    private static String providerPath(ElectronicDocumentType documentType) {
        return switch (documentType) {
            case CREDIT_NOTE -> "/api/v1/provider/credit-notes";
            case DEBIT_NOTE -> "/api/v1/provider/debit-notes";
            case POS_ADJUSTMENT_NOTE -> "/api/v1/provider/pos-adjustment-notes";
            case ELECTRONIC_INVOICE, ELECTRONIC_POS -> throw new IllegalArgumentException("unsupported note type");
        };
    }

    record ProviderSubmissionRequest(UUID companyId, UUID documentId, Map<String, Object> payload) {
    }

    record ProviderSubmissionResponse(String trackingId, String status, String cufeCude, String qrContent,
            String errorCode, String errorMessage, String rawResponse) {
    }
}
