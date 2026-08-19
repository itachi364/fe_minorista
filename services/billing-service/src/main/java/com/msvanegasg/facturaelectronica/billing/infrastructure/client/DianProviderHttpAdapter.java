package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentProviderPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.ElectronicDocumentType;
import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderStatus;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.domain.model.SaleLine;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;

@Component
public class DianProviderHttpAdapter implements ElectronicDocumentProviderPort {

    private final RestClient restClient;

    public DianProviderHttpAdapter(BillingProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.providerServiceUrl()).build();
    }

    @Override
    public ProviderSubmissionResult submit(Sale sale, UUID documentId, ElectronicDocumentType documentType,
            String idempotencyKey) {
        ProviderSubmissionResponse response = restClient.post()
                .uri(providerPath(documentType))
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ProviderSubmissionRequest.from(sale, documentId, documentType))
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
            case ELECTRONIC_POS -> "/api/v1/provider/electronic-pos";
            case ELECTRONIC_INVOICE -> "/api/v1/provider/electronic-invoices";
            case CREDIT_NOTE -> "/api/v1/provider/credit-notes";
            case DEBIT_NOTE -> "/api/v1/provider/debit-notes";
            case POS_ADJUSTMENT_NOTE -> "/api/v1/provider/pos-adjustment-notes";
        };
    }

    record ProviderSubmissionRequest(UUID companyId, UUID documentId, Map<String, Object> payload) {

        static ProviderSubmissionRequest from(Sale sale, UUID documentId, ElectronicDocumentType documentType) {
            Map<String, Object> payload = Map.of(
                    "saleId", sale.id(),
                    "documentType", documentType.name(),
                    "saleChannel", sale.saleChannel().name(),
                    "subtotal", sale.subtotal(),
                    "discountTotal", sale.discountTotal(),
                    "taxTotal", sale.taxTotal(),
                    "total", sale.total(),
                    "lines", sale.lines().stream().map(ProviderSubmissionRequest::linePayload).toList());
            return new ProviderSubmissionRequest(sale.companyId(), documentId, payload);
        }

        private static Map<String, Object> linePayload(SaleLine line) {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("productId", line.productId());
            payload.put("productSku", line.productSku());
            payload.put("productName", line.productName());
            payload.put("itemType", line.itemType().name());
            payload.put("stockTracked", line.stockTracked());
            payload.put("quantity", asPlain(line.quantity()));
            payload.put("unitPrice", asPlain(line.unitPrice()));
            payload.put("discountAmount", asPlain(line.discountAmount()));
            payload.put("taxCode", line.taxCode());
            payload.put("taxRate", asPlain(line.taxRate()));
            payload.put("subtotal", asPlain(line.subtotal()));
            payload.put("taxAmount", asPlain(line.taxAmount()));
            payload.put("total", asPlain(line.total()));
            return payload;
        }

        private static String asPlain(BigDecimal value) {
            return value.stripTrailingZeros().toPlainString();
        }
    }

    record ProviderSubmissionResponse(String trackingId, String status, String cufeCude, String qrContent,
            String errorCode, String errorMessage, List<ProviderArtifactResponse> artifacts, String rawResponse) {
    }

    record ProviderArtifactResponse(String type, String storageUri, String contentHash) {
    }
}
