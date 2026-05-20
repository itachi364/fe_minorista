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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
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

import com.msvanegasg.facturaelectronica.catalog.application.dto.ExpenseTypeCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageExpenseTypeUseCase;
import com.msvanegasg.facturaelectronica.catalog.domain.model.ExpenseType;

@ExtendWith(MockitoExtension.class)
class TipoGastoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManageExpenseTypeUseCase manageExpenseTypeUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TipoGastoController(manageExpenseTypeUseCase))
                .build();
    }

    @Test
    void findAllReturnsLegacyExpenseTypeShape() throws Exception {
        when(manageExpenseTypeUseCase.findAll())
                .thenReturn(List.of(ExpenseType.restore(1L, "Servicios", "Gastos de servicios", true)));

        mockMvc.perform(get("/api/tipogasto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idTipoGasto").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Servicios"))
                .andExpect(jsonPath("$[0].descripcion").value("Gastos de servicios"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void findActiveReturnsLegacyExpenseTypeShape() throws Exception {
        when(manageExpenseTypeUseCase.findActive())
                .thenReturn(List.of(ExpenseType.restore(1L, "Servicios", "Gastos de servicios", true)));

        mockMvc.perform(get("/api/tipogasto/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void findInactiveReturnsLegacyExpenseTypeShape() throws Exception {
        when(manageExpenseTypeUseCase.findInactive())
                .thenReturn(List.of(ExpenseType.restore(2L, "Archivado", null, false)));

        mockMvc.perform(get("/api/tipogasto/inactive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].activo").value(false));
    }

    @Test
    void getByIdReturnsLegacyDtoShape() throws Exception {
        when(manageExpenseTypeUseCase.findById(1L))
                .thenReturn(ExpenseType.restore(1L, "Servicios", "Gastos de servicios", true));

        mockMvc.perform(get("/api/tipogasto/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Servicios"))
                .andExpect(jsonPath("$.descripcion").value("Gastos de servicios"));
    }

    @Test
    void createExpenseTypeKeepsLegacyEndpointAndDtoResponse() throws Exception {
        when(manageExpenseTypeUseCase.create(any(ExpenseTypeCommand.class)))
                .thenReturn(ExpenseType.restore(1L, "Servicios", "Gastos de servicios", true));

        mockMvc.perform(post("/api/tipogasto")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Servicios\",\"descripcion\":\"Gastos de servicios\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/tipogasto/1"))
                .andExpect(jsonPath("$.nombre").value("Servicios"))
                .andExpect(jsonPath("$.descripcion").value("Gastos de servicios"));
    }

    @Test
    void updateExpenseTypeKeepsLegacyEndpointAndDtoResponse() throws Exception {
        when(manageExpenseTypeUseCase.update(eq(1L), any(ExpenseTypeCommand.class)))
                .thenReturn(ExpenseType.restore(1L, "Arriendo", "Canon mensual", true));

        mockMvc.perform(put("/api/tipogasto/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Arriendo\",\"descripcion\":\"Canon mensual\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Arriendo"))
                .andExpect(jsonPath("$.descripcion").value("Canon mensual"));
    }

    @Test
    void disableExpenseTypeReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/tipogasto/1"))
                .andExpect(status().isNoContent());

        verify(manageExpenseTypeUseCase).disable(1L);
    }

    @Test
    void enableExpenseTypeReturnsNoContent() throws Exception {
        mockMvc.perform(put("/api/tipogasto/1/activar"))
                .andExpect(status().isNoContent());

        verify(manageExpenseTypeUseCase).enable(1L);
    }
}
