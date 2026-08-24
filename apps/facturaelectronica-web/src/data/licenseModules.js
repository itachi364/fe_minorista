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

export const stepLicenseModules = {
  Empresa: 'COMPANY',
  DIAN: 'BILLING',
  Terceros: 'THIRDPARTY',
  Inventario: 'INVENTORY',
  Fiscal: 'BILLING',
  Ventas: 'BILLING',
  Nomina: 'PAYROLL',
  Reportes: 'REPORTS',
  Catalogos: 'CATALOGS',
  Logs: 'AUDIT',
  Usuarios: 'USERS',
  Roles: 'USERS',
};

export function licenseModuleLabel(moduleCode) {
  return licenseModuleOptions.find((option) => option.value === moduleCode)?.label || moduleCode;
}
