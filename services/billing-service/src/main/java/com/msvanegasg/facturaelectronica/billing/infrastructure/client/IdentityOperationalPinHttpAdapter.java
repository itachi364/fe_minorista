package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.billing.application.port.out.OperationalPinValidationPort;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;

@Component
public class IdentityOperationalPinHttpAdapter implements OperationalPinValidationPort {

    private final RestClient restClient;

    public IdentityOperationalPinHttpAdapter(BillingProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.identityServiceUrl()).build();
    }

    @Override
    public OperationalPinValidationResult verify(UUID companyId, String pin, String authorizationHeader) {
        OperationalPinResponse response = restClient.post()
                .uri("/api/v1/companies/{companyId}/operational-pin/verify", companyId)
                .headers(headers -> {
                    if (authorizationHeader != null && !authorizationHeader.isBlank()) {
                        headers.set("Authorization", authorizationHeader);
                    }
                })
                .contentType(MediaType.APPLICATION_JSON)
                .body(new OperationalPinRequest(pin))
                .retrieve()
                .body(OperationalPinResponse.class);
        if (response == null) {
            throw new IllegalStateException("No fue posible validar el PIN operacional.");
        }
        return new OperationalPinValidationResult(response.valid(), response.locked(), response.mustChange(),
                response.remainingAttempts());
    }

    record OperationalPinRequest(String pin) {
    }

    record OperationalPinResponse(boolean valid, boolean locked, boolean mustChange, int remainingAttempts) {
    }
}
