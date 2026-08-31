package com.msvanegasg.facturaelectronica.payroll.infrastructure.client;

import java.math.BigDecimal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.payroll.application.port.out.PayrollAccountingPort;
import com.msvanegasg.facturaelectronica.payroll.domain.model.DailyLaborPayment;

@Component
public class PayrollAccountingHttpAdapter implements PayrollAccountingPort {

    private final RestClient restClient;
    private final String accountingBaseUrl;

    public PayrollAccountingHttpAdapter(RestClient.Builder restClientBuilder,
            @Value("${services.accounting.base-url:}") String accountingBaseUrl) {
        this.restClient = restClientBuilder.build();
        this.accountingBaseUrl = accountingBaseUrl;
    }

    @Override
    public void applyDailyPayment(DailyLaborPayment payment) {
        if (accountingBaseUrl == null || accountingBaseUrl.isBlank()) {
            return;
        }
        restClient.post()
                .uri(accountingBaseUrl + "/api/v1/accounting-entries")
                .header("X-Company-Id", payment.companyId().toString())
                .header("Idempotency-Key", "payroll-daily-payment-" + payment.id())
                .body(AccountingEntryRequest.from(payment))
                .retrieve()
                .toBodilessEntity();
    }

    private record AccountingEntryRequest(String eventType, String sourceType, UUID sourceId, String entryDate,
            String description, UUID thirdpartyId, BigDecimal subtotal, BigDecimal taxTotal, BigDecimal total) {

        private static AccountingEntryRequest from(DailyLaborPayment payment) {
            return new AccountingEntryRequest("PAYROLL_DAILY_PAYMENT_REGISTERED", "PAYROLL_DAILY_PAYMENT",
                    payment.id(), payment.workDate().toString(), "Pago diario de nomina", payment.workerId(), BigDecimal.ZERO,
                    BigDecimal.ZERO, payment.paidAmount());
        }
    }
}
