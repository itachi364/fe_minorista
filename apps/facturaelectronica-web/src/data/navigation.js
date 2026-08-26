export const navigationGroups = [
  { label: 'Ventas', items: ['Ventas', 'Registro de Ventas'] },
  { label: 'Reportes', items: ['Reportes'] },
  { label: 'Contabilidad', items: ['Terceros', 'Inventario', 'Fiscal', 'Nomina'] },
  { label: 'Configuracion', items: ['Empresa', 'DIAN', 'Licencias', 'Catalogos', 'Logs', 'Usuarios', 'Roles'] },
];

export const steps = navigationGroups.flatMap((group) => group.items);
