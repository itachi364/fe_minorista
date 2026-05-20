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

import com.msvanegasg.facturaelectronica.catalog.application.dto.ParameterCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageParameterUseCase;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Parameter;

@ExtendWith(MockitoExtension.class)
class ParametroControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManageParameterUseCase manageParameterUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ParametroController(manageParameterUseCase))
                .build();
    }

    @Test
    void findAllReturnsLegacyParameterShape() throws Exception {
        when(manageParameterUseCase.findAll())
                .thenReturn(List.of(Parameter.restore(1L, "POS_PREFIX", "POS", "Prefijo POS", true)));

        mockMvc.perform(get("/api/parametros"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idParametro").value(1))
                .andExpect(jsonPath("$[0].clave").value("POS_PREFIX"))
                .andExpect(jsonPath("$[0].valor").value("POS"))
                .andExpect(jsonPath("$[0].descripcion").value("Prefijo POS"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void findActiveReturnsLegacyParameterShape() throws Exception {
        when(manageParameterUseCase.findActive())
                .thenReturn(List.of(Parameter.restore(1L, "POS_PREFIX", "POS", null, true)));

        mockMvc.perform(get("/api/parametros/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void findInactiveReturnsLegacyParameterShape() throws Exception {
        when(manageParameterUseCase.findInactive())
                .thenReturn(List.of(Parameter.restore(2L, "OLD_KEY", "OLD", null, false)));

        mockMvc.perform(get("/api/parametros/inactive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].activo").value(false));
    }

    @Test
    void getByIdReturnsLegacyDtoShape() throws Exception {
        when(manageParameterUseCase.findById(1L))
                .thenReturn(Parameter.restore(1L, "POS_PREFIX", "POS", "Prefijo POS", true));

        mockMvc.perform(get("/api/parametros/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clave").value("POS_PREFIX"))
                .andExpect(jsonPath("$.valor").value("POS"))
                .andExpect(jsonPath("$.descripcion").value("Prefijo POS"));
    }

    @Test
    void createParameterKeepsLegacyEndpointAndDtoResponse() throws Exception {
        when(manageParameterUseCase.create(any(ParameterCommand.class)))
                .thenReturn(Parameter.restore(1L, "POS_PREFIX", "POS", "Prefijo POS", true));

        mockMvc.perform(post("/api/parametros")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clave\":\"POS_PREFIX\",\"valor\":\"POS\",\"descripcion\":\"Prefijo POS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clave").value("POS_PREFIX"))
                .andExpect(jsonPath("$.valor").value("POS"));
    }

    @Test
    void updateParameterKeepsLegacyEndpointAndDtoResponse() throws Exception {
        when(manageParameterUseCase.update(eq(1L), any(ParameterCommand.class)))
                .thenReturn(Parameter.restore(1L, "POS_PREFIX", "P01", "Actualizado", true));

        mockMvc.perform(put("/api/parametros/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"clave\":\"POS_PREFIX\",\"valor\":\"P01\",\"descripcion\":\"Actualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clave").value("POS_PREFIX"))
                .andExpect(jsonPath("$.valor").value("P01"));
    }

    @Test
    void disableParameterReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/parametros/1"))
                .andExpect(status().isNoContent());

        verify(manageParameterUseCase).disable(1L);
    }

    @Test
    void enableParameterReturnsNoContent() throws Exception {
        mockMvc.perform(put("/api/parametros/1/activar"))
                .andExpect(status().isNoContent());

        verify(manageParameterUseCase).enable(1L);
    }
}
