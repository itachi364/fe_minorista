package com.msvanegasg.facturaelectronica.client;

import com.msvanegasg.facturaelectronica.DTO.response.ProveedorResponse2DTO;
import com.msvanegasg.facturaelectronica.exception.proveedor.ProveedorDocumentoNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProveedorClient {

    private final WebClient webClient;

    @Value("${microservicio.proveedores.url}")
    private String baseUrl;

    public ProveedorResponse2DTO obtenerProveedorPorDocumento(Long numeroDocumento, Long tipoDocumento) {
        String url = String.format("%s/documento/%s/tipo/%d", baseUrl, numeroDocumento, tipoDocumento);

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        response -> Mono.error(new ProveedorDocumentoNotFoundException(numeroDocumento,tipoDocumento))
                )
                .bodyToMono(ProveedorResponse2DTO.class)
                .block(); // sincrónico para servicio interno
    }
}
