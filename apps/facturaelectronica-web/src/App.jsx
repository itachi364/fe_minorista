import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { createIdempotencyKey, requestJson } from './api/client.js';
import { ActionStatusModal } from './components/ActionStatusModal.jsx';
import { Modal } from './components/Modal.jsx';
import { navigationGroups, steps } from './data/navigation.js';
import {
  createCatalogItemForm,
  createCompanyAdminForm,
  createCompanyForm,
  createCompanyRoleForm,
  createDailyLaborPaymentForm,
  createIssuerForm,
  createLicenseForm,
  createLoginForm,
  createManagedUserForm,
  createPayrollSettingsForm,
  createPayrollWorkerForm,
  createProductForm,
  createReportsForm,
  createResolutionForm,
  createSaleForm,
  createServiceConsumptionState,
  createThirdPartyForm,
} from './utils/formStateFactory.js';
import { LoginPanel } from './features/auth/LoginPanel.jsx';
import { AuditLogPanel } from './features/audit/AuditLogPanel.jsx';
import { CatalogAdminPanel } from './features/catalogs/CatalogAdminPanel.jsx';
import { AdminModal } from './features/company/AdminModal.jsx';
import { CompanyForm } from './features/company/CompanyForm.jsx';
import { CompanySessionPanel } from './features/company/CompanySessionPanel.jsx';
import { IssuerForm } from './features/company/IssuerForm.jsx';
import { ResolutionForm } from './features/fiscal/ResolutionForm.jsx';
import { RolesPanel, UsersPanel } from './features/identity/IdentityAdminPanel.jsx';
import { ProductForm } from './features/inventory/ProductForm.jsx';
import { PayrollPanel } from './features/payroll/PayrollPanel.jsx';
import { LicenseAdminPanel } from './features/licenses/LicenseAdminPanel.jsx';
import { ReportsForm } from './features/reports/ReportsForm.jsx';
import { SaleForm } from './features/sales/SaleForm.jsx';
import { ThirdPartyForm } from './features/thirdparties/ThirdPartyForm.jsx';
import { companyScopedPermissions, hasAnyPermission, hasAnyRole, stepPermissionRules } from './utils/authorization.js';
import { buildIssuerFromCompany } from './utils/company.js';
import {
  buildCompanyAdminPayload,
  buildCompanyPayload,
  buildIssuerPayload,
  buildLicensePayload,
  buildProductPayload,
  buildResolutionPayload,
  buildSalePayload,
  buildThirdPartyPayload,
} from './utils/payloadBuilders.js';
import { buildQuery } from './utils/query.js';
import { emptyRuntimeCatalogs, loadRuntimeCatalogs } from './utils/runtimeCatalogs.js';
import { clearStoredSession, loadStoredSession, saveStoredSession, SESSION_TIMEOUT_MS } from './utils/sessionStorage.js';
import { stepLicenseModules } from './data/licenseModules.js';
export default function App() {
  const [storedSnapshot] = useState(() => loadStoredSession());
  const [selectedStep, setSelectedStep] = useState('Ventas');
  const [loginForm, setLoginForm] = useState(createLoginForm);
  const [session, setSession] = useState(storedSnapshot?.session || null);
  const [companyAccesses, setCompanyAccesses] = useState(storedSnapshot?.companyAccesses || []);
  const [activeCompanyId, setActiveCompanyId] = useState(storedSnapshot?.activeCompanyId || '');
  const [rootCompanies, setRootCompanies] = useState(storedSnapshot?.rootCompanies || []);
  const [license, setLicense] = useState(storedSnapshot?.license || null);
  const [licenseForm, setLicenseForm] = useState(createLicenseForm);
  const [managedLicense, setManagedLicense] = useState(null);
  const [runtimeCatalogs, setRuntimeCatalogs] = useState(() => globalThis.__FACTURA_RUNTIME_CATALOGS__ || emptyRuntimeCatalogs);
  const [lastActivityAt, setLastActivityAt] = useState(storedSnapshot?.lastActivityAt || Date.now());
  const lastActivityRef = useRef(lastActivityAt);
  const [licenseModal, setLicenseModal] = useState(null);
  const [companyForm, setCompanyForm] = useState(createCompanyForm);
  const [companyAdminForm, setCompanyAdminForm] = useState(createCompanyAdminForm);
  const [managedUserForm, setManagedUserForm] = useState(createManagedUserForm);
  const [companyRoleForm, setCompanyRoleForm] = useState(createCompanyRoleForm);
  const [editingUserId, setEditingUserId] = useState('');
  const [editingRoleId, setEditingRoleId] = useState('');
  const [permissionCatalog, setPermissionCatalog] = useState([]);
  const [companyRoles, setCompanyRoles] = useState([]);
  const [managedUsers, setManagedUsers] = useState([]);
  const [catalogDefinitions, setCatalogDefinitions] = useState([]);
  const [selectedCatalogCode, setSelectedCatalogCode] = useState('');
  const [catalogItems, setCatalogItems] = useState([]);
  const [catalogItemForm, setCatalogItemForm] = useState(createCatalogItemForm);
  const [adminModalOpen, setAdminModalOpen] = useState(false);
  const [thirdPartyForm, setThirdPartyForm] = useState(createThirdPartyForm);
  const [productForm, setProductForm] = useState(createProductForm);
  const [issuerForm, setIssuerForm] = useState(createIssuerForm);
  const [resolutionForm, setResolutionForm] = useState(createResolutionForm);
  const [saleForm, setSaleForm] = useState(createSaleForm);
  const [serviceConsumption, setServiceConsumption] = useState(createServiceConsumptionState);
  const [customerSearch, setCustomerSearch] = useState('');
  const [customerOptions, setCustomerOptions] = useState([]);
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [reportsForm, setReportsForm] = useState(createReportsForm);
  const [reportsData, setReportsData] = useState(null);
  const [payrollSettingsForm, setPayrollSettingsForm] = useState(createPayrollSettingsForm);
  const [payrollWorkerForm, setPayrollWorkerForm] = useState(createPayrollWorkerForm);
  const [dailyLaborPaymentForm, setDailyLaborPaymentForm] = useState(createDailyLaborPaymentForm);
  const [payrollWorkers, setPayrollWorkers] = useState([]);
  const [dailyLaborPayments, setDailyLaborPayments] = useState([]);
  const [electronicPayrollDocuments, setElectronicPayrollDocuments] = useState([]);
  const [auditFilters, setAuditFilters] = useState(todayAuditFilters);
  const [auditResourceTypes, setAuditResourceTypes] = useState([]);
  const [auditEvents, setAuditEvents] = useState([]);
  const [saleId, setSaleId] = useState('');
  const [busy, setBusy] = useState(false);
  const [actionStatus, setActionStatus] = useState({ status: 'idle' });
  const autoAuditLoadKeyRef = useRef('');
  const autoIdentityLoadKeyRef = useRef('');

  const token = session?.accessToken || '';
  const context = useMemo(() => ({ token, companyId: activeCompanyId, userId: session?.userId }), [token, activeCompanyId, session?.userId]);
  const activeAccess = companyAccesses.find((access) => access.companyId === activeCompanyId);
  const activeCompany = rootCompanies.find((company) => company.id === activeCompanyId || company.companyId === activeCompanyId);
  const companyMunicipalityCode = activeCompany?.municipalityCode || issuerForm.municipalityCode;
  const isRoot = session?.globalRoles?.includes('ROOT') || false;
  const isCompanyAdmin = hasAnyRole(activeAccess, ['OWNER', 'ADMIN']);
  const canUse = (permissions) => isRoot || isCompanyAdmin || hasAnyPermission(activeAccess, permissions);
  const canManageUsers = canUse(stepPermissionRules.Usuarios);
  const canManageRoles = canUse(stepPermissionRules.Roles);
  const canManageCatalogs = canUse(stepPermissionRules.Catalogos);
  const canViewAudit = isRoot || isCompanyAdmin || hasAnyPermission(activeAccess, stepPermissionRules.Logs);
  const licensedModules = new Set(license?.enabledModules || []);
  const licenseAllowsStep = (step) => {
    if (isRoot || step === 'Licencias') {
      return isRoot;
    }
    const moduleCode = stepLicenseModules[step];
    return !moduleCode || licensedModules.has(moduleCode);
  };
  const visibleSteps = steps.filter((step) => licenseAllowsStep(step) && (isRoot || isCompanyAdmin || hasAnyPermission(activeAccess, stepPermissionRules[step] || [])));
  const currentStep = visibleSteps.includes(selectedStep) ? selectedStep : visibleSteps[0] || 'Ventas';
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
    loadRuntimeCatalogs({ token, companyId: activeCompanyId, userId: session.userId })
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
    if (!session || isRoot || !activeCompanyId || activeCompany) {
      return;
    }
    requestJson(`/api/v1/companies/${activeCompanyId}`, { token, companyId: activeCompanyId })
      .then((company) => {
        storeKnownCompany(company);
        hydrateCompanyForm(company);
      })
      .catch(() => undefined);
  }, [session, isRoot, activeCompanyId, activeCompany, token]);

  useEffect(() => {
    if (currentStep !== 'Logs' || !activeCompanyId || !canViewAudit) {
      return;
    }
    const key = `${activeCompanyId}|${auditFilters.resourceType}|${auditFilters.from}|${auditFilters.to}`;
    if (autoAuditLoadKeyRef.current === key) {
      return;
    }
    autoAuditLoadKeyRef.current = key;
    loadAuditResourceTypes().catch(() => undefined);
    loadAuditEvents().catch(() => undefined);
  }, [currentStep, activeCompanyId, canViewAudit, auditFilters.resourceType, auditFilters.from, auditFilters.to]);

  useEffect(() => {
    if (!activeCompanyId) {
      return;
    }
    if (currentStep === 'Roles' && canManageRoles) {
      const key = `roles|${activeCompanyId}`;
      if (autoIdentityLoadKeyRef.current === key) {
        return;
      }
      autoIdentityLoadKeyRef.current = key;
      loadIdentityAdminData().catch(() => undefined);
    }
    if (currentStep === 'Usuarios' && canManageUsers) {
      const key = `users|${activeCompanyId}`;
      if (autoIdentityLoadKeyRef.current === key) {
        return;
      }
      autoIdentityLoadKeyRef.current = key;
      const loaders = [loadCompanyUsers('')];
      if (permissionCatalog.length === 0 || companyRoles.length === 0) {
        loaders.unshift(loadIdentityAdminData());
      }
      Promise.all(loaders).catch(() => undefined);
    }
  }, [currentStep, activeCompanyId, canManageRoles, canManageUsers, permissionCatalog.length, companyRoles.length]);

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

  async function execute(action, options = {}) {
    markActivity();
    setBusy(true);
    setActionStatus({ status: 'running', message: 'Procesando solicitud...' });
    try {
      const result = await action();
      if (result === null && options.silentNullSuccess) {
        setActionStatus({ status: 'idle' });
        return null;
      }
      const successMessage = options.successMessage || 'La accion se realizo correctamente.';
      setActionStatus({ status: 'success', message: successMessage, autoClose: true });
      return result;
    } catch (caught) {
      const payload = caught.status === 403
        ? { status: 403, message: 'No tienes permisos para ejecutar esta accion.' }
        : caught.payload || { status: caught.status, message: caught.message };
      const message = isLicenseNotConfiguredError(caught)
        ? 'La empresa no tiene una licencia configurada. Contacta al administrador de la plataforma.'
        : caught.status === 401 && options.authAction
        ? 'Credenciales invalidas. Verifica el correo y la contrasena.'
        : caught.status && caught.status >= 500
          ? 'Fallo interno en la aplicacion. Intenta nuevamente o revisa los logs.'
          : 'Hay un error al realizar la accion. Revisa Logs/Auditoria para mas detalle.';
      setActionStatus({
        status: 'error',
        message,
        correlationId: payload?.correlationId,
      });
      return null;
    } finally {
      setBusy(false);
    }
  }

  function isLicenseNotConfiguredError(error) {
    const message = String(error?.payload?.message || error?.message || '').toLowerCase();
    return error?.status === 404 && message.includes('licencia') && message.includes('no existe');
  }

  async function loadRootCompanies(tokenValue = token) {
    const companies = await requestJson('/api/v1/companies', { token: tokenValue });
    setRootCompanies(companies || []);
    return companies || [];
  }

  function storeKnownCompany(company) {
    if (!company?.id) {
      return;
    }
    setRootCompanies((current) => [company, ...current.filter((item) => item.id !== company.id && item.companyId !== company.id)]);
  }

  function hydrateCompanyForm(company) {
    if (!company) {
      return;
    }
    setCompanyForm({
      legalName: company.legalName || '',
      tradeName: company.tradeName || '',
      identificationTypeCode: company.identificationTypeCode || '',
      identificationNumber: company.identificationNumber || '',
      verificationDigit: company.verificationDigit || '',
      email: company.email || '',
    });
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
      setLicenseForm((current) => ({ ...current, companyId: firstCompany?.id || '' }));
      hydrateCompanyForm(firstCompany);
      setIssuerForm((current) => buildIssuerFromCompany(firstCompany, current));
      setLicense(null);
      setSelectedStep('Ventas');
      setLicenseModal(null);
      return { login: loginResult, companies, scope: 'ROOT' };
    }
    const accesses = await requestJson('/api/v1/me/companies', { token: tokenValue });
    const nextCompanyId = accesses[0]?.companyId || '';

    if (!nextCompanyId) {
      closeSessionWithModal('Sin empresa asociada', 'Tu usuario no tiene una empresa habilitada para operar. Contacta al administrador de la licencia.');
      return null;
    }

    let validation;
    try {
      validation = await requestJson(
        `/api/v1/companies/${nextCompanyId}/license/validation?action=CREATE_TRANSACTION&module=COMPANY`,
        { token: tokenValue, companyId: nextCompanyId },
      );
    } catch (error) {
      if (isLicenseNotConfiguredError(error)) {
        closeSessionWithModal('Licencia no configurada', 'La empresa no tiene una licencia configurada. Solicita a ROOT asignar una licencia antes de ingresar.');
        return null;
      }
      throw error;
    }

    if (!validation?.allowed) {
      closeSessionWithModal('Licencia no activa', validation?.message || 'La licencia de la empresa no esta activa. La sesion se cerro automaticamente.');
      return null;
    }
    const currentLicense = await requestJson(`/api/v1/companies/${nextCompanyId}/license`, { token: tokenValue, companyId: nextCompanyId });
    const activeCompanyResult = await requestJson(`/api/v1/companies/${nextCompanyId}`, { token: tokenValue, companyId: nextCompanyId });

    const now = Date.now();
    lastActivityRef.current = now;
    setLastActivityAt(now);
    setSession(loginResult);
    setCompanyAccesses(accesses);
    setActiveCompanyId(nextCompanyId);
    storeKnownCompany(activeCompanyResult);
    hydrateCompanyForm(activeCompanyResult);
    setIssuerForm((current) => buildIssuerFromCompany(activeCompanyResult, current));
    setLicense({ ...currentLicense, validation });
    setLicenseModal(null);
    setSelectedStep('Ventas');
    return { login: loginResult, companies: accesses, license: currentLicense };
  }

  async function loadLicense(companyId = activeCompanyId, tokenValue = token) {
    if (!companyId) {
      return null;
    }
    let validation;
    try {
      validation = await requestJson(
        `/api/v1/companies/${companyId}/license/validation?action=CREATE_TRANSACTION&module=COMPANY`,
        { token: tokenValue, companyId },
      );
    } catch (error) {
      if (isLicenseNotConfiguredError(error)) {
        closeSessionWithModal('Licencia no configurada', 'La empresa no tiene una licencia configurada. Solicita a ROOT asignar una licencia antes de ingresar.');
        return null;
      }
      throw error;
    }
    if (!validation?.allowed) {
      closeSessionWithModal('Licencia no activa', validation?.message || 'La licencia de la empresa no esta activa. La sesion se cerro automaticamente.');
      return null;
    }
    const currentLicense = await requestJson(`/api/v1/companies/${companyId}/license`, { token: tokenValue, companyId });
    setLicense({ ...currentLicense, validation });
    setLicenseModal(null);
    return currentLicense;
  }

  function clearSession() {
    clearStoredSession();
    setSession(null);
    setCompanyAccesses([]);
    setActiveCompanyId('');
    setLicense(null);
    setLicenseForm(createLicenseForm());
    setManagedLicense(null);
    setPermissionCatalog([]);
    setCompanyRoles([]);
    setManagedUsers([]);
    setCatalogDefinitions([]);
    setSelectedCatalogCode('');
    setCatalogItems([]);
    setCatalogItemForm(createCatalogItemForm());
    setManagedUserForm(createManagedUserForm());
    setCompanyRoleForm(createCompanyRoleForm());
    setEditingUserId('');
    setEditingRoleId('');
    setRootCompanies([]);
    setCustomerSearch('');
    setCustomerOptions([]);
    setSelectedCustomer(null);
    setAuditEvents([]);
    setAuditResourceTypes([]);
    setAuditFilters(todayAuditFilters());
    autoAuditLoadKeyRef.current = '';
    setPayrollSettingsForm(createPayrollSettingsForm());
    setPayrollWorkerForm(createPayrollWorkerForm());
    setDailyLaborPaymentForm(createDailyLaborPaymentForm());
    setPayrollWorkers([]);
    setDailyLaborPayments([]);
    setElectronicPayrollDocuments([]);
    setSaleId('');
    setSaleForm(createSaleForm());
    setAdminModalOpen(false);
    autoIdentityLoadKeyRef.current = '';
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
      ...context,
      idempotencyKey: createIdempotencyKey('company'),
    });
    if (isRoot && created?.id) {
      setRootCompanies((current) => [created, ...current.filter((company) => company.id !== created.id)]);
      setActiveCompanyId(created.id);
      hydrateCompanyForm(created);
      setLicenseForm((current) => ({ ...current, companyId: created.id }));
      setManagedLicense(null);
      setCompanyAccesses([{ companyId: created.id, roles: ['ROOT'], permissions: ['GLOBAL_COMPANIES_MANAGE'] }]);
      setIssuerForm((current) => buildIssuerFromCompany(created, current));
    }
    return created;
  }

  async function updateCompany() {
    requireCompany();
    const updated = await requestJson(`/api/v1/companies/${activeCompanyId}`, {
      method: 'PUT',
      body: buildCompanyPayload(companyForm),
      ...context,
      idempotencyKey: createIdempotencyKey('company-update'),
    });
    storeKnownCompany(updated);
    hydrateCompanyForm(updated);
    setIssuerForm((current) => buildIssuerFromCompany(updated, current));
    return updated;
  }

  async function activateCompany() {
    requireCompany();
    const updated = await requestJson(`/api/v1/companies/${activeCompanyId}/activate`, {
      method: 'PUT',
      ...context,
      idempotencyKey: createIdempotencyKey('company-activate'),
    });
    storeKnownCompany(updated);
    hydrateCompanyForm(updated);
    return updated;
  }

  async function suspendCompany() {
    requireCompany();
    const updated = await requestJson(`/api/v1/companies/${activeCompanyId}/suspend`, {
      method: 'PUT',
      ...context,
      idempotencyKey: createIdempotencyKey('company-suspend'),
    });
    storeKnownCompany(updated);
    hydrateCompanyForm(updated);
    return updated;
  }

  function selectLicenseCompany(companyId) {
    setLicenseForm((current) => ({ ...current, companyId }));
    setManagedLicense(null);
  }

  function hydrateLicenseForm(licenseResult) {
    if (!licenseResult) {
      return;
    }
    setLicenseForm({
      companyId: licenseResult.companyId || licenseForm.companyId,
      planCode: licenseResult.planCode || 'CUSTOM',
      validFrom: licenseResult.validFrom || '',
      validTo: licenseResult.validTo || '',
      maxUsers: licenseResult.maxUsers ?? '',
      maxMonthlyDocuments: licenseResult.maxMonthlyDocuments ?? '',
      enabledModules: licenseResult.enabledModules || [],
    });
  }

  async function loadManagedLicense() {
    const companyId = licenseForm.companyId || activeCompanyId;
    if (!companyId) {
      throw new Error('Selecciona una empresa para cargar la licencia.');
    }
    const result = await requestJson(`/api/v1/companies/${companyId}/license`, { token, companyId });
    setManagedLicense(result);
    hydrateLicenseForm(result);
    return result;
  }

  async function saveManagedLicense() {
    const companyId = licenseForm.companyId || activeCompanyId;
    if (!companyId) {
      throw new Error('Selecciona una empresa para guardar la licencia.');
    }
    const result = await requestJson(`/api/v1/companies/${companyId}/license`, {
      method: 'POST',
      body: buildLicensePayload(licenseForm),
      token,
      companyId,
      idempotencyKey: createIdempotencyKey('company-license'),
    });
    setManagedLicense(result);
    hydrateLicenseForm(result);
    if (companyId === activeCompanyId) {
      setLicense(result);
    }
    return result;
  }

  async function activateManagedLicense() {
    const companyId = licenseForm.companyId || activeCompanyId;
    if (!companyId) {
      throw new Error('Selecciona una empresa para activar la licencia.');
    }
    const result = await requestJson(`/api/v1/companies/${companyId}/license/activate`, {
      method: 'PUT',
      token,
      companyId,
      idempotencyKey: createIdempotencyKey('company-license-activate'),
    });
    setManagedLicense(result);
    hydrateLicenseForm(result);
    return result;
  }

  async function suspendManagedLicense() {
    const companyId = licenseForm.companyId || activeCompanyId;
    if (!companyId) {
      throw new Error('Selecciona una empresa para suspender la licencia.');
    }
    const result = await requestJson(`/api/v1/companies/${companyId}/license/suspend`, {
      method: 'PUT',
      token,
      companyId,
      idempotencyKey: createIdempotencyKey('company-license-suspend'),
    });
    setManagedLicense(result);
    hydrateLicenseForm(result);
    return result;
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

  async function loadCompanyUsers(email = '') {
    requireCompany();
    const users = await requestJson(`/api/v1/companies/${activeCompanyId}/users${buildQuery({ email })}`, {
      token,
      companyId: activeCompanyId,
    });
    setManagedUsers(users || []);
    return users || [];
  }
  function startNewCompanyRole() {
    setEditingRoleId('');
    setCompanyRoleForm(createCompanyRoleForm());
  }

  function editCompanyRole(role) {
    setEditingRoleId(role.id);
    setCompanyRoleForm({
      name: role.name || '',
      description: role.description || '',
      permissionCodes: role.permissionCodes || [],
    });
  }

  async function saveCompanyRole() {
    requireCompany();
    const saved = await requestJson(editingRoleId
      ? `/api/v1/companies/${activeCompanyId}/roles/${editingRoleId}`
      : `/api/v1/companies/${activeCompanyId}/roles`, {
      method: editingRoleId ? 'PUT' : 'POST',
      body: companyRoleForm,
      token,
      companyId: activeCompanyId,
      idempotencyKey: createIdempotencyKey('company-role'),
    });
    setCompanyRoles((current) => [saved, ...current.filter((role) => role.id !== saved.id)]);
    setEditingRoleId(saved.id);
    return saved;
  }

  async function toggleCompanyRoleActive(role) {
    requireCompany();
    const action = role.active === false ? 'activate' : 'deactivate';
    const updated = await requestJson(`/api/v1/companies/${activeCompanyId}/roles/${role.id}/${action}`, {
      method: 'PUT',
      token,
      companyId: activeCompanyId,
      idempotencyKey: createIdempotencyKey(`company-role-${action}`),
    });
    setCompanyRoles((current) => [updated, ...current.filter((currentRole) => currentRole.id !== updated.id)]);
    return updated;
  }

  function startNewManagedUser() {
    setEditingUserId('');
    setManagedUserForm(createManagedUserForm());
  }

  function editManagedUser(user) {
    setEditingUserId(user.id);
    setManagedUserForm({
      fullName: user.fullName || '',
      email: user.email || '',
      password: '',
      roleId: '',
    });
  }

  async function assignRoleToUser(userId, roleId) {
    return requestJson(`/api/v1/companies/${activeCompanyId}/users/${userId}/role-assignments`, {
      method: 'POST',
      body: { roleIds: [roleId] },
      token,
      companyId: activeCompanyId,
      idempotencyKey: createIdempotencyKey('role-assignment'),
    });
  }

  async function saveManagedUser() {
    requireCompany();
    if (!managedUserForm.roleId) {
      throw new Error('Selecciona un rol obligatorio para el usuario.');
    }
    const user = editingUserId
      ? await requestJson(`/api/v1/companies/${activeCompanyId}/users/${editingUserId}`, {
        method: 'PUT',
        body: { fullName: managedUserForm.fullName, email: managedUserForm.email },
        token,
        companyId: activeCompanyId,
        idempotencyKey: createIdempotencyKey('managed-user-update'),
      })
      : await requestJson('/api/v1/users', {
        method: 'POST',
        body: {
          fullName: managedUserForm.fullName,
          email: managedUserForm.email,
          password: managedUserForm.password,
        },
        token,
        idempotencyKey: createIdempotencyKey('managed-user'),
      });
    await assignRoleToUser(user.id, managedUserForm.roleId);
    const users = await loadCompanyUsers('');
    setManagedUsers(users);
    setEditingUserId(user.id);
    return user;
  }

  async function toggleManagedUserActive(user) {
    requireCompany();
    const action = user.status === 'INACTIVE' ? 'activate' : 'deactivate';
    const updated = await requestJson(`/api/v1/companies/${activeCompanyId}/users/${user.id}/${action}`, {
      method: 'PUT',
      token,
      companyId: activeCompanyId,
      idempotencyKey: createIdempotencyKey(`managed-user-${action}`),
    });
    setManagedUsers((current) => [updated, ...current.filter((currentUser) => currentUser.id !== updated.id)]);
    return updated;
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
      updateSaleItem(0, 'productName', result.name || '');
      updateSaleItem(0, 'itemType', result.itemType || '');
      updateSaleItem(0, 'unitPrice', String(result.salePrice || productForm.salePrice));
    }
    return result;
  }

  async function scanSaleBarcode(barcode) {
    requireCompany();
    const product = await requestJson(`/api/v1/products/by-barcode/${encodeURIComponent(barcode)}`, context);
    if (!product?.id) {
      throw new Error('No se encontro un producto activo con ese codigo de barras.');
    }
    setSaleForm((current) => {
      const existingIndex = current.items.findIndex((item) => item.productId === product.id);
      if (existingIndex >= 0) {
        return {
          ...current,
          items: current.items.map((item, index) => index === existingIndex
            ? { ...item, quantity: String(Number(item.quantity || 0) + 1) }
            : item),
        };
      }
      const nextLine = {
        productId: product.id,
        productName: product.name || product.sku || barcode,
        itemType: product.itemType || '',
        quantity: '1',
        unitPrice: String(product.salePrice ?? '0'),
        discountAmount: '0',
        barcode: product.barcode || barcode,
      };
      const hasEmptyFirstLine = current.items.length === 1 && !current.items[0].productId;
      return {
        ...current,
        items: hasEmptyFirstLine ? [nextLine] : [...current.items, nextLine],
      };
    });
    return product;
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
    setSaleForm((current) => ({
      ...current,
      buyerIdentificationMode: customer ? 'IDENTIFIED_CUSTOMER' : current.buyerIdentificationMode,
      customerId: customer?.id || '',
    }));
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

  async function loadServiceConsumptionSuggestions(serviceProductId) {
    requireCompany();
    if (!saleId) {
      throw new Error('Primero crea la venta para asociar el consumo de insumos.');
    }
    const suggestions = await requestJson(
      `/api/v1/products/${serviceProductId}/supply-consumption-suggestions`,
      context,
    );
    setServiceConsumption({
      serviceProductId,
      sourceDocumentId: saleId,
      reason: serviceConsumption.reason || 'Consumo real de insumos por servicio facturado',
      suggestions: suggestions || [],
      quantities: Object.fromEntries((suggestions || []).map((suggestion) => [suggestion.supplyProductId, ''])),
    });
    return suggestions;
  }

  function updateServiceConsumptionQuantity(supplyProductId, quantity) {
    setServiceConsumption((current) => ({
      ...current,
      quantities: { ...current.quantities, [supplyProductId]: quantity },
    }));
  }

  function updateServiceConsumptionReason(reason) {
    setServiceConsumption((current) => ({ ...current, reason }));
  }

  async function confirmServiceSupplyConsumption() {
    requireCompany();
    const lines = serviceConsumption.suggestions
      .map((suggestion) => ({
        supplyProductId: suggestion.supplyProductId,
        quantity: serviceConsumption.quantities[suggestion.supplyProductId],
      }))
      .filter((line) => Number(line.quantity) > 0);
    if (lines.length === 0) {
      throw new Error('Ingresa al menos una cantidad consumida.');
    }
    return requestJson('/api/v1/service-supply-consumptions', {
      method: 'POST',
      body: {
        serviceProductId: serviceConsumption.serviceProductId,
        sourceDocumentId: serviceConsumption.sourceDocumentId || saleId,
        reason: serviceConsumption.reason,
        lines,
      },
      ...context,
      idempotencyKey: createIdempotencyKey('service-supply-consumption'),
    });
  }

  async function loadReports() {
    requireCompany();
    const [sales, stock, journal, ledger, expenses, accountsReceivable, accountsPayable] = await Promise.all([
      requestJson(`/api/v1/reports/sales${buildQuery({ status: reportsForm.status, from: reportsForm.from, to: reportsForm.to })}`, context),
      requestJson(`/api/v1/reports/inventory-stock${buildQuery({ active: true })}`, context),
      requestJson(`/api/v1/reports/journal${buildQuery({ from: reportsForm.from, to: reportsForm.to })}`, context),
      requestJson(`/api/v1/reports/ledger${buildQuery({ from: reportsForm.from, to: reportsForm.to, accountCode: reportsForm.accountCode })}`, context),
      requestJson(`/api/v1/reports/expenses${buildQuery({ status: reportsForm.status, from: reportsForm.from, to: reportsForm.to })}`, context),
      requestJson(`/api/v1/reports/accounts-receivable${buildQuery({ status: reportsForm.status, from: reportsForm.from, to: reportsForm.to })}`, context),
      requestJson(`/api/v1/accounts-payable${buildQuery({ status: reportsForm.status, from: reportsForm.from, to: reportsForm.to })}`, context),
    ]);
    const result = { sales, stock, journal, ledger, expenses, accountsReceivable, accountsPayable };
    setReportsData(result);
    return result;
  }

  async function initializeAccountingSetup() {
    requireCompany();
    return requestJson('/api/v1/accounting-setup/basic', {
      method: 'POST',
      ...context,
      idempotencyKey: createIdempotencyKey('accounting-setup'),
    });
  }

  async function loadPayrollData() {
    requireCompany();
    const [settings, workers, payments, documents] = await Promise.all([
      requestJson('/api/v1/payroll/settings', context),
      requestJson('/api/v1/payroll/workers', context),
      requestJson('/api/v1/payroll/daily-payments', context),
      requestJson('/api/v1/payroll/electronic-documents', context),
    ]);
    setPayrollSettingsForm({
      electronicPayrollEnabled: Boolean(settings?.electronicPayrollEnabled),
      providerMode: settings?.providerMode || 'MOCK',
    });
    setPayrollWorkers(workers || []);
    setDailyLaborPayments(payments || []);
    setElectronicPayrollDocuments(documents || []);
    return { settings, workers, payments, documents };
  }

  async function savePayrollSettings() {
    requireCompany();
    const settings = await requestJson('/api/v1/payroll/settings', {
      ...context,
      method: 'PUT',
      body: payrollSettingsForm,
    });
    setPayrollSettingsForm({
      electronicPayrollEnabled: Boolean(settings?.electronicPayrollEnabled),
      providerMode: settings?.providerMode || 'MOCK',
    });
    return settings;
  }

  async function createPayrollWorker() {
    requireCompany();
    const worker = await requestJson('/api/v1/payroll/workers', {
      ...context,
      method: 'POST',
      body: {
        ...payrollWorkerForm,
        identificationTypeCode: Number(payrollWorkerForm.identificationTypeCode),
        verificationDigit: payrollWorkerForm.verificationDigit === '' ? null : Number(payrollWorkerForm.verificationDigit),
      },
    });
    setPayrollWorkers((current) => [...current, worker]);
    setPayrollWorkerForm(createPayrollWorkerForm());
    return worker;
  }

  async function createDailyLaborPayment() {
    requireCompany();
    const payment = await requestJson('/api/v1/payroll/daily-payments', {
      ...context,
      method: 'POST',
      body: {
        ...dailyLaborPaymentForm,
        agreedAmount: Number(dailyLaborPaymentForm.agreedAmount),
        paidAmount: Number(dailyLaborPaymentForm.paidAmount),
      },
    });
    setDailyLaborPayments((current) => [payment, ...current]);
    setDailyLaborPaymentForm(createDailyLaborPaymentForm());
    return payment;
  }

  async function issueElectronicPayrollDocument(paymentId) {
    requireCompany();
    const document = await requestJson('/api/v1/payroll/electronic-documents', {
      ...context,
      method: 'POST',
      body: { dailyLaborPaymentId: paymentId },
    });
    setElectronicPayrollDocuments((current) => [document, ...current]);
    return document;
  }

  async function loadAuditEvents() {
    requireCompany();
    const events = await requestJson(`/api/v1/audit-events${buildQuery({
      resourceType: auditFilters.resourceType,
      from: toInstantQuery(auditFilters.from),
      to: toInstantQuery(auditFilters.to),
    })}`, context);
    setAuditEvents(events || []);
    return events || [];
  }

  async function loadAuditResourceTypes() {
    requireCompany();
    const resourceTypes = await requestJson('/api/v1/audit-events/resource-types', context);
    setAuditResourceTypes(resourceTypes || []);
    return resourceTypes || [];
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
    setCatalogItemForm({ ...createCatalogItemForm(), sourceVersion: '2026-08' });
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
      items: [...current.items, { productId: '', productName: '', itemType: '', quantity: '1', unitPrice: '0', discountAmount: '0' }],
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
    setLicenseForm((current) => ({ ...current, companyId }));
    setManagedLicense(null);
    setCustomerSearch('');
    setCustomerOptions([]);
    setSelectedCustomer(null);
    setSaleId('');
    setServiceConsumption(createServiceConsumptionState());
    setSaleForm((current) => ({ ...current, customerId: '' }));
    const selectedCompany = rootCompanies.find((company) => company.id === companyId || company.companyId === companyId);
    if (selectedCompany) {
      hydrateCompanyForm(selectedCompany);
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
          <LoginPanel form={loginForm} setForm={setLoginForm} busy={busy} onLogin={() => execute(login, { authAction: true, silentNullSuccess: true, successMessage: 'Sesion iniciada correctamente.' })} />
        </section>
        {licenseModal && <Modal title={licenseModal.title} message={licenseModal.message} onClose={() => setLicenseModal(null)} />}
        <ActionStatusModal state={actionStatus} onClose={() => setActionStatus({ status: 'idle' })} />
      </main>
    );
  }

  return (
    <main className="app-shell">
      <aside className="sidebar">
        <div className="brand">Factura Electronica</div>
        <nav aria-label="Flujo principal">
          {navigationGroups
            .map((group) => ({ ...group, items: group.items.filter((item) => visibleSteps.includes(item)) }))
            .filter((group) => group.items.length > 0)
            .map((group) => (
              <div className="nav-group" key={group.label}>
                {group.items.length === 1 && group.label === group.items[0] ? (
                  <button className={currentStep === group.items[0] ? 'nav-item active' : 'nav-item'} onClick={() => setSelectedStep(group.items[0])} type="button">
                    {group.label}
                  </button>
                ) : (
                  <>
                    <span className="nav-group-label">{group.label}</span>
                    {group.items.map((step) => (
                      <button key={step} className={currentStep === step ? 'nav-item child active' : 'nav-item child'} onClick={() => setSelectedStep(step)} type="button">
                        {step}
                      </button>
                    ))}
                  </>
                )}
              </div>
            ))}
        </nav>
      </aside>

      <section className="workspace">
        <header className="topbar sessionbar">
          <CompanySessionPanel accesses={companyAccesses} companies={rootCompanies} activeCompanyId={activeCompanyId} activeCompany={activeCompany} activeAccess={activeAccess} license={license} session={session} isRoot={isRoot} onCompanyChange={changeCompany} onLogout={logout} busy={busy} />
        </header>

        <section className="panel-grid">
          {currentStep === 'Empresa' && (
            <CompanyForm form={companyForm} setForm={setCompanyForm} companies={rootCompanies} activeCompanyId={activeCompanyId} activeCompany={activeCompany} isRoot={isRoot} onCompanyChange={changeCompany} onSubmit={() => execute(isRoot ? createCompany : updateCompany)} onUpdate={() => execute(updateCompany)} onActivate={() => execute(activateCompany)} onSuspend={() => execute(suspendCompany)} onOpenAdminModal={() => {
              setActionStatus({ status: 'idle' });
              setAdminModalOpen(true);
            }} busy={busy} documentTypeOptions={runtimeCatalogs.dianDocumentTypes} />
          )}
          {currentStep === 'Licencias' && (
            <LicenseAdminPanel form={licenseForm} setForm={setLicenseForm} companies={rootCompanies} license={managedLicense} onCompanyChange={selectLicenseCompany} onLoad={() => execute(loadManagedLicense)} onSave={() => execute(saveManagedLicense)} onActivate={() => execute(activateManagedLicense)} onSuspend={() => execute(suspendManagedLicense)} busy={busy || !isRoot} />
          )}
          {currentStep === 'Terceros' && (
            <ThirdPartyForm form={thirdPartyForm} setForm={setThirdPartyForm} companyMunicipalityCode={companyMunicipalityCode} onSubmit={() => execute(createThirdParty)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Terceros)} documentTypeOptionsSource={runtimeCatalogs.dianDocumentTypes} taxResponsibilityOptionsSource={runtimeCatalogs.taxResponsibilityOptions} taxRegimeOptionsSource={runtimeCatalogs.taxRegimeOptions} thirdPartyRoleCatalog={runtimeCatalogs.thirdPartyRoleCatalog} personTypeCatalog={runtimeCatalogs.personTypeCatalog} locations={runtimeCatalogs.locations} />
          )}
          {currentStep === 'Inventario' && (
            <ProductForm form={productForm} setForm={setProductForm} onSubmit={() => execute(createProduct)} busy={busy || !activeCompanyId || !canUse(['INVENTORY_MANAGE'])} taxOptions={runtimeCatalogs.salesTaxOptions} itemTypeCatalog={runtimeCatalogs.itemTypeCatalog} />
          )}
          {currentStep === 'Fiscal' && (
            <div className="split">
              <IssuerForm form={issuerForm} setForm={setIssuerForm} activeCompany={activeCompany} onSubmit={() => execute(configureIssuer)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Fiscal)} taxResponsibilityOptionsSource={runtimeCatalogs.taxResponsibilityOptions} locations={runtimeCatalogs.locations} />
              <ResolutionForm form={resolutionForm} setForm={setResolutionForm} onSubmit={() => execute(configureResolution)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Fiscal)} fiscalDocumentTypeOptions={runtimeCatalogs.fiscalDocumentTypeOptions} environmentOptions={runtimeCatalogs.fiscalEnvironmentOptions} />
            </div>
          )}
          {currentStep === 'Ventas' && (
            <SaleForm form={saleForm} setForm={setSaleForm} saleId={saleId} customerSearch={customerSearch} setCustomerSearch={setCustomerSearch} customerOptions={customerOptions} selectedCustomer={selectedCustomer} onSearchCustomers={searchCustomers} onSelectCustomer={selectCustomer} updateItem={updateSaleItem} addItem={addSaleItem} removeItem={removeSaleItem} onScanBarcode={(barcode) => execute(() => scanSaleBarcode(barcode))} onCreate={() => execute(createSale)} onConfirm={() => execute(confirmSale)} serviceConsumption={serviceConsumption} onLoadServiceConsumption={(serviceProductId) => execute(() => loadServiceConsumptionSuggestions(serviceProductId))} onUpdateServiceConsumptionQuantity={updateServiceConsumptionQuantity} onUpdateServiceConsumptionReason={updateServiceConsumptionReason} onConfirmServiceConsumption={() => execute(confirmServiceSupplyConsumption)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Ventas)} paymentOptions={runtimeCatalogs.paymentMethodOptions} walletOptions={runtimeCatalogs.virtualWalletOptions} />
          )}
          {currentStep === 'Nomina' && (
            <PayrollPanel settingsForm={payrollSettingsForm} setSettingsForm={setPayrollSettingsForm} workerForm={payrollWorkerForm} setWorkerForm={setPayrollWorkerForm} paymentForm={dailyLaborPaymentForm} setPaymentForm={setDailyLaborPaymentForm} workers={payrollWorkers} payments={dailyLaborPayments} electronicDocuments={electronicPayrollDocuments} documentTypeOptions={runtimeCatalogs.dianDocumentTypes} workerClassificationOptions={runtimeCatalogs.payrollWorkerClassificationOptions} paymentMethodOptions={runtimeCatalogs.paymentMethodOptions} onLoad={() => execute(loadPayrollData)} onSaveSettings={() => execute(savePayrollSettings)} onCreateWorker={() => execute(createPayrollWorker)} onCreateDailyPayment={() => execute(createDailyLaborPayment)} onIssueElectronicDocument={(paymentId) => execute(() => issueElectronicPayrollDocument(paymentId))} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Nomina)} />
          )}
          {currentStep === 'Reportes' && (
            <ReportsForm form={reportsForm} setForm={setReportsForm} data={reportsData} onSubmit={() => execute(loadReports)} onInitializeAccounting={() => execute(initializeAccountingSetup)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Reportes)} />
          )}
          {currentStep === 'Catalogos' && (
            <CatalogAdminPanel definitions={catalogDefinitions} selectedCatalogCode={selectedCatalogCode} setSelectedCatalogCode={setSelectedCatalogCode} items={catalogItems} form={catalogItemForm} setForm={setCatalogItemForm} onLoadDefinitions={() => execute(loadCatalogDefinitions)} onLoadItems={() => execute(() => loadCatalogItems())} onNew={startNewCatalogItem} onEdit={editCatalogItem} onSave={() => execute(saveCatalogItem)} onToggleActive={(item) => execute(() => toggleCatalogItemActive(item))} busy={busy || !canManageCatalogs} isRoot={isRoot} />
          )}
          {currentStep === 'Logs' && (
            <AuditLogPanel events={auditEvents} filters={auditFilters} setFilters={setAuditFilters} onLoad={() => execute(loadAuditEvents)} busy={busy || !activeCompanyId || !canViewAudit} canViewGlobal={isRoot} activeCompanyId={activeCompanyId} resourceTypes={auditResourceTypes} />
          )}
          {currentStep === 'Roles' && (
            <RolesPanel permissions={availableCompanyPermissions} roles={companyRoles} form={companyRoleForm} setForm={setCompanyRoleForm} editingRoleId={editingRoleId} onNew={startNewCompanyRole} onEdit={editCompanyRole} onSave={() => execute(saveCompanyRole)} onToggleActive={(role) => execute(() => toggleCompanyRoleActive(role))} onTogglePermission={togglePermissionCode} busy={busy || !activeCompanyId || !canManageRoles} />
          )}
          {currentStep === 'Usuarios' && (
            <UsersPanel users={managedUsers} roles={companyRoles} form={managedUserForm} setForm={setManagedUserForm} editingUserId={editingUserId} onNew={startNewManagedUser} onEdit={editManagedUser} onSave={() => execute(saveManagedUser)} onToggleActive={(user) => execute(() => toggleManagedUserActive(user))} busy={busy || !activeCompanyId || !canManageUsers} />
          )}
        </section>
      </section>
      {licenseModal && <Modal title={licenseModal.title} message={licenseModal.message} onClose={() => setLicenseModal(null)} />}
      <ActionStatusModal state={actionStatus} onClose={() => setActionStatus({ status: 'idle' })} />
      {adminModalOpen && <AdminModal form={companyAdminForm} setForm={setCompanyAdminForm} activeCompany={activeCompany} activeCompanyId={activeCompanyId} onSubmit={() => execute(createInitialCompanyAdmin)} onClose={() => setAdminModalOpen(false)} busy={busy || !activeCompanyId} />}
    </main>
  );
}

function toInstantQuery(value) {
  if (!value) {
    return '';
  }
  return new Date(value).toISOString();
}

function todayAuditFilters(now = new Date()) {
  const start = new Date(now);
  start.setHours(0, 0, 0, 0);
  const end = new Date(now);
  end.setHours(23, 59, 59, 999);
  return {
    resourceType: '',
    from: toDateTimeLocalValue(start),
    to: toDateTimeLocalValue(end),
  };
}

function toDateTimeLocalValue(date) {
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}
