package com.msvanegasg.facturaelectronica.accounting.infrastructure.client;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.accounting.application.port.out.ThirdPartyFiscalProfilePort;
import com.msvanegasg.facturaelectronica.accounting.domain.model.ThirdPartyFiscalProfile;

@Component
public class ThirdPartyFiscalProfileHttpAdapter implements ThirdPartyFiscalProfilePort {

    private final RestClient restClient;
    private final String thirdPartyBaseUrl;

    public ThirdPartyFiscalProfileHttpAdapter(RestClient.Builder restClientBuilder,
            @Value("${services.thirdparty.base-url:}") String thirdPartyBaseUrl) {
        this.restClient = restClientBuilder.build();
        this.thirdPartyBaseUrl = thirdPartyBaseUrl;
    }

    @Override
    public Optional<ThirdPartyFiscalProfile> findByCompanyIdAndId(UUID companyId, UUID thirdPartyId) {
        if (thirdPartyBaseUrl == null || thirdPartyBaseUrl.isBlank()) {
            return Optional.empty();
        }
        try {
            ThirdPartyResponse response = restClient.get()
                    .uri(thirdPartyBaseUrl + "/api/v1/third-parties/" + thirdPartyId)
                    .header("X-Company-Id", companyId.toString())
                    .retrieve()
                    .body(ThirdPartyResponse.class);
            if (response == null) {
                return Optional.empty();
            }
            return Optional.of(new ThirdPartyFiscalProfile(response.id(), response.taxRegime(),
                    response.taxResponsibilities() == null ? Set.of() : response.taxResponsibilities(),
                    response.municipalityCode(), response.ciiuCode(), response.active()));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private record ThirdPartyResponse(
            UUID id,
            String taxRegime,
            Set<String> taxResponsibilities,
            String municipalityCode,
            String ciiuCode,
            boolean active) {
    }
}
