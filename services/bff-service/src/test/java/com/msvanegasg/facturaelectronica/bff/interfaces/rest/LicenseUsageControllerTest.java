package com.msvanegasg.facturaelectronica.bff.interfaces.rest;

import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withForbiddenRequest;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.bff.exception.BffExceptionHandler;
import com.msvanegasg.facturaelectronica.bff.infrastructure.config.BffProperties;

class LicenseUsageControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final String TOKEN = "Bearer root-token";

    private MockMvc mockMvc;
    private MockRestServiceServer server;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        BffProperties properties = new BffProperties("http://tenant", "http://identity", "http://catalog",
                "http://thirdparty", "http://inventory", "http://billing", "http://accounting", "http://payroll",
                "http://audit");
        mockMvc = MockMvcBuilders.standaloneSetup(new LicenseUsageController(builder, properties))
                .setControllerAdvice(new BffExceptionHandler())
                .build();
    }

    @Test
    void aggregatesLicenseUsageForRoot() throws Exception {
        LocalDate from = YearMonth.now().atDay(1);
        LocalDate to = LocalDate.now();
        server.expect(requestTo("http://identity/api/v1/platform/permissions"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header("Authorization", TOKEN))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://tenant/api/v1/companies/" + COMPANY_ID))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":"11111111-1111-1111-1111-111111111111","legalName":"Empresa Demo SAS"}
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://tenant/api/v1/companies/" + COMPANY_ID + "/license"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {
                          "companyId":"11111111-1111-1111-1111-111111111111",
                          "status":"ACTIVE",
                          "validFrom":"2026-08-01",
                          "validTo":"2027-08-01",
                          "maxUsers":5,
                          "maxMonthlyDocuments":100,
                          "enabledModules":["BILLING","INVENTORY"]
                        }
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://identity/api/v1/companies/" + COMPANY_ID + "/users"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"id":"aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa","status":"ACTIVE"},
                         {"id":"bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb","status":"INACTIVE"}]
                        """, MediaType.APPLICATION_JSON));
        server.expect(requestTo("http://billing/api/v1/reports/electronic-documents?from=" + from + "&to=" + to))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        [{"id":"cccccccc-cccc-cccc-cccc-cccccccccccc"},
                         {"id":"dddddddd-dddd-dddd-dddd-dddddddddddd"}]
                        """, MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/v1/platform/licenses/usage")
                .header("Authorization", TOKEN)
                .param("companyId", COMPANY_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Empresa Demo SAS"))
                .andExpect(jsonPath("$.licenseStatus").value("ACTIVE"))
                .andExpect(jsonPath("$.activeUsers").value(1))
                .andExpect(jsonPath("$.monthlyDocuments").value(2))
                .andExpect(jsonPath("$.maxUsers").value(5))
                .andExpect(jsonPath("$.maxMonthlyDocuments").value(100));
        server.verify();
    }

    @Test
    void rejectsNonRootUsageQuery() throws Exception {
        server.expect(requestTo("http://identity/api/v1/platform/permissions"))
                .andRespond(withForbiddenRequest());

        mockMvc.perform(get("/api/v1/platform/licenses/usage")
                .header("Authorization", "Bearer user-token")
                .param("companyId", COMPANY_ID.toString()))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ACCESS_DENIED"));
    }
}
