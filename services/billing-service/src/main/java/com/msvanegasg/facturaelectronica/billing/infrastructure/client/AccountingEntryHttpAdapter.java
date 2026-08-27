package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.msvanegasg.facturaelectronica.billing.application.port.out.AccountingEntryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;

@Component
public class AccountingEntryHttpAdapter implements AccountingEntryPort {

    private static final String SALE_CONFIRMED_EVENT = "SALE_CONFIRMED";
    private static final String ACCOUNTING_SETUP_REQUIRED_MESSAGE =
            "Debes inicializar la configuracion contable basica antes de cerrar ventas.";
    private static final String ACCOUNTING_SETUP_UNAVAILABLE_MESSAGE =
            "No fue posible validar la configuracion contable antes de cerrar la venta.";
    private static final String ACCOUNTING_ENTRY_FAILED_MESSAGE =
            "No fue posible registrar el asiento contable de la venta. Revisa la configuracion contable.";
    private static final ParameterizedTypeReference<List<Map<String, Object>>> ACCOUNTING_RULE_LIST_TYPE =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;

    public AccountingEntryHttpAdapter(BillingProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.accountingServiceUrl()).build();
    }

    @Override
    public void ensureSalePostingConfigured(UUID companyId) {
        try {
            List<Map<String, Object>> activeRules = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/accounting-rules")
                            .queryParam("eventType", SALE_CONFIRMED_EVENT)
                            .queryParam("active", true)
                            .build())
                    .header("X-Company-Id", companyId.toString())
                    .retrieve()
                    .body(ACCOUNTING_RULE_LIST_TYPE);
            if (activeRules == null || activeRules.isEmpty()) {
                throw new IllegalStateException(ACCOUNTING_SETUP_REQUIRED_MESSAGE);
            }
        } catch (IllegalStateException exception) {
            throw exception;
        } catch (RestClientException exception) {
            throw new IllegalStateException(ACCOUNTING_SETUP_UNAVAILABLE_MESSAGE, exception);
        }
    }

    @Override
    public void postSale(Sale sale, String idempotencyKey) {
        try {
            restClient.post()
                    .uri("/api/v1/accounting-entries")
                    .header("X-Company-Id", sale.companyId().toString())
                    .header("Idempotency-Key", idempotencyKey + "-accounting")
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(AccountingEntryRequest.from(sale))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            throw new IllegalStateException(ACCOUNTING_ENTRY_FAILED_MESSAGE, exception);
        }
    }

    record AccountingEntryRequest(String eventType, String sourceType, UUID sourceId, String entryDate,
            String description, UUID thirdpartyId, BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total) {

        static AccountingEntryRequest from(Sale sale) {
            String entryDate = sale.confirmedAt().atZone(ZoneOffset.UTC).toLocalDate().toString();
            return new AccountingEntryRequest("SALE_CONFIRMED", "SALE", sale.id(), entryDate,
                    "Venta facturada " + sale.id(), sale.customerId(), sale.subtotal(), sale.taxTotal(),
                    sale.total());
        }
    }
}
