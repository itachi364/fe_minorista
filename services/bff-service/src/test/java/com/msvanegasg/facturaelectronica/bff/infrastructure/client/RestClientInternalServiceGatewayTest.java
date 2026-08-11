package com.msvanegasg.facturaelectronica.bff.infrastructure.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyRequest;
import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyResponse;
import com.msvanegasg.facturaelectronica.bff.domain.model.TargetService;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffProperties;
import com.sun.net.httpserver.HttpServer;

class RestClientInternalServiceGatewayTest {

    private static final String COMPANY_ID = "11111111-1111-1111-1111-111111111111";
    private static final String USER_ID = "22222222-2222-2222-2222-222222222222";

    private HttpServer identityServer;
    private HttpServer payrollServer;

    @AfterEach
    void tearDown() {
        if (identityServer != null) {
            identityServer.stop(0);
        }
        if (payrollServer != null) {
            payrollServer.stop(0);
        }
    }

    @Test
    void allowsPayrollMutationWhenUserHasManagePermission() throws IOException {
        startIdentityServer("[\"PAYROLL_MANAGE\"]");
        CapturingHandler payrollHandler = startPayrollServer();
        RestClientInternalServiceGateway gateway = gateway();

        ProxyResponse response = gateway.exchange(new ProxyRequest(TargetService.PAYROLL, HttpMethod.POST,
                URI.create("/api/v1/payroll/daily-payments"), headers(), "{\"paidAmount\":80000}".getBytes(StandardCharsets.UTF_8)));

        assertThat(response.status()).isEqualTo(HttpStatus.CREATED);
        assertThat(payrollHandler.requestBody).contains("80000");
    }

    @Test
    void rejectsPayrollMutationWhenUserDoesNotHaveManagePermission() throws IOException {
        startIdentityServer("[\"PAYROLL_VIEW\"]");
        CapturingHandler payrollHandler = startPayrollServer();
        RestClientInternalServiceGateway gateway = gateway();

        assertThatThrownBy(() -> gateway.exchange(new ProxyRequest(TargetService.PAYROLL, HttpMethod.POST,
                URI.create("/api/v1/payroll/daily-payments"), headers(), new byte[0])))
                .isInstanceOf(BffAccessDeniedException.class);
        assertThat(payrollHandler.requestBody).isNull();
    }

    private RestClientInternalServiceGateway gateway() {
        String identityUrl = "http://localhost:" + identityServer.getAddress().getPort();
        String payrollUrl = "http://localhost:" + payrollServer.getAddress().getPort();
        BffProperties properties = new BffProperties("http://tenant", identityUrl, "http://catalog", "http://thirdparty",
                "http://inventory", "http://billing", "http://accounting", payrollUrl, "http://audit");
        return new RestClientInternalServiceGateway(RestClient.builder(), properties, new ObjectMapper());
    }

    private void startIdentityServer(String permissionsJson) throws IOException {
        identityServer = HttpServer.create(new InetSocketAddress(0), 0);
        identityServer.createContext("/api/v1/platform/permissions", exchange -> {
            exchange.sendResponseHeaders(403, -1);
            exchange.close();
        });
        identityServer.createContext("/api/v1/me", exchange -> {
            byte[] body = ("{\"id\":\"" + USER_ID + "\"}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        identityServer.createContext("/api/v1/companies/" + COMPANY_ID + "/permissions", exchange -> {
            byte[] body = ("{\"companyId\":\"" + COMPANY_ID + "\",\"roles\":[\"VENDEDOR\"],\"permissions\":"
                    + permissionsJson + "}").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
        identityServer.start();
    }

    private CapturingHandler startPayrollServer() throws IOException {
        payrollServer = HttpServer.create(new InetSocketAddress(0), 0);
        CapturingHandler handler = new CapturingHandler();
        payrollServer.createContext("/api/v1/payroll/daily-payments", handler::handle);
        payrollServer.start();
        return handler;
    }

    private static HttpHeaders headers() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("token");
        headers.set("X-Company-Id", COMPANY_ID);
        headers.set("X-User-Id", USER_ID);
        headers.set("X-Correlation-Id", "corr-bff-rbac");
        return headers;
    }

    private static final class CapturingHandler {
        private String requestBody;

        private void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
            requestBody = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            byte[] body = "{\"id\":\"payment-1\"}".getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(201, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        }
    }
}
