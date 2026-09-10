package com.msvanegasg.facturaelectronica.accounting.infrastructure.client;

import java.time.LocalDate;
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
        return find(companyId, thirdPartyId, null);
    }

    @Override
    public Optional<ThirdPartyFiscalProfile> findByCompanyIdAndIdAndDate(UUID companyId, UUID thirdPartyId,
            LocalDate effectiveOn) {
        return find(companyId, thirdPartyId, effectiveOn);
    }

    private Optional<ThirdPartyFiscalProfile> find(UUID companyId, UUID thirdPartyId, LocalDate effectiveOn) {
        if (thirdPartyBaseUrl == null || thirdPartyBaseUrl.isBlank()) {
            return Optional.empty();
        }
        try {
            ThirdPartyResponse response = restClient.get()
                    .uri(thirdPartyBaseUrl + "/api/v1/third-parties/" + thirdPartyId
                            + (effectiveOn == null ? "" : "?effectiveOn=" + effectiveOn))
                    .header("X-Company-Id", companyId.toString())
                    .retrieve()
                    .body(ThirdPartyResponse.class);
            if (response == null) {
                return Optional.empty();
            }
            return Optional.of(new ThirdPartyFiscalProfile(response.id(), response.taxRegime(),
                    response.taxResponsibilities() == null ? Set.of() : response.taxResponsibilities(),
                    response.municipalityCode(), mergeCiiuCodes(response.ciiuCodes(), response.ciiuCode()),
                    response.active()));
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
            Set<String> ciiuCodes,
            boolean active) {
    }

    private static Set<String> mergeCiiuCodes(Set<String> ciiuCodes, String legacyCiiuCode) {
        java.util.LinkedHashSet<String> merged = new java.util.LinkedHashSet<>();
        if (ciiuCodes != null) {
            merged.addAll(ciiuCodes);
        }
        if (legacyCiiuCode != null && !legacyCiiuCode.isBlank()) {
            merged.add(legacyCiiuCode);
        }
        return merged;
    }
}
