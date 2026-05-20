package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public final class ElectronicPosDocument {

    private final UUID id;
    private final UUID companyId;
    private final UUID saleId;
    private final BuyerInformation buyerInformation;
    private final String prefix;
    private final long number;
    private final String cude;
    private final BigDecimal subtotal;
    private final BigDecimal taxTotal;
    private final BigDecimal total;
    private final List<CalculatedDocumentLine> lines;
    private final ElectronicDocumentStatus status;
    private final Instant issueAt;

    private ElectronicPosDocument(
            UUID id,
            UUID companyId,
            UUID saleId,
            BuyerInformation buyerInformation,
            String prefix,
            long number,
            String cude,
            BigDecimal subtotal,
            BigDecimal taxTotal,
            BigDecimal total,
            List<CalculatedDocumentLine> lines,
            ElectronicDocumentStatus status,
            Instant issueAt) {
        this.id = id;
        this.companyId = companyId;
        this.saleId = saleId;
        this.buyerInformation = buyerInformation;
        this.prefix = prefix;
        this.number = number;
        this.cude = cude;
        this.subtotal = subtotal;
        this.taxTotal = taxTotal;
        this.total = total;
        this.lines = List.copyOf(lines);
        this.status = status;
        this.issueAt = issueAt;
    }

    public static ElectronicPosDocument issue(
            UUID id,
            UUID companyId,
            UUID saleId,
            BuyerInformation buyerInformation,
            FiscalNumberAssignment fiscalNumber,
            CalculatedElectronicDocument calculatedDocument,
            Instant issueAt) {
        requireNonNull(id, "id");
        requireNonNull(companyId, "companyId");
        requireNonNull(fiscalNumber, "fiscalNumber");
        requireNonNull(calculatedDocument, "calculatedDocument");
        requireNonNull(issueAt, "issueAt");

        String cude = CudeGenerator.generateForPos(
                companyId,
                fiscalNumber.prefix(),
                fiscalNumber.number(),
                issueAt,
                calculatedDocument.subtotal(),
                calculatedDocument.taxTotal(),
                calculatedDocument.total(),
                buyerInformation);

        return new ElectronicPosDocument(
                id,
                companyId,
                saleId,
                buyerInformation == null ? new BuyerInformation(null, null, null) : buyerInformation,
                fiscalNumber.prefix(),
                fiscalNumber.number(),
                cude,
                calculatedDocument.subtotal(),
                calculatedDocument.taxTotal(),
                calculatedDocument.total(),
                calculatedDocument.lines(),
                ElectronicDocumentStatus.NUMBER_ASSIGNED,
                issueAt);
    }

    public static ElectronicPosDocument restore(
            UUID id,
            UUID companyId,
            UUID saleId,
            BuyerInformation buyerInformation,
            String prefix,
            long number,
            String cude,
            BigDecimal subtotal,
            BigDecimal taxTotal,
            BigDecimal total,
            List<CalculatedDocumentLine> lines,
            ElectronicDocumentStatus status,
            Instant issueAt) {
        requireNonNull(id, "id");
        requireNonNull(companyId, "companyId");
        requireNonNull(prefix, "prefix");
        requireNonNull(cude, "cude");
        requireNonNull(subtotal, "subtotal");
        requireNonNull(taxTotal, "taxTotal");
        requireNonNull(total, "total");
        requireNonNull(lines, "lines");
        requireNonNull(status, "status");
        requireNonNull(issueAt, "issueAt");

        return new ElectronicPosDocument(
                id,
                companyId,
                saleId,
                buyerInformation == null ? new BuyerInformation(null, null, null) : buyerInformation,
                prefix,
                number,
                cude,
                subtotal,
                taxTotal,
                total,
                lines,
                status,
                issueAt);
    }

    public UUID id() {
        return id;
    }

    public UUID companyId() {
        return companyId;
    }

    public UUID saleId() {
        return saleId;
    }

    public BuyerInformation buyerInformation() {
        return buyerInformation;
    }

    public String prefix() {
        return prefix;
    }

    public long number() {
        return number;
    }

    public String cude() {
        return cude;
    }

    public BigDecimal subtotal() {
        return subtotal;
    }

    public BigDecimal taxTotal() {
        return taxTotal;
    }

    public BigDecimal total() {
        return total;
    }

    public List<CalculatedDocumentLine> lines() {
        return lines;
    }

    public ElectronicDocumentStatus status() {
        return status;
    }

    public Instant issueAt() {
        return issueAt;
    }

    private static void requireNonNull(Object value, String fieldName) {
        Objects.requireNonNull(value, fieldName + " is required");
    }
}
