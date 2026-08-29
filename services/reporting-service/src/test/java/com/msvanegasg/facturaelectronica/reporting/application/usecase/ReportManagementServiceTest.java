package com.msvanegasg.facturaelectronica.reporting.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportOption;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportQueryCommand;
import com.msvanegasg.facturaelectronica.reporting.application.dto.ReportQueryResult;
import com.msvanegasg.facturaelectronica.reporting.application.port.out.ReportDataGateway;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ChartType;
import com.msvanegasg.facturaelectronica.reporting.domain.model.ReportExportFormat;

class ReportManagementServiceTest {

    private static final UUID COMPANY_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");

    private final FakeReportDataGateway gateway = new FakeReportDataGateway();
    private final ReportManagementService service = new ReportManagementService(gateway);

    @Test
    void listsReportDefinitionsAndOptions() {
        assertThat(service.definitions()).extracting("code").contains("SALES_BY_SELLER", "INVENTORY_STOCK");

        var options = service.options(COMPANY_ID, "SALES_BY_SELLER", "Bearer token");

        assertThat(options.options()).containsKey("sellerId");
        assertThat(options.options().get("sellerId")).containsExactly(new ReportOption("seller-1", "Ana Rojas"));
    }

    @Test
    void queriesReportWithNormalizedFilters() {
        ReportQueryResult result = service.query(new ReportQueryCommand(COMPANY_ID, "SALES_BY_PRODUCT",
                LocalDate.parse("2026-08-01"), LocalDate.parse("2026-08-24"), Map.of("productId", "p-1"),
                ChartType.BAR, "Bearer token"));

        assertThat(result.reportCode()).isEqualTo("SALES_BY_PRODUCT");
        assertThat(result.chartType()).isEqualTo(ChartType.BAR);
        assertThat(result.appliedFilters()).containsEntry("from", "2026-08-01").containsEntry("productId", "p-1");
        assertThat(result.columns()).extracting("label")
                .containsExactly("Producto", "Cantidad vendida", "Subtotal", "IVA", "Total", "Ventas");
        assertThat(result.rows()).hasSize(1);
        assertThat(result.rows().get(0)).containsEntry("product", "Cafe");
        assertThat(result.rows().get(0)).containsEntry("sales", 2);
        assertThat(result.series()).hasSize(1);
        assertThat(result.series().get(0).label()).isEqualTo("Cafe");
        assertThat(gateway.reportCode).isEqualTo("SALES_BY_PRODUCT");
    }

    @Test
    void normalizesSalesBySellerWithoutTechnicalColumns() {
        ReportQueryResult result = service.query(new ReportQueryCommand(COMPANY_ID, "SALES_BY_SELLER",
                LocalDate.parse("2026-08-01"), LocalDate.parse("2026-08-24"), Map.of(), ChartType.BAR,
                "Bearer token"));

        assertThat(result.columns()).extracting("label")
                .containsExactly("Vendedor", "Ventas cerradas", "Subtotal", "IVA", "Total", "Documentos emitidos");
        assertThat(result.columns()).extracting("label")
                .doesNotContain("Company Id", "Idempotency Key", "Created By");
        assertThat(result.rows()).hasSize(1);
        assertThat(result.rows().get(0)).containsEntry("seller", "Vendedor 99999999")
                .containsEntry("closedSales", 2)
                .containsEntry("documents", 1);
    }

    @Test
    void rejectsUnknownReportInvalidDatesAndUnsupportedChart() {
        assertThatThrownBy(() -> service.query(new ReportQueryCommand(COMPANY_ID, "UNKNOWN", null, null, Map.of(),
                ChartType.TABLE, null)))
                .isInstanceOf(ReportNotFoundException.class);

        assertThatThrownBy(() -> service.query(new ReportQueryCommand(COMPANY_ID, "SALES_BY_PRODUCT",
                LocalDate.parse("2026-08-24"), LocalDate.parse("2026-08-01"), Map.of(), ChartType.TABLE, null)))
                .isInstanceOf(IllegalArgumentException.class);

        assertThatThrownBy(() -> service.query(new ReportQueryCommand(COMPANY_ID, "ACCOUNTS_RECEIVABLE", null, null,
                Map.of(), ChartType.PIE, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void exportsReportAsCsvAndExcelCompatibleFile() {
        var command = new ReportQueryCommand(COMPANY_ID, "SALES_BY_PRODUCT", LocalDate.parse("2026-08-01"),
                LocalDate.parse("2026-08-24"), Map.of(), ChartType.TABLE, "Bearer token");

        var csv = service.export(command, ReportExportFormat.CSV);
        var xls = service.export(command, ReportExportFormat.XLS);

        assertThat(csv.filename()).endsWith(".csv");
        assertThat(new String(csv.content(), java.nio.charset.StandardCharsets.UTF_8)).contains("Producto")
                .contains("Cantidad vendida")
                .contains("Cafe")
                .doesNotContain("companyId")
                .doesNotContain("idempotencyKey");
        assertThat(xls.filename()).endsWith(".xls");
        assertThat(new String(xls.content(), java.nio.charset.StandardCharsets.UTF_8)).contains("Workbook")
                .contains("Cafe");
    }

    private static final class FakeReportDataGateway implements ReportDataGateway {

        private String reportCode;

        @Override
        public JsonNode fetchReport(UUID companyId, String reportCode, LocalDate from, LocalDate to,
                Map<String, String> filters, String authorizationHeader) {
            this.reportCode = reportCode;
            var mapper = new ObjectMapper();
            return mapper.createArrayNode()
                    .add(sale(mapper, "sale-1", "CONFIRMED", "99999999-9999-9999-9999-999999999999", "Cafe", 1,
                            "10000.00", "1900.00", "11900.00", true))
                    .add(sale(mapper, "sale-2", "CONFIRMED", "99999999-9999-9999-9999-999999999999", "Cafe", 2,
                            "20000.00", "3800.00", "23800.00", false))
                    .add(sale(mapper, "sale-3", "DRAFT", "99999999-9999-9999-9999-999999999999", "Borrador", 1,
                            "5000.00", "950.00", "5950.00", false));
        }

        @Override
        public List<ReportOption> fetchOptions(UUID companyId, String optionSource, String authorizationHeader) {
            if ("SELLERS".equals(optionSource)) {
                return List.of(new ReportOption("seller-1", "Ana Rojas"));
            }
            return List.of();
        }

        private static JsonNode sale(ObjectMapper mapper, String id, String status, String createdBy, String productName,
                int quantity, String subtotal, String taxAmount, String total, boolean withDocument) {
            var sale = mapper.createObjectNode();
            sale.put("id", id);
            sale.put("companyId", COMPANY_ID.toString());
            sale.put("status", status);
            sale.put("createdBy", createdBy);
            sale.put("subtotal", subtotal);
            sale.put("taxTotal", taxAmount);
            sale.put("total", total);
            sale.put("idempotencyKey", "technical-key");
            var line = mapper.createObjectNode();
            line.put("productId", "product-1");
            line.put("productName", productName);
            line.put("quantity", quantity);
            line.put("subtotal", subtotal);
            line.put("taxAmount", taxAmount);
            line.put("total", total);
            sale.set("lines", mapper.createArrayNode().add(line));
            if (withDocument) {
                var document = mapper.createObjectNode();
                document.put("status", "VALIDATED");
                sale.set("electronicDocument", document);
            }
            return sale;
        }
    }
}
