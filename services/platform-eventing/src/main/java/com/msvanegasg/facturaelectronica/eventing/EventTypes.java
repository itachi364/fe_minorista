package com.msvanegasg.facturaelectronica.eventing;

public final class EventTypes {

    public static final String SALE_CONFIRMED = "SaleConfirmed";
    public static final String ELECTRONIC_DOCUMENT_VALIDATED = "ElectronicDocumentValidated";
    public static final String INVENTORY_MOVEMENT_REGISTERED = "InventoryMovementRegistered";
    public static final String ACCOUNTING_ENTRY_POSTED = "AccountingEntryPosted";
    public static final String AUDIT_EVENT_REQUESTED = "AuditEventRequested";
    public static final String PROVIDER_SUBMISSION_PENDING = "ProviderSubmissionPending";
    public static final String PROVIDER_SUBMISSION_FAILED = "ProviderSubmissionFailed";

    private EventTypes() {
    }
}
