import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { createIdempotencyKey, requestJson } from './api/client.js';
import { ActionStatusModal } from './components/ActionStatusModal.jsx';
import { Modal } from './components/Modal.jsx';
import { steps } from './data/navigation.js';
import {
  createCatalogItemForm,
  createCompanyAdminForm,
  createCompanyForm,
  createCompanyRoleForm,
  createDailyLaborPaymentForm,
  createIssuerForm,
  createLoginForm,
  createManagedUserForm,
  createPayrollSettingsForm,
  createPayrollWorkerForm,
  createProductForm,
  createReportsForm,
  createResolutionForm,
  createRoleAssignmentForm,
  createSaleForm,
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
import { IdentityAdminPanel, RoleAssignmentModal } from './features/identity/IdentityAdminPanel.jsx';
import { ProductForm } from './features/inventory/ProductForm.jsx';
import { PayrollPanel } from './features/payroll/PayrollPanel.jsx';
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
  const [loginForm, setLoginForm] = useState(createLoginForm);
  const [session, setSession] = useState(storedSnapshot?.session || null);
  const [companyAccesses, setCompanyAccesses] = useState(storedSnapshot?.companyAccesses || []);
  const [activeCompanyId, setActiveCompanyId] = useState(storedSnapshot?.activeCompanyId || '');
  const [rootCompanies, setRootCompanies] = useState(storedSnapshot?.rootCompanies || []);
  const [license, setLicense] = useState(storedSnapshot?.license || null);
  const [runtimeCatalogs, setRuntimeCatalogs] = useState(() => globalThis.__FACTURA_RUNTIME_CATALOGS__ || emptyRuntimeCatalogs);
  const [lastActivityAt, setLastActivityAt] = useState(storedSnapshot?.lastActivityAt || Date.now());
  const lastActivityRef = useRef(lastActivityAt);
  const [licenseModal, setLicenseModal] = useState(null);
  const [companyForm, setCompanyForm] = useState(createCompanyForm);
  const [companyAdminForm, setCompanyAdminForm] = useState(createCompanyAdminForm);
  const [managedUserForm, setManagedUserForm] = useState(createManagedUserForm);
  const [companyRoleForm, setCompanyRoleForm] = useState(createCompanyRoleForm);
  const [roleAssignmentForm, setRoleAssignmentForm] = useState(createRoleAssignmentForm);
  const [permissionCatalog, setPermissionCatalog] = useState([]);
  const [companyRoles, setCompanyRoles] = useState([]);
  const [managedUsers, setManagedUsers] = useState([]);
  const [catalogDefinitions, setCatalogDefinitions] = useState([]);
  const [selectedCatalogCode, setSelectedCatalogCode] = useState('');
  const [catalogItems, setCatalogItems] = useState([]);
  const [catalogItemForm, setCatalogItemForm] = useState(createCatalogItemForm);
  const [adminModalOpen, setAdminModalOpen] = useState(false);
  const [roleAssignmentModalOpen, setRoleAssignmentModalOpen] = useState(false);
  const [userSearchEmail, setUserSearchEmail] = useState('');
  const [thirdPartyForm, setThirdPartyForm] = useState(createThirdPartyForm);
  const [productForm, setProductForm] = useState(createProductForm);
  const [issuerForm, setIssuerForm] = useState(createIssuerForm);
  const [resolutionForm, setResolutionForm] = useState(createResolutionForm);
  const [saleForm, setSaleForm] = useState(createSaleForm);
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

  const token = session?.accessToken || '';
  const context = useMemo(() => ({ token, companyId: activeCompanyId, userId: session?.userId }), [token, activeCompanyId, session?.userId]);
  const activeAccess = companyAccesses.find((access) => access.companyId === activeCompanyId);
  const activeCompany = rootCompanies.find((company) => company.id === activeCompanyId || company.companyId === activeCompanyId);
  const companyMunicipalityCode = activeCompany?.municipalityCode || issuerForm.municipalityCode;
  const isRoot = session?.globalRoles?.includes('ROOT') || false;
  const isCompanyAdmin = hasAnyRole(activeAccess, ['OWNER', 'ADMIN']);
  const canUse = (permissions) => isRoot || isCompanyAdmin || hasAnyPermission(activeAccess, permissions);
  const canManageSecurity = canUse(stepPermissionRules['Usuarios y roles']);
  const canManageCatalogs = canUse(stepPermissionRules.Catalogos);
  const canViewAudit = isRoot || isCompanyAdmin || hasAnyPermission(activeAccess, stepPermissionRules.Logs);
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
      const message = caught.status === 401 && options.authAction
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
    setPermissionCatalog([]);
    setCompanyRoles([]);
    setManagedUsers([]);
    setCatalogDefinitions([]);
    setSelectedCatalogCode('');
    setCatalogItems([]);
    setCatalogItemForm(createCatalogItemForm());
    setRoleAssignmentForm(createRoleAssignmentForm());
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
      ...context,
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
      updateSaleItem(0, 'productName', result.name || '');
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
      items: [...current.items, { productId: '', productName: '', quantity: '1', unitPrice: '0', discountAmount: '0' }],
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
            <CompanyForm form={companyForm} setForm={setCompanyForm} companies={rootCompanies} activeCompanyId={activeCompanyId} activeCompany={activeCompany} isRoot={isRoot} onCompanyChange={changeCompany} onSubmit={() => execute(createCompany)} onOpenAdminModal={() => {
              setActionStatus({ status: 'idle' });
              setAdminModalOpen(true);
            }} busy={busy} documentTypeOptions={runtimeCatalogs.dianDocumentTypes} />
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
          {currentStep === 'Venta POS' && (
            <SaleForm form={saleForm} setForm={setSaleForm} saleId={saleId} customerSearch={customerSearch} setCustomerSearch={setCustomerSearch} customerOptions={customerOptions} selectedCustomer={selectedCustomer} onSearchCustomers={searchCustomers} onSelectCustomer={selectCustomer} updateItem={updateSaleItem} addItem={addSaleItem} removeItem={removeSaleItem} onScanBarcode={(barcode) => execute(() => scanSaleBarcode(barcode))} onCreate={() => execute(createSale)} onConfirm={() => execute(confirmSale)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules['Venta POS'])} paymentOptions={runtimeCatalogs.paymentMethodOptions} walletOptions={runtimeCatalogs.virtualWalletOptions} />
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
          {currentStep === 'Usuarios y roles' && (
            <IdentityAdminPanel permissions={availableCompanyPermissions} roles={companyRoles} users={managedUsers} roleForm={companyRoleForm} setRoleForm={setCompanyRoleForm} userForm={managedUserForm} setUserForm={setManagedUserForm} onLoad={() => execute(loadIdentityAdminData)} onCreateRole={() => execute(createCompanyRole)} onCreateUser={() => execute(createManagedUser)} onOpenAssignModal={() => {
              setActionStatus({ status: 'idle' });
              setRoleAssignmentModalOpen(true);
            }} onTogglePermission={togglePermissionCode} busy={busy || !activeCompanyId || !canManageSecurity} />
          )}
        </section>
      </section>
      {licenseModal && <Modal title={licenseModal.title} message={licenseModal.message} onClose={() => setLicenseModal(null)} />}
      <ActionStatusModal state={actionStatus} onClose={() => setActionStatus({ status: 'idle' })} />
      {adminModalOpen && <AdminModal form={companyAdminForm} setForm={setCompanyAdminForm} activeCompany={activeCompany} activeCompanyId={activeCompanyId} onSubmit={() => execute(createInitialCompanyAdmin)} onClose={() => setAdminModalOpen(false)} busy={busy || !activeCompanyId} />}
      {roleAssignmentModalOpen && <RoleAssignmentModal users={managedUsers} roles={companyRoles} form={roleAssignmentForm} setForm={setRoleAssignmentForm} searchEmail={userSearchEmail} setSearchEmail={setUserSearchEmail} onSearch={() => execute(() => loadCompanyUsers(userSearchEmail))} onSubmit={() => execute(assignManagedRoles)} onToggleRole={toggleAssignedRole} onClose={() => setRoleAssignmentModalOpen(false)} busy={busy || !activeCompanyId || !canManageSecurity} />}
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
