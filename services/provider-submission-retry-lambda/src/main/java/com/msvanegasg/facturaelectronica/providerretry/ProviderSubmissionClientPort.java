package com.msvanegasg.facturaelectronica.providerretry;

public interface ProviderSubmissionClientPort {

    ProviderSubmissionOutcome submit(BillingDocumentSnapshot snapshot);
}