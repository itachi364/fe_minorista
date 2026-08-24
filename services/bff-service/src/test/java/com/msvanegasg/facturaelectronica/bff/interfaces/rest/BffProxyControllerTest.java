package com.msvanegasg.facturaelectronica.bff.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

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

import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyRequest;
import com.msvanegasg.facturaelectronica.bff.application.dto.ProxyResponse;
import com.msvanegasg.facturaelectronica.bff.application.port.in.ProxyPublicApiUseCase;
import com.msvanegasg.facturaelectronica.bff.domain.model.TargetService;
import com.msvanegasg.facturaelectronica.bff.exception.BffExceptionHandler;
import com.msvanegasg.facturaelectronica.bff.observability.CorrelationId;
import com.msvanegasg.facturaelectronica.bff.observability.CorrelationIdFilter;

@ExtendWith(MockitoExtension.class)
class BffProxyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProxyPublicApiUseCase proxyUseCase;

    @Captor
    private ArgumentCaptor<ProxyRequest> requestCaptor;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new BffProxyController(proxyUseCase))
                .setControllerAdvice(new BffExceptionHandler())
                .addFilters(new CorrelationIdFilter())
                .build();
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
    void rejectsRoutesNotExposedByBff() throws Exception {
        mockMvc.perform(get("/api/v1/unknown/submissions/123")
                .header(CorrelationId.HEADER_NAME, "corr-bff"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.correlationId").value("corr-bff"));
    }
}
