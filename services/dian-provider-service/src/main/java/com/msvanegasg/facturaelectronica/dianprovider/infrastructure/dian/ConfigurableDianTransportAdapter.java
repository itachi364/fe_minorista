package com.msvanegasg.facturaelectronica.dianprovider.infrastructure.dian;

import java.util.Locale;
import java.util.UUID;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianSignedDocument;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.DianTransportResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.SubmitProviderDocumentCommand;
import com.msvanegasg.facturaelectronica.dianprovider.application.port.out.DianTransportPort;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.DianCompanyConfiguration;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmissionStatus;
import com.msvanegasg.facturaelectronica.dianprovider.infrastructure.config.DianProviderProperties;

@Component
public class ConfigurableDianTransportAdapter implements DianTransportPort {

    private final DianProviderProperties properties;
    private final RestClient restClient;

    public ConfigurableDianTransportAdapter(DianProviderProperties properties, RestClient.Builder builder) {
        this.properties = properties;
        this.restClient = builder.build();
    }

    @Override
    public DianTransportResult transmit(UUID submissionId, SubmitProviderDocumentCommand command,
            DianCompanyConfiguration configuration, DianSignedDocument signedDocument) {
        if ("http".equalsIgnoreCase(properties.realTransportMode())) {
            return transmitHttp(submissionId, command, configuration, signedDocument);
        }
        ProviderSubmissionStatus status = properties.realDefaultStatus();
        String trackingId = "real-" + command.documentType().name().toLowerCase(Locale.ROOT) + "-"
                + command.documentId();
        return new DianTransportResult(status, trackingId, status == ProviderSubmissionStatus.ACCEPTED ? "00"
                : status == ProviderSubmissionStatus.REJECTED ? "DIAN_REJECTED" : "DIAN_TRANSPORT_FAILED",
                status == ProviderSubmissionStatus.ACCEPTED ? "Documento recibido por transporte DIAN stub."
                        : "Respuesta DIAN stub no aceptada.",
                applicationResponse(status, trackingId));
    }

    private DianTransportResult transmitHttp(UUID submissionId, SubmitProviderDocumentCommand command,
            DianCompanyConfiguration configuration, DianSignedDocument signedDocument) {
        if (configuration.serviceBaseUrl() == null || configuration.serviceBaseUrl().isBlank()) {
            return new DianTransportResult(ProviderSubmissionStatus.FAILED, "real-http-" + command.documentId(),
                    "DIAN_CONFIGURATION_INCOMPLETE", "Falta URL DIAN real para la empresa.", null);
        }
        try {
            String response = restClient.post()
                    .uri(configuration.serviceBaseUrl())
                    .contentType(MediaType.APPLICATION_XML)
                    .header("X-Company-Id", command.companyId().toString())
                    .header("X-Document-Id", command.documentId().toString())
                    .header("X-Submission-Id", submissionId.toString())
                    .body(signedDocument.xml())
                    .retrieve()
                    .body(String.class);
            String trackingId = "real-http-" + command.documentId();
            return new DianTransportResult(ProviderSubmissionStatus.ACCEPTED, trackingId, "00",
                    "Respuesta HTTP DIAN recibida.", response == null ? applicationResponse(ProviderSubmissionStatus.ACCEPTED,
                            trackingId) : response);
        } catch (RuntimeException exception) {
            return new DianTransportResult(ProviderSubmissionStatus.FAILED, "real-http-" + command.documentId(),
                    "DIAN_TRANSPORT_FAILED", "No fue posible transmitir el documento a DIAN.", null);
        }
    }

    private static String applicationResponse(ProviderSubmissionStatus status, String trackingId) {
        return "<ApplicationResponse><TrackingId>" + trackingId + "</TrackingId><Status>" + status
                + "</Status></ApplicationResponse>";
    }
}
