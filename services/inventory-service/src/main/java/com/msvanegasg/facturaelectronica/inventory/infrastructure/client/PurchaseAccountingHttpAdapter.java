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

import com.msvanegasg.facturaelectronica.inventory.application.port.out.PurchaseAccountingPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PaymentCondition;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;

@Component
public class PurchaseAccountingHttpAdapter implements PurchaseAccountingPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(PurchaseAccountingHttpAdapter.class);

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
            postAccountingEntry(purchase);
            if (purchase.paymentCondition() == PaymentCondition.CREDIT) {
                postAccountsPayable(purchase);
            }
        } catch (RuntimeException exception) {
            LOGGER.warn("Could not apply purchase accounting for company {} purchase {}", purchase.companyId(),
                    purchase.id(), exception);
            throw exception;
        }
    }

    private void postAccountingEntry(Purchase purchase) {
        restClient.post()
                .uri(accountingBaseUrl + "/api/v1/accounting-entries")
                .header("X-Company-Id", purchase.companyId().toString())
                .body(new AccountingEntryRequest("PURCHASE_CONFIRMED", "PURCHASE", purchase.id(),
                        entryDate(purchase).toString(), "Factura de compra", purchase.supplierId(), purchase.subtotal(),
                        purchase.taxTotal(), purchase.total()))
                .retrieve()
                .toBodilessEntity();
    }

    private void postAccountsPayable(Purchase purchase) {
        restClient.post()
                .uri(accountingBaseUrl + "/api/v1/accounts-payable")
                .header("X-Company-Id", purchase.companyId().toString())
                .body(new AccountsPayableRequest(purchase.supplierId(), "PURCHASE", purchase.id(),
                        entryDate(purchase).toString(), purchase.dueDate().toString(), purchase.total()))
                .retrieve()
                .toBodilessEntity();
    }

    private static LocalDate entryDate(Purchase purchase) {
        return purchase.confirmedAt() == null ? LocalDate.now(ZoneOffset.UTC)
                : purchase.confirmedAt().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private record AccountingEntryRequest(String eventType, String sourceType, UUID sourceId, String entryDate,
            String description, UUID thirdpartyId, BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total) {
    }

    private record AccountsPayableRequest(UUID supplierId, String sourceType, UUID sourceId, String issueDate,
            String dueDate, BigDecimal totalAmount) {
    }
}
