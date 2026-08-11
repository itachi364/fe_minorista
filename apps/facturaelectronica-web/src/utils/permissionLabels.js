const MODULE_LABELS = {
  accounting: 'Contabilidad',
  audit: 'Auditoria',
  billing: 'Facturacion',
  catalogs: 'Catalogos',
  company: 'Empresa',
  fiscal: 'Fiscal',
  inventory: 'Inventario',
  payroll: 'Nomina',
  platform: 'Plataforma',
  reports: 'Reportes',
  sales: 'Ventas',
  users: 'Usuarios y roles',
  general: 'General',
};

const PERMISSION_LABELS = {
  ACCOUNTING_MANAGE: 'Gestionar contabilidad',
  ACCOUNTING_VIEW: 'Ver contabilidad',
  AUDIT_VIEW: 'Ver auditoria de la empresa',
  CATALOGS_MANAGE: 'Gestionar catalogos',
  COMPANY_CATALOGS_MANAGE: 'Gestionar catalogos de la empresa',
  COMPANY_ROLES_MANAGE: 'Gestionar roles de la empresa',
  COMPANY_SETTINGS_MANAGE: 'Gestionar configuracion de la empresa',
  COMPANY_USERS_MANAGE: 'Gestionar usuarios de la empresa',
  FISCAL_DOCUMENTS_ISSUE: 'Emitir documentos fiscales',
  GLOBAL_AUDIT_VIEW: 'Ver auditoria global',
  GLOBAL_COMPANIES_MANAGE: 'Gestionar empresas de la plataforma',
  INVENTORY_MANAGE: 'Gestionar inventario',
  INVENTORY_VIEW: 'Ver inventario',
  PAYROLL_MANAGE: 'Gestionar nomina',
  PAYROLL_VIEW: 'Ver nomina',
  REPORTS_VIEW: 'Ver reportes',
  ROLES_MANAGE: 'Gestionar roles',
  SALES_CREATE: 'Registrar ventas POS',
  USERS_MANAGE: 'Gestionar usuarios',
};

const PERMISSION_DESCRIPTIONS = {
  ACCOUNTING_MANAGE: 'Permite configurar y registrar informacion contable.',
  ACCOUNTING_VIEW: 'Permite consultar reportes e informacion contable.',
  AUDIT_VIEW: 'Permite consultar logs y auditoria de la empresa.',
  CATALOGS_MANAGE: 'Permite administrar catalogos operativos autorizados.',
  COMPANY_CATALOGS_MANAGE: 'Permite activar o inactivar catalogos disponibles para la empresa.',
  COMPANY_ROLES_MANAGE: 'Permite crear roles empresariales y asignar permisos delegables.',
  COMPANY_SETTINGS_MANAGE: 'Permite actualizar datos y configuraciones de la empresa.',
  COMPANY_USERS_MANAGE: 'Permite crear usuarios y administrar accesos de la empresa.',
  FISCAL_DOCUMENTS_ISSUE: 'Permite emitir facturas, POS electronico y documentos fiscales.',
  GLOBAL_AUDIT_VIEW: 'Permite consultar auditoria global de plataforma.',
  GLOBAL_COMPANIES_MANAGE: 'Permite crear, actualizar, activar e inactivar empresas.',
  INVENTORY_MANAGE: 'Permite crear productos, registrar movimientos y ajustar inventario.',
  INVENTORY_VIEW: 'Permite consultar productos, existencias y movimientos.',
  PAYROLL_MANAGE: 'Permite registrar trabajadores, pagos y documentos de nomina.',
  PAYROLL_VIEW: 'Permite consultar informacion de nomina.',
  REPORTS_VIEW: 'Permite consultar reportes operativos y financieros.',
  ROLES_MANAGE: 'Permite administrar roles heredados.',
  SALES_CREATE: 'Permite registrar ventas POS y confirmar facturacion.',
  USERS_MANAGE: 'Permite administrar usuarios heredados.',
};

export function moduleLabel(module) {
  const key = String(module || 'general').toLowerCase();
  return MODULE_LABELS[key] || titleFromCode(module || 'general');
}

export function permissionLabel(code) {
  return PERMISSION_LABELS[code] || titleFromCode(code);
}

export function permissionDescription(permission) {
  return PERMISSION_DESCRIPTIONS[permission.code] || permission.description || 'Permiso empresarial configurable.';
}

function titleFromCode(value) {
  return String(value || '')
    .toLowerCase()
    .split(/[_\s-]+/)
    .filter(Boolean)
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(' ');
}
