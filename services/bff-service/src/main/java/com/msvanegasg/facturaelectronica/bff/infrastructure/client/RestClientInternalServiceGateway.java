package com.msvanegasg.facturaelectronica.bff.infrastructure.client;

import java.io.IOException;
import java.net.URI;
import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyRequest;
import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyResponse;
import com.msvanegasg.facturaelectronica.bff.application.port.out.InternalServiceGateway;
import com.msvanegasg.facturaelectronica.bff.domain.model.TargetService;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffProperties;

@Component
public class RestClientInternalServiceGateway implements InternalServiceGateway {

    private static final Set<String> FORWARDED_HEADERS = Set.of("authorization", "x-company-id", "x-correlation-id",
            "idempotency-key", "content-type", "accept");
    private static final Set<String> RESPONSE_HEADERS = Set.of("content-type", "x-correlation-id");

    private final Map<TargetService, RestClient> clients;

    public RestClientInternalServiceGateway(RestClient.Builder builder, BffProperties properties) {
        this.clients = new EnumMap<>(TargetService.class);
        this.clients.put(TargetService.TENANT, builder.clone().baseUrl(properties.tenantUrl()).build());
        this.clients.put(TargetService.IDENTITY, builder.clone().baseUrl(properties.identityUrl()).build());
        this.clients.put(TargetService.CATALOG, builder.clone().baseUrl(properties.catalogUrl()).build());
        this.clients.put(TargetService.THIRDPARTY, builder.clone().baseUrl(properties.thirdpartyUrl()).build());
        this.clients.put(TargetService.INVENTORY, builder.clone().baseUrl(properties.inventoryUrl()).build());
        this.clients.put(TargetService.BILLING, builder.clone().baseUrl(properties.billingUrl()).build());
        this.clients.put(TargetService.ACCOUNTING, builder.clone().baseUrl(properties.accountingUrl()).build());
        this.clients.put(TargetService.AUDIT, builder.clone().baseUrl(properties.auditUrl()).build());
    }

    @Override
    public ProxyResponse exchange(ProxyRequest request) {
        RestClient client = clients.get(request.targetService());
        if (client == null) {
            throw new DownstreamServiceException("Servicio interno no configurado: " + request.targetService(), null);
        }
        try {
            return client.method(request.method())
                    .uri(request.uri())
                    .headers(headers -> copyRequestHeaders(request.headers(), headers))
                    .body(request.body() == null ? new byte[0] : request.body())
                    .exchange((clientRequest, clientResponse) -> {
                        byte[] responseBody = clientResponse.getBody().readAllBytes();
                        HttpHeaders responseHeaders = filterResponseHeaders(clientResponse.getHeaders());
                        return new ProxyResponse(clientResponse.getStatusCode(), responseHeaders, responseBody);
                    });
        } catch (RestClientException exception) {
            throw new DownstreamServiceException("No fue posible comunicarse con el servicio interno.", exception);
        }
    }

    private static void copyRequestHeaders(HttpHeaders source, HttpHeaders target) {
        source.forEach((name, values) -> {
            if (FORWARDED_HEADERS.contains(name.toLowerCase())) {
                target.put(name, values);
            }
        });
    }

    private static HttpHeaders filterResponseHeaders(HttpHeaders source) {
        HttpHeaders target = new HttpHeaders();
        source.forEach((name, values) -> {
            if (RESPONSE_HEADERS.contains(name.toLowerCase())) {
                target.put(name, values);
            }
        });
        return target;
    }
}
