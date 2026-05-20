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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.msvanegasg.facturaelectronica.catalog.application.dto.CategoryCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageCategoryUseCase;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Category;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoriaControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManageCategoryUseCase manageCategoryUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new CategoriaController(manageCategoryUseCase))
                .build();
    }

    @Test
    void findAllReturnsLegacyCategoryShape() throws Exception {
        when(manageCategoryUseCase.findAll())
                .thenReturn(List.of(Category.restore(1L, "Bebidas", "Liquidos", true)));

        mockMvc.perform(get("/api/categorias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idCategoria").value(1))
                .andExpect(jsonPath("$[0].nombre").value("Bebidas"))
                .andExpect(jsonPath("$[0].descripcion").value("Liquidos"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void findByIdReturnsLegacyCategoryShape() throws Exception {
        when(manageCategoryUseCase.findById(1L))
                .thenReturn(Category.restore(1L, "Bebidas", "Liquidos", true));

        mockMvc.perform(get("/api/categorias/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCategoria").value(1))
                .andExpect(jsonPath("$.nombre").value("Bebidas"));
    }

    @Test
    void createCategoryKeepsLegacyEndpointAndResponse() throws Exception {
        when(manageCategoryUseCase.create(any(CategoryCommand.class)))
                .thenReturn(Category.restore(1L, "Bebidas", "Liquidos", true));

        mockMvc.perform(post("/api/categorias")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Bebidas\",\"descripcion\":\"Liquidos\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCategoria").value(1))
                .andExpect(jsonPath("$.nombre").value("Bebidas"))
                .andExpect(jsonPath("$.activo").value(true));
    }

    @Test
    void updateCategoryKeepsLegacyEndpointAndResponse() throws Exception {
        when(manageCategoryUseCase.update(eq(1L), any(CategoryCommand.class)))
                .thenReturn(Category.restore(1L, "Snacks", "Secos", true));

        mockMvc.perform(put("/api/categorias/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Snacks\",\"descripcion\":\"Secos\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCategoria").value(1))
                .andExpect(jsonPath("$.nombre").value("Snacks"));
    }

    @Test
    void disableCategoryReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/categorias/1"))
                .andExpect(status().isNoContent());

        verify(manageCategoryUseCase).disable(1L);
    }

    @Test
    void enableCategoryReturnsNoContent() throws Exception {
        mockMvc.perform(put("/api/categorias/activar/1"))
                .andExpect(status().isNoContent());

        verify(manageCategoryUseCase).enable(1L);
    }
}
