package com.msvanegasg.facturaelectronica.dianprovider.application.dto;

import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmissionStatus;

public record DianTransportResult(
        ProviderSubmissionStatus status,
        String trackingId,
        String dianCode,
        String dianMessage,
        String applicationResponse) {
}
