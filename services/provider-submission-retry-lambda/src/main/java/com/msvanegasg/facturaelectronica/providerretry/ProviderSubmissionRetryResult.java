package com.msvanegasg.facturaelectronica.providerretry;

public record ProviderSubmissionRetryResult(boolean processed, boolean duplicate, boolean ignored, boolean accepted,
        boolean rejected) {

    public static ProviderSubmissionRetryResult processedAccepted() {
        return new ProviderSubmissionRetryResult(true, false, false, true, false);
    }

    public static ProviderSubmissionRetryResult processedRejected() {
        return new ProviderSubmissionRetryResult(true, false, false, false, true);
    }

    public static ProviderSubmissionRetryResult duplicateResult() {
        return new ProviderSubmissionRetryResult(false, true, false, false, false);
    }

    public static ProviderSubmissionRetryResult ignoredResult() {
        return new ProviderSubmissionRetryResult(false, false, true, false, false);
    }
}