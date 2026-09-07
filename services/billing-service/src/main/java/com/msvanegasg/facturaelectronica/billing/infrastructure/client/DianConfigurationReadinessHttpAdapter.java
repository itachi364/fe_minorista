package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import java.util.UUID;

import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.msvanegasg.facturaelectronica.billing.application.port.out.DianConfigurationReadinessPort;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;

@Component
public class DianConfigurationReadinessHttpAdapter implements DianConfigurationReadinessPort {

    private final RestClient restClient;

    public DianConfigurationReadinessHttpAdapter(BillingProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.providerServiceUrl()).build();
    }

    @Override
    public boolean isReadyForElectronicIssuing(UUID companyId) {
        try {
            DianConfigurationResponse response = restClient.get()
                    .uri("/api/v1/dian-configuration/companies/{companyId}", companyId)
                    .retrieve()
                    .body(DianConfigurationResponse.class);
            return response != null
                    && companyId.equals(response.companyId())
                    && "REAL".equals(response.mode())
                    && "ACTIVE".equals(response.status())
                    && "SUCCESS".equals(response.lastTestStatus())
                    && response.certificateConfigured();
        } catch (RestClientException exception) {
            return false;
        }
    }

    record DianConfigurationResponse(UUID companyId, String mode, boolean certificateConfigured, String status,
            String lastTestStatus) {
    }
}
