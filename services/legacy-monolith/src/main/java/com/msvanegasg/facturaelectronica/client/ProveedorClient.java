package com.msvanegasg.facturaelectronica.client;

import com.msvanegasg.facturaelectronica.exception.proveedor.ProveedorDocumentoNotFoundException;
import com.msvanegasg.facturaelectronica.thirdparty.interfaces.rest.dto.SupplierResponse;
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

    public SupplierResponse obtenerProveedorPorDocumentoMigrado(Long numeroDocumento, Long tipoDocumento) {
        String url = String.format("%s/documento/%s/tipo/%d", baseUrl, numeroDocumento, tipoDocumento);

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(
                        status -> status.value() == 404,
                        response -> Mono.error(new ProveedorDocumentoNotFoundException(numeroDocumento, tipoDocumento))
                )
                .bodyToMono(SupplierResponse.class)
                .block();
    }
}
