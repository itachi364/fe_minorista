package com.msvanegasg.facturaelectronica.dianprovider.application.usecase;

import com.msvanegasg.facturaelectronica.dianprovider.application.dto.ProviderSubmissionResult;
import com.msvanegasg.facturaelectronica.dianprovider.domain.model.ProviderSubmission;

final class ProviderSubmissionResultMapper {

    private ProviderSubmissionResultMapper() {
    }

    static ProviderSubmissionResult toResult(ProviderSubmission submission) {
        return new ProviderSubmissionResult(submission.id(), submission.companyId(), submission.documentId(),
                submission.documentType(), submission.trackingId(), submission.status(), submission.cufeCude(),
                submission.qrContent(), submission.errorCode(), submission.errorMessage(), submission.createdAt(),
                submission.rawResponse());
    }
}
