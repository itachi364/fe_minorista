export const stepPermissionRules = {
  Empresa: ['COMPANY_SETTINGS_MANAGE', 'GLOBAL_COMPANIES_MANAGE'],
  DIAN: ['COMPANY_SETTINGS_MANAGE'],
  Licencias: ['GLOBAL_COMPANIES_MANAGE'],
  Terceros: ['COMPANY_SETTINGS_MANAGE'],
  Inventario: ['INVENTORY_VIEW', 'INVENTORY_MANAGE'],
  Compras: ['PURCHASES_MANAGE', 'ACCOUNTING_MANAGE'],
  Gastos: ['ACCOUNTING_MANAGE'],
  Deudores: ['ACCOUNTING_MANAGE'],
  Fiscal: ['FISCAL_DOCUMENTS_ISSUE', 'COMPANY_SETTINGS_MANAGE'],
  'Documentos fiscales': ['FISCAL_DOCUMENTS_ISSUE'],
  'Configuracion contable': ['ACCOUNTING_VIEW', 'ACCOUNTING_MANAGE'],
  Ventas: ['SALES_CREATE'],
  'Registro de Ventas': ['SALES_CREATE'],
  Nomina: ['PAYROLL_VIEW', 'PAYROLL_MANAGE'],
  Reportes: ['REPORTS_VIEW', 'ACCOUNTING_VIEW'],
  Catalogos: ['COMPANY_CATALOGS_MANAGE', 'COMPANY_SETTINGS_MANAGE'],
  Logs: ['AUDIT_VIEW', 'GLOBAL_AUDIT_VIEW'],
  Usuarios: ['COMPANY_USERS_MANAGE', 'USERS_MANAGE'],
  Roles: ['COMPANY_ROLES_MANAGE', 'ROLES_MANAGE'],
  'PIN operacional': ['OPERATIONAL_PIN_MANAGE'],
};

export function hasAnyPermission(access, permissions) {
  return permissions.some((permission) => access?.permissions?.includes(permission));
}

export function companyScopedPermissions(permissions) {
  return permissions.filter((permission) => permission.active !== false && permission.scope === 'COMPANY' && !String(permission.code).startsWith('GLOBAL_'));
}

export function hasAnyRole(access, roles) {
  return roles.some((role) => access?.roles?.includes(role));
}
