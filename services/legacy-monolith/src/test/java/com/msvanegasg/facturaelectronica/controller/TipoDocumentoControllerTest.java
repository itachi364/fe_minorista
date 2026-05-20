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

import com.msvanegasg.facturaelectronica.catalog.application.dto.DocumentTypeCommand;
import com.msvanegasg.facturaelectronica.catalog.application.port.in.ManageDocumentTypeUseCase;
import com.msvanegasg.facturaelectronica.catalog.domain.model.DocumentType;

@ExtendWith(MockitoExtension.class)
class TipoDocumentoControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ManageDocumentTypeUseCase manageDocumentTypeUseCase;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TipoDocumentoController(manageDocumentTypeUseCase))
                .build();
    }

    @Test
    void findAllReturnsLegacyDocumentTypeShape() throws Exception {
        when(manageDocumentTypeUseCase.findAll())
                .thenReturn(List.of(DocumentType.restore(13L, "Cedula", "Cedula ciudadania", true)));

        mockMvc.perform(get("/api/tipos-documento"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].codigo").value(13))
                .andExpect(jsonPath("$[0].nombre").value("Cedula"))
                .andExpect(jsonPath("$[0].descripcion").value("Cedula ciudadania"))
                .andExpect(jsonPath("$[0].activo").value(true));
    }

    @Test
    void getByCodeReturnsLegacyDocumentTypeShape() throws Exception {
        when(manageDocumentTypeUseCase.findByCode(13L))
                .thenReturn(DocumentType.restore(13L, "Cedula", "Cedula ciudadania", true));

        mockMvc.perform(get("/api/tipos-documento/codigo/13"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(13))
                .andExpect(jsonPath("$.nombre").value("Cedula"));
    }

    @Test
    void createDocumentTypeKeepsLegacyEndpointAndDtoResponse() throws Exception {
        when(manageDocumentTypeUseCase.create(any(DocumentTypeCommand.class)))
                .thenReturn(DocumentType.restore(13L, "Cedula", "Cedula ciudadania", true));

        mockMvc.perform(post("/api/tipos-documento")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":13,\"nombre\":\"Cedula\",\"descripcion\":\"Cedula ciudadania\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(13))
                .andExpect(jsonPath("$.nombre").value("Cedula"));
    }

    @Test
    void updateDocumentTypeKeepsLegacyEndpointAndDtoResponse() throws Exception {
        when(manageDocumentTypeUseCase.update(eq(13L), any(DocumentTypeCommand.class)))
                .thenReturn(DocumentType.restore(13L, "Cedula", "Actualizada", true));

        mockMvc.perform(put("/api/tipos-documento/13")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"codigo\":13,\"nombre\":\"Cedula\",\"descripcion\":\"Actualizada\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigo").value(13))
                .andExpect(jsonPath("$.descripcion").value("Actualizada"));
    }

    @Test
    void disableDocumentTypeReturnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/tipos-documento/13"))
                .andExpect(status().isNoContent());

        verify(manageDocumentTypeUseCase).disable(13L);
    }

    @Test
    void enableDocumentTypeReturnsNoContent() throws Exception {
        mockMvc.perform(put("/api/tipos-documento/13/activar"))
                .andExpect(status().isNoContent());

        verify(manageDocumentTypeUseCase).enable(13L);
    }
}
