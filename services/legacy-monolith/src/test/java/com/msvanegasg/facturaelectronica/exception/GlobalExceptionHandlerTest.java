package com.msvanegasg.facturaelectronica.exception;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.msvanegasg.facturaelectronica.observability.CorrelationId;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ErrorTestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void notFoundErrorsUseStandardContractAndCorrelationId() throws Exception {
        mockMvc.perform(get("/test-errors/not-found")
                .header("X-Correlation-Id", "corr-123"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.code").value("RESOURCE_NOT_FOUND"))
                .andExpect(jsonPath("$.message").value(containsString("ID 99")))
                .andExpect(jsonPath("$.correlationId").value("corr-123"))
                .andExpect(jsonPath("$.details", hasSize(0)));
    }

    @Test
    void errorsPreferCorrelationIdGeneratedForTheRequest() throws Exception {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new ErrorTestController())
                .setControllerAdvice(new CorrelationAttributeAdvice(), new GlobalExceptionHandler())
                .build();

        mockMvc.perform(get("/test-errors/not-found")
                .header("X-Correlation-Id", "client-corr"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.correlationId").value("generated-corr"));
    }

    @Test
    void validationErrorsIncludeFieldDetails() throws Exception {
        mockMvc.perform(post("/test-errors/validation")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.message").value("La solicitud no cumple las reglas de validacion."))
                .andExpect(jsonPath("$.correlationId", not(blankOrNullString())))
                .andExpect(jsonPath("$.details", hasSize(1)))
                .andExpect(jsonPath("$.details[0].field").value("name"));
    }

    @Test
    void externalProviderErrorsUseSafeMessage() throws Exception {
        mockMvc.perform(get("/test-errors/provider")
                .header("X-Correlation-Id", "corr-provider"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.code").value("EXTERNAL_PROVIDER_ERROR"))
                .andExpect(jsonPath("$.message").value("El proveedor tecnologico no pudo procesar la solicitud."))
                .andExpect(jsonPath("$.correlationId").value("corr-provider"))
                .andExpect(content().string(not(containsString("secret-token"))));
    }

    @Test
    void unexpectedErrorsDoNotExposeInternalDetails() throws Exception {
        mockMvc.perform(get("/test-errors/unexpected"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.code").value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.message").value("Ha ocurrido un error inesperado."))
                .andExpect(content().string(not(containsString("db-password"))));
    }

    @RestController
    private static class ErrorTestController {

        @GetMapping("/test-errors/not-found")
        void notFound() {
            throw new CategoriaNotFoundException(99L);
        }

        @PostMapping("/test-errors/validation")
        void validation(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/test-errors/provider")
        void provider() {
            throw new ExternalProviderException("provider failed with secret-token");
        }

        @GetMapping("/test-errors/unexpected")
        void unexpected() {
            throw new RuntimeException("db-password leaked");
        }
    }

    private record TestRequest(@NotBlank String name) {
    }

    @ControllerAdvice
    private static class CorrelationAttributeAdvice {

        @org.springframework.web.bind.annotation.ModelAttribute
        void addCorrelationId(HttpServletRequest request) {
            request.setAttribute(CorrelationId.REQUEST_ATTRIBUTE, "generated-corr");
        }
    }
}
