package com.msvanegasg.facturaelectronica.reporting.application.usecase;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportColumn;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportSeriesPoint;

final class ReportDatasetNormalizer {

    private static final List<ReportColumn> SALES_BY_PRODUCT_COLUMNS = List.of(
            new ReportColumn("product", "Producto", "text"),
            new ReportColumn("quantitySold", "Cantidad vendida", "number"),
            new ReportColumn("subtotal", "Subtotal", "money"),
            new ReportColumn("taxTotal", "IVA", "money"),
            new ReportColumn("total", "Total", "money"),
            new ReportColumn("sales", "Ventas", "number"));

    private static final List<ReportColumn> SALES_BY_SELLER_COLUMNS = List.of(
            new ReportColumn("seller", "Vendedor", "text"),
            new ReportColumn("closedSales", "Ventas cerradas", "number"),
            new ReportColumn("subtotal", "Subtotal", "money"),
            new ReportColumn("taxTotal", "IVA", "money"),
            new ReportColumn("total", "Total", "money"),
            new ReportColumn("documents", "Documentos emitidos", "number"));

    private ReportDatasetNormalizer() {
    }

    static NormalizedReportDataset normalize(String reportCode, JsonNode payload) {
        return switch (reportCode) {
            case "SALES_BY_PRODUCT" -> salesByProduct(payload);
            case "SALES_BY_SELLER" -> salesBySeller(payload);
            default -> genericDataset(payload);
        };
    }

    private static NormalizedReportDataset salesByProduct(JsonNode payload) {
        Map<String, ProductAccumulator> products = new LinkedHashMap<>();
        for (JsonNode sale : iterable(payload)) {
            if (!isConfirmedSale(sale)) {
                continue;
            }
            JsonNode lines = sale.path("lines");
            if (!lines.isArray()) {
                continue;
            }
            for (JsonNode line : lines) {
                String key = text(line, "productId", text(line, "productSku", text(line, "productName", "sin-producto")));
                String label = text(line, "productName", text(line, "productSku", "Producto sin nombre"));
                ProductAccumulator accumulator = products.computeIfAbsent(key, ignored -> new ProductAccumulator(label));
                accumulator.quantitySold = accumulator.quantitySold.add(decimal(line, "quantity"));
                accumulator.subtotal = accumulator.subtotal.add(decimal(line, "subtotal"));
                accumulator.taxTotal = accumulator.taxTotal.add(decimal(line, "taxAmount"));
                accumulator.total = accumulator.total.add(decimal(line, "total"));
                accumulator.saleIds.add(text(sale, "id", ""));
            }
        }
        List<Map<String, Object>> rows = products.values().stream()
                .sorted(Comparator.comparing(ProductAccumulator::total).reversed())
                .map(ProductAccumulator::row)
                .toList();
        return new NormalizedReportDataset(SALES_BY_PRODUCT_COLUMNS, rows, series(rows, "product", "total"));
    }

    private static NormalizedReportDataset salesBySeller(JsonNode payload) {
        Map<String, SellerAccumulator> sellers = new LinkedHashMap<>();
        for (JsonNode sale : iterable(payload)) {
            if (!isConfirmedSale(sale)) {
                continue;
            }
            String sellerId = text(sale, "createdBy", "");
            String key = sellerId.isBlank() ? "sin-vendedor" : sellerId;
            String label = sellerId.isBlank() ? "Vendedor sin identificar" : "Vendedor " + sellerId.substring(0, Math.min(8, sellerId.length()));
            SellerAccumulator accumulator = sellers.computeIfAbsent(key, ignored -> new SellerAccumulator(label));
            accumulator.closedSales++;
            accumulator.subtotal = accumulator.subtotal.add(decimal(sale, "subtotal"));
            accumulator.taxTotal = accumulator.taxTotal.add(decimal(sale, "taxTotal"));
            accumulator.total = accumulator.total.add(decimal(sale, "total"));
            if (sale.hasNonNull("electronicDocument")) {
                accumulator.documents++;
            }
        }
        List<Map<String, Object>> rows = sellers.values().stream()
                .sorted(Comparator.comparing(SellerAccumulator::total).reversed())
                .map(SellerAccumulator::row)
                .toList();
        return new NormalizedReportDataset(SALES_BY_SELLER_COLUMNS, rows, series(rows, "seller", "total"));
    }

    private static NormalizedReportDataset genericDataset(JsonNode payload) {
        JsonNode source = selectArray(payload);
        if (!source.isArray() || source.size() == 0) {
            return new NormalizedReportDataset(List.of(new ReportColumn("result", "Resultado", "text")), List.of(),
                    List.of());
        }
        List<Map<String, Object>> rows = new ArrayList<>();
        Set<String> keys = new LinkedHashSet<>();
        for (JsonNode item : iterable(source)) {
            Map<String, Object> row = new LinkedHashMap<>();
            item.properties().forEach(entry -> {
                if (isApprovedGenericField(entry.getKey()) && entry.getValue().isValueNode()) {
                    row.put(entry.getKey(), cellValue(entry.getValue()));
                    keys.add(entry.getKey());
                }
            });
            if (!row.isEmpty()) {
                rows.add(row);
            }
        }
        List<ReportColumn> columns = keys.stream()
                .map(key -> new ReportColumn(key, humanize(key), "text"))
                .toList();
        return new NormalizedReportDataset(columns, rows, List.of());
    }

    private static boolean isApprovedGenericField(String key) {
        return !key.equals("id") && !key.equals("companyId") && !key.equals("idempotencyKey") && !key.equals("createdBy");
    }

    private static List<ReportSeriesPoint> series(List<Map<String, Object>> rows, String labelKey, String valueKey) {
        return rows.stream()
                .map(row -> new ReportSeriesPoint(String.valueOf(row.getOrDefault(labelKey, "")),
                        (BigDecimal) row.getOrDefault(valueKey, BigDecimal.ZERO)))
                .filter(point -> point.value().compareTo(BigDecimal.ZERO) > 0)
                .toList();
    }

    private static JsonNode selectArray(JsonNode payload) {
        if (payload == null || payload.isNull()) {
            return com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.arrayNode();
        }
        if (payload.isArray()) {
            return payload;
        }
        if (!payload.isObject()) {
            return com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.arrayNode();
        }
        JsonNode selected = null;
        for (Map.Entry<String, JsonNode> entry : payload.properties()) {
            if (entry.getValue().isArray() && (selected == null || entry.getValue().size() > selected.size())) {
                selected = entry.getValue();
            }
        }
        return selected == null ? com.fasterxml.jackson.databind.node.JsonNodeFactory.instance.arrayNode() : selected;
    }

    private static Iterable<JsonNode> iterable(JsonNode node) {
        JsonNode source = selectArray(node);
        return source::elements;
    }

    private static boolean isConfirmedSale(JsonNode sale) {
        return "CONFIRMED".equalsIgnoreCase(text(sale, "status", ""));
    }

    private static BigDecimal decimal(JsonNode node, String field) {
        JsonNode value = node.path(field);
        if (value.isNumber()) {
            return value.decimalValue().setScale(2, RoundingMode.HALF_UP);
        }
        if (value.isTextual() && !value.asText().isBlank()) {
            try {
                return new BigDecimal(value.asText()).setScale(2, RoundingMode.HALF_UP);
            } catch (NumberFormatException ignored) {
                return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
            }
        }
        return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
    }

    private static String text(JsonNode node, String field, String fallback) {
        JsonNode value = node.path(field);
        if (value.isMissingNode() || value.isNull()) {
            return fallback;
        }
        return value.asText(fallback);
    }

    private static Object cellValue(JsonNode value) {
        if (value == null || value.isNull()) {
            return "";
        }
        if (value.isNumber()) {
            return value.decimalValue();
        }
        if (value.isBoolean()) {
            return value.booleanValue();
        }
        return value.asText();
    }

    private static String humanize(String value) {
        String withSpaces = value.replace("_", " ").replaceAll("([a-z])([A-Z])", "$1 $2");
        return Character.toUpperCase(withSpaces.charAt(0)) + withSpaces.substring(1);
    }

    record NormalizedReportDataset(List<ReportColumn> columns, List<Map<String, Object>> rows,
            List<ReportSeriesPoint> series) {
    }

    private static final class ProductAccumulator {
        private final String product;
        private final Set<String> saleIds = new LinkedHashSet<>();
        private BigDecimal quantitySold = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private BigDecimal subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private BigDecimal taxTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        private ProductAccumulator(String product) {
            this.product = product;
        }

        private BigDecimal total() {
            return total;
        }

        private Map<String, Object> row() {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("product", product);
            row.put("quantitySold", quantitySold);
            row.put("subtotal", subtotal);
            row.put("taxTotal", taxTotal);
            row.put("total", total);
            row.put("sales", saleIds.size());
            return row;
        }
    }

    private static final class SellerAccumulator {
        private final String seller;
        private int closedSales;
        private int documents;
        private BigDecimal subtotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private BigDecimal taxTotal = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        private BigDecimal total = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

        private SellerAccumulator(String seller) {
            this.seller = seller;
        }

        private BigDecimal total() {
            return total;
        }

        private Map<String, Object> row() {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("seller", seller);
            row.put("closedSales", closedSales);
            row.put("subtotal", subtotal);
            row.put("taxTotal", taxTotal);
            row.put("total", total);
            row.put("documents", documents);
            return row;
        }
    }
}
