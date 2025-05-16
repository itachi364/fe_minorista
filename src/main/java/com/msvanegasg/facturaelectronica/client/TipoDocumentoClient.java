package com.msvanegasg.facturaelectronica.client;

import com.msvanegasg.facturaelectronica.DTO.response.TipoDocumentoResponseDTO;
import com.msvanegasg.facturaelectronica.exception.tipodocumento.TipoDocumentoNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Component
@RequiredArgsConstructor
public class TipoDocumentoClient {

    private final WebClient webClient;

    @Value("${microservicio.tipodocumento.url}")
    private String baseUrl;

    public TipoDocumentoResponseDTO obtenerTipoDocumentoPorCodigo(Long codigo) {
        try {
            return webClient.get()
                    .uri(baseUrl + "/codigo/{codigo}", codigo)
                    .retrieve()
                    .bodyToMono(TipoDocumentoResponseDTO.class)
                    .block();
        } catch (WebClientResponseException.NotFound ex) {
            throw new TipoDocumentoNotFoundException(codigo);
        }
    }
}
