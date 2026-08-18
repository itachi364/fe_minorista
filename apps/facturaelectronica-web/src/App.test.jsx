import { afterEach, beforeEach, expect, test, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import App from './App.jsx';
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
  window.sessionStorage.clear();
});

afterEach(() => {
  cleanup();
  window.sessionStorage.clear();
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

test('company user sees only modules allowed by effective permissions', async () => {
  mockLoginFlow(ACTIVE_LICENSE, REPORT_ONLY_ACCESS);

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));

  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());
  expect(screen.getByRole('button', { name: 'Reportes' })).toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Empresa' })).not.toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Usuarios' })).not.toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Consultar' })).toBeInTheDocument();
});

test('sales user can access POS without fiscal advanced permission', async () => {
  mockLoginFlow(ACTIVE_LICENSE, SALES_ONLY_ACCESS);

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));

  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());
  expect(screen.getByRole('button', { name: 'Ventas' })).toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Fiscal' })).not.toBeInTheDocument();

  fireEvent.click(screen.getByRole('button', { name: 'Ventas' }));
  expect(screen.getByRole('button', { name: 'Crear venta' })).toBeInTheDocument();
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
  const fetchMock = vi.fn()
    .mockResolvedValueOnce(jsonResponse({
      ...LOGIN_RESPONSE,
      email: 'root@example.com',
      fullName: 'Root Platform User',
      globalRoles: ['ROOT'],
    }))
    .mockResolvedValueOnce(jsonResponse([]));
  vi.stubGlobal('fetch', fetchMock);

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));

  await waitFor(() => expect(screen.getByText('Panel global')).toBeInTheDocument());
  expect(screen.getByText('Root Platform User - root@example.com')).toBeInTheDocument();
  expect(screen.getByText('PLATAFORMA')).toBeInTheDocument();
  expect(screen.getByText('ROOT')).toBeInTheDocument();
  fireEvent.click(screen.getByRole('button', { name: 'Empresa' }));
  expect(screen.getByLabelText('Razon social')).toBeInTheDocument();
  expect(screen.getByText('Ventas')).toBeInTheDocument();
  expect(screen.getByText('Administrador inicial')).toBeInTheDocument();
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
  expect(screen.queryByText('Gestionar empresas de la plataforma')).not.toBeInTheDocument();

  fillCompanyRoleForm();
  fireEvent.click(screen.getByRole('button', { name: 'Crear rol' }));
  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(6));

  fireEvent.click(screen.getByRole('button', { name: 'Usuarios' }));
  await waitFor(() => expect(screen.getByText('Usuarios disponibles')).toBeInTheDocument());
  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(7));
  fillManagedUserForm();
  fireEvent.click(screen.getByRole('button', { name: 'Crear usuario' }));
  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(10));

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
    .mockResolvedValueOnce(jsonResponse({ id: '99999999-9999-9999-9999-999999999999', paymentMethodCode: 'VIRTUAL_WALLET' }));

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Ventas' }));
  fireEvent.change(screen.getByLabelText('Metodo de pago'), { target: { value: 'VIRTUAL_WALLET' } });
  expect(screen.getByLabelText('Billetera virtual')).toBeInTheDocument();
  fireEvent.change(screen.getByLabelText('Billetera virtual'), { target: { value: 'NEQUI' } });
  fireEvent.click(screen.getByRole('button', { name: 'Crear venta' }));

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(6));
  expect(screen.queryByLabelText('Venta creada')).not.toBeInTheDocument();
  await waitFor(() => expect(screen.getByText('Venta pendiente de confirmacion')).toBeInTheDocument());
  expect(screen.getByText('99999999-9999-9999-9999-999999999999')).toBeInTheDocument();
  const salePayload = JSON.parse(fetchMock.mock.calls[5][1].body);
  expect(salePayload.paymentMethodCode).toBe('VIRTUAL_WALLET');
  expect(salePayload.virtualWalletCode).toBe('NEQUI');
  expect(salePayload.paymentMethodId).toBeUndefined();
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
  };
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE)
    .mockResolvedValueOnce(jsonResponse([customer]))
    .mockResolvedValueOnce(jsonResponse([product]))
    .mockResolvedValueOnce(jsonResponse([purchase]))
    .mockResolvedValueOnce(jsonResponse([sale]));

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
  fireEvent.click(screen.getByRole('button', { name: 'Consultar ventas' }));
  await waitFor(() => expect(screen.getByText('33333333-3333-3333-3333-333333333333')).toBeInTheDocument());

  expect(fetchMock).toHaveBeenNthCalledWith(6, '/api/v1/customers?active=true', expect.objectContaining({
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(7, '/api/v1/products?active=true', expect.objectContaining({
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(8, '/api/v1/purchases', expect.objectContaining({
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(9, '/api/v1/sales', expect.objectContaining({
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
    .mockResolvedValueOnce(jsonResponse({ id: '99999999-9999-9999-9999-999999999999', customerId: customer.id }));

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Ventas' }));
  fireEvent.change(screen.getByLabelText('Cliente por numero de documento'), { target: { value: '900123456' } });
  await waitFor(() => expect(screen.getByText('Cliente seleccionado: Cliente Demo SAS (900123456)')).toBeInTheDocument());
  fireEvent.click(screen.getByRole('button', { name: 'Crear venta' }));

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(7));
  expect(fetchMock).toHaveBeenNthCalledWith(6, `/api/v1/customers?active=true&identificationNumberPrefix=900123456`, expect.objectContaining({
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
  const salePayload = JSON.parse(fetchMock.mock.calls[6][1].body);
  expect(salePayload.customerId).toBe(customer.id);
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

function fillSimpleNaturalCustomerForm() {
  fireEvent.change(screen.getAllByLabelText('Tipo de tercero')[0], { target: { value: 'CUSTOMER' } });
  fireEvent.change(screen.getByLabelText('Tipo de persona'), { target: { value: 'NATURAL' } });
  fireEvent.change(screen.getByLabelText('Tipo de documento'), { target: { value: '13' } });
  fireEvent.change(screen.getByLabelText('Numero de documento'), { target: { value: '1234567890' } });
  fireEvent.change(screen.getByLabelText('Nombre completo'), { target: { value: 'Cliente Demo' } });
  fireEvent.change(screen.getByLabelText('Correo electronico'), { target: { value: 'cliente@example.com' } });
  fireEvent.change(screen.getByLabelText('Telefono'), { target: { value: '3000000000' } });
}
