export const licenseModuleOptions = [
  { value: 'COMPANY', label: 'Empresa y configuracion' },
  { value: 'THIRDPARTY', label: 'Clientes y proveedores' },
  { value: 'INVENTORY', label: 'Inventario' },
  { value: 'BILLING', label: 'Ventas y facturacion electronica' },
  { value: 'ACCOUNTING', label: 'Contabilidad' },
  { value: 'PAYROLL', label: 'Nomina' },
  { value: 'REPORTS', label: 'Reportes' },
  { value: 'CATALOGS', label: 'Catalogos' },
  { value: 'AUDIT', label: 'Logs y auditoria' },
  { value: 'USERS', label: 'Usuarios, roles y permisos' },
];

export const posAndBillingLicenseModules = [
  'COMPANY',
  'INVENTORY',
  'BILLING',
  'REPORTS',
  'THIRDPARTY',
  'ACCOUNTING',
  'USERS',
];

export const licensePlanOptions = [
  { value: 'POS', label: 'POS y facturacion' },
  { value: 'FULL', label: 'Completo' },
  { value: 'CUSTOM', label: 'Personalizado' },
];

export function normalizeLicensePlanCode(planCode) {
  return licensePlanOptions.some((option) => option.value === planCode) ? planCode : 'CUSTOM';
}

export function modulesForLicensePlan(planCode, currentModules = []) {
  const normalizedPlanCode = normalizeLicensePlanCode(planCode);
  if (normalizedPlanCode === 'FULL') {
    return licenseModuleOptions.map((option) => option.value);
  }
  if (normalizedPlanCode === 'POS') {
    return posAndBillingLicenseModules;
  }
  return Array.isArray(currentModules) ? currentModules : [];
}

export function canEditLicenseModules(planCode) {
  return normalizeLicensePlanCode(planCode) === 'CUSTOM';
}

export const stepLicenseModules = {
  Empresa: 'COMPANY',
  'Puesta en marcha': 'COMPANY',
  DIAN: 'BILLING',
  Terceros: 'THIRDPARTY',
  Finanzas: 'ACCOUNTING',
  Inventario: 'INVENTORY',
  Compras: 'ACCOUNTING',
  Gastos: 'ACCOUNTING',
  Deudores: 'ACCOUNTING',
  Fiscal: 'BILLING',
  'Documentos fiscales': 'BILLING',
  'Configuracion contable': 'ACCOUNTING',
  Ventas: 'BILLING',
  'Registro de Ventas': 'BILLING',
  Nomina: 'PAYROLL',
  Reportes: 'REPORTS',
  Catalogos: 'CATALOGS',
  Logs: 'AUDIT',
  Usuarios: 'USERS',
  Roles: 'USERS',
  'PIN operacional': 'BILLING',
};

export function licenseModuleLabel(moduleCode) {
  return licenseModuleOptions.find((option) => option.value === moduleCode)?.label || moduleCode;
}
