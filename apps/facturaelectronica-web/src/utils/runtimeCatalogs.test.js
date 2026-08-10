import { afterEach, expect, test, vi } from 'vitest';
import { loadRuntimeCatalogs } from './runtimeCatalogs.js';

afterEach(() => {
  vi.restoreAllMocks();
});

test('loads catalog options and relational divipola locations from backend', async () => {
  const fetchMock = vi.fn((path) => {
    if (String(path).includes('DIAN_DOCUMENT_TYPE')) {
      return jsonResponse([{ catalogCode: 'DIAN_DOCUMENT_TYPE', code: '13', label: 'Cedula de ciudadania', active: true, enabledForCompany: true }]);
    }
    if (String(path).includes('TAX_RESPONSIBILITY')) {
      return jsonResponse([{ catalogCode: 'TAX_RESPONSIBILITY', code: 'R-99-PN', label: 'R-99-PN - No responsable / No aplica', active: true, enabledForCompany: true }]);
    }
    if (String(path).includes('TAX_REGIME')) {
      return jsonResponse([{ catalogCode: 'TAX_REGIME', code: 'NO_RESPONSABLE_IVA', label: 'No responsable de IVA', active: true, enabledForCompany: true }]);
    }
    if (String(path).includes('PAYMENT_METHOD')) {
      return jsonResponse([{ catalogCode: 'PAYMENT_METHOD', code: 'CASH', label: 'Efectivo', active: true, enabledForCompany: true }]);
    }
    if (String(path).includes('VIRTUAL_WALLET')) {
      return jsonResponse([{ catalogCode: 'VIRTUAL_WALLET', code: 'NEQUI', label: 'Nequi', active: true, enabledForCompany: true }]);
    }
    if (String(path) === '/api/v1/catalogs/departments') {
      return jsonResponse([{ code: '11', name: 'Bogota, D.C.', active: true }]);
    }
    if (String(path) === '/api/v1/catalogs/departments/11/municipalities') {
      return jsonResponse([{ code: '11001', departmentCode: '11', name: 'Bogota, D.C.', active: true }]);
    }
    return jsonResponse([]);
  });
  vi.stubGlobal('crypto', { randomUUID: () => '00000000-0000-4000-8000-000000000000' });
  vi.stubGlobal('fetch', fetchMock);

  const catalogs = await loadRuntimeCatalogs({ token: 'token', companyId: 'company-id' });

  expect(catalogs.dianDocumentTypes).toEqual([
    { value: 13, label: '13 - Cedula de ciudadania', description: '' },
  ]);
  expect(catalogs.paymentMethodOptions).toEqual([
    { value: 'CASH', label: 'Efectivo', description: '' },
  ]);
  expect(catalogs.locations).toEqual([{
    departmentCode: '11',
    departmentName: 'Bogota, D.C.',
    municipalities: [{ code: '11001', name: 'Bogota, D.C.' }],
  }]);
});

function jsonResponse(payload) {
  return Promise.resolve({
    ok: true,
    text: () => Promise.resolve(JSON.stringify(payload)),
  });
}
