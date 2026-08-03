import { afterEach, beforeEach, expect, test, vi } from 'vitest';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import App from './App.jsx';

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
const ACTIVE_LICENSE = {
  companyId: COMPANY_ID,
  action: 'CREATE_TRANSACTION',
  allowed: true,
  status: 'ACTIVE',
  reasonCode: null,
  message: 'Licencia activa.',
};

beforeEach(() => {
  vi.stubGlobal('crypto', { randomUUID: () => '00000000-0000-4000-8000-000000000000' });
});

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
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
  expect(screen.getByDisplayValue(COMPANY_ID)).toBeInTheDocument();
  expect(screen.getByText('ACTIVA')).toBeInTheDocument();
  expect(screen.getByLabelText('Razon social')).toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Validar licencia' })).not.toBeInTheDocument();
  expect(fetchMock).toHaveBeenNthCalledWith(1, '/api/v1/auth/login', expect.objectContaining({ method: 'POST' }));
  expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/v1/me/companies', expect.objectContaining({
    headers: expect.objectContaining({ Authorization: 'Bearer token-1' }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(3,
    `/api/v1/companies/${COMPANY_ID}/license/validation?action=CREATE_TRANSACTION`,
    expect.objectContaining({ headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }) }),
  );
});

test('company user sees only modules allowed by effective permissions', async () => {
  mockLoginFlow(ACTIVE_LICENSE, REPORT_ONLY_ACCESS);

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));

  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());
  expect(screen.getByRole('button', { name: 'Reportes' })).toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Empresa' })).not.toBeInTheDocument();
  expect(screen.queryByRole('button', { name: 'Usuarios y roles' })).not.toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Consultar' })).toBeInTheDocument();
});
test('login with inactive license shows modal and keeps only login visible', async () => {
  mockLoginFlow({ ...ACTIVE_LICENSE, allowed: false, status: 'SUSPENDED', message: 'La licencia esta suspendida.' });

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));

  await waitFor(() => expect(screen.getByRole('dialog')).toBeInTheDocument());
  expect(screen.getByText('Licencia no activa')).toBeInTheDocument();
  expect(screen.getByText('La licencia esta suspendida.')).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Ingresar' })).toBeInTheDocument();
  expect(screen.queryByRole('navigation', { name: 'Flujo principal' })).not.toBeInTheDocument();
  expect(screen.queryByLabelText('Razon social')).not.toBeInTheDocument();
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
  expect(screen.getByLabelText('Razon social')).toBeInTheDocument();
  expect(screen.getByText('Venta POS')).toBeInTheDocument();
  expect(screen.getByText('Administrador inicial')).toBeInTheDocument();
  expect(screen.getByRole('button', { name: 'Usuarios y roles' })).toBeInTheDocument();
  expect(fetchMock).toHaveBeenCalledTimes(2);
  expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/v1/companies', expect.objectContaining({
    headers: expect.objectContaining({ Authorization: 'Bearer token-1' }),
  }));
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

  fireEvent.click(screen.getByRole('button', { name: 'Crear empresa' }));
  await waitFor(() => expect(screen.getAllByText('Empresa Demo SAS (900123456)').length).toBeGreaterThan(0));

  fireEvent.click(screen.getAllByRole('button', { name: 'Crear administrador' })[0]);
  await waitFor(() => expect(screen.getByRole('dialog')).toBeInTheDocument());
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
    permissionCodes: ['SALES_CREATE', 'FISCAL_DOCUMENTS_ISSUE', 'INVENTORY_VIEW'],
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
    .mockResolvedValueOnce(jsonResponse(createdUser))
    .mockResolvedValueOnce(jsonResponse(assignedAccess));
  vi.stubGlobal('fetch', fetchMock);

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByText('Panel global')).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Crear empresa' }));
  await waitFor(() => expect(screen.getAllByText('Empresa Demo SAS (900123456)').length).toBeGreaterThan(0));

  fireEvent.click(screen.getByRole('button', { name: 'Usuarios y roles' }));
  fireEvent.click(screen.getByRole('button', { name: 'Cargar permisos y roles' }));
  await waitFor(() => expect(screen.getByText('SALES_CREATE')).toBeInTheDocument());
  expect(screen.queryByText('GLOBAL_COMPANIES_MANAGE')).not.toBeInTheDocument();

  fireEvent.click(screen.getByRole('button', { name: 'Crear rol' }));
  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(6));

  fireEvent.click(screen.getByRole('button', { name: 'Crear usuario' }));
  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(7));

  fireEvent.click(screen.getByRole('button', { name: 'Asignar rol' }));
  await waitFor(() => expect(screen.getByRole('dialog')).toBeInTheDocument());
  const assignButtons = screen.getAllByRole('button', { name: 'Asignar rol' });
  fireEvent.click(assignButtons[assignButtons.length - 1]);
  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(8));

  expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/v1/platform/permissions', expect.objectContaining({
    headers: expect.objectContaining({ Authorization: 'Bearer token-1', 'X-Company-Id': COMPANY_ID }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(5, `/api/v1/companies/${COMPANY_ID}/roles`, expect.objectContaining({
    headers: expect.objectContaining({ Authorization: 'Bearer token-1', 'X-Company-Id': COMPANY_ID }),
  }));
  expect(JSON.parse(fetchMock.mock.calls[5][1].body)).toEqual({
    name: 'VENDEDOR',
    description: 'Puede registrar ventas POS y consultar inventario.',
    permissionCodes: ['SALES_CREATE', 'FISCAL_DOCUMENTS_ISSUE', 'INVENTORY_VIEW'],
  });
  expect(JSON.parse(fetchMock.mock.calls[6][1].body)).toEqual({
    email: 'vendedor@example.com',
    fullName: 'Usuario Vendedor',
    password: 'VendedorDemo#2026!',
  });
  expect(fetchMock).toHaveBeenNthCalledWith(8, `/api/v1/companies/${COMPANY_ID}/users/${createdUser.id}/role-assignments`, expect.objectContaining({
    method: 'POST',
    body: JSON.stringify({ roleIds: [createdRole.id] }),
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
});

test('creates third party with numeric DIAN identification type code', async () => {
  const fetchMock = mockLoginFlow(ACTIVE_LICENSE)
    .mockResolvedValueOnce(jsonResponse({ id: '55555555-5555-5555-5555-555555555555', identificationTypeCode: 31 }));

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByRole('button', { name: 'Cerrar sesion' })).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Terceros' }));
  fireEvent.click(screen.getByRole('button', { name: 'Guardar tercero' }));

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(4));
  const thirdPartyPayload = JSON.parse(fetchMock.mock.calls[3][1].body);
  expect(thirdPartyPayload.identificationTypeCode).toBe(31);
  expect(thirdPartyPayload.roles).toEqual(['CUSTOMER']);
  expect(thirdPartyPayload.role).toBeUndefined();
  expect(fetchMock).toHaveBeenNthCalledWith(4, '/api/v1/third-parties', expect.objectContaining({
    method: 'POST',
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
});test('logout clears session and returns to login screen', async () => {
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
    .mockResolvedValueOnce(jsonResponse(licensePayload));
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
