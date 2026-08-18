package com.msvanegasg.facturaelectronica.bff.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class BffRouteResolverTest {

    private final BffRouteResolver resolver = new BffRouteResolver();

    @Test
    void resolvesCoreBusinessRoutes() {
        assertThat(resolver.resolve("/api/v1/companies")).isEqualTo(TargetService.TENANT);
        assertThat(resolver.resolve("/api/v1/auth/login")).isEqualTo(TargetService.IDENTITY);
        assertThat(resolver.resolve("/api/v1/catalog-definitions")).isEqualTo(TargetService.CATALOG);
        assertThat(resolver.resolve("/api/v1/catalogs/countries")).isEqualTo(TargetService.CATALOG);
        assertThat(resolver.resolve("/api/v1/third-parties")).isEqualTo(TargetService.THIRDPARTY);
        assertThat(resolver.resolve("/api/v1/products/123/kardex")).isEqualTo(TargetService.INVENTORY);
        assertThat(resolver.resolve("/api/v1/sales/123/confirm")).isEqualTo(TargetService.BILLING);
        assertThat(resolver.resolve("/api/v1/accounting-rules")).isEqualTo(TargetService.ACCOUNTING);
        assertThat(resolver.resolve("/api/v1/payroll/workers")).isEqualTo(TargetService.PAYROLL);
        assertThat(resolver.resolve("/api/v1/audit-events")).isEqualTo(TargetService.AUDIT);
    }

    @Test
    void resolvesTenantAndIdentityCompanyRoutes() {
        assertThat(resolver.resolve("/api/v1/companies/11111111-1111-1111-1111-111111111111/license/validation"))
                .isEqualTo(TargetService.TENANT);
        assertThat(resolver.resolve("/api/v1/companies/11111111-1111-1111-1111-111111111111/memberships"))
                .isEqualTo(TargetService.IDENTITY);
        assertThat(resolver.resolve("/api/v1/companies/11111111-1111-1111-1111-111111111111/users/22222222-2222-2222-2222-222222222222/roles"))
                .isEqualTo(TargetService.IDENTITY);
        assertThat(resolver.resolve("/api/v1/companies/11111111-1111-1111-1111-111111111111/permissions"))
                .isEqualTo(TargetService.IDENTITY);
        assertThat(resolver.resolve("/api/v1/platform/permissions"))
                .isEqualTo(TargetService.IDENTITY);
        assertThat(resolver.resolve("/api/v1/companies/11111111-1111-1111-1111-111111111111/roles"))
                .isEqualTo(TargetService.IDENTITY);
        assertThat(resolver.resolve("/api/v1/companies/11111111-1111-1111-1111-111111111111/users/22222222-2222-2222-2222-222222222222/role-assignments"))
                .isEqualTo(TargetService.IDENTITY);
        assertThat(resolver.resolve("/api/v1/companies/11111111-1111-1111-1111-111111111111/users/22222222-2222-2222-2222-222222222222/effective-permissions"))
                .isEqualTo(TargetService.IDENTITY);
    }

    @Test
    void resolvesReportRoutesByOwningService() {
        assertThat(resolver.resolve("/api/v1/reports/sales")).isEqualTo(TargetService.BILLING);
        assertThat(resolver.resolve("/api/v1/reports/inventory-stock")).isEqualTo(TargetService.INVENTORY);
        assertThat(resolver.resolve("/api/v1/reports/journal")).isEqualTo(TargetService.ACCOUNTING);
        assertThat(resolver.resolve("/api/v1/reports/income-statement")).isEqualTo(TargetService.ACCOUNTING);
        assertThat(resolver.resolve("/api/v1/reports/balance-sheet")).isEqualTo(TargetService.ACCOUNTING);
    }

    @Test
    void rejectsUnsupportedRoutes() {
        assertThatThrownBy(() -> resolver.resolve("/api/v1/provider/electronic-pos"))
                .isInstanceOf(UnsupportedBffRouteException.class);
    }
}
