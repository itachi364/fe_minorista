export const stepPermissionRules = {
  Empresa: ['COMPANY_SETTINGS_MANAGE', 'GLOBAL_COMPANIES_MANAGE'],
  Terceros: ['COMPANY_SETTINGS_MANAGE'],
  Inventario: ['INVENTORY_VIEW', 'INVENTORY_MANAGE'],
  Fiscal: ['FISCAL_DOCUMENTS_ISSUE', 'COMPANY_SETTINGS_MANAGE'],
  'Venta POS': ['SALES_CREATE', 'FISCAL_DOCUMENTS_ISSUE'],
  Reportes: ['REPORTS_VIEW', 'ACCOUNTING_VIEW'],
  Catalogos: ['COMPANY_CATALOGS_MANAGE', 'COMPANY_SETTINGS_MANAGE'],
  Logs: ['AUDIT_VIEW', 'GLOBAL_AUDIT_VIEW'],
  'Usuarios y roles': ['COMPANY_USERS_MANAGE', 'COMPANY_ROLES_MANAGE', 'USERS_MANAGE', 'ROLES_MANAGE'],
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
