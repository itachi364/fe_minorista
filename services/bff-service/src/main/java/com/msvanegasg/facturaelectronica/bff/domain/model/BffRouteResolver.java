package com.msvanegasg.facturaelectronica.bff.domain.model;

import java.util.Locale;

public final class BffRouteResolver {

    public TargetService resolve(String path) {
        String normalized = normalize(path);
        if (normalized.isEmpty()) {
            throw new UnsupportedBffRouteException(path);
        }
        if (isIdentityCompanyRoute(normalized)) {
            return TargetService.IDENTITY;
        }
        if (isTenantCompanyRoute(normalized)) {
            return TargetService.TENANT;
        }
        if (matchesAny(normalized, "auth", "users", "me", "platform")) {
            return TargetService.IDENTITY;
        }
        if (matchesAny(normalized, "catalog-definitions", "catalogs", "company-catalogs")) {
            return TargetService.CATALOG;
        }
        if (matchesAny(normalized, "third-parties", "customers", "suppliers")) {
            return TargetService.THIRDPARTY;
        }
        if (matchesAny(normalized, "products", "purchases", "inventory-movements", "service-supply-references")) {
            return TargetService.INVENTORY;
        }
        if (matchesAny(normalized, "fiscal-policy", "issuers", "numbering-resolutions", "sales", "electronic-pos",
                "electronic-invoices", "credit-notes", "debit-notes")) {
            return TargetService.BILLING;
        }
        if (matchesAny(normalized, "accounts", "accounting-rules", "accounting-setup", "accounting-configuration",
                "accounting-entries", "accounts-payable", "accounts-receivable", "expenses")) {
            return TargetService.ACCOUNTING;
        }
        if (matchesAny(normalized, "payroll")) {
            return TargetService.PAYROLL;
        }
        if (matchesAny(normalized, "report-definitions")
                || normalized.equals("reports/query")
                || normalized.equals("reports/export")
                || matchesAny(normalized, "reports/export-jobs")
                || matchesAny(normalized, "reportes/descarga")
                || normalized.matches("reports/[^/]+/options")) {
            return TargetService.REPORTING;
        }
        if (normalized.startsWith("reports/")) {
            return resolveReport(normalized);
        }
        if (matchesAny(normalized, "audit-events")) {
            return TargetService.AUDIT;
        }
        if (matchesAny(normalized, "dian-configuration", "provider")) {
            return TargetService.DIAN_PROVIDER;
        }
        throw new UnsupportedBffRouteException(path);
    }

    private static boolean isIdentityCompanyRoute(String normalized) {
        return normalized.matches("companies/[^/]+/memberships(/.*)?")
                || normalized.matches("companies/[^/]+/users(/.*)?")
                || normalized.matches("companies/[^/]+/users/[^/]+/role-assignments(/.*)?")
                || normalized.matches("companies/[^/]+/users/[^/]+/effective-permissions(/.*)?")
                || normalized.matches("companies/[^/]+/operational-pin(/.*)?")
                || normalized.matches("companies/[^/]+/roles(/.*)?")
                || normalized.matches("companies/[^/]+/permissions(/.*)?");
    }

    private static boolean isTenantCompanyRoute(String normalized) {
        return normalized.equals("companies")
                || normalized.matches("companies/[^/]+")
                || normalized.matches("companies/[^/]+/(activate|suspend)")
                || normalized.matches("companies/[^/]+/branding(/.*)?")
                || normalized.matches("companies/[^/]+/files(/.*)?")
                || normalized.matches("companies/[^/]+/license(/.*)?");
    }

    private static TargetService resolveReport(String normalized) {
        if (matchesAny(normalized, "reports/sales", "reports/electronic-documents")) {
            return TargetService.BILLING;
        }
        if (matchesAny(normalized, "reports/inventory-stock", "reports/kardex", "reports/purchases")) {
            return TargetService.INVENTORY;
        }
        if (matchesAny(normalized, "reports/expenses", "reports/journal", "reports/ledger", "reports/trial-balance",
                "reports/accounts-receivable", "reports/income-statement", "reports/balance-sheet")) {
            return TargetService.ACCOUNTING;
        }
        throw new UnsupportedBffRouteException("/api/v1/" + normalized);
    }

    private static String normalize(String path) {
        String value = path == null ? "" : path.strip().toLowerCase(Locale.ROOT);
        if (value.startsWith("/")) {
            value = value.substring(1);
        }
        if (value.startsWith("api/v1/")) {
            value = value.substring("api/v1/".length());
        }
        return value;
    }

    private static boolean matchesAny(String normalized, String... prefixes) {
        for (String prefix : prefixes) {
            if (normalized.equals(prefix) || normalized.startsWith(prefix + "/")) {
                return true;
            }
        }
        return false;
    }
}
