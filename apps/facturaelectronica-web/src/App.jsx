import { useMemo, useState } from 'react';
import { createIdempotencyKey, requestJson } from './api/client.js';

const steps = ['Empresa', 'Terceros', 'Inventario', 'Fiscal', 'Venta POS', 'Reportes'];

const initialLogin = { email: 'owner@example.com', password: 'secret123' };
const demoStamp = Date.now().toString().slice(-8);
const initialCompany = {
  legalName: `Empresa Demo ${demoStamp} SAS`,
  tradeName: `Tienda Demo ${demoStamp}`,
  identificationTypeId: '00000000-0000-0000-0000-000000000001',
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
const initialThirdParty = {
  role: 'CUSTOMER',
  personType: 'JURIDICA',
  identificationTypeCode: 'NIT',
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
  return compactObject({
    ...form,
    fullName: form.fullName || null,
    businessName: form.businessName || null,
    taxResponsibilities: commaList(form.taxResponsibilities),
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
  const [license, setLicense] = useState(null);
  const [licenseModal, setLicenseModal] = useState(null);
  const [companyForm, setCompanyForm] = useState(initialCompany);
  const [companyAdminForm, setCompanyAdminForm] = useState(initialCompanyAdmin);
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
  const isRoot = session?.globalRoles?.includes('ROOT') || false;
  const visibleSteps = steps;

  async function execute(action) {
    setBusy(true);
    setError(null);
    try {
      const result = await action();
      setOutput(result);
      return result;
    } catch (caught) {
      setError(caught.payload || { message: caught.message });
      return null;
    } finally {
      setBusy(false);
    }
  }

  async function login() {
    const loginResult = await requestJson('/api/v1/auth/login', {
      method: 'POST',
      body: loginForm,
    });
    const tokenValue = loginResult.accessToken;
    if (loginResult.globalRoles?.includes('ROOT')) {
      setSession(loginResult);
      setCompanyAccesses([]);
      setActiveCompanyId('');
      setLicense(null);
      setSelectedStep('Empresa');
      setLicenseModal(null);
      return { login: loginResult, scope: 'ROOT' };
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
      setActiveCompanyId(created.id);
      setCompanyAccesses([{ companyId: created.id, roles: ['ROOT'], permissions: ['GLOBAL_COMPANIES_MANAGE'] }]);
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
    return { user, membership };
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
            <button key={step} className={selectedStep === step ? 'nav-item active' : 'nav-item'} onClick={() => setSelectedStep(step)} type="button">
              {step}
            </button>
          ))}
        </nav>
      </aside>

      <section className="workspace">
        <header className="topbar sessionbar">
          <CompanySessionPanel accesses={companyAccesses} activeCompanyId={activeCompanyId} activeAccess={activeAccess} license={license} session={session} isRoot={isRoot} onCompanyChange={changeCompany} onLogout={logout} busy={busy} />
        </header>

        <section className="panel-grid">
          {selectedStep === 'Empresa' && (
            <CompanyForm form={companyForm} setForm={setCompanyForm} adminForm={companyAdminForm} setAdminForm={setCompanyAdminForm} activeCompanyId={activeCompanyId} isRoot={isRoot} onSubmit={() => execute(createCompany)} onCreateAdmin={() => execute(createInitialCompanyAdmin)} busy={busy} />
          )}
          {selectedStep === 'Terceros' && (
            <ThirdPartyForm form={thirdPartyForm} setForm={setThirdPartyForm} onSubmit={() => execute(createThirdParty)} busy={busy || !activeCompanyId} />
          )}
          {selectedStep === 'Inventario' && (
            <ProductForm form={productForm} setForm={setProductForm} onSubmit={() => execute(createProduct)} busy={busy || !activeCompanyId} />
          )}
          {selectedStep === 'Fiscal' && (
            <div className="split">
              <IssuerForm form={issuerForm} setForm={setIssuerForm} onSubmit={() => execute(configureIssuer)} busy={busy || !activeCompanyId} />
              <ResolutionForm form={resolutionForm} setForm={setResolutionForm} onSubmit={() => execute(configureResolution)} busy={busy || !activeCompanyId} />
            </div>
          )}
          {selectedStep === 'Venta POS' && (
            <SaleForm form={saleForm} setForm={setSaleForm} saleId={saleId} setSaleId={setSaleId} updateItem={updateSaleItem} addItem={addSaleItem} removeItem={removeSaleItem} onCreate={() => execute(createSale)} onConfirm={() => execute(confirmSale)} busy={busy || !activeCompanyId} />
          )}
          {selectedStep === 'Reportes' && (
            <ReportsForm form={reportsForm} setForm={setReportsForm} onSubmit={() => execute(loadReports)} busy={busy || !activeCompanyId} />
          )}
        </section>

        <section className="result-grid">
          <Result title="Respuesta" value={output} />
          <Result title="Error" value={error} tone="danger" />
        </section>
      </section>
      {licenseModal && <Modal title={licenseModal.title} message={licenseModal.message} onClose={() => setLicenseModal(null)} />}
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

function CompanySessionPanel({ accesses, activeCompanyId, activeAccess, license, session, isRoot, onCompanyChange, onLogout, busy }) {
  if (isRoot) {
    return (
      <section className="top-panel app-header-panel root-header-panel">
        <div>
          <h1>Panel global</h1>
          <p>{session.fullName} - {session.email}</p>
        </div>
        <div className="status-row">
          <StatusBadge label="Alcance" value="PLATAFORMA" tone="ok" />
          <StatusBadge label="Rol" value="ROOT" />
          <StatusBadge label="Empresa activa" value={activeCompanyId || 'SIN SELECCION'} />
        </div>
        <p className="hint">Gestiona empresas contratantes y administradores iniciales.</p>
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

function CompanyForm({ form, setForm, adminForm, setAdminForm, activeCompanyId, isRoot, onSubmit, onCreateAdmin, busy }) {
  return <div className="stack">
    <FormPanel title="Empresa" submitLabel="Crear empresa" onSubmit={onSubmit} busy={busy}>
      <div className="form-grid">
        <Field label="Razon social" value={form.legalName} onChange={(value) => setForm({ ...form, legalName: value })} />
        <Field label="Nombre comercial" value={form.tradeName} onChange={(value) => setForm({ ...form, tradeName: value })} />
        <Field label="Tipo identificacion" value={form.identificationTypeId} onChange={(value) => setForm({ ...form, identificationTypeId: value })} />
        <Field label="Numero identificacion" value={form.identificationNumber} onChange={(value) => setForm({ ...form, identificationNumber: value })} />
        <Field label="Digito verificacion" value={form.verificationDigit} onChange={(value) => setForm({ ...form, verificationDigit: value })} />
        <Field label="Email" value={form.email} onChange={(value) => setForm({ ...form, email: value })} type="email" />
      </div>
    </FormPanel>
    {isRoot && (
      <FormPanel title="Administrador inicial" submitLabel="Crear administrador" onSubmit={onCreateAdmin} busy={busy || !activeCompanyId}>
        <div className="form-grid">
          <Field label="Empresa activa" value={activeCompanyId || 'Crea una empresa primero'} onChange={() => {}} />
          <Field label="Nombre completo administrador" value={adminForm.fullName} onChange={(value) => setAdminForm({ ...adminForm, fullName: value })} />
          <Field label="Email administrador" value={adminForm.email} onChange={(value) => setAdminForm({ ...adminForm, email: value })} type="email" />
          <Field label="Password inicial" value={adminForm.password} onChange={(value) => setAdminForm({ ...adminForm, password: value })} type="password" />
          <SelectField label="Rol inicial" value={adminForm.role} onChange={(value) => setAdminForm({ ...adminForm, role: value })} options={['OWNER']} />
        </div>
      </FormPanel>
    )}
  </div>;
}
function ThirdPartyForm({ form, setForm, onSubmit, busy }) {
  return <FormPanel title="Cliente / proveedor" submitLabel="Guardar tercero" onSubmit={onSubmit} busy={busy}>
    <div className="form-grid">
      <SelectField label="Rol" value={form.role} onChange={(value) => setForm({ ...form, role: value })} options={['CUSTOMER', 'SUPPLIER', 'BOTH']} />
      <SelectField label="Tipo persona" value={form.personType} onChange={(value) => setForm({ ...form, personType: value })} options={['NATURAL', 'JURIDICA']} />
      <Field label="Tipo documento" value={form.identificationTypeCode} onChange={(value) => setForm({ ...form, identificationTypeCode: value })} />
      <Field label="Numero documento" value={form.identificationNumber} onChange={(value) => setForm({ ...form, identificationNumber: value })} />
      <Field label="Nombre completo" value={form.fullName} onChange={(value) => setForm({ ...form, fullName: value })} />
      <Field label="Razon social" value={form.businessName} onChange={(value) => setForm({ ...form, businessName: value })} />
      <Field label="Nombre comercial" value={form.tradeName} onChange={(value) => setForm({ ...form, tradeName: value })} />
      <Field label="Email" value={form.email} onChange={(value) => setForm({ ...form, email: value })} type="email" />
      <Field label="Telefono" value={form.phone} onChange={(value) => setForm({ ...form, phone: value })} />
      <Field label="Direccion" value={form.address} onChange={(value) => setForm({ ...form, address: value })} />
      <Field label="Municipio" value={form.municipalityCode} onChange={(value) => setForm({ ...form, municipalityCode: value })} />
      <Field label="Responsabilidades" value={form.taxResponsibilities} onChange={(value) => setForm({ ...form, taxResponsibilities: value })} />
      <Field label="Regimen tributario" value={form.taxRegime} onChange={(value) => setForm({ ...form, taxRegime: value })} />
    </div>
  </FormPanel>;
}

function ProductForm({ form, setForm, onSubmit, busy }) {
  return <FormPanel title="Producto / servicio / insumo" submitLabel="Crear item" onSubmit={onSubmit} busy={busy}>
    <div className="form-grid">
      <Field label="SKU" value={form.sku} onChange={(value) => setForm({ ...form, sku: value })} />
      <Field label="Codigo de barras" value={form.barcode} onChange={(value) => setForm({ ...form, barcode: value })} />
      <Field label="Nombre" value={form.name} onChange={(value) => setForm({ ...form, name: value })} />
      <Field label="Descripcion" value={form.description} onChange={(value) => setForm({ ...form, description: value })} />
      <SelectField label="Tipo item" value={form.itemType} onChange={(value) => setForm({ ...form, itemType: value })} options={['PHYSICAL_GOOD', 'SERVICE', 'SUPPLY']} />
      <Field label="Precio venta" value={form.salePrice} onChange={(value) => setForm({ ...form, salePrice: value })} type="number" />
      <Field label="Costo" value={form.cost} onChange={(value) => setForm({ ...form, cost: value })} type="number" />
      <Field label="Stock inicial" value={form.initialStock} onChange={(value) => setForm({ ...form, initialStock: value })} type="number" />
      <CheckField label="Vendido" checked={form.saleEnabled} onChange={(value) => setForm({ ...form, saleEnabled: value })} />
      <CheckField label="Comprado" checked={form.purchaseEnabled} onChange={(value) => setForm({ ...form, purchaseEnabled: value })} />
      <CheckField label="Controla stock" checked={form.stockTracked} onChange={(value) => setForm({ ...form, stockTracked: value })} />
    </div>
  </FormPanel>;
}

function IssuerForm({ form, setForm, onSubmit, busy }) {
  return <FormPanel title="Emisor fiscal" submitLabel="Guardar emisor" onSubmit={onSubmit} busy={busy}>
    <div className="form-grid compact">
      <Field label="Razon social" value={form.legalName} onChange={(value) => setForm({ ...form, legalName: value })} />
      <Field label="NIT" value={form.nit} onChange={(value) => setForm({ ...form, nit: value })} />
      <Field label="DV" value={form.verificationDigit} onChange={(value) => setForm({ ...form, verificationDigit: value })} />
      <Field label="Responsabilidades" value={form.taxResponsibilities} onChange={(value) => setForm({ ...form, taxResponsibilities: value })} />
      <Field label="Municipio" value={form.municipalityCode} onChange={(value) => setForm({ ...form, municipalityCode: value })} />
      <Field label="Direccion" value={form.address} onChange={(value) => setForm({ ...form, address: value })} />
    </div>
  </FormPanel>;
}

function ResolutionForm({ form, setForm, onSubmit, busy }) {
  return <FormPanel title="Resolucion" submitLabel="Crear resolucion" onSubmit={onSubmit} busy={busy}>
    <div className="form-grid compact">
      <SelectField label="Tipo documento" value={form.documentType} onChange={(value) => setForm({ ...form, documentType: value })} options={['ELECTRONIC_POS', 'ELECTRONIC_INVOICE', 'CREDIT_NOTE', 'DEBIT_NOTE', 'POS_ADJUSTMENT_NOTE']} />
      <Field label="Numero resolucion" value={form.resolutionNumber} onChange={(value) => setForm({ ...form, resolutionNumber: value })} />
      <Field label="Prefijo" value={form.prefix} onChange={(value) => setForm({ ...form, prefix: value })} />
      <Field label="Desde" value={form.fromNumber} onChange={(value) => setForm({ ...form, fromNumber: value })} type="number" />
      <Field label="Hasta" value={form.toNumber} onChange={(value) => setForm({ ...form, toNumber: value })} type="number" />
      <Field label="Vigencia desde" value={form.validFrom} onChange={(value) => setForm({ ...form, validFrom: value })} type="date" />
      <Field label="Vigencia hasta" value={form.validTo} onChange={(value) => setForm({ ...form, validTo: value })} type="date" />
      <SelectField label="Ambiente" value={form.environment} onChange={(value) => setForm({ ...form, environment: value })} options={['TEST', 'PRODUCTION']} />
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

function Field({ label, value, onChange, type = 'text' }) {
  return <label>
    {label}
    <input value={value} onChange={(event) => onChange(event.target.value)} type={type} />
  </label>;
}

function SelectField({ label, value, onChange, options }) {
  return <label>
    {label}
    <select value={value} onChange={(event) => onChange(event.target.value)}>
      {options.map((option) => <option key={option} value={option}>{option}</option>)}
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