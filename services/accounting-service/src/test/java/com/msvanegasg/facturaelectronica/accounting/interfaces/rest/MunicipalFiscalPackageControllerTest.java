package com.msvanegasg.facturaelectronica.accounting.interfaces.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.accounting.application.port.in.ManageMunicipalFiscalPackagesUseCase;

class MunicipalFiscalPackageControllerTest {

    @Test
    void csvTemplateMatchesTheOperationAwareImportContract() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                new MunicipalFiscalPackageController(mock(ManageMunicipalFiscalPackagesUseCase.class))).build();

        MvcResult result = mockMvc.perform(get("/api/v1/fiscal-rule-packages/municipalities/template.csv"))
                .andExpect(status().isOk())
                .andReturn();

        String header = result.getResponse().getContentAsString(StandardCharsets.UTF_8);
        assertThat(header).startsWith("municipalityDivipolaCode,packageCode,version,operationType,conceptCode,");
        assertThat(header.strip().split(",")).hasSize(15);
    }
}
