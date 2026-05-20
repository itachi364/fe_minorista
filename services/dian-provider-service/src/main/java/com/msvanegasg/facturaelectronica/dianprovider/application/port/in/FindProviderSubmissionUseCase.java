package com.msvanegasg.facturaelectronica.dianprovider.application.port.in;

import java.util.UUID;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.ProviderSubmissionResult;

public interface FindProviderSubmissionUseCase {

    ProviderSubmissionResult findByTrackingId(UUID companyId, String trackingId);
}
