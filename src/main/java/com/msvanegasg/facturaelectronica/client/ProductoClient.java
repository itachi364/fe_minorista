package com.msvanegasg.facturaelectronica.client;

import com.msvanegasg.facturaelectronica.DTO.request.AumentarStockRequestDTO;
import com.msvanegasg.facturaelectronica.DTO.response.ProductoResponseDTO;
import com.msvanegasg.facturaelectronica.exception.producto.ProductoCodigoNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class ProductoClient {

	private final WebClient webClient;

	@Value("${microservicio.productos.url}")
	private String baseUrl;

	public ProductoResponseDTO obtenerProductoPorCodigoBarras(Long codigoBarras) {
		try {
			return webClient.get().uri(baseUrl + "/codigo/{codigoBarras}", codigoBarras).retrieve()
					.bodyToMono(ProductoResponseDTO.class).block();
		} catch (WebClientResponseException.NotFound ex) {
			throw new ProductoCodigoNotFoundException(codigoBarras);
		}
	}

	public void aumentarStock(AumentarStockRequestDTO requestDTO) {
		webClient.put().uri(baseUrl + "/aumentar-stock")
				.body(Mono.just(requestDTO), AumentarStockRequestDTO.class).retrieve().bodyToMono(Void.class).block();
	}

}