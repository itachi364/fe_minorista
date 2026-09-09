import { expect, test } from 'vitest';
import { navigationGroups, steps } from './navigation.js';
import { stepPermissionRules } from '../utils/authorization.js';

test('places fiscal and accounting settings under configuration', () => {
  const configuration = navigationGroups.find((group) => group.label === 'Configuracion');
  const accounting = navigationGroups.find((group) => group.label === 'Contabilidad');

  expect(configuration.items).toEqual(expect.arrayContaining(['Reglas fiscales', 'Configuracion contable']));
  expect(accounting.items).not.toContain('Configuracion contable');
  expect(steps).not.toContain('Catalogo fiscal');
});

test('protects fiscal configuration with its dedicated permission', () => {
  expect(stepPermissionRules['Reglas fiscales']).toEqual(['FISCAL_SETTINGS_MANAGE']);
  expect(stepPermissionRules.DIAN).toEqual(['FISCAL_SETTINGS_MANAGE']);
  expect(stepPermissionRules['Documentos fiscales']).toEqual(['FISCAL_DOCUMENTS_ISSUE']);
});
