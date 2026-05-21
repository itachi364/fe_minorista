package com.msvanegasg.facturaelectronica.thirdparty.domain.model;

public final class NitVerificationDigit {

    private static final int[] WEIGHTS_RIGHT_TO_LEFT = {
            3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71
    };

    private NitVerificationDigit() {
    }

    public static int calculate(String documentNumber) {
        String normalized = normalize(documentNumber);
        int sum = 0;
        int weightIndex = 0;
        for (int index = normalized.length() - 1; index >= 0; index--) {
            int digit = Character.digit(normalized.charAt(index), 10);
            sum += digit * WEIGHTS_RIGHT_TO_LEFT[weightIndex++];
        }
        int residue = sum % 11;
        if (residue == 0) {
            return 0;
        }
        if (residue == 1) {
            return 1;
        }
        return 11 - residue;
    }

    private static String normalize(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("documentNumber is required");
        }
        String normalized = value.trim();
        if (normalized.length() > WEIGHTS_RIGHT_TO_LEFT.length) {
            throw new IllegalArgumentException("documentNumber length is invalid for NIT verification digit");
        }
        if (!normalized.chars().allMatch(Character::isDigit)) {
            throw new IllegalArgumentException("documentNumber must contain only digits");
        }
        return normalized;
    }
}
