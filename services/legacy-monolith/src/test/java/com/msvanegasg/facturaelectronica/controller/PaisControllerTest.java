package com.msvanegasg.facturaelectronica.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CountryCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageCountryUseCase;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Country;

@ExtendWith(MockitoExtension.class)
class PaisControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManageCountryUseCase manageCountryUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new PaisController(manageCountryUseCase))
                .build();
    }

    @Test
    void findAllReturnsLegacyCountryShape() throws Exception {
        when(manageCountryUseCase.findAll())
                .thenReturn(List.of(Country.restore("CO", "Colombia", "COP", true)));

        mockMvc.perform(get("/api/paises"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigoPais").value("CO"))
                .andExpect(jsonPath("$[0].nombre").value("Colombia"))
                .andExpect(jsonPath("$[0].moneda").value("COP"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void getByCodeReturnsLegacyCountryShape() throws Exception {
        when(manageCountryUseCase.findByCode("CO"))
                .thenReturn(Country.restore("CO", "Colombia", "COP", true));

        mockMvc.perform(get("/api/paises/CO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoPais").value("CO"))
                .andExpect(jsonPath("$.nombre").value("Colombia"));
    }

    @Test
    void createCountryKeepsLegacyEndpointAndDtoResponse() throws Exception {
        when(manageCountryUseCase.create(any(CountryCommand.class)))
                .thenReturn(Country.restore("CO", "Colombia", "COP", true));

        mockMvc.perform(post("/api/paises")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigoPais\":\"CO\",\"nombre\":\"Colombia\",\"moneda\":\"COP\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoPais").value("CO"))
                .andExpect(jsonPath("$.nombre").value("Colombia"))
                .andExpect(jsonPath("$.moneda").value("COP"));
    }

    @Test
    void updateCountryKeepsLegacyEndpointAndDtoResponse() throws Exception {
        when(manageCountryUseCase.update(eq("CO"), any(CountryCommand.class)))
                .thenReturn(Country.restore("CO", "Republica de Colombia", "COP", true));

        mockMvc.perform(put("/api/paises/CO")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigoPais\":\"CO\",\"nombre\":\"Republica de Colombia\",\"moneda\":\"COP\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoPais").value("CO"))
                .andExpect(jsonPath("$.nombre").value("Republica de Colombia"));
    }

    @Test
    void disableCountryReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/paises/CO"))
                .andExpect(status().isNoContent());

        verify(manageCountryUseCase).disable("CO");
    }

    @Test
    void enableCountryReturnsNoContent() throws Exception {
        mockMvc.perform(put("/api/paises/CO/activar"))
                .andExpect(status().isNoContent());

        verify(manageCountryUseCase).enable("CO");
    }
}
