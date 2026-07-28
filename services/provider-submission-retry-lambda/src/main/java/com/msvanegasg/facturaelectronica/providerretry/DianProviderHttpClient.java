package com.msvanegasg.facturaelectronica.providerretry;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DianProviderHttpClient implements ProviderSubmissionClientPort {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final ProviderRetrySettings settings;

    public DianProviderHttpClient(ObjectMapper objectMapper, ProviderRetrySettings settings) {
        this(HttpClient.newHttpClient(), objectMapper, settings);
    }

    DianProviderHttpClient(HttpClient httpClient, ObjectMapper objectMapper, ProviderRetrySettings settings) {
        this.httpClient = Objects.requireNonNull(httpClient, "httpClient is required");
        this.objectMapper = Objects.requireNonNull(objectMapper, "objectMapper is required");
        this.settings = Objects.requireNonNull(settings, "settings is required");
    }

    @Override
    public ProviderSubmissionOutcome submit(BillingDocumentSnapshot snapshot) {
        try {
            String body = objectMapper.writeValueAsString(request(snapshot));
            HttpRequest request = HttpRequest.newBuilder(endpoint(snapshot.documentType()))
                    .header("Content-Type", "application/json")
                    .header("Idempotency-Key", snapshot.idempotencyKey())
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                throw new IllegalStateException("provider returned HTTP " + response.statusCode());
            }
            ProviderSubmissionResponse provider = objectMapper.readValue(response.body(), ProviderSubmissionResponse.class);
            return new ProviderSubmissionOutcome(ProviderStatus.valueOf(provider.status()), provider.trackingId(),
                    provider.cufeCude(), provider.qrContent(), provider.errorCode(), provider.errorMessage());
        } catch (IOException exception) {
            throw new IllegalStateException("provider retry request could not be sent", exception);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("provider retry request was interrupted", exception);
        }
    }

    private URI endpoint(String documentType) {
        return settings.providerBaseUri().resolve(providerPath(documentType));
    }

    private static String providerPath(String documentType) {
        return switch (documentType) {
            case "ELECTRONIC_POS" -> "/api/v1/provider/electronic-pos";
            case "ELECTRONIC_INVOICE" -> "/api/v1/provider/electronic-invoices";
            case "CREDIT_NOTE" -> "/api/v1/provider/credit-notes";
            case "DEBIT_NOTE" -> "/api/v1/provider/debit-notes";
            case "POS_ADJUSTMENT_NOTE" -> "/api/v1/provider/pos-adjustment-notes";
            default -> throw new IllegalArgumentException("unsupported documentType " + documentType);
        };
    }

    private static ProviderSubmissionRequest request(BillingDocumentSnapshot snapshot) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("saleId", snapshot.saleId().toString());
        payload.put("documentType", snapshot.documentType());
        payload.put("saleChannel", snapshot.saleChannel());
        payload.put("subtotal", asPlain(snapshot.subtotal()));
        payload.put("taxTotal", asPlain(snapshot.taxTotal()));
        payload.put("total", asPlain(snapshot.total()));
        payload.put("lines", snapshot.lines().stream().map(DianProviderHttpClient::linePayload).toList());
        return new ProviderSubmissionRequest(snapshot.companyId().toString(), snapshot.documentId().toString(), payload);
    }

    private static Map<String, Object> linePayload(SaleLineSnapshot line) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("productId", line.productId().toString());
        payload.put("productSku", line.productSku());
        payload.put("productName", line.productName());
        payload.put("itemType", line.itemType());
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
        return (value == null ? BigDecimal.ZERO : value).stripTrailingZeros().toPlainString();
    }

    record ProviderSubmissionRequest(String companyId, String documentId, Map<String, Object> payload) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ProviderSubmissionResponse(String trackingId, String status, String cufeCude, String qrContent,
            String errorCode, String errorMessage) {
    }
}