package com.msvanegasg.facturaelectronica.auditlambda;

public record AuditEventWriterResult(boolean processed, boolean duplicate, boolean ignored) {

    public static AuditEventWriterResult asProcessed() {
        return new AuditEventWriterResult(true, false, false);
    }

    public static AuditEventWriterResult asDuplicate() {
        return new AuditEventWriterResult(false, true, false);
    }

    public static AuditEventWriterResult asIgnored() {
        return new AuditEventWriterResult(false, false, true);
    }
}
