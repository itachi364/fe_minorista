package com.msvanegasg.facturaelectronica.reportinglambda;

public record ReportingProjectionResult(boolean processed, boolean duplicate, boolean ignored) {

    public static ReportingProjectionResult processedResult() {
        return new ReportingProjectionResult(true, false, false);
    }

    public static ReportingProjectionResult duplicateResult() {
        return new ReportingProjectionResult(false, true, false);
    }

    public static ReportingProjectionResult ignoredResult() {
        return new ReportingProjectionResult(false, false, true);
    }
}