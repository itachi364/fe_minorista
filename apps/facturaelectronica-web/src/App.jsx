import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { createIdempotencyKey, requestJson } from './api/client.js';
import { Result } from './components/forms.jsx';
import { Modal } from './components/Modal.jsx';
import { steps } from './data/catalogs.js';
import {
  initialCatalogItem,
  initialCompany,
  initialCompanyAdmin,
  initialCompanyRole,
  initialIssuer,
  initialLogin,
  initialManagedUser,
  initialProduct,
  initialReports,
  initialResolution,
  initialRoleAssignment,
  initialSale,
  initialThirdParty,
} from './data/initialState.js';
import { LoginPanel } from './features/auth/LoginPanel.jsx';
import { CatalogAdminPanel } from './features/catalogs/CatalogAdminPanel.jsx';
import { AdminModal } from './features/company/AdminModal.jsx';
import { CompanyForm } from './features/company/CompanyForm.jsx';
import { CompanySessionPanel } from './features/company/CompanySessionPanel.jsx';
import { IssuerForm } from './features/company/IssuerForm.jsx';
import { ResolutionForm } from './features/fiscal/ResolutionForm.jsx';
import { IdentityAdminPanel, RoleAssignmentModal } from './features/identity/IdentityAdminPanel.jsx';
import { ProductForm } from './features/inventory/ProductForm.jsx';
import { ReportsForm } from './features/reports/ReportsForm.jsx';
import { SaleForm } from './features/sales/SaleForm.jsx';
import { ThirdPartyForm } from './features/thirdparties/ThirdPartyForm.jsx';
import { companyScopedPermissions, hasAnyPermission, hasAnyRole, stepPermissionRules } from './utils/authorization.js';
import { buildIssuerFromCompany } from './utils/company.js';
import {
  buildCompanyAdminPayload,
  buildCompanyPayload,
  buildIssuerPayload,
  buildProductPayload,
  buildResolutionPayload,
  buildSalePayload,
  buildThirdPartyPayload,
} from './utils/payloadBuilders.js';
import { buildQuery } from './utils/query.js';
import { emptyRuntimeCatalogs, loadRuntimeCatalogs } from './utils/runtimeCatalogs.js';
import { clearStoredSession, loadStoredSession, saveStoredSession, SESSION_TIMEOUT_MS } from './utils/sessionStorage.js';
export default function App() {
  const [storedSnapshot] = useState(() => loadStoredSession());
  const [selectedStep, setSelectedStep] = useState(steps[0]);
  const [loginForm, setLoginForm] = useState(initialLogin);
  const [session, setSession] = useState(storedSnapshot?.session || null);
  const [companyAccesses, setCompanyAccesses] = useState(storedSnapshot?.companyAccesses || []);
  const [activeCompanyId, setActiveCompanyId] = useState(storedSnapshot?.activeCompanyId || '');
  const [rootCompanies, setRootCompanies] = useState(storedSnapshot?.rootCompanies || []);
  const [license, setLicense] = useState(storedSnapshot?.license || null);
  const [runtimeCatalogs, setRuntimeCatalogs] = useState(() => globalThis.__FACTURA_RUNTIME_CATALOGS__ || emptyRuntimeCatalogs);
  const [lastActivityAt, setLastActivityAt] = useState(storedSnapshot?.lastActivityAt || Date.now());
  const lastActivityRef = useRef(lastActivityAt);
  const [licenseModal, setLicenseModal] = useState(null);
  const [companyForm, setCompanyForm] = useState(initialCompany);
  const [companyAdminForm, setCompanyAdminForm] = useState(initialCompanyAdmin);
  const [managedUserForm, setManagedUserForm] = useState(initialManagedUser);
  const [companyRoleForm, setCompanyRoleForm] = useState(initialCompanyRole);
  const [roleAssignmentForm, setRoleAssignmentForm] = useState(initialRoleAssignment);
  const [permissionCatalog, setPermissionCatalog] = useState([]);
  const [companyRoles, setCompanyRoles] = useState([]);
  const [managedUsers, setManagedUsers] = useState([]);
  const [catalogDefinitions, setCatalogDefinitions] = useState([]);
  const [selectedCatalogCode, setSelectedCatalogCode] = useState('');
  const [catalogItems, setCatalogItems] = useState([]);
  const [catalogItemForm, setCatalogItemForm] = useState(initialCatalogItem);
  const [adminModalOpen, setAdminModalOpen] = useState(false);
  const [roleAssignmentModalOpen, setRoleAssignmentModalOpen] = useState(false);
  const [userSearchEmail, setUserSearchEmail] = useState('');
  const [thirdPartyForm, setThirdPartyForm] = useState(initialThirdParty);
  const [productForm, setProductForm] = useState(initialProduct);
  const [issuerForm, setIssuerForm] = useState(initialIssuer);
  const [resolutionForm, setResolutionForm] = useState(initialResolution);
  const [saleForm, setSaleForm] = useState(initialSale);
  const [customerSearch, setCustomerSearch] = useState('');
  const [customerOptions, setCustomerOptions] = useState([]);
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [reportsForm, setReportsForm] = useState(initialReports);
  const [saleId, setSaleId] = useState('');
  const [output, setOutput] = useState(null);
  const [error, setError] = useState(null);
  const [busy, setBusy] = useState(false);

  const token = session?.accessToken || '';
  const context = useMemo(() => ({ token, companyId: activeCompanyId }), [token, activeCompanyId]);
  const activeAccess = companyAccesses.find((access) => access.companyId === activeCompanyId);
  const activeCompany = rootCompanies.find((company) => company.id === activeCompanyId || company.companyId === activeCompanyId);
  const companyMunicipalityCode = activeCompany?.municipalityCode || issuerForm.municipalityCode;
  const isRoot = session?.globalRoles?.includes('ROOT') || false;
  const isCompanyAdmin = hasAnyRole(activeAccess, ['OWNER', 'ADMIN']);
  const canUse = (permissions) => isRoot || isCompanyAdmin || hasAnyPermission(activeAccess, permissions);
  const canManageSecurity = canUse(stepPermissionRules['Usuarios y roles']);
  const canManageCatalogs = canUse(stepPermissionRules.Catalogos);
  const visibleSteps = steps.filter((step) => isRoot || isCompanyAdmin || hasAnyPermission(activeAccess, stepPermissionRules[step] || []));
  const currentStep = visibleSteps.includes(selectedStep) ? selectedStep : visibleSteps[0] || 'Empresa';
  const availableCompanyPermissions = companyScopedPermissions(permissionCatalog);

  function markActivity() {
    const now = Date.now();
    if (now - lastActivityRef.current < 1000) {
      return;
    }
    lastActivityRef.current = now;
    setLastActivityAt(now);
  }

  useEffect(() => {
    lastActivityRef.current = lastActivityAt;
  }, [lastActivityAt]);

  useEffect(() => {
    if (!session) {
      clearStoredSession();
      return;
    }
    saveStoredSession({ session, companyAccesses, activeCompanyId, rootCompanies, license, lastActivityAt });
  }, [session, companyAccesses, activeCompanyId, rootCompanies, license, lastActivityAt]);

  useEffect(() => {
    if (!session || import.meta.env.MODE === 'test') {
      return undefined;
    }
    let ignore = false;
    loadRuntimeCatalogs({ token, companyId: activeCompanyId })
      .then((catalogs) => {
        if (!ignore) {
          setRuntimeCatalogs(catalogs);
        }
      })
      .catch(() => {
        if (!ignore) {
          setRuntimeCatalogs(emptyRuntimeCatalogs);
        }
      });
    return () => {
      ignore = true;
    };
  }, [session, token, activeCompanyId]);

  useEffect(() => {
    if (!session) {
      return undefined;
    }
    const activityEvents = ['click', 'keydown', 'mousemove', 'scroll', 'touchstart'];
    activityEvents.forEach((eventName) => window.addEventListener(eventName, markActivity, { passive: true }));
    const intervalId = window.setInterval(() => {
      if (Date.now() - lastActivityRef.current >= SESSION_TIMEOUT_MS) {
        closeSessionWithModal('Sesion cerrada por inactividad', 'No se detecto actividad durante 5 minutos. Ingresa de nuevo para continuar.');
      }
    }, 1000);
    return () => {
      activityEvents.forEach((eventName) => window.removeEventListener(eventName, markActivity));
      window.clearInterval(intervalId);
    };
  }, [session]);

  async function execute(action) {
    markActivity();
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
      const now = Date.now();
      lastActivityRef.current = now;
      setLastActivityAt(now);
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

    const now = Date.now();
    lastActivityRef.current = now;
    setLastActivityAt(now);
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
    clearStoredSession();
    setSession(null);
    setCompanyAccesses([]);
    setActiveCompanyId('');
    setLicense(null);
    setOutput(null);
    setError(null);
    setPermissionCatalog([]);
    setCompanyRoles([]);
    setManagedUsers([]);
    setCatalogDefinitions([]);
    setSelectedCatalogCode('');
    setCatalogItems([]);
    setCatalogItemForm(initialCatalogItem);
    setRoleAssignmentForm(initialRoleAssignment);
    setRootCompanies([]);
    setCustomerSearch('');
    setCustomerOptions([]);
    setSelectedCustomer(null);
    setSaleId('');
    setSaleForm(initialSale);
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
      body: buildThirdPartyPayload(thirdPartyForm, companyMunicipalityCode),
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

  const searchCustomers = useCallback(async (identificationNumberPrefix) => {
    requireCompany();
    const normalizedPrefix = String(identificationNumberPrefix || '').trim();
    if (normalizedPrefix.length < 2) {
      setCustomerOptions([]);
      return [];
    }
    const customers = await requestJson(`/api/v1/customers${buildQuery({ active: true, identificationNumberPrefix: normalizedPrefix })}`, context);
    setCustomerOptions(customers || []);
    return customers || [];
  }, [activeCompanyId, context]);

  const selectCustomer = useCallback((customer) => {
    setSelectedCustomer(customer);
    if (customer) {
      setCustomerSearch(customer.identificationNumber || '');
    }
    setSaleForm((current) => ({ ...current, customerId: customer?.id || '' }));
    setCustomerOptions([]);
  }, []);

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

  async function loadCatalogDefinitions() {
    const definitions = await requestJson('/api/v1/catalog-definitions', context);
    setCatalogDefinitions(definitions || []);
    if (!selectedCatalogCode && definitions?.length > 0) {
      setSelectedCatalogCode(definitions[0].code);
    }
    return definitions || [];
  }

  async function loadCatalogItems(catalogCode = selectedCatalogCode) {
    if (!catalogCode) {
      throw new Error('Selecciona un catalogo antes de consultar.');
    }
    const path = isRoot
      ? `/api/v1/catalogs/${catalogCode}/items?includeInactive=true`
      : `/api/v1/company-catalogs/${catalogCode}/items`;
    const items = await requestJson(path, context);
    setCatalogItems(items || []);
    return items || [];
  }

  function startNewCatalogItem() {
    setCatalogItemForm({ ...initialCatalogItem, sourceVersion: '2026-08' });
  }

  function editCatalogItem(item) {
    setCatalogItemForm({
      editingCode: item.code,
      code: item.code,
      label: item.label || '',
      description: item.description || '',
      regulatory: Boolean(item.regulatory),
      source: item.source || 'APP',
      sourceVersion: item.sourceVersion || '2026-08',
      validFrom: item.validFrom || '',
      validTo: item.validTo || '',
      sortOrder: String(item.sortOrder ?? 10),
    });
  }

  function buildCatalogItemPayload() {
    return {
      code: catalogItemForm.code,
      label: catalogItemForm.label,
      description: catalogItemForm.description || null,
      regulatory: catalogItemForm.regulatory,
      source: catalogItemForm.source,
      sourceVersion: catalogItemForm.sourceVersion,
      validFrom: catalogItemForm.validFrom || null,
      validTo: catalogItemForm.validTo || null,
      sortOrder: Number(catalogItemForm.sortOrder || 0),
    };
  }

  async function saveCatalogItem() {
    if (!selectedCatalogCode) {
      throw new Error('Selecciona un catalogo antes de guardar.');
    }
    if (!isRoot) {
      throw new Error('Solo ROOT puede crear o actualizar catalogos globales.');
    }
    const editingCode = catalogItemForm.editingCode;
    const path = editingCode
      ? `/api/v1/catalogs/${selectedCatalogCode}/items/${editingCode}`
      : `/api/v1/catalogs/${selectedCatalogCode}/items`;
    const saved = await requestJson(path, {
      method: editingCode ? 'PUT' : 'POST',
      body: buildCatalogItemPayload(),
      ...context,
      idempotencyKey: createIdempotencyKey('catalog-item'),
    });
    await loadCatalogItems(selectedCatalogCode);
    editCatalogItem(saved);
    return saved;
  }

  async function toggleCatalogItemActive(item) {
    const path = isRoot
      ? `/api/v1/catalogs/${item.catalogCode}/items/${item.code}/activation`
      : `/api/v1/company-catalogs/${item.catalogCode}/items/${item.code}/activation`;
    const nextActive = isRoot ? !item.active : item.enabledForCompany === false;
    const updated = await requestJson(path, {
      method: 'PUT',
      body: { active: nextActive },
      ...context,
      idempotencyKey: createIdempotencyKey('catalog-item-activation'),
    });
    await loadCatalogItems(item.catalogCode);
    return updated;
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
    setCustomerSearch('');
    setCustomerOptions([]);
    setSelectedCustomer(null);
    setSaleId('');
    setSaleForm((current) => ({ ...current, customerId: '' }));
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
            <CompanyForm form={companyForm} setForm={setCompanyForm} companies={rootCompanies} activeCompanyId={activeCompanyId} activeCompany={activeCompany} isRoot={isRoot} onCompanyChange={changeCompany} onSubmit={() => execute(createCompany)} onOpenAdminModal={() => setAdminModalOpen(true)} busy={busy} documentTypeOptions={runtimeCatalogs.dianDocumentTypes} />
          )}
          {currentStep === 'Terceros' && (
            <ThirdPartyForm form={thirdPartyForm} setForm={setThirdPartyForm} companyMunicipalityCode={companyMunicipalityCode} onSubmit={() => execute(createThirdParty)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Terceros)} documentTypeOptionsSource={runtimeCatalogs.dianDocumentTypes} taxResponsibilityOptionsSource={runtimeCatalogs.taxResponsibilityOptions} taxRegimeOptionsSource={runtimeCatalogs.taxRegimeOptions} locations={runtimeCatalogs.locations} />
          )}
          {currentStep === 'Inventario' && (
            <ProductForm form={productForm} setForm={setProductForm} onSubmit={() => execute(createProduct)} busy={busy || !activeCompanyId || !canUse(['INVENTORY_MANAGE'])} />
          )}
          {currentStep === 'Fiscal' && (
            <div className="split">
              <IssuerForm form={issuerForm} setForm={setIssuerForm} activeCompany={activeCompany} onSubmit={() => execute(configureIssuer)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Fiscal)} taxResponsibilityOptionsSource={runtimeCatalogs.taxResponsibilityOptions} locations={runtimeCatalogs.locations} />
              <ResolutionForm form={resolutionForm} setForm={setResolutionForm} onSubmit={() => execute(configureResolution)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Fiscal)} fiscalDocumentTypeOptions={runtimeCatalogs.fiscalDocumentTypeOptions} environmentOptions={runtimeCatalogs.fiscalEnvironmentOptions} />
            </div>
          )}
          {currentStep === 'Venta POS' && (
            <SaleForm form={saleForm} setForm={setSaleForm} saleId={saleId} customerSearch={customerSearch} setCustomerSearch={setCustomerSearch} customerOptions={customerOptions} selectedCustomer={selectedCustomer} onSearchCustomers={searchCustomers} onSelectCustomer={selectCustomer} updateItem={updateSaleItem} addItem={addSaleItem} removeItem={removeSaleItem} onCreate={() => execute(createSale)} onConfirm={() => execute(confirmSale)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules['Venta POS'])} paymentOptions={runtimeCatalogs.paymentMethodOptions} walletOptions={runtimeCatalogs.virtualWalletOptions} />
          )}
          {currentStep === 'Reportes' && (
            <ReportsForm form={reportsForm} setForm={setReportsForm} onSubmit={() => execute(loadReports)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Reportes)} />
          )}
          {currentStep === 'Catalogos' && (
            <CatalogAdminPanel definitions={catalogDefinitions} selectedCatalogCode={selectedCatalogCode} setSelectedCatalogCode={setSelectedCatalogCode} items={catalogItems} form={catalogItemForm} setForm={setCatalogItemForm} onLoadDefinitions={() => execute(loadCatalogDefinitions)} onLoadItems={() => execute(() => loadCatalogItems())} onNew={startNewCatalogItem} onEdit={editCatalogItem} onSave={() => execute(saveCatalogItem)} onToggleActive={(item) => execute(() => toggleCatalogItemActive(item))} busy={busy || !canManageCatalogs} isRoot={isRoot} />
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
