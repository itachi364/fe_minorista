package com.msvanegasg.facturaelectronica.catalog.infrastructure.client;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CatalogAuditEventCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.out.CatalogAuditEventPort;

@Component
public class AuditEventHttpAdapter implements CatalogAuditEventPort {

    private static final Logger log = LoggerFactory.getLogger(AuditEventHttpAdapter.class);

    private final RestClient restClient;
    private final String auditServiceUrl;

    public AuditEventHttpAdapter(RestClient.Builder restClientBuilder,
            @Value("${audit.service.url:${AUDIT_SERVICE_URL:http://localhost:8091}}") String auditServiceUrl) {
        this.restClient = restClientBuilder.build();
        this.auditServiceUrl = auditServiceUrl;
    }

    @Override
    public void register(CatalogAuditEventCommand command) {
        if (command.companyId() == null) {
            log.warn("event=audit_skipped reason=missing_company action={} resourceType={} resourceId={}",
                    command.action(), command.resourceType(), command.resourceId());
            return;
        }
        try {
            restClient.post()
                    .uri(auditServiceUrl + "/api/v1/audit-events")
                    .header("X-Company-Id", command.companyId().toString())
                    .body(new AuditEventRequest(command.userId(), command.eventType(), command.resourceType(),
                            command.resourceId(), command.action(), command.result(), command.detail()))
                    .retrieve()
                    .toBodilessEntity();
        } catch (RuntimeException ex) {
            log.warn("event=audit_register_failed action={} resourceType={} resourceId={} error={}",
                    command.action(), command.resourceType(), command.resourceId(), ex.getMessage());
        }
    }

    private record AuditEventRequest(UUID userId, String eventType, String resourceType, String resourceId,
            String action, String result, String detail) {
    }
}
