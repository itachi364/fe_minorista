package com.msvanegasg.facturaelectronica.bff.interfaces.rest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffProperties;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class CompanyReadinessControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private MockMvc mockMvc;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        BffProperties properties = new BffProperties("http://tenant", "http://identity", "http://catalog",
                "http://thirdparty", "http://inventory", "http://billing", "http://accounting", "http://payroll",
                "http://reporting", "http://dian", "http://audit");
        mockMvc = MockMvcBuilders.standaloneSetup(new CompanyReadinessController(builder, properties)).build();
    }

    @Test
    void returnsReadyWhenCoreOperationalConfigurationExists() throws Exception {
        expectGet("http://tenant/api/v1/companies/" + COMPANY_ID + "/license",
                "{\"status\":\"ACTIVE\"}");
        expectGet("http://billing/api/v1/issuers", "[{\"active\":true}]");
        expectGet("http://billing/api/v1/numbering-resolutions", "[{\"active\":true}]");
        expectGet("http://billing/api/v1/fiscal-policy", "{\"defaultSaleDocumentType\":\"ELECTRONIC_INVOICE\"}");
        expectGet("http://accounting/api/v1/accounts?active=true", "[{\"active\":true}]");
        expectGet("http://accounting/api/v1/accounting-rules?active=true", "[{\"active\":true}]");
        expectGet("http://inventory/api/v1/products?active=true", "[{\"active\":true}]");

        mockMvc.perform(get("/api/v1/readiness/company")
                .header("X-Company-Id", COMPANY_ID)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("READY"))
                .andExpect(jsonPath("$.items[0].label").value("Licencia empresarial"));

        server.verify();
    }

    @Test
    void returnsBlockedWhenRequiredConfigurationIsMissing() throws Exception {
        expectGet("http://tenant/api/v1/companies/" + COMPANY_ID + "/license",
                "{\"status\":\"ACTIVE\"}");
        expectGet("http://billing/api/v1/issuers", "[]");
        expectGet("http://billing/api/v1/numbering-resolutions", "[]");
        expectGet("http://billing/api/v1/fiscal-policy", "{}");
        expectGet("http://accounting/api/v1/accounts?active=true", "[]");
        expectGet("http://accounting/api/v1/accounting-rules?active=true", "[]");
        expectGet("http://inventory/api/v1/products?active=true", "[]");

        mockMvc.perform(get("/api/v1/readiness/company")
                .header("X-Company-Id", COMPANY_ID)
                .header("Authorization", "Bearer token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("BLOCKED"))
                .andExpect(jsonPath("$.items[1].status").value("BLOCKED"))
                .andExpect(jsonPath("$.items[6].status").value("WARNING"));

        server.verify();
    }

    private void expectGet(String url, String body) {
        server.expect(requestTo(url))
                .andExpect(header("X-Company-Id", COMPANY_ID.toString()))
                .andExpect(header("Authorization", "Bearer token"))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    }
}
