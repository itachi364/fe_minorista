package com.msvanegasg.facturaelectronica.reporting.domain.model;

import java.util.List;
import java.util.Optional;

public final class ReportCatalog {

    private static final List<ReportDefinition> DEFINITIONS = List.of(
            report("SALES_BY_SELLER", "Ventas por vendedor", "Ventas confirmadas agrupables por usuario vendedor.",
                    "Ventas", List.of(date("from", "Desde", true), date("to", "Hasta", true),
                            select("sellerId", "Vendedor", false, "SELLERS")),
                    List.of(ChartType.TABLE, ChartType.BAR, ChartType.LINE)),
            report("SALES_BY_PRODUCT", "Ventas por producto", "Ventas confirmadas por producto o servicio.",
                    "Ventas", List.of(date("from", "Desde", true), date("to", "Hasta", true),
                            select("productId", "Producto", false, "PRODUCTS")),
                    List.of(ChartType.TABLE, ChartType.BAR)),
            report("PURCHASES", "Compras realizadas", "Compras e incrementos de inventario por periodo.",
                    "Inventario", List.of(date("from", "Desde", true), date("to", "Hasta", true)),
                    List.of(ChartType.TABLE, ChartType.BAR)),
            report("INVENTORY_STOCK", "Estado de inventario", "Stock actual de productos, servicios e insumos.",
                    "Inventario", List.of(select("active", "Estado", false, "ACTIVE_STATUS")),
                    List.of(ChartType.TABLE, ChartType.BAR)),
            report("PROFITABILITY", "Rentabilidad operativa", "Ingresos, costos y margen por periodo.",
                    "Contabilidad", List.of(date("from", "Desde", true), date("to", "Hasta", true)),
                    List.of(ChartType.TABLE, ChartType.BAR, ChartType.LINE)),
            report("DAILY_PROFIT_AND_LOSS", "Ganancias y gastos diarios",
                    "Ingresos, egresos y resultado neto por dia o rango corto.",
                    "Contabilidad", List.of(date("from", "Desde", true), date("to", "Hasta", true)),
                    List.of(ChartType.TABLE, ChartType.BAR)),
            report("CASH_FLOW", "Flujo de caja", "Entradas, salidas y saldo operativo del periodo.",
                    "Contabilidad", List.of(date("from", "Desde", true), date("to", "Hasta", true)),
                    List.of(ChartType.TABLE, ChartType.BAR, ChartType.LINE)),
            report("ACCOUNTS_PAYABLE", "Cuentas por pagar", "Obligaciones pendientes y vencimientos por proveedor.",
                    "Contabilidad", List.of(date("from", "Desde", false), date("to", "Hasta", false),
                            select("status", "Estado", false, "ACCOUNT_STATUS")),
                    List.of(ChartType.TABLE)),
            report("FINANCIAL_DAILY_SUMMARY", "Resumen financiero diario",
                    "Ventas, compras, gastos, cartera y obligaciones para decision diaria.",
                    "Contabilidad", List.of(date("from", "Desde", true), date("to", "Hasta", true)),
                    List.of(ChartType.TABLE, ChartType.BAR)),
            report("ACCOUNTS_RECEIVABLE", "Cuentas por cobrar", "Cartera y saldos pendientes por cliente.",
                    "Contabilidad", List.of(date("from", "Desde", false), date("to", "Hasta", false),
                            select("status", "Estado", false, "ACCOUNT_STATUS")),
                    List.of(ChartType.TABLE)),
            report("PAYROLL_DAILY_PAYMENTS", "Pagos diarios de nomina", "Pagos por contratacion diaria o informal.",
                    "Nomina", List.of(date("from", "Desde", true), date("to", "Hasta", true)),
                    List.of(ChartType.TABLE, ChartType.BAR)),
            report("LICENSE_USAGE", "Uso de licencia", "Uso de documentos y limites contratados por empresa.",
                    "Plataforma", List.of(date("from", "Desde", true), date("to", "Hasta", true)),
                    List.of(ChartType.TABLE)));

    private ReportCatalog() {
    }

    public static List<ReportDefinition> definitions() {
        return DEFINITIONS;
    }

    public static Optional<ReportDefinition> find(String code) {
        return DEFINITIONS.stream().filter(definition -> definition.code().equalsIgnoreCase(code)).findFirst();
    }

    private static ReportDefinition report(String code, String label, String description, String category,
            List<ReportFilter> filters, List<ChartType> chartTypes) {
        return new ReportDefinition(code, label, description, category, filters, chartTypes);
    }

    private static ReportFilter date(String code, String label, boolean required) {
        return new ReportFilter(code, label, FilterType.DATE, required, null);
    }

    private static ReportFilter select(String code, String label, boolean required, String optionSource) {
        return new ReportFilter(code, label, FilterType.SELECT, required, optionSource);
    }
}
