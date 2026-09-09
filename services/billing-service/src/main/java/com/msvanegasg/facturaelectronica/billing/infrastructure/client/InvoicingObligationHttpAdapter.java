package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.msvanegasg.facturaelectronica.billing.application.port.out.InvoicingObligationPort;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;

@Component
public class InvoicingObligationHttpAdapter implements InvoicingObligationPort {
    private final RestClient restClient;

    public InvoicingObligationHttpAdapter(BillingProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.tenantServiceUrl()).build();
    }

    @Override
    public boolean allowsNonFiscalSale(UUID companyId) {
        try {
            Response response = restClient.get()
                    .uri("/api/v1/companies/{companyId}/invoicing-obligation", companyId)
                    .retrieve()
                    .body(Response.class);
            return response != null && companyId.equals(response.companyId())
                    && "NOT_OBLIGATED_VERIFIED".equals(response.status());
        } catch (RestClientException exception) {
            return false;
        }
    }

    record Response(UUID companyId, String status) {}
}
