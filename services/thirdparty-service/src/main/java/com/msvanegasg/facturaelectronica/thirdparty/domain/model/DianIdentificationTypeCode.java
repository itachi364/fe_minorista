package com.msvanegasg.facturaelectronica.thirdparty.domain.model;

import java.util.Set;

public final class DianIdentificationTypeCode {

    private static final Set<Integer> SUPPORTED_CODES = Set.of(11, 12, 13, 21, 22, 31, 41, 42, 43, 47, 48);

    private DianIdentificationTypeCode() {
    }

    public static void validate(Integer code) {
        if (code == null) {
            throw new IllegalArgumentException("identificationTypeCode is required");
        }
        if (!SUPPORTED_CODES.contains(code)) {
            throw new IllegalArgumentException("identificationTypeCode must be a valid DIAN document type code");
        }
    }
}
