-- Legacy data audit for TASK-040/TASK-166.
-- This script is read-only for application data. It uses psql meta-commands
-- to execute COUNT queries only for tables that exist in the current database.
-- It audits historical public.* tables against the current Clean Architecture
-- schemas. It must not be used as a migration or destructive cleanup script.

\echo '== Legacy/public and extracted-schema table availability =='

WITH audit_targets(area, role, schema_name, table_name, replacement) AS (
  VALUES
    ('identity', 'legacy', 'public', 'roles', 'identity.permission_catalog / identity.company_role'),
    ('identity', 'legacy', 'public', 'usuarios', 'identity.user_account / identity.company_membership'),
    ('audit', 'legacy', 'public', 'auditoria', 'audit.audit_event'),
    ('audit', 'legacy', 'public', 'registro_accesos', 'audit.audit_event or future identity-service'),
    ('audit', 'target', 'audit', 'audit_event', 'active audit-service'),
    ('catalog', 'legacy', 'public', 'tipodocumento', 'catalog.catalog_item DIAN_DOCUMENT_TYPE'),
    ('catalog', 'target', 'catalog', 'catalog_item', 'active catalog-service'),
    ('catalog', 'legacy', 'public', 'categoria', 'inventory.product or catalog.catalog_item if configurable'),
    ('catalog', 'legacy', 'public', 'producto', 'inventory.product'),
    ('catalog', 'target', 'inventory', 'product', 'active inventory-service'),
    ('catalog', 'target', 'inventory', 'stock_balance', 'active inventory-service'),
    ('catalog', 'legacy', 'public', 'metodo_pago', 'catalog.catalog_item PAYMENT_METHOD'),
    ('catalog', 'legacy', 'public', 'tipo_gasto', 'catalog.catalog_item / accounting.expense'),
    ('catalog', 'legacy', 'public', 'pais', 'catalog.catalog_item or catalog.department/municipality when applicable'),
    ('catalog', 'legacy', 'public', 'impuesto', 'catalog.catalog_item SALES_TAX'),
    ('catalog', 'legacy', 'public', 'parametros', 'catalog.catalog_item or owning-service config'),
    ('thirdparty', 'legacy', 'public', 'cliente', 'thirdparty.third_party / thirdparty.third_party_role'),
    ('thirdparty', 'target', 'thirdparty', 'third_party', 'active thirdparty-service'),
    ('thirdparty', 'legacy', 'public', 'proveedor', 'thirdparty.third_party / thirdparty.third_party_role'),
    ('thirdparty', 'target', 'thirdparty', 'third_party_role', 'active thirdparty-service'),
    ('inventory', 'legacy', 'public', 'compra', 'inventory.purchase'),
    ('inventory', 'target', 'inventory', 'purchase', 'active inventory-service'),
    ('inventory', 'legacy', 'public', 'detalle_compra', 'inventory.purchase_line'),
    ('inventory', 'target', 'inventory', 'purchase_line', 'active inventory-service'),
    ('inventory', 'target', 'inventory', 'inventory_movement', 'active inventory-service'),
    ('expenses', 'legacy', 'public', 'gastos', 'accounting_expense / accounting_accounts_payable'),
    ('expenses', 'legacy', 'public', 'detalle_gasto', 'accounting_expense detail in accounting-service'),
    ('billing', 'legacy', 'public', 'factura', 'billing.sale and billing.electronic_document'),
    ('billing', 'legacy', 'public', 'detalle_factura', 'billing.sale_line'),
    ('billing', 'target', 'billing', 'sale', 'active billing-service'),
    ('billing', 'target', 'billing', 'sale_line', 'active billing-service'),
    ('billing', 'target', 'billing', 'electronic_document', 'active billing-service'),
    ('billing', 'target', 'billing', 'issuer_profile', 'active billing-service fiscal config'),
    ('billing', 'target', 'billing', 'numbering_resolution', 'active billing-service fiscal config'),
    ('billing', 'legacy', 'public', 'billing_issuer_profile', 'replaced by billing.issuer_profile'),
    ('billing', 'legacy', 'public', 'billing_numbering_resolution', 'replaced by billing.numbering_resolution'),
    ('billing', 'legacy', 'public', 'billing_electronic_pos_document', 'billing.electronic_document partial replacement'),
    ('billing', 'legacy', 'public', 'billing_electronic_pos_document_line', 'billing.sale_line partial replacement'),
    ('billing', 'legacy', 'public', 'billing_provider_submission', 'dian_provider.provider_submission'),
    ('billing', 'target', 'dian_provider', 'provider_submission', 'active dian-provider-service'),
    ('billing', 'legacy', 'public', 'billing_electronic_document_trace_event', 'audit.audit_event / billing.outbox_event'),
    ('billing', 'legacy', 'public', 'billing_fiscal_audit_event', 'audit.audit_event'),
    ('accounting', 'legacy', 'public', 'accounting_account', 'active accounting-service table'),
    ('accounting', 'legacy', 'public', 'accounting_rule', 'active accounting-service table'),
    ('accounting', 'legacy', 'public', 'accounting_rule_line', 'active accounting-service table'),
    ('accounting', 'legacy', 'public', 'accounting_entry', 'active accounting-service table'),
    ('accounting', 'legacy', 'public', 'accounting_entry_line', 'active accounting-service table')
)
SELECT
  area,
  role,
  schema_name,
  table_name,
  CASE WHEN to_regclass(format('%I.%I', schema_name, table_name)) IS NULL THEN 'MISSING' ELSE 'PRESENT' END AS status,
  replacement
FROM audit_targets
ORDER BY area, role, schema_name, table_name;

\echo '== Exact row counts for present audit tables =='

WITH audit_targets(area, role, schema_name, table_name, replacement) AS (
  VALUES
    ('identity', 'legacy', 'public', 'roles', 'identity.permission_catalog / identity.company_role'),
    ('identity', 'legacy', 'public', 'usuarios', 'identity.user_account / identity.company_membership'),
    ('audit', 'legacy', 'public', 'auditoria', 'audit.audit_event'),
    ('audit', 'legacy', 'public', 'registro_accesos', 'audit.audit_event or future identity-service'),
    ('audit', 'target', 'audit', 'audit_event', 'active audit-service'),
    ('catalog', 'legacy', 'public', 'tipodocumento', 'catalog.catalog_item DIAN_DOCUMENT_TYPE'),
    ('catalog', 'target', 'catalog', 'catalog_item', 'active catalog-service'),
    ('catalog', 'legacy', 'public', 'categoria', 'inventory.product or catalog.catalog_item if configurable'),
    ('catalog', 'legacy', 'public', 'producto', 'inventory.product'),
    ('catalog', 'target', 'inventory', 'product', 'active inventory-service'),
    ('catalog', 'target', 'inventory', 'stock_balance', 'active inventory-service'),
    ('catalog', 'legacy', 'public', 'metodo_pago', 'catalog.catalog_item PAYMENT_METHOD'),
    ('catalog', 'legacy', 'public', 'tipo_gasto', 'catalog.catalog_item / accounting.expense'),
    ('catalog', 'legacy', 'public', 'pais', 'catalog.catalog_item or catalog.department/municipality when applicable'),
    ('catalog', 'legacy', 'public', 'impuesto', 'catalog.catalog_item SALES_TAX'),
    ('catalog', 'legacy', 'public', 'parametros', 'catalog.catalog_item or owning-service config'),
    ('thirdparty', 'legacy', 'public', 'cliente', 'thirdparty.third_party / thirdparty.third_party_role'),
    ('thirdparty', 'target', 'thirdparty', 'third_party', 'active thirdparty-service'),
    ('thirdparty', 'legacy', 'public', 'proveedor', 'thirdparty.third_party / thirdparty.third_party_role'),
    ('thirdparty', 'target', 'thirdparty', 'third_party_role', 'active thirdparty-service'),
    ('inventory', 'legacy', 'public', 'compra', 'inventory.purchase'),
    ('inventory', 'target', 'inventory', 'purchase', 'active inventory-service'),
    ('inventory', 'legacy', 'public', 'detalle_compra', 'inventory.purchase_line'),
    ('inventory', 'target', 'inventory', 'purchase_line', 'active inventory-service'),
    ('inventory', 'target', 'inventory', 'inventory_movement', 'active inventory-service'),
    ('expenses', 'legacy', 'public', 'gastos', 'accounting_expense / accounting_accounts_payable'),
    ('expenses', 'legacy', 'public', 'detalle_gasto', 'accounting_expense detail in accounting-service'),
    ('billing', 'legacy', 'public', 'factura', 'billing.sale and billing.electronic_document'),
    ('billing', 'legacy', 'public', 'detalle_factura', 'billing.sale_line'),
    ('billing', 'target', 'billing', 'sale', 'active billing-service'),
    ('billing', 'target', 'billing', 'sale_line', 'active billing-service'),
    ('billing', 'target', 'billing', 'electronic_document', 'active billing-service'),
    ('billing', 'target', 'billing', 'issuer_profile', 'active billing-service fiscal config'),
    ('billing', 'target', 'billing', 'numbering_resolution', 'active billing-service fiscal config'),
    ('billing', 'legacy', 'public', 'billing_issuer_profile', 'replaced by billing.issuer_profile'),
    ('billing', 'legacy', 'public', 'billing_numbering_resolution', 'replaced by billing.numbering_resolution'),
    ('billing', 'legacy', 'public', 'billing_electronic_pos_document', 'billing.electronic_document partial replacement'),
    ('billing', 'legacy', 'public', 'billing_electronic_pos_document_line', 'billing.sale_line partial replacement'),
    ('billing', 'legacy', 'public', 'billing_provider_submission', 'dian_provider.provider_submission'),
    ('billing', 'target', 'dian_provider', 'provider_submission', 'active dian-provider-service'),
    ('billing', 'legacy', 'public', 'billing_electronic_document_trace_event', 'audit.audit_event / billing.outbox_event'),
    ('billing', 'legacy', 'public', 'billing_fiscal_audit_event', 'audit.audit_event'),
    ('accounting', 'legacy', 'public', 'accounting_account', 'active accounting-service table'),
    ('accounting', 'legacy', 'public', 'accounting_rule', 'active accounting-service table'),
    ('accounting', 'legacy', 'public', 'accounting_rule_line', 'active accounting-service table'),
    ('accounting', 'legacy', 'public', 'accounting_entry', 'active accounting-service table'),
    ('accounting', 'legacy', 'public', 'accounting_entry_line', 'active accounting-service table')
)
SELECT format(
  'SELECT %L AS area, %L AS role, %L AS table_name, COUNT(*) AS row_count FROM %I.%I;',
  area,
  role,
  schema_name || '.' || table_name,
  schema_name,
  table_name
)
FROM audit_targets
WHERE to_regclass(format('%I.%I', schema_name, table_name)) IS NOT NULL
ORDER BY area, role, schema_name, table_name
\gexec
