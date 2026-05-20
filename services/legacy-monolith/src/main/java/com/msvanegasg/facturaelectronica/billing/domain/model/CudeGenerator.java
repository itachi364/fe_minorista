package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.UUID;

public final class CudeGenerator {

    private CudeGenerator() {
    }

    public static String generateForPos(
            UUID companyId,
            String prefix,
            long number,
            Instant issueAt,
            BigDecimal subtotal,
            BigDecimal taxTotal,
            BigDecimal total,
            BuyerInformation buyerInformation) {
        String seed = String.join("|",
                companyId.toString(),
                ElectronicDocumentType.ELECTRONIC_POS.name(),
                prefix == null ? "" : prefix,
                Long.toString(number),
                issueAt.toString(),
                subtotal.toPlainString(),
                taxTotal.toPlainString(),
                total.toPlainString(),
                buyerInformation == null || !buyerInformation.isPresent() ? "" : buyerInformation.documentType(),
                buyerInformation == null || !buyerInformation.isPresent() ? "" : buyerInformation.documentNumber());

        return sha384(seed);
    }

    private static String sha384(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-384");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8))).toUpperCase();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-384 algorithm is not available", exception);
        }
    }
}
