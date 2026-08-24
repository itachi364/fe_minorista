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
        assertThat(gateway.reportCode).isEqualTo("SALES_BY_PRODUCT");
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
        assertThat(new String(csv.content(), java.nio.charset.StandardCharsets.UTF_8)).contains("productName")
                .contains("Cafe");
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
            var row = mapper.createObjectNode();
            row.put("productName", "Cafe");
            row.put("total", 15000);
            return mapper.createArrayNode().add(row);
        }

        @Override
        public List<ReportOption> fetchOptions(UUID companyId, String optionSource, String authorizationHeader) {
            if ("SELLERS".equals(optionSource)) {
                return List.of(new ReportOption("seller-1", "Ana Rojas"));
            }
            return List.of();
        }
    }
}
