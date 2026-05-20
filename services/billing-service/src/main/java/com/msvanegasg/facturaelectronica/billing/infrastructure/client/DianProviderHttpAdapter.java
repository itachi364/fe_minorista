package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.billing.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.billing.application.port.out.ElectronicDocumentProviderPort;
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
    public ProviderSubmissionResult submitElectronicPos(Sale sale, UUID documentId, String idempotencyKey) {
        ProviderSubmissionResponse response = restClient.post()
                .uri("/api/v1/provider/electronic-pos")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(ProviderSubmissionRequest.from(sale, documentId))
                .retrieve()
                .body(ProviderSubmissionResponse.class);
        if (response == null) {
            throw new IllegalStateException("El proveedor DIAN mock no retorno respuesta.");
        }
        return new ProviderSubmissionResult(ProviderStatus.valueOf(response.status()), response.trackingId(),
                response.cufeCude(), response.qrContent(), response.errorCode(), response.errorMessage());
    }

    record ProviderSubmissionRequest(UUID companyId, UUID documentId, Map<String, Object> payload) {

        static ProviderSubmissionRequest from(Sale sale, UUID documentId) {
            Map<String, Object> payload = Map.of(
                    "saleId", sale.id(),
                    "saleChannel", sale.saleChannel().name(),
                    "subtotal", sale.subtotal(),
                    "discountTotal", sale.discountTotal(),
                    "taxTotal", sale.taxTotal(),
                    "total", sale.total(),
                    "lines", sale.lines().stream().map(ProviderSubmissionRequest::linePayload).toList());
            return new ProviderSubmissionRequest(sale.companyId(), documentId, payload);
        }

        private static Map<String, Object> linePayload(SaleLine line) {
            return Map.of(
                    "productId", line.productId(),
                    "quantity", asPlain(line.quantity()),
                    "unitPrice", asPlain(line.unitPrice()),
                    "discountAmount", asPlain(line.discountAmount()),
                    "taxCode", line.taxCode(),
                    "taxRate", asPlain(line.taxRate()),
                    "subtotal", asPlain(line.subtotal()),
                    "taxAmount", asPlain(line.taxAmount()),
                    "total", asPlain(line.total()));
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
