export const navigationGroups = [
  { label: 'Ventas', items: ['Ventas', 'Registro de Ventas'] },
  { label: 'Reportes', items: ['Reportes'] },
  { label: 'Contabilidad', items: ['Finanzas', 'Terceros', 'Inventario', 'Compras', 'Gastos', 'Deudores', 'Fiscal', 'Documentos fiscales', 'Nomina'] },
  { label: 'Configuracion', items: ['Puesta en marcha', 'Empresa', 'DIAN', 'Licencias', 'Catalogos', 'Reglas fiscales', 'Configuracion contable', 'Logs', 'Usuarios', 'Roles', 'PIN operacional'] },
];

export const steps = navigationGroups.flatMap((group) => group.items);
