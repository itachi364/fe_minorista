package com.msvanegasg.facturaelectronica.identity.infrastructure.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.msvanegasg.facturaelectronica.identity.application.port.out.LicenseValidationPort;
import com.msvanegasg.facturaelectronica.identity.application.usecase.LicenseBlockedException;
import com.msvanegasg.facturaelectronica.identity.domain.model.LicenseAction;

@Component
public class TenantLicenseHttpAdapter implements LicenseValidationPort {

    private final RestClient restClient;

    public TenantLicenseHttpAdapter(@Value("${identity.tenant-service-url}") String tenantServiceUrl) {
        this.restClient = RestClient.builder().baseUrl(tenantServiceUrl).build();
    }

    @Override
    public void ensureAllowed(UUID companyId, LicenseAction action) {
        try {
            LicenseValidationResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder.path("/api/v1/companies/{companyId}/license/validation")
                            .queryParam("action", action.name())
                            .build(companyId))
                    .retrieve()
                    .body(LicenseValidationResponse.class);
            if (response == null) {
                throw new LicenseBlockedException("No fue posible validar la licencia de la empresa.");
            }
            if (!response.allowed()) {
                throw new LicenseBlockedException(response.message());
            }
        } catch (RestClientException exception) {
            throw new LicenseBlockedException("No fue posible validar la licencia de la empresa.");
        }
    }

    record LicenseValidationResponse(UUID companyId, LicenseAction action, boolean allowed, String status,
            String reasonCode, String message) {
    }
}
