package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import java.math.BigDecimal;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.billing.application.port.out.AccountingEntryPort;
import com.msvanegasg.facturaelectronica.billing.domain.model.Sale;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;

@Component
public class AccountingEntryHttpAdapter implements AccountingEntryPort {

    private final RestClient restClient;

    public AccountingEntryHttpAdapter(BillingProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.accountingServiceUrl()).build();
    }

    @Override
    public void postSale(Sale sale, String idempotencyKey) {
        restClient.post()
                .uri("/api/v1/accounting-entries")
                .header("X-Company-Id", sale.companyId().toString())
                .header("Idempotency-Key", idempotencyKey + "-accounting")
                .contentType(MediaType.APPLICATION_JSON)
                .body(AccountingEntryRequest.from(sale))
                .retrieve()
                .toBodilessEntity();
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
