package com.msvanegasg.facturaelectronica.dianprovider.domain.model;

public enum DianSubmissionEventType {
    XML_BUILT,
    IDENTIFIERS_CALCULATED,
    SIGNED,
    VALIDATED,
    TRANSMITTED,
    ACCEPTED,
    REJECTED,
    RETRY_SCHEDULED,
    FAILED
}
