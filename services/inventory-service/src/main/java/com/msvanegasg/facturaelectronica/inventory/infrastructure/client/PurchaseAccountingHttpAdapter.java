package com.msvanegasg.facturaelectronica.inventory.infrastructure.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.msvanegasg.facturaelectronica.inventory.application.port.out.PurchaseAccountingPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PaymentCondition;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;

@Component
public class PurchaseAccountingHttpAdapter implements PurchaseAccountingPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseAccountingHttpAdapter.class);
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final RestClient restClient;
    private final String accountingBaseUrl;

    public PurchaseAccountingHttpAdapter(RestClient.Builder restClientBuilder,
            @Value("${services.accounting.base-url:}") String accountingBaseUrl) {
        this.restClient = restClientBuilder.build();
        this.accountingBaseUrl = accountingBaseUrl;
    }

    @Override
    public void applyConfirmedPurchase(Purchase purchase, UUID createdBy) {
        if (accountingBaseUrl == null || accountingBaseUrl.isBlank()) {
            return;
        }
        try {
            confirmFiscalAndAccounting(purchase);
        } catch (RestClientResponseException exception) {
            LOGGER.warn("Could not apply purchase accounting for company {} purchase {}", purchase.companyId(),
                    purchase.id(), exception);
            if (exception.getStatusCode().is4xxClientError()) {
                throw new IllegalStateException(responseMessage(exception), exception);
            }
            throw exception;
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not apply purchase accounting for company {} purchase {}", purchase.companyId(),
                    purchase.id(), exception);
            throw exception;
        }
    }

    private void confirmFiscalAndAccounting(Purchase purchase) {
        restClient.post()
                .uri(accountingBaseUrl + "/api/v1/fiscal-confirmations/purchases")
                .header("X-Company-Id", purchase.companyId().toString())
                .body(new PurchaseFiscalConfirmationRequest(purchase.id(), purchase.supplierId(),
                        entryDate(purchase).toString(), null,
                        purchase.paymentCondition() == PaymentCondition.CREDIT,
                        purchase.dueDate() == null ? null : purchase.dueDate().toString(), purchase.subtotal(),
                        purchase.taxTotal(), purchase.total(), null, purchase.lines().stream()
                                .map(line -> new FiscalLineRequest(line.id(),
                                        purchase.fiscalConceptCode() == null ? "ANY" : purchase.fiscalConceptCode(),
                                        null, line.subtotal(), line.tax(), BigDecimal.ZERO, line.total()))
                                .toList()))
                .retrieve()
                .toBodilessEntity();
    }

    private static LocalDate entryDate(Purchase purchase) {
        return purchase.confirmedAt() == null ? LocalDate.now(ZoneOffset.UTC)
                : purchase.confirmedAt().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private static String responseMessage(RestClientResponseException exception) {
        try {
            JsonNode body = OBJECT_MAPPER.readTree(exception.getResponseBodyAsString());
            String message = body.path("message").asText();
            if (!message.isBlank()) {
                return message;
            }
        } catch (com.fasterxml.jackson.core.JsonProcessingException ignored) {
            // Use a stable functional message when the remote body is not valid JSON.
        }
        return "El motor fiscal rechazo la confirmacion de la compra.";
    }

    private record PurchaseFiscalConfirmationRequest(UUID sourceId, UUID supplierId, String operationDate,
            String municipalityCode, boolean creditPurchase, String dueDate, BigDecimal subtotal,
            BigDecimal taxTotal, BigDecimal total, UUID contractId, java.util.List<FiscalLineRequest> lines) {
    }

    private record FiscalLineRequest(UUID lineId, String conceptCode, String ciiuCode,
            BigDecimal taxableBaseAmount, BigDecimal taxAmount, BigDecimal aiuAmount,
            BigDecimal grossPaymentAmount) { }
}
