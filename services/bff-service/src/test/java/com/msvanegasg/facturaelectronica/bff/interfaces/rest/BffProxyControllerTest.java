package com.msvanegasg.facturaelectronica.bff.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyRequest;
import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyResponse;
import com.msvanegasg.facturaelectronica.bff.application.port.in.ProxyPublicApiUseCase;
import com.msvanegasg.facturaelectronica.bff.domain.model.TargetService;
import com.msvanegasg.facturaelectronica.bff.exception.BffExceptionHandler;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffAuthProperties;
import com.msvanegasg.facturaelectronica.bff.infrastructure.security.BffEncryptedSessionStore;
import com.msvanegasg.facturaelectronica.bff.infrastructure.security.BffUserSession;
import com.msvanegasg.facturaelectronica.bff.observability.CorrelationId;
import com.msvanegasg.facturaelectronica.bff.observability.CorrelationIdFilter;

import jakarta.servlet.http.Cookie;

@ExtendWith(MockitoExtension.class)
class BffProxyControllerTest {

    private MockMvc mockMvc;
    private BffEncryptedSessionStore sessionStore;

    @Mock
    private ProxyPublicApiUseCase proxyUseCase;

    @Captor
    private ArgumentCaptor<ProxyRequest> requestCaptor;

    @BeforeEach
    void setUp() {
        BffAuthProperties properties = new BffAuthProperties("cognito", "prod", "https://auth.example.com",
                "client-id", "https://api.example.com/api/v1/auth/callback", "https://app.example.com",
                "https://app.example.com", "test-session-encryption-key-32-chars", true, "Strict", true);
        sessionStore = new BffEncryptedSessionStore(new ObjectMapper().findAndRegisterModules(), properties);
        mockMvc = MockMvcBuilders.standaloneSetup(new BffProxyController(proxyUseCase, sessionStore))
                .setControllerAdvice(new BffExceptionHandler())
                .addFilters(new CorrelationIdFilter())
                .build();
    }

    @Test
    void attachesInternalAuthorizationFromServerSideCookieSession() throws Exception {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        when(proxyUseCase.proxy(any())).thenReturn(new ProxyResponse(HttpStatus.OK, responseHeaders,
                "{\"ok\":true}".getBytes(StandardCharsets.UTF_8)));
        UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        String sessionId = sessionStore.createSession(new BffUserSession(userId, "subject", "user@example.com",
                "User Example", Set.of("OWNER"), "internal-token", "id-token", "refresh-token",
                Instant.now().plusSeconds(600), Instant.now()));

        mockMvc.perform(get("/api/v1/products")
                .cookie(new Cookie(BffAuthController.SESSION_COOKIE, sessionId))
                .header("X-Company-Id", "11111111-1111-1111-1111-111111111111")
                .header(CorrelationId.HEADER_NAME, "corr-bff"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ok").value(true));

        org.mockito.Mockito.verify(proxyUseCase).proxy(requestCaptor.capture());
        ProxyRequest proxied = requestCaptor.getValue();
        assertThat(proxied.headers().getFirst("Authorization")).isEqualTo("Bearer internal-token");
        assertThat(proxied.headers().getFirst("X-User-Id")).isEqualTo(userId.toString());
        assertThat(proxied.headers().getFirst("X-Company-Id")).isEqualTo("11111111-1111-1111-1111-111111111111");
    }

    @Test
    void proxiesRequestsToResolvedServiceWithRequiredHeaders() throws Exception {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        when(proxyUseCase.proxy(any())).thenReturn(new ProxyResponse(HttpStatus.CREATED, responseHeaders,
                "{\"id\":\"company-1\"}".getBytes(StandardCharsets.UTF_8)));

        mockMvc.perform(post("/api/v1/companies?bootstrap=true")
                .header("Authorization", "Bearer token")
                .header("X-Company-Id", "11111111-1111-1111-1111-111111111111")
                .header("X-User-Id", "22222222-2222-2222-2222-222222222222")
                .header(CorrelationId.HEADER_NAME, "corr-bff")
                .header("Idempotency-Key", "company-1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"legalName\":\"ACME SAS\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string(CorrelationId.HEADER_NAME, "corr-bff"))
                .andExpect(jsonPath("$.id").value("company-1"));

        org.mockito.Mockito.verify(proxyUseCase).proxy(requestCaptor.capture());
        ProxyRequest proxied = requestCaptor.getValue();
        assertThat(proxied.targetService()).isEqualTo(TargetService.TENANT);
        assertThat(proxied.uri().toString()).isEqualTo("/api/v1/companies?bootstrap=true");
        assertThat(proxied.headers().getFirst("Authorization")).isEqualTo("Bearer token");
        assertThat(proxied.headers().getFirst("X-User-Id")).isEqualTo("22222222-2222-2222-2222-222222222222");
        assertThat(new String(proxied.body(), StandardCharsets.UTF_8)).contains("ACME SAS");
    }

    @Test
    void rejectsCriticalCookieMutationWhenMfaIsMissing() throws Exception {
        UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        String sessionId = sessionStore.createSession(new BffUserSession(userId, "subject", "root@example.com",
                "Root Example", Set.of("ROOT"), "internal-token", "id-token", "refresh-token",
                Instant.now().plusSeconds(600), Instant.now(), false));

        mockMvc.perform(put("/api/v1/companies/11111111-1111-1111-1111-111111111111/license")
                .cookie(new Cookie(BffAuthController.SESSION_COOKIE, sessionId))
                .header(CorrelationId.HEADER_NAME, "corr-bff")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"active\":true}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));

        org.mockito.Mockito.verify(proxyUseCase, never()).proxy(any());
    }

    @Test
    void allowsSalesCookieMutationWithoutAdditionalMfa() throws Exception {
        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.setContentType(MediaType.APPLICATION_JSON);
        when(proxyUseCase.proxy(any())).thenReturn(new ProxyResponse(HttpStatus.CREATED, responseHeaders,
                "{\"id\":\"sale-1\"}".getBytes(StandardCharsets.UTF_8)));
        UUID userId = UUID.fromString("22222222-2222-2222-2222-222222222222");
        String sessionId = sessionStore.createSession(new BffUserSession(userId, "subject", "seller@example.com",
                "Seller Example", Set.of(), "internal-token", "id-token", "refresh-token",
                Instant.now().plusSeconds(600), Instant.now(), false));

        mockMvc.perform(post("/api/v1/sales")
                .cookie(new Cookie(BffAuthController.SESSION_COOKIE, sessionId))
                .header("X-Company-Id", "11111111-1111-1111-1111-111111111111")
                .header(CorrelationId.HEADER_NAME, "corr-bff")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"lines\":[]}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("sale-1"));

        org.mockito.Mockito.verify(proxyUseCase).proxy(requestCaptor.capture());
        assertThat(requestCaptor.getValue().headers().getFirst("Authorization")).isEqualTo("Bearer internal-token");
    }

    @Test
    void rejectsRoutesNotExposedByBff() throws Exception {
        mockMvc.perform(get("/api/v1/unknown/submissions/123")
                .header(CorrelationId.HEADER_NAME, "corr-bff"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.correlationId").value("corr-bff"));
    }
}
