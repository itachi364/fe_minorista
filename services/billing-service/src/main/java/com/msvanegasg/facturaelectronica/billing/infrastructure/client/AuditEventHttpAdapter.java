package com.msvanegasg.facturaelectronica.billing.infrastructure.client;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.msvanegasg.facturaelectronica.billing.application.dto.AuditEventCommand;
import com.msvanegasg.facturaelectronica.billing.application.dto.AuditResult;
import com.msvanegasg.facturaelectronica.billing.application.port.out.AuditEventPort;
import com.msvanegasg.facturaelectronica.billing.infrastructure.config.BillingProperties;

@Component
public class AuditEventHttpAdapter implements AuditEventPort {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditEventHttpAdapter.class);

    private final RestClient restClient;

    public AuditEventHttpAdapter(BillingProperties properties) {
        this.restClient = RestClient.builder().baseUrl(properties.auditServiceUrl()).build();
    }

    @Override
    public void register(AuditEventCommand command) {
        try {
            restClient.post()
                    .uri("/api/v1/audit-events")
                    .header("X-Company-Id", command.companyId().toString())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(AuditEventRequest.from(command))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RestClientException exception) {
            LOGGER.warn("event=audit_event_publish_failed companyId={} resourceType={} resourceId={} action={}",
                    command.companyId(), command.resourceType(), command.resourceId(), command.action(), exception);
        }
    }

    record AuditEventRequest(UUID userId, String eventType, String resourceType, String resourceId, String action,
            AuditResult result, String detail) {

        static AuditEventRequest from(AuditEventCommand command) {
            return new AuditEventRequest(command.userId(), command.eventType(), command.resourceType(),
                    command.resourceId(), command.action(), command.result(), command.detail());
        }
    }
}
