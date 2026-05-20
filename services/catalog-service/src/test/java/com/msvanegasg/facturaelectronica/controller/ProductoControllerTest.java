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

import com.msvanegasg.facturaelectronica.catalog.application.dto.IncreaseProductStockCommand;
import com.msvanegasg.facturaelectronica.catalog.application.dto.ProductCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageProductUseCase;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Category;
import com.msvanegasg.facturaelectronica.catalog.domain.model.Product;

@ExtendWith(MockitoExtension.class)
class ProductoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManageProductUseCase manageProductUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProductoController(manageProductUseCase))
                .build();
    }

    @Test
    void createProductKeepsLegacyEndpointAndResponseDto() throws Exception {
        when(manageProductUseCase.create(any(ProductCommand.class))).thenReturn(product(1L, true, 10));

        mockMvc.perform(post("/api/productos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Cafe"))
                .andExpect(jsonPath("$.categoria.id").value(1))
                .andExpect(jsonPath("$.codigoBarras").value(7701234567890L));
    }

    @Test
    void getByIdReturnsLegacyResponseDto() throws Exception {
        when(manageProductUseCase.findById(1L)).thenReturn(product(1L, true, 10));

        mockMvc.perform(get("/api/productos/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Cafe"))
                .andExpect(jsonPath("$.categoria.nombre").value("Bebidas"));
    }

    @Test
    void updateProductKeepsLegacyEndpointAndResponseDto() throws Exception {
        when(manageProductUseCase.update(eq(1L), any(ProductCommand.class))).thenReturn(product(1L, true, 10));

        mockMvc.perform(put("/api/productos/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(productJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.precioBase").value(1500.00));
    }

    @Test
    void deleteProductReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/productos/1"))
                .andExpect(status().isNoContent());

        verify(manageProductUseCase).disable(1L);
    }

    @Test
    void listAllReturnsLegacyResponseDtoList() throws Exception {
        when(manageProductUseCase.findAll()).thenReturn(List.of(product(1L, true, 10)));

        mockMvc.perform(get("/api/productos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void listActiveReturnsLegacyResponseDtoList() throws Exception {
        when(manageProductUseCase.findActive()).thenReturn(List.of(product(1L, true, 10)));

        mockMvc.perform(get("/api/productos/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Cafe"));
    }

    @Test
    void listInactiveReturnsLegacyResponseDtoList() throws Exception {
        when(manageProductUseCase.findInactive()).thenReturn(List.of(product(2L, false, 10)));

        mockMvc.perform(get("/api/productos/inactive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    void listByNameReturnsLegacyResponseDtoList() throws Exception {
        when(manageProductUseCase.findByName("Cafe")).thenReturn(List.of(product(1L, true, 10)));

        mockMvc.perform(get("/api/productos/nombre/Cafe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nombre").value("Cafe"));
    }

    @Test
    void getByBarcodeReturnsLegacyResponseDto() throws Exception {
        when(manageProductUseCase.findByBarcode(7701234567890L)).thenReturn(product(1L, true, 10));

        mockMvc.perform(get("/api/productos/codigo/7701234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoBarras").value(7701234567890L));
    }

    @Test
    void enableProductReturnsNoContent() throws Exception {
        mockMvc.perform(put("/api/productos/activar/1"))
                .andExpect(status().isNoContent());

        verify(manageProductUseCase).enable(1L);
    }

    @Test
    void increaseStockKeepsLegacyEndpointAndResponseDto() throws Exception {
        when(manageProductUseCase.increaseStock(any(IncreaseProductStockCommand.class)))
                .thenReturn(product(1L, true, 15));

        mockMvc.perform(put("/api/productos/aumentar-stock")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigoBarras\":7701234567890,\"cantidadASumar\":5}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cantidadStock").value(15));
    }

    private static String productJson() {
        return """
                {"nombre":"Cafe","descripcion":"Producto de tienda","precioBase":1500.00,"cantidadStock":10,"idCategoria":1,"codigoBarras":7701234567890}
                """;
    }

    private static Product product(Long id, boolean active, int stockQuantity) {
        return Product.restore(
                id,
                "Cafe",
                "Producto de tienda",
                new BigDecimal("1500.00"),
                stockQuantity,
                Category.restore(1L, "Bebidas", "Categoria de bebidas", true),
                7701234567890L,
                active);
    }
}
