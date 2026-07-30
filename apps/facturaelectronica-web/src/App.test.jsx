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
const COMPANY_ACCESS = [{ companyId: COMPANY_ID, roles: ['OWNER'], permissions: ['REPORTS_VIEW'] }];
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
  const fetchMock = vi.fn().mockResolvedValueOnce(jsonResponse({
    ...LOGIN_RESPONSE,
    email: 'root@example.com',
    fullName: 'Root Platform User',
    globalRoles: ['ROOT'],
  }));
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
  expect(fetchMock).toHaveBeenCalledTimes(1);
});

test('root creates company and initial administrator', async () => {
  const createdCompany = { id: COMPANY_ID, legalName: 'Empresa Demo SAS', status: 'ACTIVE' };
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
    .mockResolvedValueOnce(jsonResponse(createdCompany))
    .mockResolvedValueOnce(jsonResponse(createdUser))
    .mockResolvedValueOnce(jsonResponse(createdMembership));
  vi.stubGlobal('fetch', fetchMock);

  render(<App />);
  fireEvent.click(screen.getByRole('button', { name: 'Ingresar' }));
  await waitFor(() => expect(screen.getByText('Panel global')).toBeInTheDocument());

  fireEvent.click(screen.getByRole('button', { name: 'Crear empresa' }));
  await waitFor(() => expect(screen.getAllByDisplayValue(COMPANY_ID).length).toBeGreaterThan(0));

  fireEvent.click(screen.getByRole('button', { name: 'Crear administrador' }));

  await waitFor(() => expect(fetchMock).toHaveBeenCalledTimes(4));
  expect(fetchMock).toHaveBeenNthCalledWith(2, '/api/v1/companies', expect.objectContaining({
    method: 'POST',
    headers: expect.objectContaining({ Authorization: 'Bearer token-1' }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(3, '/api/v1/users', expect.objectContaining({
    method: 'POST',
    body: JSON.stringify({
      email: 'admin.empresa@example.com',
      fullName: 'Administrador Empresa',
      password: 'AdminDemo#2026!',
    }),
  }));
  expect(fetchMock).toHaveBeenNthCalledWith(4, `/api/v1/companies/${COMPANY_ID}/memberships`, expect.objectContaining({
    method: 'POST',
    body: JSON.stringify({ userId: createdUser.id, roles: ['OWNER'] }),
    headers: expect.objectContaining({ 'X-Company-Id': COMPANY_ID }),
  }));
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

function mockLoginFlow(licensePayload) {
  const fetchMock = vi.fn()
    .mockResolvedValueOnce(jsonResponse(LOGIN_RESPONSE))
    .mockResolvedValueOnce(jsonResponse(COMPANY_ACCESS))
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