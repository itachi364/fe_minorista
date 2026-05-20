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

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.catalog.application.dto.TaxCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageTaxUseCase;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Country;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Tax;
import com.msvanegasg.facturaelectronica.exception.impuesto.ImpuestoNotFoundException;

@ExtendWith(MockitoExtension.class)
class ImpuestoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManageTaxUseCase manageTaxUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ImpuestoController(manageTaxUseCase))
                .build();
    }

    @Test
    void findAllReturnsLegacyTaxShape() throws Exception {
        when(manageTaxUseCase.findAll()).thenReturn(List.of(tax(1L, true)));

        mockMvc.perform(get("/api/impuesto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idImpuesto").value(1))
                .andExpect(jsonPath("$[0].nombre").value("IVA"))
                .andExpect(jsonPath("$[0].porcentaje").value(19.00))
                .andExpect(jsonPath("$[0].tipo").value("IVA"))
                .andExpect(jsonPath("$[0].pais.codigoPais").value("CO"))
                .andExpect(jsonPath("$[0].activo").value(true))
                .andExpect(jsonPath("$[0].descripcion").value("Impuesto general"));
    }

    @Test
    void findActiveReturnsLegacyTaxShape() throws Exception {
        when(manageTaxUseCase.findActive()).thenReturn(tax(1L, true));

        mockMvc.perform(get("/api/impuesto/activos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void findInactiveReturnsLegacyTaxShape() throws Exception {
        when(manageTaxUseCase.findInactive()).thenReturn(tax(2L, false));

        mockMvc.perform(get("/api/impuesto/inactivos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.activo").value(false));
    }

    @Test
    void getByIdReturnsLegacyTaxShape() throws Exception {
        when(manageTaxUseCase.findById(1L)).thenReturn(tax(1L, true));

        mockMvc.perform(get("/api/impuesto/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idImpuesto").value(1))
                .andExpect(jsonPath("$.pais.codigoPais").value("CO"));
    }

    @Test
    void findByPercentageReturnsLegacyTaxShape() throws Exception {
        when(manageTaxUseCase.findByPercentage(new BigDecimal("19.00"))).thenReturn(tax(1L, true));

        mockMvc.perform(get("/api/impuesto/porcentaje/19.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.porcentaje").value(19.00));
    }

    @Test
    void findByTypeReturnsLegacyTaxShape() throws Exception {
        when(manageTaxUseCase.findByType("IVA")).thenReturn(tax(1L, true));

        mockMvc.perform(get("/api/impuesto/tipo/IVA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipo").value("IVA"));
    }

    @Test
    void createTaxKeepsLegacyEndpointAndEntityResponse() throws Exception {
        when(manageTaxUseCase.create(any(TaxCommand.class))).thenReturn(tax(1L, true));

        mockMvc.perform(post("/api/impuesto")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"IVA","porcentaje":19.00,"tipo":"IVA","codPais":"CO","descripcion":"Impuesto general"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idImpuesto").value(1))
                .andExpect(jsonPath("$.pais.codigoPais").value("CO"));
    }

    @Test
    void updateTaxKeepsLegacyEndpointAndResponseDto() throws Exception {
        when(manageTaxUseCase.update(eq(1L), any(TaxCommand.class))).thenReturn(tax(1L, true));

        mockMvc.perform(put("/api/impuesto/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                        {"nombre":"IVA","porcentaje":19.00,"tipo":"IVA","codPais":"CO","descripcion":"Impuesto general"}
                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.codigoPais.codigoPais").value("CO"));
    }

    @Test
    void disableTaxReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/impuesto/1"))
                .andExpect(status().isNoContent());

        verify(manageTaxUseCase).disable(1L);
    }

    @Test
    void disableMissingTaxReturnsNotFound() throws Exception {
        org.mockito.Mockito.doThrow(new ImpuestoNotFoundException(99L))
                .when(manageTaxUseCase).disable(99L);

        mockMvc.perform(delete("/api/impuesto/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void enableTaxReturnsNoContent() throws Exception {
        mockMvc.perform(put("/api/impuesto/1/activar"))
                .andExpect(status().isNoContent());

        verify(manageTaxUseCase).enable(1L);
    }

    private static Tax tax(Long id, boolean active) {
        return Tax.restore(
                id,
                "IVA",
                new BigDecimal("19.00"),
                "IVA",
                Country.restore("CO", "Colombia", "COP", true),
                "Impuesto general",
                active);
    }
}
