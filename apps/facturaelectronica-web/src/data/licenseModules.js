export const licenseModuleOptions = [
  moduleOption('COMPANY', 'Empresa y configuracion', [
    feature('COMPANY_BASIC', 'Empresa y configuracion basica'), feature('BRANDING_BASIC', 'Identidad visual'),
  ]),
  moduleOption('THIRDPARTY', 'Clientes y proveedores', [
    feature('CUSTOMERS', 'Clientes'), feature('SUPPLIERS', 'Proveedores'),
  ]),
  moduleOption('INVENTORY', 'Inventario', [
    feature('PRODUCTS_SERVICES', 'Productos y servicios'), feature('INVENTORY_BASIC', 'Inventario basico'),
    feature('INVENTORY_ADVANCED', 'Inventario avanzado'),
  ]),
  moduleOption('BILLING', 'Ventas y facturacion electronica', [
    feature('POS_SALES', 'Ventas POS'), feature('ELECTRONIC_BILLING', 'Facturacion electronica y documento equivalente'),
    feature('SALES_REGISTRY', 'Registro de ventas'), feature('FISCAL_SETTINGS_BASIC', 'Configuracion fiscal basica'),
    feature('FISCAL_DOCUMENTS_BASIC', 'Documentos fiscales basicos'), feature('OPERATIONAL_PIN', 'PIN operacional'),
  ]),
  moduleOption('ACCOUNTING', 'Contabilidad', [
    feature('ACCOUNTING_ADVANCED', 'Contabilidad avanzada'), feature('PURCHASES', 'Compras'),
    feature('EXPENSES', 'Gastos'), feature('RECEIVABLES', 'Deudores'),
    feature('FISCAL_RULES_ADVANCED', 'Reglas fiscales avanzadas'),
  ]),
  moduleOption('PAYROLL', 'Nomina', [feature('PAYROLL', 'Gestion de nomina')]),
  moduleOption('REPORTS', 'Reportes', [
    feature('REPORTS_BASIC', 'Reportes basicos'), feature('REPORTS_ASYNC', 'Reportes avanzados asincronos'),
  ]),
  moduleOption('CATALOGS', 'Catalogos', [feature('CATALOGS_OPERATING', 'Catalogos operativos')]),
  moduleOption('AUDIT', 'Logs y auditoria', [
    feature('AUDIT_BASIC', 'Logs basicos'), feature('AUDIT_ADVANCED', 'Auditoria avanzada'),
  ]),
  moduleOption('USERS', 'Usuarios, roles y permisos', [
    feature('USERS_BASIC', 'Usuarios y permisos basicos'), feature('USERS_ADVANCED', 'Administracion avanzada de roles'),
  ]),
];

export const internalLicenseFeatures = ['ACCOUNTING_CORE'];
export const commercialLicenseFeatureOptions = [
  feature('ACCOUNTANT_PORTAL', 'Modulo contador'), feature('CUSTOM_RULES', 'Reglas especificas por empresa'),
  feature('CUSTOM_REPORTS', 'Reportes personalizados'), feature('CUSTOM_WORKFLOWS', 'Flujos personalizados'),
  feature('CUSTOM_INTEGRATIONS', 'Integraciones bajo cotizacion'), feature('PRIORITY_SUPPORT', 'Acompanamiento prioritario'),
];

export const posAndBillingLicenseModules = [
  'COMPANY', 'THIRDPARTY', 'INVENTORY', 'BILLING', 'REPORTS', 'CATALOGS', 'AUDIT', 'USERS',
];

export const posAndBillingLicenseFeatures = [
  'COMPANY_BASIC', 'BRANDING_BASIC', 'CUSTOMERS', 'PRODUCTS_SERVICES', 'INVENTORY_BASIC',
  'POS_SALES', 'ELECTRONIC_BILLING', 'SALES_REGISTRY', 'FISCAL_SETTINGS_BASIC',
  'FISCAL_DOCUMENTS_BASIC', 'REPORTS_BASIC', 'CATALOGS_OPERATING', 'AUDIT_BASIC',
  'USERS_BASIC', 'OPERATIONAL_PIN', 'ACCOUNTING_CORE',
];

export const fullLicenseFeatures = [
  ...licenseModuleOptions.flatMap((module) => module.features.map((item) => item.value)), ...internalLicenseFeatures,
];

export const licensePlanOptions = [
  { value: 'POS', label: 'POS y facturacion' },
  { value: 'FULL', label: 'Completa' },
  { value: 'CUSTOM', label: 'Personalizable' },
];

export function normalizeLicensePlanCode(planCode) {
  return licensePlanOptions.some((option) => option.value === planCode) ? planCode : 'CUSTOM';
}

export function modulesForLicensePlan(planCode, currentModules = []) {
  const normalizedPlanCode = normalizeLicensePlanCode(planCode);
  if (normalizedPlanCode === 'FULL') return licenseModuleOptions.map((option) => option.value);
  if (normalizedPlanCode === 'POS') return posAndBillingLicenseModules;
  return Array.isArray(currentModules) ? currentModules : [];
}

export function featuresForLicensePlan(planCode, currentFeatures = [], currentModules = []) {
  const normalizedPlanCode = normalizeLicensePlanCode(planCode);
  if (normalizedPlanCode === 'FULL') return fullLicenseFeatures;
  if (normalizedPlanCode === 'POS') return posAndBillingLicenseFeatures;
  if (Array.isArray(currentFeatures) && currentFeatures.length > 0) return currentFeatures;
  return featuresForModules(currentModules);
}

export function featuresForModules(modules = []) {
  const selected = new Set(modules);
  const features = licenseModuleOptions.filter((module) => selected.has(module.value))
    .flatMap((module) => module.features.map((item) => item.value));
  if (selected.has('BILLING') || selected.has('ACCOUNTING')) features.push('ACCOUNTING_CORE');
  return [...new Set(features)];
}

export function canEditLicenseModules(planCode) {
  return normalizeLicensePlanCode(planCode) === 'CUSTOM';
}

export const stepLicenseModules = {
  Empresa: 'COMPANY', 'Puesta en marcha': 'COMPANY', DIAN: 'BILLING', Terceros: 'THIRDPARTY',
  Finanzas: 'ACCOUNTING', Inventario: 'INVENTORY', Compras: 'ACCOUNTING', Gastos: 'ACCOUNTING',
  Deudores: 'ACCOUNTING', Fiscal: 'BILLING', 'Reglas fiscales': 'ACCOUNTING',
  'Documentos fiscales': 'BILLING', 'Configuracion contable': 'ACCOUNTING', Ventas: 'BILLING',
  'Registro de Ventas': 'BILLING', Nomina: 'PAYROLL', Reportes: 'REPORTS', Catalogos: 'CATALOGS',
  Logs: 'AUDIT', Usuarios: 'USERS', Roles: 'USERS', 'PIN operacional': 'BILLING',
};

export const stepLicenseFeatures = {
  Empresa: 'COMPANY_BASIC', 'Puesta en marcha': 'COMPANY_BASIC', DIAN: 'FISCAL_SETTINGS_BASIC',
  Terceros: 'CUSTOMERS', Finanzas: 'ACCOUNTING_ADVANCED', Inventario: 'INVENTORY_BASIC',
  Compras: 'PURCHASES', Gastos: 'EXPENSES', Deudores: 'RECEIVABLES', Fiscal: 'FISCAL_SETTINGS_BASIC',
  'Reglas fiscales': 'FISCAL_RULES_ADVANCED', 'Documentos fiscales': 'FISCAL_DOCUMENTS_BASIC',
  'Configuracion contable': 'ACCOUNTING_ADVANCED', Ventas: 'POS_SALES',
  'Registro de Ventas': 'SALES_REGISTRY', Nomina: 'PAYROLL', Reportes: 'REPORTS_BASIC',
  Catalogos: 'CATALOGS_OPERATING', Logs: 'AUDIT_BASIC', Usuarios: 'USERS_BASIC',
  Roles: 'USERS_BASIC', 'PIN operacional': 'OPERATIONAL_PIN',
};

export function licenseModuleLabel(moduleCode) {
  return licenseModuleOptions.find((option) => option.value === moduleCode)?.label || moduleCode;
}

function moduleOption(value, label, features) {
  return { value, label, features };
}

function feature(value, label) {
  return { value, label };
}
