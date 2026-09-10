package com.msvanegasg.facturaelectronica.inventory.infrastructure.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.List;

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
            WithholdingCalculationResponse fiscal = calculateWithholdings(purchase);
            postAccountingEntry(purchase, fiscal);
            if (purchase.paymentCondition() == PaymentCondition.CREDIT) {
                postAccountsPayable(purchase, fiscal.netPayable());
            }
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

    private WithholdingCalculationResponse calculateWithholdings(Purchase purchase) {
        WithholdingCalculationResponse response = restClient.post()
                .uri(accountingBaseUrl + "/api/v1/fiscal-calculations/withholdings/documents")
                .header("X-Company-Id", purchase.companyId().toString())
                .body(new WithholdingCalculationRequest("PURCHASE", purchase.supplierId(),
                        entryDate(purchase).toString(), null, "PURCHASE", purchase.id(), purchase.lines().stream()
                                .map(line -> new FiscalLineRequest(line.id(),
                                        purchase.fiscalConceptCode() == null ? "ANY" : purchase.fiscalConceptCode(),
                                        null, line.subtotal(), line.tax()))
                                .toList()))
                .retrieve()
                .body(WithholdingCalculationResponse.class);
        if (response == null) {
            throw new IllegalStateException("El motor fiscal no devolvio un resultado para la compra.");
        }
        return response;
    }

    private void postAccountingEntry(Purchase purchase, WithholdingCalculationResponse fiscal) {
        restClient.post()
                .uri(accountingBaseUrl + "/api/v1/accounting-entries")
                .header("X-Company-Id", purchase.companyId().toString())
                .body(new AccountingEntryRequest("PURCHASE_CONFIRMED", "PURCHASE", purchase.id(),
                        entryDate(purchase).toString(), "Factura de compra", purchase.supplierId(),
                        purchase.subtotal(), purchase.taxTotal(), purchase.total(), fiscal.amount("RETEFUENTE"),
                        fiscal.amount("RETEIVA"), fiscal.amount("RETEICA"), fiscal.amount("AUTORETENCION"),
                        fiscal.withholdingTotal(), fiscal.netPayable()))
                .retrieve()
                .toBodilessEntity();
    }

    private void postAccountsPayable(Purchase purchase, BigDecimal netPayable) {
        restClient.post()
                .uri(accountingBaseUrl + "/api/v1/accounts-payable")
                .header("X-Company-Id", purchase.companyId().toString())
                .body(new AccountsPayableRequest(purchase.supplierId(), "PURCHASE", purchase.id(),
                        entryDate(purchase).toString(), purchase.dueDate().toString(), netPayable))
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

    private record AccountingEntryRequest(String eventType, String sourceType, UUID sourceId, String entryDate,
            String description, UUID thirdpartyId, BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total,
            BigDecimal retefuente, BigDecimal reteiva, BigDecimal reteica, BigDecimal selfWithholding,
            BigDecimal withholdingTotal, BigDecimal netPayable) {
    }

    private record AccountsPayableRequest(UUID supplierId, String sourceType, UUID sourceId, String issueDate,
            String dueDate, BigDecimal totalAmount) {
    }

    private record WithholdingCalculationRequest(String operationType, UUID thirdPartyId, String operationDate,
            String municipalityCode, String sourceType, UUID sourceId, List<FiscalLineRequest> lines) {
    }

    private record FiscalLineRequest(UUID lineId, String conceptCode, String ciiuCode,
            BigDecimal taxableBaseAmount, BigDecimal taxAmount) { }

    private record WithholdingCalculationItem(String withholdingType, String decision, BigDecimal amount) {
    }

    private record FiscalLineResponse(List<WithholdingCalculationItem> items) { }

    private record WithholdingCalculationResponse(List<FiscalLineResponse> lines, BigDecimal grossAmount,
            BigDecimal withholdingTotal, BigDecimal netPayable) {
        BigDecimal amount(String type) {
            if (lines == null) {
                return BigDecimal.ZERO;
            }
            return lines.stream().filter(line -> line.items() != null).flatMap(line -> line.items().stream())
                    .filter(item -> type.equals(item.withholdingType()) && "APPLIED".equals(item.decision()))
                    .map(WithholdingCalculationItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }
}
