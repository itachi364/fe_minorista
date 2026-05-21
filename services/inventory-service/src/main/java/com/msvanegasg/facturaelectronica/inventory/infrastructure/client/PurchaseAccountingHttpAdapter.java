package com.msvanegasg.facturaelectronica.inventory.infrastructure.client;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.inventory.application.port.out.PurchaseAccountingPort;
import com.msvanegasg.facturaelectronica.inventory.domain.model.PaymentCondition;
import com.msvanegasg.facturaelectronica.inventory.domain.model.Purchase;

@Component
public class PurchaseAccountingHttpAdapter implements PurchaseAccountingPort {

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
        } catch (RuntimeException ignored) {
            // Accounting is intentionally best-effort until the NATS outbox/inbox flow is implemented.
        }
    }

    private void postAccountingEntry(Purchase purchase) {
        restClient.post()
                .uri(accountingBaseUrl + "/api/v1/accounting-entries")
                .header("X-Company-Id", purchase.companyId().toString())
                .body(new AccountingEntryRequest("PURCHASE_CONFIRMED", "PURCHASE", purchase.id(),
                        entryDate(purchase), "Compra de inventario", purchase.supplierId(), purchase.subtotal(),
                        purchase.taxTotal(), purchase.total()))
                .retrieve()
                .toBodilessEntity();
    }

    private void postAccountsPayable(Purchase purchase) {
        restClient.post()
                .uri(accountingBaseUrl + "/api/v1/accounts-payable")
                .header("X-Company-Id", purchase.companyId().toString())
                .body(new AccountsPayableRequest(purchase.supplierId(), "PURCHASE", purchase.id(),
                        entryDate(purchase), purchase.dueDate(), purchase.total()))
                .retrieve()
                .toBodilessEntity();
    }

    private static LocalDate entryDate(Purchase purchase) {
        return purchase.confirmedAt() == null ? LocalDate.now(ZoneOffset.UTC)
                : purchase.confirmedAt().atZone(ZoneOffset.UTC).toLocalDate();
    }

    private record AccountingEntryRequest(String eventType, String sourceType, UUID sourceId, LocalDate entryDate,
            String description, UUID thirdpartyId, BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total) {
    }

    private record AccountsPayableRequest(UUID supplierId, String sourceType, UUID sourceId, LocalDate issueDate,
            LocalDate dueDate, BigDecimal totalAmount) {
    }
}
