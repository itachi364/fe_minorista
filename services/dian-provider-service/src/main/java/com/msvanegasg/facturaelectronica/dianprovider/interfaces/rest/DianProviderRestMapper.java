package com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest;

import java.util.List;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.dianprovider.application.dto.SubmitProviderDocumentCommand;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderDocumentType;
import com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest.dto.ProviderArtifactResponse;
import com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest.dto.ProviderSubmissionRequest;
import com.msvanegasg.facturaelectronica.dianprovider.interfaces.rest.dto.ProviderSubmissionResponse;

final class DianProviderRestMapper {

    private DianProviderRestMapper() {
    }

    static SubmitProviderDocumentCommand toCommand(ProviderDocumentType documentType, ProviderSubmissionRequest request,
            String idempotencyKey) {
        return new SubmitProviderDocumentCommand(request.companyId(), request.documentId(), documentType, idempotencyKey,
                request.payload() == null ? "{}" : request.payload().toString());
    }

    static ProviderSubmissionResponse toResponse(ProviderSubmissionResult result) {
        List<ProviderArtifactResponse> artifacts = result.status().name().equals("ACCEPTED")
                ? List.of(new ProviderArtifactResponse("FISCAL_ARTIFACTS",
                        "provider://submissions/" + result.trackingId() + "/artifacts",
                        "sha256:" + result.cufeCude()))
                : List.of();
        return new ProviderSubmissionResponse(result.id(), result.companyId(), result.documentId(),
                result.documentType().name(), result.trackingId(), result.status().name(), result.cufeCude(),
                result.qrContent(), result.errorCode(), result.errorMessage(), result.createdAt(), artifacts,
                result.rawResponse());
    }
}
