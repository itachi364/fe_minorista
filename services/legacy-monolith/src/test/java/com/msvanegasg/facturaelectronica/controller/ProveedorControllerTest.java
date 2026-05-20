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

import com.msvanegasg.facturaelectronica.thirdparty.application.dto.DocumentTypeSummary;
import com.msvanegasg.facturaelectronica.thirdparty.application.dto.SupplierCommand;
import com.msvanegasg.facturaelectronica.thirdparty.application.port.in.ManageSupplierUseCase;
import com.msvanegasg.facturaelectronica.thirdparty.domain.model.Supplier;

@ExtendWith(MockitoExtension.class)
class ProveedorControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManageSupplierUseCase manageSupplierUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ProveedorController(manageSupplierUseCase))
                .build();
    }

    @Test
    void listAllReturnsSuppliers() throws Exception {
        when(manageSupplierUseCase.findAll()).thenReturn(List.of(supplier(true)));

        mockMvc.perform(get("/api/proveedores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].idProveedor").value(7))
                .andExpect(jsonPath("$[0].tipoDocumento.nombre").value("Cedula"));
    }

    @Test
    void listActiveReturnsSuppliers() throws Exception {
        when(manageSupplierUseCase.findActive()).thenReturn(List.of(supplier(true)));

        mockMvc.perform(get("/api/proveedores/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void listInactiveReturnsSuppliers() throws Exception {
        when(manageSupplierUseCase.findInactive()).thenReturn(List.of(supplier(false)));

        mockMvc.perform(get("/api/proveedores/inactive"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].activo").value(false));
    }

    @Test
    void findByIdReturnsSupplier() throws Exception {
        when(manageSupplierUseCase.findById(7L)).thenReturn(supplier(true));

        mockMvc.perform(get("/api/proveedores/7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idProveedor").value(7))
                .andExpect(jsonPath("$.numeroDocumento").value(123456789));
    }

    @Test
    void findByDocumentReturnsSupplier() throws Exception {
        when(manageSupplierUseCase.findByDocument(13L, 123456789L)).thenReturn(supplier(true));

        mockMvc.perform(get("/api/proveedores/documento/123456789/tipo/13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Proveedor Prueba"));
    }

    @Test
    void findByNameReturnsSupplier() throws Exception {
        when(manageSupplierUseCase.findByName("Proveedor")).thenReturn(supplier(true));

        mockMvc.perform(get("/api/proveedores/nombre/Proveedor"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.tipoDocumento.id").value(13));
    }

    @Test
    void createSupplierKeepsLegacyEndpointAndResponseDto() throws Exception {
        when(manageSupplierUseCase.create(any(SupplierCommand.class))).thenReturn(supplier(true));

        mockMvc.perform(post("/api/proveedores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(supplierJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idTipoDocumento").value(13))
                .andExpect(jsonPath("$.correo").value("proveedor@example.com"));
    }

    @Test
    void updateSupplierKeepsLegacyEndpointAndResponseDto() throws Exception {
        when(manageSupplierUseCase.update(eq(13L), eq(123456789L), any(SupplierCommand.class)))
                .thenReturn(supplier(true));

        mockMvc.perform(put("/api/proveedores/documento/123456789/tipo/13")
                .contentType(MediaType.APPLICATION_JSON)
                .content(supplierJson()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Proveedor Prueba"));
    }

    @Test
    void disableSupplierReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/proveedores/documento/123456789/tipo/13"))
                .andExpect(status().isNoContent());

        verify(manageSupplierUseCase).disable(13L, 123456789L);
    }

    @Test
    void enableSupplierReturnsNoContent() throws Exception {
        mockMvc.perform(put("/api/proveedores/documento/123456789/tipo/13/activar"))
                .andExpect(status().isNoContent());

        verify(manageSupplierUseCase).enable(13L, 123456789L);
    }

    private static String supplierJson() {
        return """
                {"nombre":"Proveedor Prueba","idTipoDocumento":13,"numeroDocumento":123456789,"direccion":"Calle 1","telefono":"3001234567","correo":"proveedor@example.com"}
                """;
    }

    private static Supplier supplier(boolean active) {
        return Supplier.restore(
                7L,
                "Proveedor Prueba",
                new DocumentTypeSummary(13L, "Cedula"),
                123456789L,
                null,
                "Calle 1",
                "3001234567",
                "proveedor@example.com",
                active);
    }
}
