import { useMemo, useState } from 'react';
import { createIdempotencyKey, requestJson } from './api/client.js';

const steps = ['Empresa', 'Terceros', 'Inventario', 'Fiscal', 'Venta POS', 'Reportes', 'Usuarios y roles'];
const dianDocumentTypes = [
  { value: 11, label: '11 - Registro civil de nacimiento' },
  { value: 12, label: '12 - Tarjeta de identidad' },
  { value: 13, label: '13 - Cedula de ciudadania' },
  { value: 21, label: '21 - Tarjeta de extranjeria' },
  { value: 22, label: '22 - Cedula de extranjeria' },
  { value: 31, label: '31 - NIT' },
  { value: 41, label: '41 - Pasaporte' },
  { value: 42, label: '42 - Tipo de documento extranjero' },
  { value: 43, label: '43 - Sin identificacion exterior / uso DIAN' },
  { value: 47, label: '47 - Permiso Especial de Permanencia' },
  { value: 48, label: '48 - Permiso por Proteccion Temporal' },
];const thirdPartyTypeOptions = [
  { value: 'CUSTOMER', label: 'Cliente' },
  { value: 'SUPPLIER', label: 'Proveedor' },
  { value: 'BOTH', label: 'Cliente y proveedor' },
];
const personTypeOptions = [
  { value: 'NATURAL', label: 'Natural' },
  { value: 'JURIDICA', label: 'Juridica' },
];
const itemTypeOptions = [
  { value: 'PHYSICAL_GOOD', label: 'Bien fisico' },
  { value: 'SERVICE', label: 'Servicio / intangible' },
  { value: 'SUPPLY', label: 'Insumo' },
];
const fiscalDocumentTypeOptions = [
  { value: 'ELECTRONIC_POS', label: 'POS electronico' },
  { value: 'ELECTRONIC_INVOICE', label: 'Factura electronica' },
  { value: 'CREDIT_NOTE', label: 'Nota credito' },
  { value: 'DEBIT_NOTE', label: 'Nota debito' },
  { value: 'POS_ADJUSTMENT_NOTE', label: 'Nota de ajuste POS' },
];
const environmentOptions = [
  { value: 'TEST', label: 'Pruebas' },
  { value: 'PRODUCTION', label: 'Produccion' },
];
const colombiaLocations = [
  { departmentCode: '05', departmentName: 'Antioquia', municipalities: [{ code: '05001', name: 'Medellin' }, { code: '05088', name: 'Bello' }, { code: '05360', name: 'Itagui' }] },
  { departmentCode: '08', departmentName: 'Atlantico', municipalities: [{ code: '08001', name: 'Barranquilla' }, { code: '08758', name: 'Soledad' }] },
  { departmentCode: '11', departmentName: 'Bogota D.C.', municipalities: [{ code: '11001', name: 'Bogota D.C.' }] },
  { departmentCode: '13', departmentName: 'Bolivar', municipalities: [{ code: '13001', name: 'Cartagena de Indias' }, { code: '13430', name: 'Magangue' }] },
  { departmentCode: '25', departmentName: 'Cundinamarca', municipalities: [{ code: '25175', name: 'Chia' }, { code: '25286', name: 'Funza' }, { code: '25754', name: 'Soacha' }] },
  { departmentCode: '68', departmentName: 'Santander', municipalities: [{ code: '68001', name: 'Bucaramanga' }, { code: '68276', name: 'Floridablanca' }] },
  { departmentCode: '76', departmentName: 'Valle del Cauca', municipalities: [{ code: '76001', name: 'Cali' }, { code: '76109', name: 'Buenaventura' }, { code: '76892', name: 'Yumbo' }] },
].map((department) => ({ ...department, municipalities: [...department.municipalities].sort((a, b) => a.name.localeCompare(b.name, 'es')) }))
  .sort((a, b) => a.departmentName.localeCompare(b.departmentName, 'es'));

const initialLogin = { email: 'owner@example.com', password: 'secret123' };
const demoStamp = Date.now().toString().slice(-8);
const initialCompany = {
  legalName: `Empresa Demo ${demoStamp} SAS`,
  tradeName: `Tienda Demo ${demoStamp}`,
  identificationTypeCode: 31,
  identificationNumber: `90${demoStamp}`,
  verificationDigit: '7',
  email: `admin-${demoStamp}@example.com`,
};
const initialCompanyAdmin = {
  fullName: 'Administrador Empresa',
  email: 'admin.empresa@example.com',
  password: 'AdminDemo#2026!',
  role: 'OWNER',
};
const initialManagedUser = {
  fullName: 'Usuario Vendedor',
  email: 'vendedor@example.com',
  password: 'VendedorDemo#2026!',
};
const initialCompanyRole = {
  name: 'VENDEDOR',
  description: 'Puede registrar ventas POS y consultar inventario.',
  permissionCodes: ['SALES_CREATE', 'FISCAL_DOCUMENTS_ISSUE', 'INVENTORY_VIEW'],
};
const initialRoleAssignment = {
  userId: '',
  roleIds: [],
};
const initialThirdParty = {
  thirdPartyType: 'CUSTOMER',
  personType: 'JURIDICA',
  identificationTypeCode: 31,
  identificationNumber: '900123456',
  fullName: '',
  businessName: 'Cliente Demo SAS',
  tradeName: 'Cliente Demo',
  email: 'cliente@example.com',
  phone: '3000000000',
  address: 'Calle 1 # 2-3',
  municipalityCode: '11001',
  taxResponsibilities: 'O-13',
  taxRegime: 'RESPONSABLE_IVA',
};
const initialProduct = {
  sku: 'SKU-DEMO-001',
  barcode: '7701234567890',
  name: 'Cafe 500g',
  description: 'Bolsa de cafe',
  itemType: 'PHYSICAL_GOOD',
  saleEnabled: true,
  purchaseEnabled: true,
  stockTracked: true,
  salePrice: '15000',
  cost: '9000',
  initialStock: '10',
};
const initialIssuer = {
  legalName: 'Empresa Demo SAS',
  nit: '900123456',
  verificationDigit: '7',
  taxResponsibilities: 'O-13',
  municipalityCode: '11001',
  address: 'Calle 1 # 2-3',
};
const initialResolution = {
  documentType: 'ELECTRONIC_POS',
  resolutionNumber: '18760000001',
  prefix: 'POS',
  fromNumber: '100',
  toNumber: '999',
  validFrom: '2026-01-01',
  validTo: '2026-12-31',
  environment: 'TEST',
};
const initialSale = {
  customerId: '',
  saleChannel: 'POS',
  paymentMethodId: '',
  items: [{ productId: '', quantity: '1', unitPrice: '15000', discountAmount: '0', taxCode: 'IVA_19', taxRate: '19' }],
};
const initialReports = {
  status: '',
  from: '',
  to: '',
  productId: '',
  accountCode: '',
};

function toNumber(value) {
  if (value === '' || value === null || value === undefined) {
    return undefined;
  }
  return Number(value);
}

function compactObject(value) {
  return Object.fromEntries(Object.entries(value).filter(([, entry]) => entry !== '' && entry !== undefined));
}

function commaList(value) {
  return value.split(',').map((item) => item.trim()).filter(Boolean);
}

function asPretty(value) {
  return JSON.stringify(value, null, 2);
}

function buildCompanyPayload(form) {
  return compactObject(form);
}

function buildCompanyAdminPayload(form) {
  return compactObject({
    email: form.email,
    fullName: form.fullName,
    password: form.password,
  });
}

function buildThirdPartyPayload(form) {
  const roles = form.thirdPartyType === 'BOTH' ? ['CUSTOMER', 'SUPPLIER'] : [form.thirdPartyType];
  return compactObject({
    personType: form.personType,
    identificationTypeCode: form.identificationTypeCode,
    identificationNumber: form.identificationNumber,
    fullName: form.fullName || null,
    businessName: form.businessName || null,
    tradeName: form.tradeName,
    email: form.email,
    phone: form.phone,
    address: form.address,
    municipalityCode: form.municipalityCode,
    taxResponsibilities: commaList(form.taxResponsibilities),
    taxRegime: form.taxRegime,
    roles,
  });
}

function buildProductPayload(form) {
  return compactObject({
    ...form,
    salePrice: toNumber(form.salePrice),
    cost: toNumber(form.cost),
    initialStock: toNumber(form.initialStock),
  });
}

function buildIssuerPayload(form) {
  return compactObject({
    ...form,
    taxResponsibilities: commaList(form.taxResponsibilities),
  });
}

function buildResolutionPayload(form) {
  return compactObject({
    ...form,
    fromNumber: toNumber(form.fromNumber),
    toNumber: toNumber(form.toNumber),
  });
}

function buildSalePayload(form) {
  return compactObject({
    customerId: form.customerId || null,
    saleChannel: form.saleChannel,
    paymentMethodId: form.paymentMethodId || null,
    items: form.items.map((item) => compactObject({
      productId: item.productId,
      quantity: toNumber(item.quantity),
      unitPrice: toNumber(item.unitPrice),
      discountAmount: toNumber(item.discountAmount) ?? 0,
      taxCode: item.taxCode,
      taxRate: toNumber(item.taxRate),
    })),
  });
}

function findLocationByMunicipality(municipalityCode) {
  for (const department of colombiaLocations) {
    const municipality = department.municipalities.find((item) => item.code === municipalityCode);
    if (municipality) {
      return { department, municipality };
    }
  }
  return { department: colombiaLocations[0], municipality: colombiaLocations[0].municipalities[0] };
}

function companyLabel(company) {
  if (!company) return 'Sin empresa seleccionada';
  const id = company.id || company.companyId;
  return `${company.legalName || company.tradeName || id} (${company.identificationNumber || id})`;
}

function buildIssuerFromCompany(company, currentIssuer) {
  if (!company) return currentIssuer;
  return {
    ...currentIssuer,
    legalName: company.legalName || currentIssuer.legalName,
    nit: company.identificationNumber || currentIssuer.nit,
    verificationDigit: company.verificationDigit || currentIssuer.verificationDigit,
  };
}
function hasAnyPermission(access, permissions) {
  return permissions.some((permission) => access?.permissions?.includes(permission));
}

function companyScopedPermissions(permissions) {
  return permissions.filter((permission) => permission.active !== false && permission.scope === 'COMPANY' && !String(permission.code).startsWith('GLOBAL_'));
}

function hasAnyRole(access, roles) {
  return roles.some((role) => access?.roles?.includes(role));
}

const stepPermissionRules = {
  Empresa: ['COMPANY_SETTINGS_MANAGE', 'GLOBAL_COMPANIES_MANAGE'],
  Terceros: ['COMPANY_SETTINGS_MANAGE'],
  Inventario: ['INVENTORY_VIEW', 'INVENTORY_MANAGE'],
  Fiscal: ['FISCAL_DOCUMENTS_ISSUE', 'COMPANY_SETTINGS_MANAGE'],
  'Venta POS': ['SALES_CREATE', 'FISCAL_DOCUMENTS_ISSUE'],
  Reportes: ['REPORTS_VIEW', 'ACCOUNTING_VIEW'],
  'Usuarios y roles': ['COMPANY_USERS_MANAGE', 'COMPANY_ROLES_MANAGE', 'USERS_MANAGE', 'ROLES_MANAGE'],
};

function buildQuery(params) {
  const query = new URLSearchParams();
  Object.entries(params).forEach(([key, value]) => {
    if (value !== '' && value !== undefined && value !== null) {
      query.set(key, value);
    }
  });
  const text = query.toString();
  return text ? `?${text}` : '';
}

export default function App() {
  const [selectedStep, setSelectedStep] = useState(steps[0]);
  const [loginForm, setLoginForm] = useState(initialLogin);
  const [session, setSession] = useState(null);
  const [companyAccesses, setCompanyAccesses] = useState([]);
  const [activeCompanyId, setActiveCompanyId] = useState('');
  const [rootCompanies, setRootCompanies] = useState([]);
  const [license, setLicense] = useState(null);
  const [licenseModal, setLicenseModal] = useState(null);
  const [companyForm, setCompanyForm] = useState(initialCompany);
  const [companyAdminForm, setCompanyAdminForm] = useState(initialCompanyAdmin);
  const [managedUserForm, setManagedUserForm] = useState(initialManagedUser);
  const [companyRoleForm, setCompanyRoleForm] = useState(initialCompanyRole);
  const [roleAssignmentForm, setRoleAssignmentForm] = useState(initialRoleAssignment);
  const [permissionCatalog, setPermissionCatalog] = useState([]);
  const [companyRoles, setCompanyRoles] = useState([]);
  const [managedUsers, setManagedUsers] = useState([]);
  const [adminModalOpen, setAdminModalOpen] = useState(false);
  const [roleAssignmentModalOpen, setRoleAssignmentModalOpen] = useState(false);
  const [userSearchEmail, setUserSearchEmail] = useState('');
  const [thirdPartyForm, setThirdPartyForm] = useState(initialThirdParty);
  const [productForm, setProductForm] = useState(initialProduct);
  const [issuerForm, setIssuerForm] = useState(initialIssuer);
  const [resolutionForm, setResolutionForm] = useState(initialResolution);
  const [saleForm, setSaleForm] = useState(initialSale);
  const [reportsForm, setReportsForm] = useState(initialReports);
  const [saleId, setSaleId] = useState('');
  const [output, setOutput] = useState(null);
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const token = session?.accessToken || '';
  const context = useMemo(() => ({ token, companyId: activeCompanyId }), [token, activeCompanyId]);
  const activeAccess = companyAccesses.find((access) => access.companyId === activeCompanyId);
  const activeCompany = rootCompanies.find((company) => company.id === activeCompanyId || company.companyId === activeCompanyId);
  const isRoot = session?.globalRoles?.includes('ROOT') || false;
  const isCompanyAdmin = hasAnyRole(activeAccess, ['OWNER', 'ADMIN']);
  const canUse = (permissions) => isRoot || isCompanyAdmin || hasAnyPermission(activeAccess, permissions);
  const canManageSecurity = canUse(stepPermissionRules['Usuarios y roles']);
  const visibleSteps = steps.filter((step) => isRoot || isCompanyAdmin || hasAnyPermission(activeAccess, stepPermissionRules[step] || []));
  const currentStep = visibleSteps.includes(selectedStep) ? selectedStep : visibleSteps[0] || 'Empresa';
  const availableCompanyPermissions = companyScopedPermissions(permissionCatalog);

  async function execute(action) {
    setBusy(true);
    setError(null);
    try {
      const result = await action();
      setOutput(result);
      return result;
    } catch (caught) {
      setError(caught.status === 403 ? { status: 403, message: 'No tienes permisos para ejecutar esta accion.' } : caught.payload || { message: caught.message });
      return null;
    } finally {
      setBusy(false);
    }
  }

  async function loadRootCompanies(tokenValue = token) {
    const companies = await requestJson('/api/v1/companies', { token: tokenValue });
    setRootCompanies(companies || []);
    return companies || [];
  }
  async function login() {
    const loginResult = await requestJson('/api/v1/auth/login', {
      method: 'POST',
      body: loginForm,
    });
    const tokenValue = loginResult.accessToken;
    if (loginResult.globalRoles?.includes('ROOT')) {
      const companies = await loadRootCompanies(tokenValue);
      const firstCompany = companies[0] || null;
      setSession(loginResult);
      setCompanyAccesses(firstCompany ? [{ companyId: firstCompany.id, roles: ['ROOT'], permissions: ['GLOBAL_COMPANIES_MANAGE'] }] : []);
      setActiveCompanyId(firstCompany?.id || '');
      setIssuerForm((current) => buildIssuerFromCompany(firstCompany, current));
      setLicense(null);
      setSelectedStep('Empresa');
      setLicenseModal(null);
      return { login: loginResult, companies, scope: 'ROOT' };
    }
    const accesses = await requestJson('/api/v1/me/companies', { token: tokenValue });
    const nextCompanyId = accesses[0]?.companyId || '';

    if (!nextCompanyId) {
      closeSessionWithModal('Sin empresa asociada', 'Tu usuario no tiene una empresa habilitada para operar. Contacta al administrador de la licencia.');
      return null;
    }

    const validation = await requestJson(
      `/api/v1/companies/${nextCompanyId}/license/validation?action=CREATE_TRANSACTION`,
      { token: tokenValue, companyId: nextCompanyId },
    );

    if (!validation?.allowed) {
      closeSessionWithModal('Licencia no activa', validation?.message || 'La licencia de la empresa no esta activa. La sesion se cerro automaticamente.');
      return null;
    }

    setSession(loginResult);
    setCompanyAccesses(accesses);
    setActiveCompanyId(nextCompanyId);
    setLicense(validation);
    setLicenseModal(null);
    return { login: loginResult, companies: accesses, license: validation };
  }

  async function loadLicense(companyId = activeCompanyId, tokenValue = token) {
    if (!companyId) {
      return null;
    }
    const validation = await requestJson(
      `/api/v1/companies/${companyId}/license/validation?action=CREATE_TRANSACTION`,
      { token: tokenValue, companyId },
    );
    if (!validation?.allowed) {
      closeSessionWithModal('Licencia no activa', validation?.message || 'La licencia de la empresa no esta activa. La sesion se cerro automaticamente.');
      return null;
    }
    setLicense(validation);
    setLicenseModal(null);
    return validation;
  }

  function clearSession() {
    setSession(null);
    setCompanyAccesses([]);
    setActiveCompanyId('');
    setLicense(null);
    setOutput(null);
    setError(null);
    setPermissionCatalog([]);
    setCompanyRoles([]);
    setManagedUsers([]);
    setRoleAssignmentForm(initialRoleAssignment);
    setRootCompanies([]);
    setAdminModalOpen(false);
    setRoleAssignmentModalOpen(false);
    setUserSearchEmail('');
  }

  function closeSessionWithModal(title, message) {
    clearSession();
    setLicenseModal({ title, message });
  }

  function logout() {
    clearSession();
    setLicenseModal(null);
  }

  function requireCompany() {
    if (!activeCompanyId) {
      throw new Error('Inicia sesion y selecciona una empresa antes de operar.');
    }
  }

  async function createCompany() {
    const created = await requestJson('/api/v1/companies', {
      method: 'POST',
      body: buildCompanyPayload(companyForm),
      token,
      idempotencyKey: createIdempotencyKey('company'),
    });
    if (isRoot && created?.id) {
      setRootCompanies((current) => [created, ...current.filter((company) => company.id !== created.id)]);
      setActiveCompanyId(created.id);
      setCompanyAccesses([{ companyId: created.id, roles: ['ROOT'], permissions: ['GLOBAL_COMPANIES_MANAGE'] }]);
      setIssuerForm((current) => buildIssuerFromCompany(created, current));
    }
    return created;
  }

  async function createInitialCompanyAdmin() {
    requireCompany();
    const user = await requestJson('/api/v1/users', {
      method: 'POST',
      body: buildCompanyAdminPayload(companyAdminForm),
      token,
      idempotencyKey: createIdempotencyKey('company-admin-user'),
    });
    const membership = await requestJson(`/api/v1/companies/${activeCompanyId}/memberships`, {
      method: 'POST',
      body: { userId: user.id, roles: [companyAdminForm.role || 'OWNER'] },
      token,
      companyId: activeCompanyId,
      idempotencyKey: createIdempotencyKey('company-admin-membership'),
    });
    setAdminModalOpen(false);
    return { user, membership };
  }

  async function loadIdentityAdminData() {
    requireCompany();
    const [permissions, roles] = await Promise.all([
      requestJson(isRoot ? '/api/v1/platform/permissions' : `/api/v1/companies/${activeCompanyId}/permissions/catalog`, { token, companyId: activeCompanyId }),
      requestJson(`/api/v1/companies/${activeCompanyId}/roles`, { token, companyId: activeCompanyId }),
    ]);
    setPermissionCatalog(permissions || []);
    setCompanyRoles(roles || []);
    return { permissions, roles };
  }

  async function loadCompanyUsers(email = userSearchEmail) {
    requireCompany();
    const users = await requestJson(`/api/v1/companies/${activeCompanyId}/users${buildQuery({ email })}`, {
      token,
      companyId: activeCompanyId,
    });
    setManagedUsers(users || []);
    return users || [];
  }
  async function createCompanyRole() {
    requireCompany();
    const created = await requestJson(`/api/v1/companies/${activeCompanyId}/roles`, {
      method: 'POST',
      body: companyRoleForm,
      token,
      companyId: activeCompanyId,
      idempotencyKey: createIdempotencyKey('company-role'),
    });
    setCompanyRoles((current) => [created, ...current.filter((role) => role.id !== created.id)]);
    setRoleAssignmentForm((current) => ({ ...current, roleIds: created?.id ? [created.id] : current.roleIds }));
    return created;
  }

  async function createManagedUser() {
    const created = await requestJson('/api/v1/users', {
      method: 'POST',
      body: managedUserForm,
      token,
      idempotencyKey: createIdempotencyKey('managed-user'),
    });
    setManagedUsers((current) => [created, ...current.filter((user) => user.id !== created.id)]);
    setRoleAssignmentForm((current) => ({ ...current, userId: created?.id || current.userId }));
    return created;
  }

  async function assignManagedRoles() {
    requireCompany();
    if (!roleAssignmentForm.userId || roleAssignmentForm.roleIds.length === 0) {
      throw new Error('Selecciona un usuario y al menos un rol para asignar.');
    }
    const assigned = await requestJson(`/api/v1/companies/${activeCompanyId}/users/${roleAssignmentForm.userId}/role-assignments`, {
      method: 'POST',
      body: { roleIds: roleAssignmentForm.roleIds },
      token,
      companyId: activeCompanyId,
      idempotencyKey: createIdempotencyKey('role-assignment'),
    });
    setRoleAssignmentModalOpen(false);
    return assigned;
  }

  function togglePermissionCode(code) {
    setCompanyRoleForm((current) => {
      const exists = current.permissionCodes.includes(code);
      return {
        ...current,
        permissionCodes: exists
          ? current.permissionCodes.filter((permission) => permission !== code)
          : [...current.permissionCodes, code],
      };
    });
  }

  function toggleAssignedRole(roleId) {
    setRoleAssignmentForm((current) => {
      const exists = current.roleIds.includes(roleId);
      return {
        ...current,
        roleIds: exists ? current.roleIds.filter((id) => id !== roleId) : [...current.roleIds, roleId],
      };
    });
  }
  async function createThirdParty() {
    requireCompany();
    return requestJson('/api/v1/third-parties', {
      method: 'POST',
      body: buildThirdPartyPayload(thirdPartyForm),
      ...context,
      idempotencyKey: createIdempotencyKey('third-party'),
    });
  }

  async function createProduct() {
    requireCompany();
    const result = await requestJson('/api/v1/products', {
      method: 'POST',
      body: buildProductPayload(productForm),
      ...context,
      idempotencyKey: createIdempotencyKey('product'),
    });
    if (result?.id) {
      updateSaleItem(0, 'productId', result.id);
      updateSaleItem(0, 'unitPrice', String(result.salePrice || productForm.salePrice));
    }
    return result;
  }

  async function configureIssuer() {
    requireCompany();
    return requestJson('/api/v1/issuers', {
      method: 'POST',
      body: buildIssuerPayload(issuerForm),
      ...context,
      idempotencyKey: createIdempotencyKey('issuer'),
    });
  }

  async function configureResolution() {
    requireCompany();
    return requestJson('/api/v1/numbering-resolutions', {
      method: 'POST',
      body: buildResolutionPayload(resolutionForm),
      ...context,
      idempotencyKey: createIdempotencyKey('resolution'),
    });
  }

  async function createSale() {
    requireCompany();
    const result = await requestJson('/api/v1/sales', {
      method: 'POST',
      body: buildSalePayload(saleForm),
      ...context,
      idempotencyKey: createIdempotencyKey('sale'),
    });
    if (result?.id) {
      setSaleId(result.id);
    }
    return result;
  }

  async function confirmSale() {
    requireCompany();
    return requestJson(`/api/v1/sales/${saleId}/confirm`, {
      method: 'POST',
      ...context,
      idempotencyKey: createIdempotencyKey('confirm-sale'),
    });
  }

  async function loadReports() {
    requireCompany();
    const [sales, stock, journal] = await Promise.all([
      requestJson(`/api/v1/reports/sales${buildQuery({ status: reportsForm.status, from: reportsForm.from, to: reportsForm.to })}`, context),
      requestJson(`/api/v1/reports/inventory-stock${buildQuery({ active: true })}`, context),
      requestJson(`/api/v1/reports/journal${buildQuery({ from: reportsForm.from, to: reportsForm.to })}`, context),
    ]);
    return { sales, stock, journal };
  }

  function updateSaleItem(index, field, value) {
    setSaleForm((current) => ({
      ...current,
      items: current.items.map((item, itemIndex) => (itemIndex === index ? { ...item, [field]: value } : item)),
    }));
  }

  function addSaleItem() {
    setSaleForm((current) => ({
      ...current,
      items: [...current.items, { productId: '', quantity: '1', unitPrice: '0', discountAmount: '0', taxCode: 'IVA_19', taxRate: '19' }],
    }));
  }

  function removeSaleItem(index) {
    setSaleForm((current) => ({
      ...current,
      items: current.items.length === 1 ? current.items : current.items.filter((_, itemIndex) => itemIndex !== index),
    }));
  }

  function changeCompany(companyId) {
    setActiveCompanyId(companyId);
    const selectedCompany = rootCompanies.find((company) => company.id === companyId || company.companyId === companyId);
    if (selectedCompany) {
      setIssuerForm((current) => buildIssuerFromCompany(selectedCompany, current));
      setCompanyAccesses([{ companyId, roles: ['ROOT'], permissions: ['GLOBAL_COMPANIES_MANAGE'] }]);
      return;
    }
    execute(() => loadLicense(companyId));
  }

  if (!session) {
    return (
      <main className="login-shell">
        <section className="login-card">
          <div className="login-brand">
            <span>Factura Electronica</span>
            <h1>Iniciar sesion</h1>
            <p>Acceso seguro al panel operativo de facturacion, inventario y contabilidad.</p>
          </div>
          <LoginPanel form={loginForm} setForm={setLoginForm} busy={busy} onLogin={() => execute(login)} />
        </section>
        {licenseModal && <Modal title={licenseModal.title} message={licenseModal.message} onClose={() => setLicenseModal(null)} />}
      </main>
    );
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand">Factura Electronica</div>
        <nav aria-label="Flujo principal">
          {visibleSteps.map((step) => (
            <button key={step} className={currentStep === step ? 'nav-item active' : 'nav-item'} onClick={() => setSelectedStep(step)} type="button">
              {step}
            </button>
          ))}
        </nav>
      </aside>

      <section className="workspace">
        <header className="topbar sessionbar">
          <CompanySessionPanel accesses={companyAccesses} companies={rootCompanies} activeCompanyId={activeCompanyId} activeAccess={activeAccess} license={license} session={session} isRoot={isRoot} onCompanyChange={changeCompany} onLogout={logout} busy={busy} />
        </header>

        <section className="panel-grid">
          {currentStep === 'Empresa' && (
            <CompanyForm form={companyForm} setForm={setCompanyForm} companies={rootCompanies} activeCompanyId={activeCompanyId} activeCompany={activeCompany} isRoot={isRoot} onCompanyChange={changeCompany} onSubmit={() => execute(createCompany)} onOpenAdminModal={() => setAdminModalOpen(true)} busy={busy} />
          )}
          {currentStep === 'Terceros' && (
            <ThirdPartyForm form={thirdPartyForm} setForm={setThirdPartyForm} onSubmit={() => execute(createThirdParty)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Terceros)} />
          )}
          {currentStep === 'Inventario' && (
            <ProductForm form={productForm} setForm={setProductForm} onSubmit={() => execute(createProduct)} busy={busy || !activeCompanyId || !canUse(['INVENTORY_MANAGE'])} />
          )}
          {currentStep === 'Fiscal' && (
            <div className="split">
              <IssuerForm form={issuerForm} setForm={setIssuerForm} activeCompany={activeCompany} onSubmit={() => execute(configureIssuer)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Fiscal)} />
              <ResolutionForm form={resolutionForm} setForm={setResolutionForm} onSubmit={() => execute(configureResolution)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Fiscal)} />
            </div>
          )}
          {currentStep === 'Venta POS' && (
            <SaleForm form={saleForm} setForm={setSaleForm} saleId={saleId} setSaleId={setSaleId} updateItem={updateSaleItem} addItem={addSaleItem} removeItem={removeSaleItem} onCreate={() => execute(createSale)} onConfirm={() => execute(confirmSale)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules['Venta POS'])} />
          )}
          {currentStep === 'Reportes' && (
            <ReportsForm form={reportsForm} setForm={setReportsForm} onSubmit={() => execute(loadReports)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Reportes)} />
          )}
          {currentStep === 'Usuarios y roles' && (
            <IdentityAdminPanel permissions={availableCompanyPermissions} roles={companyRoles} users={managedUsers} roleForm={companyRoleForm} setRoleForm={setCompanyRoleForm} userForm={managedUserForm} setUserForm={setManagedUserForm} onLoad={() => execute(loadIdentityAdminData)} onCreateRole={() => execute(createCompanyRole)} onCreateUser={() => execute(createManagedUser)} onOpenAssignModal={() => setRoleAssignmentModalOpen(true)} onTogglePermission={togglePermissionCode} busy={busy || !activeCompanyId || !canManageSecurity} />
          )}
        </section>

        <section className="result-grid">
          <Result title="Respuesta" value={output} />
          <Result title="Error" value={error} tone="danger" />
        </section>
      </section>
      {licenseModal && <Modal title={licenseModal.title} message={licenseModal.message} onClose={() => setLicenseModal(null)} />}
      {adminModalOpen && <AdminModal form={companyAdminForm} setForm={setCompanyAdminForm} activeCompany={activeCompany} activeCompanyId={activeCompanyId} onSubmit={() => execute(createInitialCompanyAdmin)} onClose={() => setAdminModalOpen(false)} busy={busy || !activeCompanyId} />}
      {roleAssignmentModalOpen && <RoleAssignmentModal users={managedUsers} roles={companyRoles} form={roleAssignmentForm} setForm={setRoleAssignmentForm} searchEmail={userSearchEmail} setSearchEmail={setUserSearchEmail} onSearch={() => execute(() => loadCompanyUsers(userSearchEmail))} onSubmit={() => execute(assignManagedRoles)} onToggleRole={toggleAssignedRole} onClose={() => setRoleAssignmentModalOpen(false)} busy={busy || !activeCompanyId || !canManageSecurity} />}
    </main>
  );
}

function LoginPanel({ form, setForm, busy, onLogin }) {
  return (
    <section className="top-panel login-panel">
      <form className="login-form" onSubmit={(event) => { event.preventDefault(); onLogin(); }}>
        <Field label="Email" value={form.email} onChange={(value) => setForm({ ...form, email: value })} type="email" />
        <Field label="Password" value={form.password} onChange={(value) => setForm({ ...form, password: value })} type="password" />
        <button className="primary" disabled={busy} type="submit">Ingresar</button>
      </form>
    </section>
  );
}

function CompanySessionPanel({ accesses, companies, activeCompanyId, activeAccess, license, session, isRoot, onCompanyChange, onLogout, busy }) {
  if (isRoot) {
    return (
      <section className="top-panel app-header-panel root-header-panel">
        <div>
          <h1>Panel global</h1>
          <p>{session.fullName} - {session.email}</p>
        </div>
        <label>
          Empresa activa
          <select value={activeCompanyId} onChange={(event) => onCompanyChange(event.target.value)} disabled={busy || companies.length === 0}>
            <option value="">Seleccione una empresa</option>
            {companies.map((company) => <option key={company.id} value={company.id}>{companyLabel(company)}</option>)}
          </select>
        </label>
        <div className="status-row">
          <StatusBadge label="Alcance" value="PLATAFORMA" tone="ok" />
          <StatusBadge label="Rol" value="ROOT" />
        </div>
        <button className="secondary" onClick={onLogout} type="button">Cerrar sesion</button>
      </section>
    );
  }

  return (
    <section className="top-panel app-header-panel">
      <div>
        <h1>Empresa activa</h1>
        <p>{session.fullName} - {session.email}</p>
      </div>
      <label>
        Empresa del usuario
        <select value={activeCompanyId} onChange={(event) => onCompanyChange(event.target.value)} disabled={!accesses.length || busy}>
          {accesses.map((access) => <option key={access.companyId} value={access.companyId}>{access.companyId}</option>)}
        </select>
      </label>
      <div className="status-row">
        <StatusBadge label="Licencia" value={license?.allowed ? 'ACTIVA' : license?.status || 'SIN VALIDAR'} tone={license?.allowed ? 'ok' : 'warn'} />
        <StatusBadge label="Roles" value={activeAccess?.roles?.join(', ') || 'N/A'} />
      </div>
      <button className="secondary" onClick={onLogout} type="button">Cerrar sesion</button>
    </section>
  );
}

function Modal({ title, message, onClose }) {
  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="license-modal-title">
      <section className="modal-card">
        <h1 id="license-modal-title">{title}</h1>
        <p>{message}</p>
        <button className="primary" onClick={onClose} type="button">Aceptar</button>
      </section>
    </div>
  );
}


function MunicipalityFields({ municipalityCode, onChange }) {
  const current = findLocationByMunicipality(municipalityCode);
  const departmentCode = current.department.departmentCode;
  const municipalities = current.department.municipalities;
  return <>
    <label>
      Departamento
      <select value={departmentCode} onChange={(event) => onChange(colombiaLocations.find((department) => department.departmentCode === event.target.value)?.municipalities[0]?.code || municipalityCode)}>
        {colombiaLocations.map((department) => <option key={department.departmentCode} value={department.departmentCode}>{department.departmentName}</option>)}
      </select>
    </label>
    <label>
      Municipio / ciudad
      <select value={current.municipality.code} onChange={(event) => onChange(event.target.value)}>
        {municipalities.map((municipality) => <option key={municipality.code} value={municipality.code}>{municipality.name}</option>)}
      </select>
    </label>
  </>;
}

function AdminModal({ form, setForm, activeCompany, activeCompanyId, onSubmit, onClose, busy }) {
  return <ActionModal title="Crear administrador inicial" onClose={onClose}>
    <div className="form-grid compact modal-form-grid">
      <Field label="Empresa" value={companyLabel(activeCompany) || activeCompanyId} onChange={() => {}} readOnly />
      <Field label="Nombre completo" value={form.fullName} onChange={(value) => setForm({ ...form, fullName: value })} />
      <Field label="Correo electronico" value={form.email} onChange={(value) => setForm({ ...form, email: value })} type="email" />
      <Field label="Password inicial" value={form.password} onChange={(value) => setForm({ ...form, password: value })} type="password" />
      <Field label="Rol inicial" value="OWNER - Administrador empresarial" onChange={() => {}} readOnly />
    </div>
    <div className="modal-actions">
      <button className="secondary" onClick={onClose} type="button">Cancelar</button>
      <button className="primary" disabled={busy} onClick={onSubmit} type="button">Crear administrador</button>
    </div>
  </ActionModal>;
}

function RoleAssignmentModal({ users, roles, form, setForm, searchEmail, setSearchEmail, onSearch, onSubmit, onToggleRole, onClose, busy }) {
  const selectedUser = users.find((user) => user.id === form.userId);
  return <ActionModal title="Asignar rol empresarial" onClose={onClose}>
    <div className="form-grid compact modal-form-grid">
      <Field label="Buscar por correo" value={searchEmail} onChange={setSearchEmail} type="email" />
      <label>
        Usuario
        <select value={form.userId} onChange={(event) => setForm({ ...form, userId: event.target.value })}>
          <option value="">Seleccione un usuario</option>
          {users.map((user) => <option key={user.id} value={user.id}>{user.email} - {user.fullName}</option>)}
        </select>
      </label>
      <label>
        Rol empresarial
        <select value={form.roleIds[0] || ''} onChange={(event) => setForm({ ...form, roleIds: event.target.value ? [event.target.value] : [] })}>
          <option value="">Seleccione un rol</option>
          {roles.map((role) => <option key={role.id} value={role.id}>{role.name}</option>)}
        </select>
      </label>
      <Field label="Usuario ID que viaja al backend" value={selectedUser?.id || form.userId} onChange={() => {}} readOnly />
    </div>
    <div className="role-list compact-role-list">
      {roles.map((role) => (
        <label className="role-option" key={role.id}>
          <input checked={form.roleIds.includes(role.id)} onChange={() => onToggleRole(role.id)} type="checkbox" />
          <span><b>{role.name}</b><small>{role.description || 'Sin descripcion'} - {role.permissionCodes?.length || 0} permisos</small></span>
        </label>
      ))}
    </div>
    <div className="modal-actions">
      <button className="secondary" disabled={busy} onClick={onSearch} type="button">Buscar usuario</button>
      <button className="secondary" onClick={onClose} type="button">Cancelar</button>
      <button className="primary" disabled={busy || !form.userId || form.roleIds.length === 0} onClick={onSubmit} type="button">Asignar rol</button>
    </div>
  </ActionModal>;
}

function ActionModal({ title, children, onClose }) {
  return <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="action-modal-title">
    <section className="modal-card action-modal-card">
      <header className="panel-header">
        <h1 id="action-modal-title">{title}</h1>
        <button className="secondary" onClick={onClose} type="button">Cerrar</button>
      </header>
      {children}
    </section>
  </div>;
}

function CompanyForm({ form, setForm, companies, activeCompanyId, activeCompany, isRoot, onCompanyChange, onSubmit, onOpenAdminModal, busy }) {
  return <div className="stack">
    <FormPanel title="Empresa contratante" submitLabel="Crear empresa" onSubmit={onSubmit} busy={busy}>
      <div className="form-grid">
        <Field label="Razon social" value={form.legalName} onChange={(value) => setForm({ ...form, legalName: value })} />
        <Field label="Nombre comercial" value={form.tradeName} onChange={(value) => setForm({ ...form, tradeName: value })} />
        <SelectField label="Tipo de identificacion" value={form.identificationTypeCode} onChange={(value) => setForm({ ...form, identificationTypeCode: Number(value) })} options={dianDocumentTypes} />
        <Field label="Numero de identificacion" value={form.identificationNumber} onChange={(value) => setForm({ ...form, identificationNumber: value })} />
        <Field label="Digito de verificacion" value={form.verificationDigit} onChange={(value) => setForm({ ...form, verificationDigit: value })} />
        <Field label="Correo administrativo" value={form.email} onChange={(value) => setForm({ ...form, email: value })} type="email" />
      </div>
    </FormPanel>
    {isRoot && (
      <section className="tool-panel">
        <header className="panel-header">
          <div>
            <h1>Administrador inicial</h1>
            <p>Selecciona una empresa y crea el usuario administrador con rol OWNER empresarial.</p>
          </div>
          <button className="primary" disabled={busy || !activeCompanyId} onClick={onOpenAdminModal} type="button">Crear administrador</button>
        </header>
        <div className="form-grid compact">
          <label>
            Empresa activa
            <select value={activeCompanyId} onChange={(event) => onCompanyChange(event.target.value)} disabled={busy || companies.length === 0}>
              <option value="">Seleccione una empresa</option>
              {companies.map((company) => <option key={company.id} value={company.id}>{companyLabel(company)}</option>)}
            </select>
          </label>
          <Field label="Empresa seleccionada" value={companyLabel(activeCompany)} onChange={() => {}} readOnly />
        </div>
      </section>
    )}
  </div>;
}
function ThirdPartyForm({ form, setForm, onSubmit, busy }) {
  return <FormPanel title="Cliente / proveedor" submitLabel="Guardar tercero" onSubmit={onSubmit} busy={busy}>
    <div className="form-grid">
      <SelectField label="Tipo de tercero" value={form.thirdPartyType} onChange={(value) => setForm({ ...form, thirdPartyType: value })} options={thirdPartyTypeOptions} />
      <SelectField label="Tipo de persona" value={form.personType} onChange={(value) => setForm({ ...form, personType: value })} options={personTypeOptions} />
      <SelectField label="Tipo de documento" value={form.identificationTypeCode} onChange={(value) => setForm({ ...form, identificationTypeCode: Number(value) })} options={dianDocumentTypes} />
      <Field label="Numero de documento" value={form.identificationNumber} onChange={(value) => setForm({ ...form, identificationNumber: value })} />
      <Field label="Nombre completo" value={form.fullName} onChange={(value) => setForm({ ...form, fullName: value })} />
      <Field label="Razon social" value={form.businessName} onChange={(value) => setForm({ ...form, businessName: value })} />
      <Field label="Nombre comercial" value={form.tradeName} onChange={(value) => setForm({ ...form, tradeName: value })} />
      <Field label="Correo electronico" value={form.email} onChange={(value) => setForm({ ...form, email: value })} type="email" />
      <Field label="Telefono" value={form.phone} onChange={(value) => setForm({ ...form, phone: value })} />
      <Field label="Direccion" value={form.address} onChange={(value) => setForm({ ...form, address: value })} />
      <MunicipalityFields municipalityCode={form.municipalityCode} onChange={(value) => setForm({ ...form, municipalityCode: value })} />
      <Field label="Responsabilidades fiscales" value={form.taxResponsibilities} onChange={(value) => setForm({ ...form, taxResponsibilities: value })} />
      <Field label="Regimen tributario" value={form.taxRegime} onChange={(value) => setForm({ ...form, taxRegime: value })} />
    </div>
  </FormPanel>;
}

function IdentityAdminPanel({ permissions, roles, users, roleForm, setRoleForm, userForm, setUserForm, onLoad, onCreateRole, onCreateUser, onOpenAssignModal, onTogglePermission, busy }) {
  const groupedPermissions = permissions.reduce((groups, permission) => {
    const key = permission.module || 'general';
    return { ...groups, [key]: [...(groups[key] || []), permission] };
  }, {});

  return <div className="stack identity-admin">
    <section className="tool-panel identity-overview">
      <header className="panel-header">
        <div>
          <h1>Usuarios, roles y permisos</h1>
          <p>Administra roles empresariales con permisos delegables y asigna accesos a usuarios.</p>
        </div>
        <div className="button-row">
          <button className="secondary" disabled={busy} onClick={onLoad} type="button">Cargar permisos y roles</button>
          <button className="primary" disabled={busy || roles.length === 0} onClick={onOpenAssignModal} type="button">Asignar rol</button>
        </div>
      </header>
      <div className="summary-strip">
        <StatusBadge label="Permisos" value={permissions.length || 0} />
        <StatusBadge label="Roles" value={roles.length || 0} />
        <StatusBadge label="Usuarios" value={users.length || 0} />
      </div>
    </section>

    <div className="split identity-split">
      <FormPanel title="Rol empresarial" submitLabel="Crear rol" onSubmit={onCreateRole} busy={busy || roleForm.permissionCodes.length === 0}>
        <div className="form-grid compact">
          <Field label="Nombre del rol" value={roleForm.name} onChange={(value) => setRoleForm({ ...roleForm, name: value })} />
          <Field label="Descripcion" value={roleForm.description} onChange={(value) => setRoleForm({ ...roleForm, description: value })} />
        </div>
        <PermissionPicker groupedPermissions={groupedPermissions} selected={roleForm.permissionCodes} onToggle={onTogglePermission} />
      </FormPanel>

      <FormPanel title="Usuario empresarial" submitLabel="Crear usuario" onSubmit={onCreateUser} busy={busy}>
        <div className="form-grid compact">
          <Field label="Nombre completo" value={userForm.fullName} onChange={(value) => setUserForm({ ...userForm, fullName: value })} />
          <Field label="Correo electronico" value={userForm.email} onChange={(value) => setUserForm({ ...userForm, email: value })} type="email" />
          <Field label="Password inicial" value={userForm.password} onChange={(value) => setUserForm({ ...userForm, password: value })} type="password" />
        </div>
      </FormPanel>
    </div>
  </div>;
}

function PermissionPicker({ groupedPermissions, selected, onToggle }) {
  const modules = Object.keys(groupedPermissions).sort();
  if (modules.length === 0) {
    return <p className="hint">Carga el catalogo de permisos para seleccionar permisos delegables.</p>;
  }
  return <div className="permission-groups">
    {modules.map((module) => (
      <section className="permission-group" key={module}>
        <h2>{module}</h2>
        <div className="permission-list">
          {groupedPermissions[module].map((permission) => (
            <label className="permission-option" key={permission.code}>
              <input checked={selected.includes(permission.code)} onChange={() => onToggle(permission.code)} type="checkbox" />
              <span>
                <b>{permission.code}</b>
                <small>{permission.description}</small>
              </span>
            </label>
          ))}
        </div>
      </section>
    ))}
  </div>;
}
function ProductForm({ form, setForm, onSubmit, busy }) {
  return <FormPanel title="Producto / servicio / insumo" submitLabel="Crear item" onSubmit={onSubmit} busy={busy}>
    <div className="form-grid">
      <Field label="SKU" value={form.sku} onChange={(value) => setForm({ ...form, sku: value })} />
      <Field label="Codigo de barras" value={form.barcode} onChange={(value) => setForm({ ...form, barcode: value })} />
      <Field label="Nombre" value={form.name} onChange={(value) => setForm({ ...form, name: value })} />
      <Field label="Descripcion" value={form.description} onChange={(value) => setForm({ ...form, description: value })} />
      <SelectField label="Tipo de item" value={form.itemType} onChange={(value) => setForm({ ...form, itemType: value })} options={itemTypeOptions} />
      <Field label="Precio venta" value={form.salePrice} onChange={(value) => setForm({ ...form, salePrice: value })} type="number" />
      <Field label="Costo" value={form.cost} onChange={(value) => setForm({ ...form, cost: value })} type="number" />
      <Field label="Stock inicial" value={form.initialStock} onChange={(value) => setForm({ ...form, initialStock: value })} type="number" />
      <CheckField label="Vendido" checked={form.saleEnabled} onChange={(value) => setForm({ ...form, saleEnabled: value })} />
      <CheckField label="Comprado" checked={form.purchaseEnabled} onChange={(value) => setForm({ ...form, purchaseEnabled: value })} />
      <CheckField label="Controla stock" checked={form.stockTracked} onChange={(value) => setForm({ ...form, stockTracked: value })} />
    </div>
  </FormPanel>;
}

function IssuerForm({ form, setForm, activeCompany, onSubmit, busy }) {
  return <FormPanel title="Emisor fiscal" submitLabel="Guardar emisor" onSubmit={onSubmit} busy={busy}>
    <p className="hint">El emisor fiscal corresponde a la empresa activa. Los datos principales se precargan desde la empresa contratante.</p>
    <div className="form-grid compact">
      <Field label="Razon social" value={activeCompany?.legalName || form.legalName} onChange={(value) => setForm({ ...form, legalName: value })} readOnly={Boolean(activeCompany)} />
      <Field label="NIT" value={activeCompany?.identificationNumber || form.nit} onChange={(value) => setForm({ ...form, nit: value })} readOnly={Boolean(activeCompany)} />
      <Field label="DV" value={activeCompany?.verificationDigit || form.verificationDigit} onChange={(value) => setForm({ ...form, verificationDigit: value })} readOnly={Boolean(activeCompany)} />
      <Field label="Responsabilidades fiscales" value={form.taxResponsibilities} onChange={(value) => setForm({ ...form, taxResponsibilities: value })} />
      <MunicipalityFields municipalityCode={form.municipalityCode} onChange={(value) => setForm({ ...form, municipalityCode: value })} />
      <Field label="Direccion fiscal" value={form.address} onChange={(value) => setForm({ ...form, address: value })} />
    </div>
  </FormPanel>;
}

function ResolutionForm({ form, setForm, onSubmit, busy }) {
  return <FormPanel title="Resolucion" submitLabel="Crear resolucion" onSubmit={onSubmit} busy={busy}>
    <div className="form-grid compact">
      <SelectField label="Tipo de documento fiscal" value={form.documentType} onChange={(value) => setForm({ ...form, documentType: value })} options={fiscalDocumentTypeOptions} />
      <Field label="Numero resolucion" value={form.resolutionNumber} onChange={(value) => setForm({ ...form, resolutionNumber: value })} />
      <Field label="Prefijo" value={form.prefix} onChange={(value) => setForm({ ...form, prefix: value })} />
      <Field label="Desde" value={form.fromNumber} onChange={(value) => setForm({ ...form, fromNumber: value })} type="number" />
      <Field label="Hasta" value={form.toNumber} onChange={(value) => setForm({ ...form, toNumber: value })} type="number" />
      <Field label="Vigencia desde" value={form.validFrom} onChange={(value) => setForm({ ...form, validFrom: value })} type="date" />
      <Field label="Vigencia hasta" value={form.validTo} onChange={(value) => setForm({ ...form, validTo: value })} type="date" />
      <SelectField label="Ambiente" value={form.environment} onChange={(value) => setForm({ ...form, environment: value })} options={environmentOptions} />
    </div>
  </FormPanel>;
}

function SaleForm({ form, setForm, saleId, setSaleId, updateItem, addItem, removeItem, onCreate, onConfirm, busy }) {
  return <section className="tool-panel">
    <header className="panel-header">
      <h1>Venta POS</h1>
      <div className="button-row">
        <button className="secondary" onClick={addItem} type="button">Agregar linea</button>
        <button className="primary" disabled={busy} onClick={onCreate} type="button">Crear venta</button>
      </div>
    </header>
    <div className="form-grid compact">
      <Field label="Cliente" value={form.customerId} onChange={(value) => setForm({ ...form, customerId: value })} />
      <SelectField label="Canal" value={form.saleChannel} onChange={(value) => setForm({ ...form, saleChannel: value })} options={['POS', 'ELECTRONIC_INVOICE']} />
      <Field label="Metodo pago" value={form.paymentMethodId} onChange={(value) => setForm({ ...form, paymentMethodId: value })} />
      <Field label="Venta creada" value={saleId} onChange={setSaleId} />
    </div>
    <div className="line-list">
      {form.items.map((item, index) => (
        <div className="line-row" key={`${index}-${item.productId}`}>
          <Field label="Producto" value={item.productId} onChange={(value) => updateItem(index, 'productId', value)} />
          <Field label="Cantidad" value={item.quantity} onChange={(value) => updateItem(index, 'quantity', value)} type="number" />
          <Field label="Precio" value={item.unitPrice} onChange={(value) => updateItem(index, 'unitPrice', value)} type="number" />
          <Field label="Descuento" value={item.discountAmount} onChange={(value) => updateItem(index, 'discountAmount', value)} type="number" />
          <Field label="Impuesto" value={item.taxCode} onChange={(value) => updateItem(index, 'taxCode', value)} />
          <Field label="Tasa" value={item.taxRate} onChange={(value) => updateItem(index, 'taxRate', value)} type="number" />
          <button className="icon-button" onClick={() => removeItem(index)} type="button" aria-label="Eliminar linea">X</button>
        </div>
      ))}
    </div>
    <button className="primary" disabled={busy || !saleId} onClick={onConfirm} type="button">Confirmar POS</button>
  </section>;
}

function ReportsForm({ form, setForm, onSubmit, busy }) {
  return <FormPanel title="Reportes" submitLabel="Consultar" onSubmit={onSubmit} busy={busy}>
    <div className="form-grid">
      <Field label="Estado" value={form.status} onChange={(value) => setForm({ ...form, status: value })} />
      <Field label="Desde" value={form.from} onChange={(value) => setForm({ ...form, from: value })} type="date" />
      <Field label="Hasta" value={form.to} onChange={(value) => setForm({ ...form, to: value })} type="date" />
      <Field label="Producto" value={form.productId} onChange={(value) => setForm({ ...form, productId: value })} />
      <Field label="Cuenta" value={form.accountCode} onChange={(value) => setForm({ ...form, accountCode: value })} />
    </div>
  </FormPanel>;
}

function FormPanel({ title, submitLabel, onSubmit, busy, children }) {
  return <form className="tool-panel" onSubmit={(event) => { event.preventDefault(); onSubmit(); }}>
    <header className="panel-header">
      <h1>{title}</h1>
      <button className="primary" disabled={busy} type="submit">{submitLabel}</button>
    </header>
    {children}
  </form>;
}

function Field({ label, value, onChange, type = 'text', readOnly = false }) {
  return <label>
    {label}
    <input value={value} onChange={(event) => onChange(event.target.value)} type={type} readOnly={readOnly} />
  </label>;
}

function SelectField({ label, value, onChange, options }) {
  return <label>
    {label}
    <select value={value} onChange={(event) => onChange(event.target.value)}>
      {options.map((option) => {
        const normalized = typeof option === 'object' ? option : { value: option, label: option };
        return <option key={normalized.value} value={normalized.value}>{normalized.label}</option>;
      })}
    </select>
  </label>;
}

function CheckField({ label, checked, onChange }) {
  return <label className="check-field">
    <input checked={checked} onChange={(event) => onChange(event.target.checked)} type="checkbox" />
    {label}
  </label>;
}

function StatusBadge({ label, value, tone }) {
  return <span className={tone === 'ok' ? 'status-badge ok' : tone === 'warn' ? 'status-badge warn' : 'status-badge'}>
    <b>{label}</b> {value}
  </span>;
}

function Result({ title, value, tone }) {
  return (
    <section className={tone === 'danger' ? 'result-panel danger' : 'result-panel'}>
      <h2>{title}</h2>
      <pre>{value ? asPretty(value) : ''}</pre>
    </section>
  );
}
