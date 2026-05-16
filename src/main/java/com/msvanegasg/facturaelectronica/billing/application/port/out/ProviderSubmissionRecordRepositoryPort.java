package com.msvanegasg.facturaelectronica.billing.application.port.out;

import com.msvanegasg.facturaelectronica.billing.domain.model.ProviderSubmissionRecord;

public interface ProviderSubmissionRecordRepositoryPort {

    ProviderSubmissionRecord save(ProviderSubmissionRecord submissionRecord);
}
