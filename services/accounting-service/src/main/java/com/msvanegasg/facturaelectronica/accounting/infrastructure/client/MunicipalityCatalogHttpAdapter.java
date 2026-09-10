package com.msvanegasg.facturaelectronica.accounting.infrastructure.client;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.MunicipalityCatalogPort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.MunicipalityReference;

@Component
public class MunicipalityCatalogHttpAdapter implements MunicipalityCatalogPort {
    private final RestClient restClient;
    private final String catalogBaseUrl;

    public MunicipalityCatalogHttpAdapter(RestClient.Builder restClientBuilder,
            @Value("${services.catalog.base-url:}") String catalogBaseUrl) {
        this.restClient = restClientBuilder.build();
        this.catalogBaseUrl = catalogBaseUrl;
    }

    @Override
    public Optional<MunicipalityReference> findActive(String municipalityCode) {
        if (catalogBaseUrl == null || catalogBaseUrl.isBlank() || municipalityCode == null
                || !municipalityCode.matches("\\d{5}")) {
            return Optional.empty();
        }
        try {
            MunicipalityResponse response = restClient.get()
                    .uri(catalogBaseUrl + "/api/v1/catalogs/municipalities/" + municipalityCode)
                    .retrieve().body(MunicipalityResponse.class);
            if (response == null || !response.active()) {
                return Optional.empty();
            }
            return Optional.of(new MunicipalityReference(response.code(), response.departmentCode(), response.name(),
                    true));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private record MunicipalityResponse(String code, String departmentCode, String name, boolean active) { }
}
