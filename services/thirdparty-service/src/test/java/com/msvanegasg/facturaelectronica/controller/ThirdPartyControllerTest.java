package com.msvanegasg.facturaelectronica.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.thirdparty.application.dto.ThirdPartyCommand;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.ThirdPartyResult;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.in.ManageThirdPartyUseCase;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.PersonType;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.TaxRegime;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.ThirdPartyRole;

@ExtendWith(MockitoExtension.class)
class ThirdPartyControllerTest {

    private static final UUID COMPANY_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
    private static final UUID THIRD_PARTY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private MockMvc mockMvc;

    @Mock
    private ManageThirdPartyUseCase useCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThirdPartyController(useCase)).build();
    }

    @Test
    void createThirdPartyUsesCompanyHeaderAndReturnsCalculatedDigit() throws Exception {
        when(useCase.create(any(ThirdPartyCommand.class))).thenReturn(result(roles(ThirdPartyRole.CUSTOMER)));

        mockMvc.perform(post("/api/v1/third-parties")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(thirdPartyJson(Set.of("CUSTOMER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyId").value(COMPANY_ID.toString()))
                .andExpect(jsonPath("$.identificationTypeCode").value(31))
                .andExpect(jsonPath("$.verificationDigit").value(8))
                .andExpect(jsonPath("$.taxResponsibilities[0]").value("O-13"))
                .andExpect(jsonPath("$.taxRegime").value("RESPONSABLE_IVA"))
                .andExpect(jsonPath("$.roles[0]").value("CUSTOMER"));
    }

    @Test
    void listCustomersFiltersCustomerRole() throws Exception {
        when(useCase.findByRole(eq(COMPANY_ID), eq(ThirdPartyRole.CUSTOMER), eq(true)))
                .thenReturn(List.of(result(roles(ThirdPartyRole.CUSTOMER))));

        mockMvc.perform(get("/api/v1/customers")
                .header("X-Company-Id", COMPANY_ID)
                .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].roles[0]").value("CUSTOMER"));
    }

    @Test
    void createSupplierAddsSupplierRole() throws Exception {
        when(useCase.create(any(ThirdPartyCommand.class))).thenReturn(result(roles(ThirdPartyRole.SUPPLIER)));

        mockMvc.perform(post("/api/v1/suppliers")
                .header("X-Company-Id", COMPANY_ID)
                .contentType(MediaType.APPLICATION_JSON)
                .content(thirdPartyJson(Set.of("CUSTOMER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roles[0]").value("SUPPLIER"));
    }

    private static ThirdPartyResult result(Set<ThirdPartyRole> roles) {
        return new ThirdPartyResult(THIRD_PARTY_ID, COMPANY_ID, PersonType.JURIDICA, 31, "900123456", 8,
                null, "Cliente SAS", "Cliente", "cliente@example.com", "3000000000", "Calle 1", "11001",
                Set.of("O-13"), TaxRegime.RESPONSABLE_IVA, roles, true);
    }

    private static Set<ThirdPartyRole> roles(ThirdPartyRole role) {
        LinkedHashSet<ThirdPartyRole> roles = new LinkedHashSet<>();
        roles.add(role);
        return roles;
    }

    private static String thirdPartyJson(Set<String> roles) {
        String roleJson = roles.stream().map(role -> "\"" + role + "\"").reduce((left, right) -> left + "," + right)
                .orElse("\"CUSTOMER\"");
        return """
                {"personType":"JURIDICA","identificationTypeCode":31,"identificationNumber":"900123456","businessName":"Cliente SAS","tradeName":"Cliente","email":"cliente@example.com","phone":"3000000000","address":"Calle 1","municipalityCode":"11001","taxResponsibilities":["O-13"],"taxRegime":"RESPONSABLE_IVA","roles":[%s]}
                """.formatted(roleJson);
    }
}
