import { afterEach, beforeEach, expect, test, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import App from './App.jsx';
import { AccountingConfigurationPanel } from './features/accounting/AccountingConfigurationPanel.jsx';
import { loadStoredSession, saveStoredSession, SESSION_TIMEOUT_MS } from './utils/sessionStorage.js';

const COMPANY_ID = '11111111-1111-1111-1111-111111111111';
const LOGIN_RESPONSE = {
  userId: '22222222-2222-2222-2222-222222222222',
  email: 'owner@example.com',
  fullName: 'Owner User',
  tokenType: 'Bearer',
  accessToken: 'token-1',
  expiresAt: '2026-07-30T22:00:00Z',
  globalRoles: [],
};
const COMPANY_ACCESS = [{
  companyId: COMPANY_ID,
  roles: ['OWNER'],
  permissions: [
    'COMPANY_SETTINGS_MANAGE',
    'COMPANY_USERS_MANAGE',
    'COMPANY_ROLES_MANAGE',
    'SALES_CREATE',
    'FISCAL_DOCUMENTS_ISSUE',
    'INVENTORY_VIEW',
    'INVENTORY_MANAGE',
    'REPORTS_VIEW',
    'ACCOUNTING_VIEW',
  ],
}];
const REPORT_ONLY_ACCESS = [{ companyId: COMPANY_ID, roles: ['REPORT_VIEWER'], permissions: ['REPORTS_VIEW'] }];
const SALES_ONLY_ACCESS = [{ companyId: COMPANY_ID, roles: ['VENDEDOR'], permissions: ['SALES_CREATE'] }];
const ACTIVE_LICENSE = {
  id: 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  companyId: COMPANY_ID,
  action: 'CREATE_TRANSACTION',
  module: 'COMPANY',
  allowed: true,
  status: 'ACTIVE',
  reasonCode: null,
  message: 'Licencia activa.',
  planCode: 'CUSTOM',
  validFrom: '2026-01-01',
  validTo: '2027-01-01',
  maxUsers: 10,
  maxMonthlyDocuments: 1000,
  enabledModules: ['COMPANY', 'THIRDPARTY', 'INVENTORY', 'BILLING', 'ACCOUNTING', 'REPORTS', 'USERS'],
};
const ACTIVE_COMPANY = {
  id: COMPANY_ID,
  legalName: 'Empresa Demo SAS',
  tradeName: 'Tienda Demo',
  identificationTypeCode: 31,
  identificationNumber: '900123456',
  verificationDigit: '7',
  email: 'admin@example.com',
  status: 'ACTIVE',
};
const TEST_RUNTIME_CATALOGS = {
  thirdPartyRoleCatalog: [
    { value: 'CUSTOMER', label: 'Cliente' },
    { value: 'SUPPLIER', label: 'Proveedor' },
    { value: 'BOTH', label: 'Cliente y proveedor' },
  ],
  personTypeCatalog: [
    { value: 'NATURAL', label: 'Natural' },
    { value: 'JURIDICA', label: 'Juridica' },
  ],
  itemTypeCatalog: [
    { value: 'PHYSICAL_GOOD', label: 'Bien fisico' },
    { value: 'SERVICE', label: 'Servicio / intangible' },
    { value: 'SUPPLY', label: 'Insumo' },
  ],
  dianDocumentTypes: [
    { value: 13, label: '13 - Cedula de ciudadania' },
    { value: 22, label: '22 - Cedula de extranjeria' },
    { value: 31, label: '31 - NIT' },
  ],
  taxResponsibilityOptions: [
    { value: 'O-13', label: 'O-13 - Gran contribuyente' },
    { value: 'R-99-PN', label: 'R-99-PN - No responsable / No aplica' },
  ],
  taxRegimeOptions: [
    { value: 'RESPONSABLE_IVA', label: 'Responsable de IVA' },
    { value: 'NO_RESPONSABLE_IVA', label: 'No responsable de IVA' },
  ],
  paymentMethodOptions: [
    { value: 'CASH', label: 'Efectivo' },
    { value: 'VIRTUAL_WALLET', label: 'Billetera virtual' },
  ],
  virtualWalletOptions: [
    { value: 'NEQUI', label: 'Nequi' },
    { value: 'DAVIPLATA', label: 'Daviplata' },
  ],
  fiscalDocumentTypeOptions: [
    { value: 'ELECTRONIC_INVOICE', label: 'Factura electronica de venta' },
    { value: 'ELECTRONIC_POS', label: 'POS electronico' },
  ],
  fiscalEnvironmentOptions: [
    { value: 'TEST', label: 'Pruebas' },
  ],
  salesTaxOptions: [
    { value: 'IVA_19', label: 'IVA 19%', taxCategoryCode: 'IVA', taxCode: 'IVA_19', taxLabel: 'IVA 19%', taxRate: 19 },
  ],
  locations: [
    {
      departmentCode: '11',
      departmentName: 'Bogota, D.C.',
      municipalities: [{ code: '11001', name: 'Bogota, D.C.' }],
    },
    {
      departmentCode: '91',
      departmentName: 'Amazonas',
      municipalities: [{ code: '91001', name: 'Leticia' }],
    },
  ],
};

beforeEach(() => {
  vi.stubGlobal('crypto', { randomUUID: () => '00000000-0000-4000-8000-000000000000' });
  vi.stubGlobal('__FACTURA_RUNTIME_CATALOGS__', TEST_RUNTIME_CATALOGS);
  if (!URL.createObjectURL) {
    Object.defineProperty(URL, 'createObjectURL', { value: vi.fn(), writable: true });
  }
  vi.spyOn(URL, 'createObjectURL').mockReturnValue('blob:nexofiscal-receipt');
  vi.spyOn(window, 'open').mockReturnValue(null);
  window.sessionStorage.clear();
});

afterEach(() => {
  cleanup();
  window.sessionStorage.clear();
  window.history.pushState({}, '', '/');
  vi.restoreAllMocks();
  vi.useRealTimers();
});

test('renders only login when there is no active session', () => {
  const { container } = render(<App />);

  expect(screen.getByRole('heading', { name: 'Iniciar sesion' })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Ingresar' })).toBeInTheDocument();
  expect(screen.queryByRole('navigation', { name: 'Flujo principal' })).not.toBeInTheDocument();
  expect(screen.queryByLabelText('Razon social')).not.toBeInTheDocument();
  expect(screen.queryByText('Respuesta')).not.toBeInTheDocument();
  expect(container.querySelector('textarea')).toBeNull();
});

test('accounting configuration template is previewed and submitted as batch payload', async () => {
  const onConfigure = vi.fn().mockResolvedValue({ accounts: [], rules: [] });

  render(<AccountingConfigurationPanel
    accounts={[]}
    rules={[]}
    onLoad={vi.fn()}
    onConfigure={onConfigure}
    busy={false}
  />);

  fireEvent.click(screen.getByRole('button', { name: 'Usar plantilla recomendada' }));
  expect(screen.getAllByDisplayValue('1105').length).toBeGreaterThan(0);
  expect(screen.getAllByText('Movimientos contables').length).toBeGreaterThan(0);

  fireEvent.click(screen.getByRole('button', { name: 'Guardar configuracion' }));

  await waitFor(() => expect(onConfigure).toHaveBeenCalledTimes(1));
  expect(onConfigure.mock.calls[0][0]).toMatchObject({
    accounts: expect.arrayContaining([expect.objectContaining({ code: '1105', name: 'Caja' })]),
    rules: expect.arrayContaining([
      expect.objectContaining({
        eventType: 'SALE_CONFIRMED',
        lines: expect.arrayContaining([expect.objectContaining({ accountCode: '1105', side: 'DEBIT' })]),
      }),
    ]),
  });
});

test('accounting configuration shows usage and protects used accounts and rules', () => {
  const onDeactivateAccount = vi.fn();
  const onDeactivateRule = vi.fn();

  render(<AccountingConfigurationPanel
    accounts={[
      { id: 'account-1', code: '1105', name: 'Caja', category: 'ASSET', nature: 'DEBIT', active: true, used: false, usageCount: 0 },
      { id: 'account-2', code: '4135', name: 'Ingresos', category: 'INCOME', nature: 'CREDIT', active: true, used: true, usageCount: 2 },
    ]}
    rules={[
      { id: 'rule-1', eventType: 'SALE_CONFIRMED', sourceType: 'SALE', name: 'Venta', active: true, used: false, usageCount: 0, lines: [] },
      { id: 'rule-2', eventType: 'EXPENSE_CONFIRMED', sourceType: 'EXPENSE', name: 'Egreso', active: true, used: true, usageCount: 1, lines: [] },
    ]}
    onLoad={vi.fn()}
    onConfigure={vi.fn()}
    onDeactivateAccount={onDeactivateAccount}
    onDeactivateRule={onDeactivateRule}
    busy={false}
  />);

  expect(screen.getAllByText('Uso').length).toBeGreaterThan(0);
  expect(screen.getAllByText('Sin uso').length).toBeGreaterThan(0);
  expect(screen.getByText('Usada (2)')).toBeInTheDocument();
  expect(screen.getByText('Usada (1)')).toBeInTheDocument();
  expect(screen.getAllByText('Solo lectura')).toHaveLength(2);

  fireEvent.click(screen.getAllByRole('button', { name: 'Inactivar' })[0]);
  expect(onDeactivateRule).toHaveBeenCalledWith('rule-1');
  fireEvent.click(screen.getAllByRole('button', { name: 'Inactivar' })[1]);
  expect(onDeactivateAccount).toHaveBeenCalledWith('account-1');
});

test('login with active license hides login and shows operational shell', async () => {
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE);

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));

  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());
  expect(screen.queryByRole('heading', { name: 'Iniciar sesion' })).not.toBeInTheDocument();
  expect(screen.getByText('Owner User - owner@example.com')).toBeInTheDocument();
  expect(screen.getByDisplayValue('Empresa Demo SAS (900123456)')).toBeInTheDocument();
  expect(screen.queryByDisplayValue(COMPANY_ID)).not.toBeInTheDocument();
  expect(screen.getByText('ACTIVA')).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Ventas' })).toBeInTheDocument();
  fireEvent.click(screen.getByRole('button', { name: 'Empresa' }));
  expect(screen.getByLabelText('Razon social')).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Actualizar empresa' })).toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Crear empresa' })).not.toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Validar licencia' })).not.toBeInTheDocument();
  expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/v1/auth/login', expect.objectContaining({ method: 'POST' }));
  expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/v1/me/companies', expect.objectContaining({
    headers: expect.objectContaining({ Authorization: 'Bearer token-1' }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(3,
    `/api/v1/companies/${COMPANY_ID}/license/validation?action=CREATE_TRANSACTION&module=COMPANY`,
    expect.objectContaining({ headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }) }),
  );
  expect(fetchMock).toHaveBeenNthCalledWith(5, `/api/v1/companies/${COMPANY_ID}`, expect.objectContaining({
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
});

test('restores active session after page reload simulation', async () => {
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE);

  const firstRender = render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());
  firstRender.unmount();

  render(<App />);

  expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument();
  expect(screen.queryByRole('heading', { name: 'Iniciar sesion' })).not.toBeInTheDocument();
  expect(fetchMock).toHaveBeenCalledTimes(5);
});

test('stored session expires after five minutes of inactivity', () => {
  saveStoredSession({ session: LOGIN_RESPONSE, companyAccesses: COMPANY_ACCESS, activeCompanyId: COMPANY_ID, lastActivityAt: 1000 });

  expect(loadStoredSession(1000 + SESSION_TIMEOUT_MS - 1)).not.toBeNull();
  expect(loadStoredSession(1000 + SESSION_TIMEOUT_MS)).toBeNull();
});

test('cookie backed sessions do not persist browser-readable tokens', () => {
  saveStoredSession({
    session: {
      ...LOGIN_RESPONSE,
      authMode: 'cognito',
      cookieSession: true,
      accessToken: 'prod-access-token',
      refreshToken: 'prod-refresh-token',
      idToken: 'prod-id-token',
    },
    companyAccesses: COMPANY_ACCESS,
    activeCompanyId: COMPANY_ID,
    lastActivityAt: 1000,
  });

  const storageKey = window.sessionStorage.key(0);
  const rawSnapshot = window.sessionStorage.getItem(storageKey);
  const restored = loadStoredSession(1001);

  expect(rawSnapshot).not.toContain('prod-access-token');
  expect(rawSnapshot).not.toContain('prod-refresh-token');
  expect(rawSnapshot).not.toContain('prod-id-token');
  expect(restored.session.accessToken).toBeUndefined();
  expect(restored.session.refreshToken).toBeUndefined();
  expect(restored.session.idToken).toBeUndefined();
});

test('company user sees only modules allowed by effective permissions', async () => {
  mockLoginFlow(ACTIVE_LICENSE, REPORT_ONLY_ACCESS);

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));

  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());
  expect(screen.getByRole('button', { name: 'Reportes' })).toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Empresa' })).not.toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Usuarios' })).not.toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Generar reporte' })).toBeInTheDocument();
});

test('sales user can access POS without fiscal advanced permission', async () => {
  mockLoginFlow(ACTIVE_LICENSE, SALES_ONLY_ACCESS);

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));

  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());
  expect(screen.getByRole('button', { name: 'Ventas' })).toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Fiscal' })).not.toBeInTheDocument();

  fireEvent.click(screen.getByRole('button', { name: 'Ventas' }));
  expect(screen.getAllByRole('button', { name: 'Cerrar venta' })).toHaveLength(1);
});

test('company owner updates active company without create company action', async () => {
  const updatedCompany = { ...ACTIVE_COMPANY, legalName: 'Empresa Actualizada SAS' };
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE)
    .mockResolvedValueOnce(jsonResponse(updatedCompany));

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());
  fireEvent.click(screen.getByRole('button', { name: 'Empresa' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Actualizar empresa' })).toBeInTheDocument());

  expect(screen.queryByRole('button', { name: 'Crear empresa' })).not.toBeInTheDocument();
  fireEvent.change(screen.getByLabelText('Razon social'), { target: { value: 'Empresa Actualizada SAS' } });
  fireEvent.click(screen.getByRole('button', { name: 'Actualizar empresa' }));

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(6));
  expect(fetchMock).toHaveBeenNthCalledWith(6, `/api/v1/companies/${COMPANY_ID}`, expect.objectContaining({
    method: 'PUT',
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
  expect(JSON.parse(fetchMock.mock.calls[5][1].body)).toMatchObject({
    legalName: 'Empresa Actualizada SAS',
  });
});

test('login with inactive license shows modal and keeps only login visible', async () => {
  mockLoginFlow({ ...ACTIVE_LICENSE, allowed: false, status: 'SUSPENDED', message: 'La licencia esta suspendida.' });

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));

  await waitFor(() => expect(screen.getByText('Licencia no activa')).toBeInTheDocument());
  expect(screen.getByText('La licencia esta suspendida.')).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Ingresar' })).toBeInTheDocument();
  expect(screen.queryByRole('navigation', { name: 'Flujo principal' })).not.toBeInTheDocument();
  expect(screen.queryByLabelText('Razon social')).not.toBeInTheDocument();
});

test('login with missing company license shows license configuration message', async () => {
  const fetchMock = vi.fn()
    .mockResolvedValueOnce(jsonResponse(LOGIN_RESPONSE))
    .mockResolvedValueOnce(jsonResponse(COMPANY_ACCESS))
    .mockResolvedValueOnce(errorResponse(404, {
      status: 404,
      code: 'RESOURCE_NOT_FOUND',
      message: `No existe licencia configurada para la empresa ${COMPANY_ID}.`,
      correlationId: 'corr-license',
    }));
  vi.stubGlobal('fetch', fetchMock);

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));

  await waitFor(() => expect(screen.getByText('Licencia no configurada')).toBeInTheDocument());
  expect(screen.getByText('La empresa no tiene una licencia configurada. Solicita a ROOT asignar una licencia antes de ingresar.')).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Ingresar' })).toBeInTheDocument();
  expect(screen.queryByRole('navigation', { name: 'Flujo principal' })).not.toBeInTheDocument();
});


test('root login shows global panel without company or license validation', async () => {
  const existingCompany = { id: COMPANY_ID, legalName: 'Empresa Demo SAS', tradeName: 'Tienda Demo', identificationTypeCode: 31, identificationNumber: '900123456', verificationDigit: '7', email: 'admin@example.com', status: 'ACTIVE' };
  const fetchMock = vi.fn()
    .mockResolvedValueOnce(jsonResponse({
      ...LOGIN_RESPONSE,
      email: 'root@example.com',
      fullName: 'Root Platform User',
      globalRoles: ['ROOT'],
    }))
    .mockResolvedValueOnce(jsonResponse([existingCompany]));
  vi.stubGlobal('fetch', fetchMock);

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));

  await waitFor(() => expect(screen.getByText('Panel global')).toBeInTheDocument());
  expect(screen.getByText('Root Platform User - root@example.com')).toBeInTheDocument();
  expect(screen.getByText('PLATAFORMA')).toBeInTheDocument();
  expect(screen.getByText('ROOT')).toBeInTheDocument();
  fireEvent.click(screen.getByRole('button', { name: 'Empresa' }));
  expect(screen.getByLabelText('Razon social')).toHaveValue('');
  expect(screen.getByRole('button', { name: 'Ventas' })).toBeInTheDocument();
  expect(screen.getByText('Empresas registradas')).toBeInTheDocument();
  expect(screen.getAllByText('Empresa Demo SAS (900123456)').length).toBeGreaterThan(0);
  expect(screen.getByRole('button', { name: 'Crear empresa' })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Actualizar' })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Crear administrador' })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Crear marca empresarial' })).toBeInTheDocument();
  fireEvent.click(screen.getByRole('button', { name: 'Actualizar' }));
  expect(screen.getByLabelText('Razon social')).toHaveValue('Empresa Demo SAS');
  expect(screen.getByRole('button', { name: 'Actualizar empresa' })).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Usuarios' })).toBeInTheDocument();
  expect(fetchMock).toHaveBeenCalledTimes(2);
  expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/v1/companies', expect.objectContaining({
    headers: expect.objectContaining({ Authorization: 'Bearer token-1' }),
  }));
});

test('root assigns configurable company license', async () => {
  const createdCompany = { id: COMPANY_ID, legalName: 'Empresa Demo SAS', tradeName: 'Tienda Demo', identificationTypeCode: 31, identificationNumber: '900123456', verificationDigit: '7', email: 'admin@example.com', status: 'ACTIVE' };
  const savedLicense = {
    ...ACTIVE_LICENSE,
    companyId: COMPANY_ID,
    planCode: 'CUSTOM',
    enabledModules: ['COMPANY', 'BILLING', 'USERS'],
  };
  const fetchMock = vi.fn()
    .mockResolvedValueOnce(jsonResponse({
      ...LOGIN_RESPONSE,
      email: 'root@example.com',
      fullName: 'Root Platform User',
      globalRoles: ['ROOT'],
    }))
    .mockResolvedValueOnce(jsonResponse([createdCompany]))
    .mockResolvedValueOnce(jsonResponse(savedLicense));
  vi.stubGlobal('fetch', fetchMock);

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByText('Panel global')).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Licencias' }));
  fireEvent.change(screen.getByLabelText('Empresa contratante'), { target: { value: COMPANY_ID } });
  fireEvent.click(screen.getByLabelText('Empresa y configuracion'));
  fireEvent.click(screen.getByLabelText('Ventas y facturacion electronica'));
  fireEvent.click(screen.getByLabelText('Usuarios, roles y permisos'));
  fireEvent.click(screen.getByRole('button', { name: 'Guardar licencia' }));

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(3));
  expect(fetchMock).toHaveBeenNthCalledWith(3, `/api/v1/companies/${COMPANY_ID}/license`, expect.objectContaining({
    method: 'POST',
    headers: expect.objectContaining({ Authorization: 'Bearer token-1', 'X-Company-Id': COMPANY_ID }),
  }));
  expect(JSON.parse(fetchMock.mock.calls[2][1].body)).toMatchObject({
    planCode: 'CUSTOM',
    enabledModules: ['COMPANY', 'BILLING', 'USERS'],
  });
});

test('root creates company and initial administrator', async () => {
  const createdCompany = { id: COMPANY_ID, legalName: 'Empresa Demo SAS', tradeName: 'Tienda Demo', identificationTypeCode: 31, identificationNumber: '900123456', verificationDigit: '7', email: 'admin@example.com', status: 'ACTIVE' };
  const createdUser = {
    id: '33333333-3333-3333-3333-333333333333',
    email: 'admin.empresa@example.com',
    fullName: 'Administrador Empresa',
    status: 'ACTIVE',
  };
  const createdMembership = {
    id: '44444444-4444-4444-4444-444444444444',
    companyId: COMPANY_ID,
    userId: createdUser.id,
    roles: ['OWNER'],
    active: true,
  };
  const fetchMock = vi.fn()
    .mockResolvedValueOnce(jsonResponse({
      ...LOGIN_RESPONSE,
      email: 'root@example.com',
      fullName: 'Root Platform User',
      globalRoles: ['ROOT'],
    }))
    .mockResolvedValueOnce(jsonResponse([]))
    .mockResolvedValueOnce(jsonResponse(createdCompany))
    .mockResolvedValueOnce(jsonResponse(createdUser))
    .mockResolvedValueOnce(jsonResponse(createdMembership));
  vi.stubGlobal('fetch', fetchMock);

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByText('Panel global')).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Empresa' }));
  fillCompanyForm();
  fireEvent.click(screen.getByRole('button', { name: 'Crear empresa' }));
  await waitFor(() => expect(screen.getAllByText('Empresa Demo SAS (900123456)').length).toBeGreaterThan(0));

  fireEvent.click(screen.getAllByRole('button', { name: 'Crear administrador' })[0]);
  await waitFor(() => expect(screen.getByRole('dialog')).toBeInTheDocument());
  fillInitialAdminForm();
  const adminButtons = screen.getAllByRole('button', { name: 'Crear administrador' });
  fireEvent.click(adminButtons[adminButtons.length - 1]);

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(5));
  expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/v1/companies', expect.objectContaining({
    method: 'POST',
    headers: expect.objectContaining({ Authorization: 'Bearer token-1' }),
    body: expect.stringContaining('"identificationTypeCode":31'),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/v1/users', expect.objectContaining({
    method: 'POST',
    body: JSON.stringify({
      email: 'admin.empresa@example.com',
      fullName: 'Administrador Empresa',
      password: 'AdminDemo#2026!',
    }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(5, `/api/v1/companies/${COMPANY_ID}/memberships`, expect.objectContaining({
    method: 'POST',
    body: JSON.stringify({ userId: createdUser.id, roles: ['OWNER'] }),
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
});

test('root manages company roles users and assignments', async () => {
  const createdCompany = { id: COMPANY_ID, legalName: 'Empresa Demo SAS', tradeName: 'Tienda Demo', identificationTypeCode: 31, identificationNumber: '900123456', verificationDigit: '7', email: 'admin@example.com', status: 'ACTIVE' };
  const permissionCatalog = [
    { code: 'GLOBAL_COMPANIES_MANAGE', scope: 'GLOBAL', module: 'platform', description: 'Manage companies', active: true },
    { code: 'SALES_CREATE', scope: 'COMPANY', module: 'sales', description: 'Create sales', active: true },
    { code: 'SALES_CANCEL', scope: 'COMPANY', module: 'sales', description: 'Cancel sales', active: true },
    { code: 'FISCAL_DOCUMENTS_ISSUE', scope: 'COMPANY', module: 'billing', description: 'Issue fiscal documents', active: true },
    { code: 'INVENTORY_VIEW', scope: 'COMPANY', module: 'inventory', description: 'View inventory', active: true },
  ];
  const createdRole = {
    id: '66666666-6666-6666-6666-666666666666',
    companyId: COMPANY_ID,
    name: 'VENDEDOR',
    description: 'Puede registrar ventas POS y consultar inventario.',
    permissionCodes: ['SALES_CREATE', 'INVENTORY_VIEW'],
    active: true,
  };
  const createdUser = {
    id: '77777777-7777-7777-7777-777777777777',
    email: 'vendedor@example.com',
    fullName: 'Usuario Vendedor',
    status: 'ACTIVE',
  };
  const assignedAccess = { companyId: COMPANY_ID, userId: createdUser.id, roles: ['VENDEDOR'], permissions: createdRole.permissionCodes };
  const fetchMock = vi.fn()
    .mockResolvedValueOnce(jsonResponse({
      ...LOGIN_RESPONSE,
      email: 'root@example.com',
      fullName: 'Root Platform User',
      globalRoles: ['ROOT'],
    }))
    .mockResolvedValueOnce(jsonResponse([]))
    .mockResolvedValueOnce(jsonResponse(createdCompany))
    .mockResolvedValueOnce(jsonResponse(permissionCatalog))
    .mockResolvedValueOnce(jsonResponse([]))
    .mockResolvedValueOnce(jsonResponse(createdRole))
    .mockResolvedValueOnce(jsonResponse([]))
    .mockResolvedValueOnce(jsonResponse(createdUser))
    .mockResolvedValueOnce(jsonResponse(assignedAccess))
    .mockResolvedValueOnce(jsonResponse([createdUser]))
    .mockResolvedValueOnce(jsonResponse(permissionCatalog))
    .mockResolvedValueOnce(jsonResponse([createdRole]))
    .mockResolvedValueOnce(jsonResponse([createdUser]));
  vi.stubGlobal('fetch', fetchMock);

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByText('Panel global')).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Empresa' }));
  fillCompanyForm();
  fireEvent.click(screen.getByRole('button', { name: 'Crear empresa' }));
  await waitFor(() => expect(screen.getAllByText('Empresa Demo SAS (900123456)').length).toBeGreaterThan(0));

  fireEvent.click(screen.getByRole('button', { name: 'Roles' }));
  await waitFor(() => expect(screen.getByText('Registrar ventas POS')).toBeInTheDocument());
  expect(screen.getByText('Anular ventas')).toBeInTheDocument();
  expect(screen.queryByText('Gestionar empresas de la plataforma')).not.toBeInTheDocument();

  fillCompanyRoleForm();
  fireEvent.click(screen.getByRole('button', { name: 'Crear rol' }));
  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(6));

  fireEvent.click(screen.getByRole('button', { name: 'Usuarios' }));
  await waitFor(() => expect(screen.getByText('Usuarios disponibles')).toBeInTheDocument());
  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(7));
  fillManagedUserForm();
  fireEvent.click(screen.getByRole('button', { name: 'Crear usuario' }));
  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(13));

  expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/v1/platform/permissions', expect.objectContaining({
    headers: expect.objectContaining({ Authorization: 'Bearer token-1', 'X-Company-Id': COMPANY_ID }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(5, `/api/v1/companies/${COMPANY_ID}/roles`, expect.objectContaining({
    headers: expect.objectContaining({ Authorization: 'Bearer token-1', 'X-Company-Id': COMPANY_ID }),
  }));
  expect(JSON.parse(fetchMock.mock.calls[5][1].body)).toEqual({
    name: 'VENDEDOR',
    description: 'Puede registrar ventas POS y consultar inventario.',
    permissionCodes: ['SALES_CREATE', 'INVENTORY_VIEW'],
  });
  expect(fetchMock).toHaveBeenNthCalledWith(7, `/api/v1/companies/${COMPANY_ID}/users`, expect.objectContaining({
    headers: expect.objectContaining({ Authorization: 'Bearer token-1', 'X-Company-Id': COMPANY_ID }),
  }));
  expect(JSON.parse(fetchMock.mock.calls[7][1].body)).toEqual({
    email: 'vendedor@example.com',
    fullName: 'Usuario Vendedor',
    password: 'VendedorDemo#2026!',
  });
  expect(fetchMock).toHaveBeenNthCalledWith(9, `/api/v1/companies/${COMPANY_ID}/users/${createdUser.id}/role-assignments`, expect.objectContaining({
    method: 'POST',
    body: JSON.stringify({ roleIds: [createdRole.id] }),
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(11, '/api/v1/platform/permissions', expect.objectContaining({
    headers: expect.objectContaining({ Authorization: 'Bearer token-1', 'X-Company-Id': COMPANY_ID }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(12, `/api/v1/companies/${COMPANY_ID}/roles`, expect.objectContaining({
    headers: expect.objectContaining({ Authorization: 'Bearer token-1', 'X-Company-Id': COMPANY_ID }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(13, `/api/v1/companies/${COMPANY_ID}/users`, expect.objectContaining({
    headers: expect.objectContaining({ Authorization: 'Bearer token-1', 'X-Company-Id': COMPANY_ID }),
  }));
});

test('creates simple natural customer with automatic fiscal profile', async () => {
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE)
    .mockResolvedValueOnce(jsonResponse({ id: '55555555-5555-5555-5555-555555555555', identificationTypeCode: 13 }));

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Terceros' }));
  fillSimpleNaturalCustomerForm();
  expect(screen.getByLabelText('Tipo de documento')).not.toHaveTextContent('31 - NIT');
  expect(screen.queryByLabelText('Razon social')).not.toBeInTheDocument();
  expect(screen.queryByLabelText('Nombre comercial')).not.toBeInTheDocument();
  expect(screen.getByLabelText('Responsabilidades fiscales')).toHaveValue('R-99-PN - No responsable / No aplica');
  expect(screen.getByLabelText('Regimen tributario')).toHaveValue('No responsable de IVA');
  fireEvent.click(screen.getByRole('button', { name: 'Guardar tercero' }));

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(6));
  const thirdPartyPayload = JSON.parse(fetchMock.mock.calls[5][1].body);
  expect(thirdPartyPayload.personType).toBe('NATURAL');
  expect(thirdPartyPayload.identificationTypeCode).toBe(13);
  expect(thirdPartyPayload.identificationNumber).toBe('1234567890');
  expect(thirdPartyPayload.verificationDigit).toBeNull();
  expect(thirdPartyPayload.businessName).toBeNull();
  expect(thirdPartyPayload.tradeName).toBeNull();
  expect(thirdPartyPayload.taxResponsibilities).toEqual(['R-99-PN']);
  expect(thirdPartyPayload.taxRegime).toBe('NO_RESPONSABLE_IVA');
  expect(thirdPartyPayload.roles).toEqual(['CUSTOMER']);
  expect(thirdPartyPayload.role).toBeUndefined();
  expect(fetchMock).toHaveBeenNthCalledWith(6, '/api/v1/third-parties', expect.objectContaining({
    method: 'POST',
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
});

test('creates third party with municipality loaded from backend catalogs', async () => {
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE)
    .mockResolvedValueOnce(jsonResponse({ id: '88888888-8888-8888-8888-888888888888', municipalityCode: '91001' }));

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Terceros' }));
  fillSimpleNaturalCustomerForm();
  fireEvent.change(screen.getByLabelText('Direccion'), { target: { value: 'Calle 10 # 1-2' } });
  fireEvent.change(screen.getByLabelText('Departamento'), { target: { value: '91' } });
  await waitFor(() => expect(screen.getByText('Leticia')).toBeInTheDocument());
  fireEvent.change(screen.getByLabelText('Municipio / ciudad'), { target: { value: '91001' } });
  fireEvent.click(screen.getByRole('button', { name: 'Guardar tercero' }));

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(6));
  const thirdPartyPayload = JSON.parse(fetchMock.mock.calls[5][1].body);
  expect(thirdPartyPayload.municipalityCode).toBe('91001');
});

test('creates POS sale with controlled virtual wallet payment method', async () => {
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE)
    .mockResolvedValueOnce(jsonResponse({ id: '99999999-9999-9999-9999-999999999999', paymentMethodCode: 'VIRTUAL_WALLET' }))
    .mockResolvedValueOnce(downloadResponse());

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Ventas' }));
  fireEvent.change(screen.getAllByLabelText('Metodo de pago')[0], { target: { value: 'VIRTUAL_WALLET' } });
  expect(screen.getByLabelText('Billetera virtual')).toBeInTheDocument();
  fireEvent.change(screen.getByLabelText('Billetera virtual'), { target: { value: 'NEQUI' } });
  fireEvent.click(screen.getAllByRole('button', { name: 'Cerrar venta' })[0]);

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(7));
  expect(screen.queryByLabelText('Venta creada')).not.toBeInTheDocument();
  expect(screen.getByText('Agrega productos y cierra la venta en un solo paso.')).toBeInTheDocument();
  expect(fetchMock).toHaveBeenNthCalledWith(6, '/api/v1/sales/close', expect.objectContaining({ method: 'POST' }));
  const salePayload = JSON.parse(fetchMock.mock.calls[5][1].body);
  expect(salePayload.paymentMethodCode).toBe('VIRTUAL_WALLET');
  expect(salePayload.virtualWalletCode).toBe('NEQUI');
  expect(salePayload.paymentMethodId).toBeUndefined();
});

test('adds scanned product without showing success modal or manual add line action', async () => {
  const scannedProduct = {
    id: '44444444-4444-4444-4444-444444444444',
    sku: 'SKU-SCAN',
    barcode: '123456789',
    name: 'Cafe escaneado',
    itemType: 'PHYSICAL_GOOD',
    salePrice: 5042.02,
    taxCode: 'IVA_19',
    taxRate: 19,
  };
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE)
    .mockResolvedValueOnce(jsonResponse(scannedProduct));

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Ventas' }));
  expect(screen.queryByRole('button', { name: 'Agregar linea' })).not.toBeInTheDocument();
  fireEvent.change(screen.getByLabelText('Scanner codigo de barras'), { target: { value: '123456789' } });

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(6));
  expect(fetchMock).toHaveBeenNthCalledWith(6, '/api/v1/products/by-barcode/123456789', expect.objectContaining({
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
  expect(screen.getByDisplayValue('Cafe escaneado')).toBeInTheDocument();
  expect(screen.getByLabelText('Scanner codigo de barras')).toHaveValue('');
  expect(screen.queryByText('La accion se realizo correctamente.')).not.toBeInTheDocument();
});

test('clears product form after successful item creation', async () => {
  const createdProduct = {
    id: '44444444-4444-4444-4444-444444444444',
    sku: '123456789',
    barcode: '123456789',
    name: 'Cafe r',
    itemType: 'PHYSICAL_GOOD',
    salePrice: 5042.02,
    taxCode: 'IVA_19',
    taxRate: 19,
    currentStock: 100,
    active: true,
  };
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE)
    .mockResolvedValueOnce(jsonResponse(createdProduct));

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Inventario' }));
  fillProductForm();
  fireEvent.click(screen.getByRole('button', { name: 'Crear item' }));

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(6));
  expect(screen.getByLabelText('SKU')).toHaveValue('');
  expect(screen.getByLabelText('Codigo de barras')).toHaveValue('');
  expect(screen.getByLabelText('Nombre')).toHaveValue('');
  expect(screen.getByLabelText('Precio final')).toHaveValue(null);
  expect(screen.getByText('Cafe r')).toBeInTheDocument();
});

test('clears issuer and resolution forms after successful fiscal save', async () => {
  const policy = {
    companyId: COMPANY_ID,
    defaultSaleDocumentType: 'ELECTRONIC_INVOICE',
    allowDocumentTypeOverride: true,
    requirePinForOverride: true,
  };
  const issuer = {
    id: '11111111-2222-3333-4444-555555555555',
    legalName: ACTIVE_COMPANY.legalName,
    nit: ACTIVE_COMPANY.identificationNumber,
    verificationDigit: ACTIVE_COMPANY.verificationDigit,
    municipalityCode: '11001',
    active: true,
  };
  const resolution = {
    id: '99999999-8888-7777-6666-555555555555',
    documentType: 'ELECTRONIC_INVOICE',
    resolutionNumber: '987654321',
    prefix: 'FE',
    fromNumber: 1,
    toNumber: 100,
    currentNumber: 0,
    validFrom: '2026-01-01',
    validTo: '2026-12-31',
    environment: 'TEST',
    active: true,
  };
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE)
    .mockResolvedValueOnce(jsonResponse([]))
    .mockResolvedValueOnce(jsonResponse([]))
    .mockResolvedValueOnce(jsonResponse(policy))
    .mockResolvedValueOnce(jsonResponse(issuer))
    .mockResolvedValueOnce(jsonResponse([issuer]))
    .mockResolvedValueOnce(jsonResponse([]))
    .mockResolvedValueOnce(jsonResponse(policy))
    .mockResolvedValueOnce(jsonResponse(resolution))
    .mockResolvedValueOnce(jsonResponse([issuer]))
    .mockResolvedValueOnce(jsonResponse([resolution]))
    .mockResolvedValueOnce(jsonResponse(policy));

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Fiscal' }));
  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(8));
  fireEvent.change(screen.getByLabelText('Direccion fiscal'), { target: { value: 'Calle 1 # 2-3' } });
  fireEvent.click(screen.getByRole('button', { name: 'Guardar emisor' }));
  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(12));
  expect(screen.getByLabelText('Direccion fiscal')).toHaveValue('');

  fireEvent.change(screen.getByLabelText('Numero resolucion'), { target: { value: '987654321' } });
  fireEvent.change(screen.getByLabelText('Prefijo'), { target: { value: 'FE' } });
  fireEvent.change(screen.getByLabelText('Desde'), { target: { value: '1' } });
  fireEvent.change(screen.getByLabelText('Hasta'), { target: { value: '100' } });
  fireEvent.change(screen.getByLabelText('Vigencia desde'), { target: { value: '2026-01-01' } });
  fireEvent.change(screen.getByLabelText('Vigencia hasta'), { target: { value: '2026-12-31' } });
  fireEvent.change(screen.getByLabelText('Ambiente'), { target: { value: 'TEST' } });
  fireEvent.click(screen.getByRole('button', { name: 'Crear resolucion' }));
  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(16));
  expect(screen.getByLabelText('Numero resolucion')).toHaveValue('');
  expect(screen.getByLabelText('Prefijo')).toHaveValue('');
  expect(screen.getByLabelText('Desde')).toHaveValue(null);
  expect(screen.getByLabelText('Hasta')).toHaveValue(null);
});

test('manages operational PIN from configuration menu', async () => {
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE)
    .mockResolvedValueOnce(jsonResponse({
      companyId: COMPANY_ID,
      configured: false,
      valid: false,
      locked: false,
      mustChange: false,
      remainingAttempts: 3,
      updatedAt: null,
    }))
    .mockResolvedValueOnce(jsonResponse({
      companyId: COMPANY_ID,
      configured: true,
      valid: true,
      locked: false,
      mustChange: false,
      remainingAttempts: 3,
      updatedAt: '2026-08-28T12:00:00Z',
    }));

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'PIN operacional' }));
  await waitFor(() => expect(screen.getByText('Sin configurar')).toBeInTheDocument());
  fireEvent.change(screen.getByLabelText('PIN de 6 digitos'), { target: { value: '123456' } });
  fireEvent.click(screen.getByRole('button', { name: 'Crear PIN' }));

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(7));
  expect(fetchMock).toHaveBeenNthCalledWith(6, `/api/v1/companies/${COMPANY_ID}/operational-pin`, expect.objectContaining({
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(7, `/api/v1/companies/${COMPANY_ID}/operational-pin`, expect.objectContaining({
    method: 'PUT',
    body: JSON.stringify({ pin: '123456' }),
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
  expect(screen.getByLabelText('PIN de 6 digitos')).toHaveValue('');
  expect(screen.getByText('Configurado')).toBeInTheDocument();
});

test('shows fiscal setup guidance when POS confirmation lacks active issuer', async () => {
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE)
    .mockResolvedValueOnce(errorResponse(400, {
      status: 400,
      code: 'BUSINESS_RULE_VIOLATION',
      message: 'Debes configurar un emisor fiscal activo antes de emitir documentos fiscales.',
      correlationId: 'corr-issuer',
      details: [],
    }))
    .mockResolvedValueOnce(jsonResponse([]))
    .mockResolvedValueOnce(jsonResponse([]))
    .mockResolvedValueOnce(jsonResponse({ defaultSaleDocumentType: 'ELECTRONIC_INVOICE' }));

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Ventas' }));
  fireEvent.click(screen.getAllByRole('button', { name: 'Cerrar venta' })[0]);

  await waitFor(() => expect(screen.getByText(/Debes configurar un emisor fiscal activo/)).toBeInTheDocument());
  expect(screen.getByText(/Ve al modulo Fiscal/)).toBeInTheDocument();
  expect(screen.getByText('Emisor fiscal')).toBeInTheDocument();
  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(9));
  expect(fetchMock).toHaveBeenNthCalledWith(7, '/api/v1/issuers', expect.any(Object));
  expect(fetchMock).toHaveBeenNthCalledWith(8, '/api/v1/numbering-resolutions', expect.any(Object));
  expect(fetchMock).toHaveBeenNthCalledWith(9, '/api/v1/fiscal-policy', expect.any(Object));
});

test('loads operational lists for sales third parties products and purchases', async () => {
  const customer = {
    id: '33333333-3333-3333-3333-333333333333',
    identificationNumber: '1234567890',
    fullName: 'Cliente Operativo',
    personType: 'NATURAL',
    email: 'cliente@example.com',
    phone: '3000000000',
    active: true,
  };
  const product = {
    id: '44444444-4444-4444-4444-444444444444',
    sku: 'SKU-001',
    name: 'Cafe 500g',
    itemType: 'PHYSICAL_GOOD',
    currentStock: 12,
    unitCost: 9000,
    salePrice: 15000,
    active: true,
  };
  const purchase = {
    id: '55555555-5555-5555-5555-555555555555',
    createdAt: '2026-08-18T10:00:00Z',
    status: 'CONFIRMED',
    supplierId: '66666666-6666-6666-6666-666666666666',
    subtotal: 90000,
    taxTotal: 17100,
    total: 107100,
    dueDate: '2026-09-18',
  };
  const sale = {
    id: '77777777-7777-7777-7777-777777777777',
    saleDate: '2026-08-18',
    status: 'CONFIRMED',
    customerId: customer.id,
    paymentMethodCode: 'CASH',
    subtotal: 15000,
    taxTotal: 2850,
    total: 17850,
    electronicDocument: {
      prefix: 'SETP',
      documentNumber: 100,
      status: 'VALIDATED',
      providerTrackingId: 'track-100',
      cufeCude: 'mock-cufe-100',
    },
    lines: [
      {
        id: '88888888-8888-8888-8888-888888888888',
        productId: product.id,
        productSku: product.sku,
        productName: product.name,
        itemType: 'PHYSICAL_GOOD',
        quantity: 1,
        unitPrice: 15000,
        taxCode: 'IVA_19',
        taxRate: 19,
        total: 17850,
      },
    ],
  };
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE)
    .mockResolvedValueOnce(jsonResponse([customer]))
    .mockResolvedValueOnce(jsonResponse([product]))
    .mockResolvedValueOnce(jsonResponse([purchase]))
    .mockResolvedValueOnce(jsonResponse([sale]))
    .mockResolvedValueOnce(jsonResponse(sale));

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Terceros' }));
  fireEvent.click(screen.getByRole('button', { name: 'Consultar terceros' }));
  await waitFor(() => expect(screen.getByText('Cliente Operativo')).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Inventario' }));
  fireEvent.click(screen.getByRole('button', { name: 'Consultar productos' }));
  await waitFor(() => expect(screen.getByText('Cafe 500g')).toBeInTheDocument());
  fireEvent.click(screen.getByRole('button', { name: 'Consultar compras' }));
  await waitFor(() => expect(screen.getByText('66666666-6666-6666-6666-666666666666')).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Ventas' }));
  expect(screen.queryByText('Ventas registradas')).not.toBeInTheDocument();

  fireEvent.click(screen.getByRole('button', { name: 'Registro de Ventas' }));
  fireEvent.click(screen.getByRole('button', { name: 'Consultar ventas' }));
  await waitFor(() => expect(screen.getByText('33333333-3333-3333-3333-333333333333')).toBeInTheDocument());
  fireEvent.click(screen.getByRole('button', { name: 'Ver detalle' }));
  await waitFor(() => expect(screen.getByText('mock-cufe-100')).toBeInTheDocument());
  expect(screen.getByText('track-100')).toBeInTheDocument();

  expect(fetchMock).toHaveBeenNthCalledWith(6, '/api/v1/customers?active=true', expect.objectContaining({
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(7, '/api/v1/products?active=true', expect.objectContaining({
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(8, '/api/v1/purchases', expect.objectContaining({
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(9, '/api/v1/sales/history', expect.objectContaining({
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(10, '/api/v1/sales/77777777-7777-7777-7777-777777777777', expect.objectContaining({
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
});

test('searches customer by document and sends selected customer id in POS sale', async () => {
  const customer = {
    id: '12121212-1212-1212-1212-121212121212',
    identificationNumber: '900123456',
    verificationDigit: 8,
    businessName: 'Cliente Demo SAS',
  };
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE)
    .mockResolvedValueOnce(jsonResponse([customer]))
    .mockResolvedValueOnce(jsonResponse({ id: '99999999-9999-9999-9999-999999999999', customerId: customer.id }))
    .mockResolvedValueOnce(downloadResponse());

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Ventas' }));
  fireEvent.change(screen.getByLabelText('Comprador'), { target: { value: 'IDENTIFIED_CUSTOMER' } });
  fireEvent.change(screen.getByLabelText('Cliente por numero de documento'), { target: { value: '900123456' } });
  await waitFor(() => expect(screen.getByText('Cliente seleccionado: Cliente Demo SAS (900123456)')).toBeInTheDocument());
  fireEvent.click(screen.getAllByRole('button', { name: 'Cerrar venta' })[0]);

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(8));
  expect(fetchMock).toHaveBeenNthCalledWith(6, `/api/v1/customers?active=true&identificationNumberPrefix=900123456`, expect.objectContaining({
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(7, '/api/v1/sales/close', expect.objectContaining({ method: 'POST' }));
  const salePayload = JSON.parse(fetchMock.mock.calls[6][1].body);
  expect(salePayload.customerId).toBe(customer.id);
});

test('requests fiscal document override before confirming the same sale draft', async () => {
  const authorizer = {
    id: '99999999-9999-9999-9999-999999999999',
    email: 'admin@example.com',
    fullName: 'Administrador Empresa',
    status: 'ACTIVE',
  };
  const draftSale = {
    id: '33333333-3333-3333-3333-333333333333',
    saleDate: '2026-08-28',
    status: 'DRAFT',
  };
  const confirmedSale = {
    ...draftSale,
    status: 'CONFIRMED',
    electronicDocument: {
      prefix: 'POS',
      documentNumber: 10,
      cufeCude: 'mock-cufe-override',
    },
  };
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE)
    .mockResolvedValueOnce(jsonResponse([authorizer]))
    .mockResolvedValueOnce(jsonResponse(draftSale))
    .mockResolvedValueOnce(jsonResponse({ saleId: draftSale.id, documentType: 'ELECTRONIC_POS' }))
    .mockResolvedValueOnce(jsonResponse(confirmedSale))
    .mockResolvedValueOnce(downloadResponse());

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Ventas' }));
  expect(screen.getAllByRole('button', { name: 'Cerrar venta' })).toHaveLength(1);
  fireEvent.click(screen.getByRole('button', { name: 'Solicitar cambio de documento fiscal' }));
  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(6));
  fireEvent.change(screen.getByLabelText('Usuario autorizador'), { target: { value: authorizer.email } });
  fireEvent.change(screen.getByLabelText('PIN operacional'), { target: { value: '123456' } });
  fireEvent.change(screen.getByLabelText('Motivo del cambio'), { target: { value: 'Cliente solicita POS electronico' } });
  fireEvent.click(screen.getByRole('button', { name: 'Autorizar cambio' }));

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(8));
  expect(fetchMock).toHaveBeenNthCalledWith(6, `/api/v1/companies/${COMPANY_ID}/users`, expect.objectContaining({
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(7, '/api/v1/sales', expect.objectContaining({ method: 'POST' }));
  expect(fetchMock).toHaveBeenNthCalledWith(8, `/api/v1/sales/${draftSale.id}/document-type-override`, expect.objectContaining({
    method: 'POST',
    headers: expect.objectContaining({
      'X-Company-Id': COMPANY_ID,
      'Idempotency-Key': expect.stringContaining('sale-document-type-override-'),
    }),
  }));
  expect(JSON.parse(fetchMock.mock.calls[7][1].body)).toMatchObject({
    documentType: 'ELECTRONIC_POS',
    authorizedBy: authorizer.id,
    pin: '123456',
    reason: 'Cliente solicita POS electronico',
  });
  expect(screen.getByText('Cambio autorizado solo para esta venta.')).toBeInTheDocument();

  fireEvent.click(screen.getByRole('button', { name: 'Cerrar venta' }));

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(10));
  expect(fetchMock).toHaveBeenNthCalledWith(9, `/api/v1/sales/${draftSale.id}/confirm`, expect.objectContaining({ method: 'POST' }));
  expect(fetchMock).toHaveBeenNthCalledWith(10, `/api/v1/sales/${draftSale.id}/receipt?widthMm=80`, expect.objectContaining({ method: 'POST' }));
});

test('creates fiscal credit note from dedicated fiscal documents module', async () => {
  const note = {
    id: '99999999-9999-9999-9999-999999999999',
    originalDocumentId: '88888888-8888-8888-8888-888888888888',
    status: 'NUMBER_ASSIGNED',
    prefix: 'NC',
    documentNumber: 1,
  };
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE)
    .mockResolvedValueOnce(jsonResponse(note));

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Documentos fiscales' }));
  fireEvent.change(screen.getAllByLabelText('Documento original')[0], { target: { value: note.originalDocumentId } });
  fireEvent.change(screen.getAllByLabelText('Motivo')[0], { target: { value: 'Devolucion parcial' } });
  fireEvent.change(screen.getAllByLabelText('Subtotal')[0], { target: { value: '10000' } });
  fireEvent.change(screen.getAllByLabelText('Impuesto')[0], { target: { value: '1900' } });
  fireEvent.change(screen.getAllByLabelText('Total')[0], { target: { value: '11900' } });
  fireEvent.click(screen.getByRole('button', { name: 'Crear nota credito' }));

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(6));
  expect(fetchMock).toHaveBeenNthCalledWith(6, '/api/v1/credit-notes', expect.objectContaining({
    method: 'POST',
    headers: expect.objectContaining({
      'X-Company-Id': COMPANY_ID,
      'Idempotency-Key': expect.stringContaining('fiscal-note-credit-'),
    }),
  }));
  const notePayload = JSON.parse(fetchMock.mock.calls[5][1].body);
  expect(notePayload).toMatchObject({
    originalDocumentId: note.originalDocumentId,
    reason: 'Devolucion parcial',
    subtotal: 10000,
    taxTotal: 1900,
    total: 11900,
  });
});
test('logout clears session and returns to login screen', async () => {
  mockLoginFlow(ACTIVE_LICENSE);

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Cerrar sesion' }));

  expect(screen.getByRole('heading', { name: 'Iniciar sesion' })).toBeInTheDocument();
  expect(screen.queryByRole('navigation', { name: 'Flujo principal' })).not.toBeInTheDocument();
});

test('hydrates Cognito cookie session without browser Authorization header', async () => {
  window.history.pushState({}, '', '/?auth=success');
  const fetchMock = vi.fn()
    .mockResolvedValueOnce(jsonResponse({
      authenticated: true,
      authMode: 'cognito',
      userId: LOGIN_RESPONSE.userId,
      email: LOGIN_RESPONSE.email,
      fullName: LOGIN_RESPONSE.fullName,
      groups: [],
      expiresAt: LOGIN_RESPONSE.expiresAt,
    }))
    .mockResolvedValueOnce(jsonResponse(COMPANY_ACCESS))
    .mockResolvedValueOnce(jsonResponse(ACTIVE_LICENSE))
    .mockResolvedValueOnce(jsonResponse(ACTIVE_LICENSE))
    .mockResolvedValueOnce(jsonResponse(ACTIVE_COMPANY));
  vi.stubGlobal('fetch', fetchMock);

  render(<App />);

  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());
  expect(screen.getByText('Owner User - owner@example.com')).toBeInTheDocument();
  expect(screen.getByDisplayValue('Empresa Demo SAS (900123456)')).toBeInTheDocument();
  expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/v1/auth/session', expect.objectContaining({
    credentials: 'same-origin',
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/v1/me/companies', expect.objectContaining({
    headers: expect.not.objectContaining({ Authorization: expect.any(String) }),
  }));
});

function mockLoginFlow(licensePayload, accesses = COMPANY_ACCESS) {
  const fetchMock = vi.fn()
    .mockResolvedValueOnce(jsonResponse(LOGIN_RESPONSE))
    .mockResolvedValueOnce(jsonResponse(accesses))
    .mockResolvedValueOnce(jsonResponse(licensePayload))
    .mockResolvedValueOnce(jsonResponse(licensePayload))
    .mockResolvedValueOnce(jsonResponse(ACTIVE_COMPANY));
  vi.stubGlobal('fetch', fetchMock);
  return fetchMock;
}

function jsonResponse(payload) {
  return {
    ok: true,
    status: 200,
    text: () => Promise.resolve(JSON.stringify(payload)),
  };
}

function errorResponse(status, payload) {
  return {
    ok: false,
    status,
    text: () => Promise.resolve(JSON.stringify(payload)),
  };
}

function downloadResponse() {
  return {
    ok: true,
    status: 200,
    headers: {
      get: (name) => name?.toLowerCase() === 'content-disposition'
        ? 'inline; filename="nexofiscal-pos.html"'
        : null,
    },
    blob: () => Promise.resolve(new Blob(['<html>POS</html>'], { type: 'text/html' })),
  };
}

function fillCompanyForm() {
  fireEvent.change(screen.getByLabelText('Razon social'), { target: { value: 'Empresa Demo SAS' } });
  fireEvent.change(screen.getByLabelText('Nombre comercial'), { target: { value: 'Tienda Demo' } });
  fireEvent.change(screen.getByLabelText('Tipo de identificacion'), { target: { value: '31' } });
  fireEvent.change(screen.getByLabelText('Numero de identificacion'), { target: { value: '900123456' } });
  fireEvent.change(screen.getByLabelText('Correo administrativo'), { target: { value: 'admin@example.com' } });
}

function fillInitialAdminForm() {
  fireEvent.change(screen.getByLabelText('Nombre completo'), { target: { value: 'Administrador Empresa' } });
  fireEvent.change(screen.getByLabelText('Correo electronico'), { target: { value: 'admin.empresa@example.com' } });
  fireEvent.change(screen.getByLabelText('Password inicial'), { target: { value: 'AdminDemo#2026!' } });
}

function fillCompanyRoleForm() {
  fireEvent.change(screen.getByLabelText('Nombre del rol'), { target: { value: 'VENDEDOR' } });
  fireEvent.change(screen.getByLabelText('Descripcion'), { target: { value: 'Puede registrar ventas POS y consultar inventario.' } });
  fireEvent.click(screen.getByText('Registrar ventas POS').closest('label').querySelector('input'));
  fireEvent.click(screen.getByText('Ver inventario').closest('label').querySelector('input'));
}

function fillManagedUserForm() {
  fireEvent.change(screen.getByLabelText('Nombre completo'), { target: { value: 'Usuario Vendedor' } });
  fireEvent.change(screen.getByLabelText('Correo electronico'), { target: { value: 'vendedor@example.com' } });
  fireEvent.change(screen.getByLabelText('Password inicial'), { target: { value: 'VendedorDemo#2026!' } });
  fireEvent.change(screen.getByLabelText('Rol obligatorio'), { target: { value: '66666666-6666-6666-6666-666666666666' } });
}

function fillProductForm() {
  fireEvent.change(screen.getByLabelText('SKU'), { target: { value: '123456789' } });
  fireEvent.change(screen.getByLabelText('Codigo de barras'), { target: { value: '123456789' } });
  fireEvent.change(screen.getByLabelText('Nombre'), { target: { value: 'Cafe r' } });
  fireEvent.change(screen.getByLabelText('Descripcion'), { target: { value: 'cafe r' } });
  fireEvent.change(screen.getByLabelText('Tipo de item'), { target: { value: 'PHYSICAL_GOOD' } });
  fireEvent.change(screen.getByLabelText('Impuesto de venta'), { target: { value: 'IVA_19' } });
  fireEvent.change(screen.getByLabelText('Precio final'), { target: { value: '6000' } });
  fireEvent.change(screen.getByLabelText('Costo'), { target: { value: '5000' } });
  fireEvent.change(screen.getByLabelText('Stock inicial'), { target: { value: '100' } });
  fireEvent.change(screen.getByLabelText('Uso del item'), { target: { value: 'SELL_STOCK' } });
}

function fillSimpleNaturalCustomerForm() {
  fireEvent.change(screen.getAllByLabelText('Tipo de tercero')[0], { target: { value: 'CUSTOMER' } });
  fireEvent.change(screen.getByLabelText('Tipo de persona'), { target: { value: 'NATURAL' } });
  fireEvent.change(screen.getByLabelText('Tipo de documento'), { target: { value: '13' } });
  fireEvent.change(screen.getByLabelText('Numero de documento'), { target: { value: '1234567890' } });
  fireEvent.change(screen.getByLabelText('Nombre completo'), { target: { value: 'Cliente Demo' } });
  fireEvent.change(screen.getByLabelText('Correo electronico'), { target: { value: 'cliente@example.com' } });
  fireEvent.change(screen.getByLabelText('Telefono'), { target: { value: '3000000000' } });
}
