import { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import { createIdempotencyKey, requestDownload, requestFormData, requestJson } from './api/client.js';
import { ActionStatusModal } from './components/ActionStatusModal.jsx';
import { DataTable } from './components/DataTable.jsx';
import { Modal } from './components/Modal.jsx';
import { navigationGroups, steps } from './data/navigation.js';
import {
  createCatalogItemForm,
  createCompanyBrandingForm,
  createCompanyAdminForm,
  createCompanyForm,
  createCompanyRoleForm,
  createDailyLaborPaymentForm,
  createDianConfigurationForm,
  createFiscalNoteForm,
  createFiscalPolicyForm,
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
import { AccountingConfigurationPanel } from './features/accounting/AccountingConfigurationPanel.jsx';
import { AuditLogPanel } from './features/audit/AuditLogPanel.jsx';
import { CatalogAdminPanel } from './features/catalogs/CatalogAdminPanel.jsx';
import { AdminModal } from './features/company/AdminModal.jsx';
import { CompanyBrandingModal, CompanyBrandingPanel } from './features/company/CompanyBrandingPanel.jsx';
import { CompanyForm } from './features/company/CompanyForm.jsx';
import { CompanySessionPanel } from './features/company/CompanySessionPanel.jsx';
import { IssuerForm } from './features/company/IssuerForm.jsx';
import { DianConfigurationPanel } from './features/dian/DianConfigurationPanel.jsx';
import { FiscalNotesPanel } from './features/fiscal/FiscalNotesPanel.jsx';
import { FiscalPolicyForm } from './features/fiscal/FiscalPolicyForm.jsx';
import { ResolutionForm } from './features/fiscal/ResolutionForm.jsx';
import { RolesPanel, UsersPanel } from './features/identity/IdentityAdminPanel.jsx';
import { ProductForm } from './features/inventory/ProductForm.jsx';
import { PayrollPanel } from './features/payroll/PayrollPanel.jsx';
import { LicenseAdminPanel } from './features/licenses/LicenseAdminPanel.jsx';
import { ReportsForm } from './features/reports/ReportsForm.jsx';
import { SaleForm } from './features/sales/SaleForm.jsx';
import { SalesRegistryPanel } from './features/sales/SalesRegistryPanel.jsx';
import { ThirdPartyForm } from './features/thirdparties/ThirdPartyForm.jsx';
import { companyScopedPermissions, hasAnyPermission, hasAnyRole, stepPermissionRules } from './utils/authorization.js';
import { buildIssuerFromCompany } from './utils/company.js';
import {
  buildCompanyAdminPayload,
  buildCompanyPayload,
  buildFiscalNotePayload,
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

const PRODUCT_NAME = 'NexoFiscal';

export default function App() {
  const [storedSnapshot] = useState(() => loadStoredSession());
  const storedSnapshotIsRoot = storedSnapshot?.session?.globalRoles?.includes('ROOT') || false;
  const [selectedStep, setSelectedStep] = useState('Ventas');
  const [loginForm, setLoginForm] = useState(createLoginForm);
  const [session, setSession] = useState(storedSnapshot?.session || null);
  const [companyAccesses, setCompanyAccesses] = useState(storedSnapshotIsRoot ? [] : storedSnapshot?.companyAccesses || []);
  const [activeCompanyId, setActiveCompanyId] = useState(storedSnapshotIsRoot ? '' : storedSnapshot?.activeCompanyId || '');
  const [rootCompanies, setRootCompanies] = useState(storedSnapshot?.rootCompanies || []);
  const [license, setLicense] = useState(storedSnapshotIsRoot ? null : storedSnapshot?.license || null);
  const [licenseForm, setLicenseForm] = useState(createLicenseForm);
  const [managedLicense, setManagedLicense] = useState(null);
  const [licenseUsage, setLicenseUsage] = useState(null);
  const [runtimeCatalogs, setRuntimeCatalogs] = useState(() => globalThis.__FACTURA_RUNTIME_CATALOGS__ || emptyRuntimeCatalogs);
  const [lastActivityAt, setLastActivityAt] = useState(storedSnapshot?.lastActivityAt || Date.now());
  const lastActivityRef = useRef(lastActivityAt);
  const [licenseModal, setLicenseModal] = useState(null);
  const [companyForm, setCompanyForm] = useState(createCompanyForm);
  const [editingCompanyId, setEditingCompanyId] = useState('');
  const [companyBrandingForm, setCompanyBrandingForm] = useState(createCompanyBrandingForm);
  const [companyBranding, setCompanyBranding] = useState(null);
  const [brandingEditorForm, setBrandingEditorForm] = useState(createCompanyBrandingForm);
  const [brandingEditor, setBrandingEditor] = useState(null);
  const [brandingModalOpen, setBrandingModalOpen] = useState(false);
  const [brandingTargetCompanyId, setBrandingTargetCompanyId] = useState('');
  const [companyAdminForm, setCompanyAdminForm] = useState(createCompanyAdminForm);
  const [adminTargetCompanyId, setAdminTargetCompanyId] = useState('');
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
  const [fiscalPolicyForm, setFiscalPolicyForm] = useState(createFiscalPolicyForm);
  const [fiscalPolicy, setFiscalPolicy] = useState(null);
  const [fiscalNoteForms, setFiscalNoteForms] = useState(() => ({
    credit: createFiscalNoteForm(),
    debit: createFiscalNoteForm(),
    posAdjustment: createFiscalNoteForm(),
  }));
  const [fiscalNoteResults, setFiscalNoteResults] = useState({});
  const [issuerProfiles, setIssuerProfiles] = useState([]);
  const [numberingResolutions, setNumberingResolutions] = useState([]);
  const [dianConfigurationForm, setDianConfigurationForm] = useState(createDianConfigurationForm);
  const [dianConfiguration, setDianConfiguration] = useState(null);
  const [saleForm, setSaleForm] = useState(createSaleForm);
  const [serviceConsumption, setServiceConsumption] = useState(createServiceConsumptionState);
  const [operationalListFilters, setOperationalListFilters] = useState(createOperationalListFilters);
  const [thirdPartyList, setThirdPartyList] = useState([]);
  const [productList, setProductList] = useState([]);
  const [purchaseList, setPurchaseList] = useState([]);
  const [salesList, setSalesList] = useState([]);
  const [selectedSaleDetail, setSelectedSaleDetail] = useState(null);
  const [customerSearch, setCustomerSearch] = useState('');
  const [customerOptions, setCustomerOptions] = useState([]);
  const [selectedCustomer, setSelectedCustomer] = useState(null);
  const [reportsForm, setReportsForm] = useState(createReportsForm);
  const [reportDefinitions, setReportDefinitions] = useState([]);
  const [reportOptions, setReportOptions] = useState({});
  const [reportsData, setReportsData] = useState(null);
  const [reportJobs, setReportJobs] = useState([]);
  const [accountingAccounts, setAccountingAccounts] = useState([]);
  const [accountingRules, setAccountingRules] = useState([]);
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
  const autoReportsLoadKeyRef = useRef('');
  const autoAccountingLoadKeyRef = useRef('');

  const token = session?.accessToken || '';
  const context = useMemo(() => ({ token, companyId: activeCompanyId, userId: session?.userId }), [token, activeCompanyId, session?.userId]);
  const activeAccess = companyAccesses.find((access) => access.companyId === activeCompanyId);
  const activeCompany = rootCompanies.find((company) => company.id === activeCompanyId || company.companyId === activeCompanyId);
  const adminTargetCompany = rootCompanies.find((company) => company.id === adminTargetCompanyId || company.companyId === adminTargetCompanyId);
  const brandingTargetCompany = rootCompanies.find((company) => company.id === brandingTargetCompanyId || company.companyId === brandingTargetCompanyId);
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
  const activeBrandName = companyBranding?.displayName || activeCompany?.tradeName || activeCompany?.legalName || PRODUCT_NAME;
  const activeBrandLogo = companyBranding?.headerLogoUrl || companyBranding?.mainLogoUrl || '';

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
    const authCallbackSuccess = new URLSearchParams(window.location.search).get('auth') === 'success';
    if (session || (import.meta.env.MODE === 'test' && !authCallbackSuccess)) {
      return undefined;
    }
    let ignore = false;
    requestJson('/api/v1/auth/session')
      .then(async (authSession) => {
        if (ignore || !authSession?.authenticated) {
          return;
        }
        await completeAuthenticatedLogin({
          userId: authSession.userId,
          email: authSession.email,
          fullName: authSession.fullName,
          expiresAt: authSession.expiresAt,
          globalRoles: authSession.groups || [],
          authMode: authSession.authMode || 'cognito',
          cookieSession: true,
        }, '');
      })
      .catch(() => undefined);
    return () => {
      ignore = true;
    };
  }, [session]);

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
    if (!session || !activeCompanyId || import.meta.env.MODE === 'test') {
      setCompanyBranding(null);
      setCompanyBrandingForm(createCompanyBrandingForm());
      return undefined;
    }
    let ignore = false;
    requestJson(`/api/v1/companies/${activeCompanyId}/branding`, { token, companyId: activeCompanyId, userId: session.userId })
      .then((branding) => {
        if (!ignore) {
          setCompanyBranding(branding);
          hydrateBrandingForm(branding);
        }
      })
      .catch(() => {
        if (!ignore) {
          setCompanyBranding(null);
          setCompanyBrandingForm(createCompanyBrandingForm());
        }
      });
    return () => {
      ignore = true;
    };
  }, [session, token, activeCompanyId]);

  useEffect(() => {
    if (typeof document === 'undefined') {
      return;
    }
    document.title = activeBrandName === PRODUCT_NAME ? PRODUCT_NAME : `${activeBrandName} | ${PRODUCT_NAME}`;
    let favicon = document.querySelector('link[data-nexofiscal-favicon]');
    if (!favicon) {
      favicon = document.createElement('link');
      favicon.setAttribute('data-nexofiscal-favicon', 'true');
      favicon.rel = 'icon';
      document.head.appendChild(favicon);
    }
    if (companyBranding?.faviconUrl) {
      favicon.href = companyBranding.faviconUrl;
    } else {
      favicon.removeAttribute('href');
    }
  }, [activeBrandName, companyBranding?.faviconUrl]);

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
    if (activeCompany) {
      if (!isRoot) {
        hydrateCompanyForm(activeCompany);
      }
      setIssuerForm((current) => buildIssuerFromCompany(activeCompany, current));
    }
  }, [activeCompany?.id, isRoot]);

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
    if (currentStep !== 'Fiscal' || !activeCompanyId || !canUse(stepPermissionRules.Fiscal)) {
      return;
    }
    loadFiscalConfiguration().catch(() => undefined);
  }, [currentStep, activeCompanyId]);

  useEffect(() => {
    if (currentStep !== 'Reportes' || !activeCompanyId || !canUse(stepPermissionRules.Reportes)) {
      return;
    }
    const key = `reports|${activeCompanyId}`;
    if (autoReportsLoadKeyRef.current === key) {
      return;
    }
    autoReportsLoadKeyRef.current = key;
    Promise.all([loadReportDefinitions(), loadReportJobs()]).catch(() => undefined);
  }, [currentStep, activeCompanyId, reportDefinitions.length]);

  useEffect(() => {
    if (currentStep !== 'Configuracion contable' || !activeCompanyId || !canUse(stepPermissionRules['Configuracion contable'])) {
      return;
    }
    const key = `accounting|${activeCompanyId}`;
    if (autoAccountingLoadKeyRef.current === key) {
      return;
    }
    autoAccountingLoadKeyRef.current = key;
    loadAccountingConfiguration().catch(() => undefined);
  }, [currentStep, activeCompanyId]);

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
        : isAccountingSetupRequiredError(caught)
        ? 'Debes inicializar la configuracion contable basica antes de cerrar ventas. Ve al modulo Configuracion contable.'
        : isFiscalSetupRequiredError(caught)
        ? `${payload.message} Ve al modulo Fiscal y completa la configuracion antes de confirmar la venta.`
        : options.authAction && !caught.status
        ? 'No fue posible conectar con el servicio de autenticacion. Verifica que el BFF este levantado e intenta nuevamente.'
        : caught.status === 401 && options.authAction
        ? 'Credenciales invalidas. Verifica el correo y la contrasena.'
        : caught.status && caught.status >= 500
          ? 'Fallo interno en la aplicacion. Intenta nuevamente o revisa los logs.'
          : 'Hay un error al realizar la accion. Revisa Logs/Auditoria para mas detalle.';
      if (isFiscalSetupRequiredError(caught)) {
        setSelectedStep('Fiscal');
      }
      if (isAccountingSetupRequiredError(caught)) {
        setSelectedStep('Configuracion contable');
      }
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

  function isFiscalSetupRequiredError(error) {
    const message = String(error?.payload?.message || error?.message || '').toLowerCase();
    return (error?.status === 400 || error?.status >= 500)
      && (message.includes('emisor fiscal activo')
        || message.includes('resolucion de numeracion activa')
        || message.includes('active issuer profile')
        || message.includes('active numbering resolution'));
  }

  function isAccountingSetupRequiredError(error) {
    const message = String(error?.payload?.message || error?.message || '').toLowerCase();
    return (error?.status === 400 || error?.status >= 500)
      && (message.includes('configuracion contable basica')
        || message.includes('accounting rule was not found')
        || message.includes('asiento contable'));
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

  function hydrateBrandingForm(branding) {
    setCompanyBrandingForm({
      displayName: branding?.displayName || '',
      primaryColor: branding?.primaryColor || '',
      accentColor: branding?.accentColor || '',
    });
  }

  async function login() {
    const rawLoginResult = await requestJson('/api/v1/auth/login', {
      method: 'POST',
      body: loginForm,
    });
    const loginResult = { ...rawLoginResult, authMode: rawLoginResult.authMode || 'local' };
    return completeAuthenticatedLogin(loginResult, loginResult.accessToken || '');
  }

  async function completeAuthenticatedLogin(loginResult, tokenValue) {
    if (loginResult.globalRoles?.includes('ROOT')) {
      const companies = await loadRootCompanies(tokenValue);
      const now = Date.now();
      lastActivityRef.current = now;
      setLastActivityAt(now);
      setSession(loginResult);
      setCompanyAccesses([]);
      setActiveCompanyId('');
      setLicenseForm(createLicenseForm());
      setCompanyForm(createCompanyForm());
      setEditingCompanyId('');
      setIssuerForm(createIssuerForm());
      setLicense(null);
      setManagedLicense(null);
      setLicenseUsage(null);
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
    setEditingCompanyId('');
    setCompanyBranding(null);
    setCompanyBrandingForm(createCompanyBrandingForm());
    setBrandingEditor(null);
    setBrandingEditorForm(createCompanyBrandingForm());
    setBrandingModalOpen(false);
    setBrandingTargetCompanyId('');
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
    setThirdPartyList([]);
    setProductList([]);
    setPurchaseList([]);
    setSalesList([]);
    setOperationalListFilters(createOperationalListFilters());
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
    setAccountingAccounts([]);
    setAccountingRules([]);
    setSaleId('');
    setSaleForm(createSaleForm());
    setIssuerProfiles([]);
    setNumberingResolutions([]);
    setAdminModalOpen(false);
    setAdminTargetCompanyId('');
    autoIdentityLoadKeyRef.current = '';
    autoAccountingLoadKeyRef.current = '';
  }

  function closeSessionWithModal(title, message) {
    clearSession();
    setLicenseModal({ title, message });
  }

  function logout() {
    const tokenToRevoke = token;
    clearSession();
    setLicenseModal(null);
    requestJson('/api/v1/auth/logout', { method: 'POST', token: tokenToRevoke }).catch(() => undefined);
  }

  function requireCompany() {
    if (!activeCompanyId) {
      throw new Error('Inicia sesion y selecciona una empresa antes de operar.');
    }
  }

  function requireTargetCompany(companyId) {
    if (!companyId) {
      throw new Error('Selecciona una empresa antes de ejecutar esta accion.');
    }
  }

  async function createCompany() {
    const created = await requestJson('/api/v1/companies', {
      method: 'POST',
      body: buildCompanyPayload(companyForm),
      token,
      userId: session?.userId,
      idempotencyKey: createIdempotencyKey('company'),
    });
    if (isRoot && created?.id) {
      setRootCompanies((current) => [created, ...current.filter((company) => company.id !== created.id)]);
      setActiveCompanyId(created.id);
      setCompanyForm(createCompanyForm());
      setEditingCompanyId('');
      setLicenseForm((current) => ({ ...current, companyId: created.id }));
      setManagedLicense(null);
      setCompanyAccesses([{ companyId: created.id, roles: ['ROOT'], permissions: ['GLOBAL_COMPANIES_MANAGE'] }]);
      setIssuerForm((current) => buildIssuerFromCompany(created, current));
    }
    return created;
  }

  async function updateCompany() {
    const targetCompanyId = isRoot ? editingCompanyId : activeCompanyId;
    requireTargetCompany(targetCompanyId);
    const updated = await requestJson(`/api/v1/companies/${targetCompanyId}`, {
      method: 'PUT',
      body: buildCompanyPayload(companyForm),
      ...context,
      companyId: targetCompanyId,
      idempotencyKey: createIdempotencyKey('company-update'),
    });
    storeKnownCompany(updated);
    if (!isRoot || editingCompanyId) {
      hydrateCompanyForm(updated);
    }
    setIssuerForm((current) => buildIssuerFromCompany(updated, current));
    return updated;
  }

  async function activateCompany(companyId = activeCompanyId) {
    requireTargetCompany(companyId);
    const updated = await requestJson(`/api/v1/companies/${companyId}/activate`, {
      method: 'PUT',
      ...context,
      companyId,
      idempotencyKey: createIdempotencyKey('company-activate'),
    });
    storeKnownCompany(updated);
    if (!isRoot || editingCompanyId === companyId) {
      hydrateCompanyForm(updated);
    }
    return updated;
  }

  async function suspendCompany(companyId = activeCompanyId) {
    requireTargetCompany(companyId);
    const updated = await requestJson(`/api/v1/companies/${companyId}/suspend`, {
      method: 'PUT',
      ...context,
      companyId,
      idempotencyKey: createIdempotencyKey('company-suspend'),
    });
    storeKnownCompany(updated);
    if (!isRoot || editingCompanyId === companyId) {
      hydrateCompanyForm(updated);
    }
    return updated;
  }

  function startNewCompany() {
    setEditingCompanyId('');
    setCompanyForm(createCompanyForm());
  }

  function editCompanyFromTable(company) {
    const companyId = company?.id || company?.companyId || '';
    requireTargetCompany(companyId);
    setEditingCompanyId(companyId);
    hydrateCompanyForm(company);
  }

  async function toggleCompanyActiveFromTable(company) {
    const companyId = company?.id || company?.companyId || '';
    requireTargetCompany(companyId);
    return company.status === 'SUSPENDED' ? activateCompany(companyId) : suspendCompany(companyId);
  }

  function openAdminModalForCompany(company) {
    const companyId = company?.id || company?.companyId || '';
    requireTargetCompany(companyId);
    setAdminTargetCompanyId(companyId);
    setCompanyAdminForm(createCompanyAdminForm());
    setActionStatus({ status: 'idle' });
    setAdminModalOpen(true);
  }

  async function openBrandingModalForCompany(company) {
    const companyId = company?.id || company?.companyId || '';
    requireTargetCompany(companyId);
    setBrandingTargetCompanyId(companyId);
    setBrandingEditor(null);
    setBrandingEditorForm(createCompanyBrandingForm());
    setActionStatus({ status: 'idle' });
    setBrandingModalOpen(true);
    try {
      const branding = await requestJson(`/api/v1/companies/${companyId}/branding`, { token, companyId, userId: session?.userId });
      setBrandingEditor(branding);
      setBrandingEditorForm({
        displayName: branding?.displayName || '',
        primaryColor: branding?.primaryColor || '',
        accentColor: branding?.accentColor || '',
      });
      return branding;
    } catch (error) {
      if (error.status === 404) {
        return null;
      }
      throw error;
    }
  }

  async function saveCompanyBranding(companyId = activeCompanyId, form = companyBrandingForm, updateActiveBranding = true) {
    requireTargetCompany(companyId);
    const result = await requestJson(`/api/v1/companies/${companyId}/branding`, {
      method: 'PUT',
      body: form,
      ...context,
      companyId,
      idempotencyKey: createIdempotencyKey('company-branding'),
    });
    if (updateActiveBranding || companyId === activeCompanyId) {
      setCompanyBranding(result);
      hydrateBrandingForm(result);
    }
    if (companyId === brandingTargetCompanyId) {
      setBrandingEditor(result);
      setBrandingEditorForm({
        displayName: result?.displayName || '',
        primaryColor: result?.primaryColor || '',
        accentColor: result?.accentColor || '',
      });
    }
    return result;
  }

  async function uploadCompanyBrandingAsset(purpose, file, companyId = activeCompanyId, updateActiveBranding = true) {
    requireTargetCompany(companyId);
    const formData = new FormData();
    formData.append('purpose', purpose);
    formData.append('file', file);
    const result = await requestFormData(`/api/v1/companies/${companyId}/branding/assets`, {
      method: 'POST',
      formData,
      ...context,
      companyId,
      idempotencyKey: createIdempotencyKey(`company-branding-${purpose.toLowerCase()}`),
    });
    if (updateActiveBranding || companyId === activeCompanyId) {
      setCompanyBranding(result);
      hydrateBrandingForm(result);
    }
    if (companyId === brandingTargetCompanyId) {
      setBrandingEditor(result);
    }
    return result;
  }

  function selectLicenseCompany(companyId) {
    setLicenseForm((current) => ({ ...current, companyId }));
    setManagedLicense(null);
    setLicenseUsage(null);
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
    await loadLicenseUsage(companyId);
    return result;
  }

  async function loadLicenseUsage(companyId = licenseForm.companyId || activeCompanyId) {
    if (!companyId || !isRoot) {
      setLicenseUsage(null);
      return null;
    }
    const result = await requestJson(`/api/v1/platform/licenses/usage${buildQuery({ companyId })}`, { token });
    setLicenseUsage(result);
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
    await loadLicenseUsage(companyId);
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
    await loadLicenseUsage(companyId);
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
    await loadLicenseUsage(companyId);
    return result;
  }

  async function createInitialCompanyAdmin() {
    const companyId = adminTargetCompanyId || activeCompanyId;
    requireTargetCompany(companyId);
    const user = await requestJson('/api/v1/users', {
      method: 'POST',
      body: buildCompanyAdminPayload(companyAdminForm),
      token,
      idempotencyKey: createIdempotencyKey('company-admin-user'),
    });
    const membership = await requestJson(`/api/v1/companies/${companyId}/memberships`, {
      method: 'POST',
      body: { userId: user.id, roles: [companyAdminForm.role || 'OWNER'] },
      token,
      companyId,
      idempotencyKey: createIdempotencyKey('company-admin-membership'),
    });
    setAdminModalOpen(false);
    setAdminTargetCompanyId('');
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
    try {
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
    } finally {
      await loadIdentityAdminData().catch(() => undefined);
      await loadCompanyUsers('').catch(() => undefined);
    }
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
    const result = await requestJson('/api/v1/third-parties', {
      method: 'POST',
      body: buildThirdPartyPayload(thirdPartyForm, companyMunicipalityCode),
      ...context,
      idempotencyKey: createIdempotencyKey('third-party'),
    });
    setThirdPartyList((current) => [result, ...current.filter((item) => item.id !== result.id)]);
    return result;
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
      setProductList((current) => [result, ...current.filter((item) => item.id !== result.id)]);
      updateSaleItem(0, 'productId', result.id);
      updateSaleItem(0, 'productName', result.name || '');
      updateSaleItem(0, 'itemType', result.itemType || '');
      updateSaleItem(0, 'unitPrice', String(result.salePrice || productForm.salePrice));
      updateSaleItem(0, 'taxCode', result.taxCode || productForm.taxCode || '');
      updateSaleItem(0, 'taxRate', String(result.taxRate ?? productForm.taxRate ?? ''));
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
        taxCode: product.taxCode || '',
        taxRate: String(product.taxRate ?? ''),
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
    const result = await requestJson('/api/v1/issuers', {
      method: 'POST',
      body: buildIssuerPayload(issuerForm),
      ...context,
      idempotencyKey: createIdempotencyKey('issuer'),
    });
    await loadFiscalConfiguration();
    return result;
  }

  async function configureResolution() {
    requireCompany();
    const result = await requestJson('/api/v1/numbering-resolutions', {
      method: 'POST',
      body: buildResolutionPayload(resolutionForm),
      ...context,
      idempotencyKey: createIdempotencyKey('resolution'),
    });
    await loadFiscalConfiguration();
    return result;
  }

  async function configureFiscalPolicy() {
    requireCompany();
    const result = await requestJson('/api/v1/fiscal-policy', {
      method: 'PUT',
      body: {
        defaultSaleDocumentType: fiscalPolicyForm.defaultSaleDocumentType || 'ELECTRONIC_INVOICE',
        allowDocumentTypeOverride: Boolean(fiscalPolicyForm.allowDocumentTypeOverride),
        requirePinForOverride: Boolean(fiscalPolicyForm.allowDocumentTypeOverride && fiscalPolicyForm.requirePinForOverride),
      },
      ...context,
      idempotencyKey: createIdempotencyKey('fiscal-policy'),
    });
    hydrateFiscalPolicyForm(result);
    return result;
  }

  function hydrateFiscalPolicyForm(policy) {
    setFiscalPolicy(policy || null);
    setFiscalPolicyForm({
      ...createFiscalPolicyForm(),
      defaultSaleDocumentType: policy?.defaultSaleDocumentType || 'ELECTRONIC_INVOICE',
      allowDocumentTypeOverride: policy?.allowDocumentTypeOverride ?? true,
      requirePinForOverride: policy?.requirePinForOverride ?? true,
    });
  }

  async function createFiscalNote(noteType) {
    requireCompany();
    const form = fiscalNoteForms[noteType];
    const payload = buildFiscalNotePayload(form);
    const endpoint = noteType === 'credit'
      ? '/api/v1/credit-notes'
      : noteType === 'debit'
        ? '/api/v1/debit-notes'
        : `/api/v1/electronic-pos/${form.originalDocumentId}/adjustment-notes`;
    const result = await requestJson(endpoint, {
      method: 'POST',
      body: payload,
      ...context,
      idempotencyKey: createIdempotencyKey(`fiscal-note-${noteType}`),
    });
    setFiscalNoteResults((current) => ({ ...current, [noteType]: result }));
    setFiscalNoteForms((current) => ({ ...current, [noteType]: createFiscalNoteForm() }));
    return result;
  }

  async function loadFiscalConfiguration() {
    requireCompany();
    const [issuers, resolutions, policy] = await Promise.all([
      requestJson('/api/v1/issuers', context),
      requestJson('/api/v1/numbering-resolutions', context),
      requestJson('/api/v1/fiscal-policy', context),
    ]);
    setIssuerProfiles(issuers || []);
    setNumberingResolutions(resolutions || []);
    hydrateFiscalPolicyForm(policy);
    return { issuers, resolutions, policy };
  }

  async function toggleIssuerActive(issuer) {
    requireCompany();
    const action = issuer.active ? 'deactivate' : 'activate';
    const result = await requestJson(`/api/v1/issuers/${issuer.id}/${action}`, {
      method: 'PUT',
      ...context,
      idempotencyKey: createIdempotencyKey(`issuer-${action}`),
    });
    await loadFiscalConfiguration();
    return result;
  }

  async function toggleResolutionActive(resolution) {
    requireCompany();
    const action = resolution.active ? 'deactivate' : 'activate';
    const result = await requestJson(`/api/v1/numbering-resolutions/${resolution.id}/${action}`, {
      method: 'PUT',
      ...context,
      idempotencyKey: createIdempotencyKey(`resolution-${action}`),
    });
    await loadFiscalConfiguration();
    return result;
  }

  function hydrateDianConfigurationForm(configuration) {
    setDianConfigurationForm({
      ...createDianConfigurationForm(),
      mode: configuration?.mode || 'MOCK',
      environment: configuration?.environment || 'TEST',
      softwareId: configuration?.softwareId || '',
      certificateAlias: configuration?.certificateAlias || '',
      certificateFingerprint: configuration?.certificateFingerprint || '',
      certificateExpiresAt: configuration?.certificateExpiresAt
        ? toDateTimeLocalValue(new Date(configuration.certificateExpiresAt))
        : '',
      serviceBaseUrl: configuration?.serviceBaseUrl || '',
      testSetId: configuration?.testSetId || '',
      acceptedResponsibility: Boolean(configuration?.acceptedResponsibility),
    });
  }

  async function loadDianConfiguration() {
    requireCompany();
    try {
      const configuration = await requestJson(`/api/v1/dian-configuration/companies/${activeCompanyId}`, context);
      setDianConfiguration(configuration);
      hydrateDianConfigurationForm(configuration);
      return configuration;
    } catch (caught) {
      if (caught.status === 404) {
        setDianConfiguration(null);
        hydrateDianConfigurationForm(null);
        return null;
      }
      throw caught;
    }
  }

  async function saveDianConfiguration() {
    requireCompany();
    const configuration = await requestJson(`/api/v1/dian-configuration/companies/${activeCompanyId}`, {
      ...context,
      method: 'PUT',
      body: {
        ...dianConfigurationForm,
        certificateExpiresAt: dianConfigurationForm.certificateExpiresAt
          ? new Date(dianConfigurationForm.certificateExpiresAt).toISOString()
          : null,
      },
    });
    setDianConfiguration(configuration);
    hydrateDianConfigurationForm(configuration);
    return configuration;
  }

  async function testDianConfiguration() {
    requireCompany();
    const configuration = await requestJson(`/api/v1/dian-configuration/companies/${activeCompanyId}/test`, {
      ...context,
      method: 'POST',
    });
    setDianConfiguration(configuration);
    hydrateDianConfigurationForm(configuration);
    return configuration;
  }

  async function activateDianConfiguration() {
    requireCompany();
    const configuration = await requestJson(`/api/v1/dian-configuration/companies/${activeCompanyId}/activate`, {
      ...context,
      method: 'POST',
    });
    setDianConfiguration(configuration);
    hydrateDianConfigurationForm(configuration);
    return configuration;
  }

  async function deactivateDianConfiguration() {
    requireCompany();
    const configuration = await requestJson(`/api/v1/dian-configuration/companies/${activeCompanyId}/deactivate`, {
      ...context,
      method: 'POST',
    });
    setDianConfiguration(configuration);
    hydrateDianConfigurationForm(configuration);
    return configuration;
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
      setSalesList((current) => [result, ...current.filter((item) => item.id !== result.id)]);
    }
    return result;
  }

  async function closeSale() {
    requireCompany();
    const result = await requestJson('/api/v1/sales/close', {
      method: 'POST',
      body: buildSalePayload(saleForm),
      ...context,
      idempotencyKey: createIdempotencyKey('close-sale'),
    });
    if (result?.id) {
      setSalesList((current) => [result, ...current.filter((item) => item.id !== result.id)]);
      await openSaleReceipt(result.id);
    }
    setSaleId('');
    setSaleForm(createSaleForm());
    setCustomerSearch('');
    setSelectedCustomer(null);
    setCustomerOptions([]);
    setServiceConsumption(createServiceConsumptionState());
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
    const result = await requestJson(`/api/v1/sales/${saleId}/confirm`, {
      method: 'POST',
      ...context,
      idempotencyKey: createIdempotencyKey('confirm-sale'),
    });
    setSalesList((current) => [result, ...current.filter((item) => item.id !== result.id)]);
    if (result?.id) {
      await openSaleReceipt(result.id);
    }
    return result;
  }

  async function openSaleReceipt(targetSaleId = saleId) {
    requireCompany();
    if (!targetSaleId) {
      throw new Error('No hay una venta seleccionada para imprimir.');
    }
    const result = await requestDownload(`/api/v1/sales/${targetSaleId}/receipt${buildQuery({ widthMm: 80 })}`, {
      method: 'POST',
      ...context,
      idempotencyKey: createIdempotencyKey('print-receipt'),
    });
    openBlob(result.blob);
    return { filename: result.filename };
  }

  async function loadThirdPartyList() {
    requireCompany();
    const type = operationalListFilters.thirdPartyType === 'SUPPLIER' ? 'suppliers' : 'customers';
    const items = await requestJson(`/api/v1/${type}${buildQuery({ active: optionalBoolean(operationalListFilters.thirdPartyActive) })}`, context);
    setThirdPartyList(items || []);
    return items || [];
  }

  async function loadProductList() {
    requireCompany();
    const items = await requestJson(`/api/v1/products${buildQuery({ active: optionalBoolean(operationalListFilters.productActive) })}`, context);
    setProductList(items || []);
    return items || [];
  }

  async function loadPurchaseList() {
    requireCompany();
    const items = await requestJson(`/api/v1/purchases${buildQuery({
      status: operationalListFilters.purchaseStatus,
      from: operationalListFilters.purchaseFrom,
      to: operationalListFilters.purchaseTo,
    })}`, context);
    setPurchaseList(items || []);
    return items || [];
  }

  async function loadSalesList() {
    requireCompany();
    const items = await requestJson(`/api/v1/sales/history${buildQuery({
      status: operationalListFilters.saleStatus,
      from: operationalListFilters.saleFrom,
      to: operationalListFilters.saleTo,
      paymentMethodCode: operationalListFilters.salePaymentMethodCode,
      documentStatus: operationalListFilters.saleDocumentStatus,
    })}`, context);
    setSalesList(items || []);
    return items || [];
  }

  async function openSaleDetail(sale) {
    requireCompany();
    const saleIdToLoad = sale?.id;
    if (!saleIdToLoad) {
      throw new Error('No hay una venta seleccionada para consultar.');
    }
    const detail = await requestJson(`/api/v1/sales/${saleIdToLoad}`, context);
    setSelectedSaleDetail(detail);
    return detail;
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
    const definition = reportDefinitions.find((report) => report.code === reportsForm.reportCode);
    if (!definition) {
      throw new Error('Selecciona un reporte.');
    }
    const filters = reportsForm.filters || {};
    for (const filter of definition.filters || []) {
      if (filter.required && !filters[filter.code]) {
        throw new Error(`El filtro ${filter.label || filter.code} es obligatorio.`);
      }
    }
    const result = await requestJson('/api/v1/reports/query', {
      method: 'POST',
      body: {
        reportCode: reportsForm.reportCode,
        chartType: reportsForm.chartType || 'TABLE',
        from: filters.from || null,
        to: filters.to || null,
        filters,
      },
      ...context,
    });
    setReportsData(result);
    return result;
  }

  async function exportReport(format) {
    requireCompany();
    const definition = reportDefinitions.find((report) => report.code === reportsForm.reportCode);
    if (!definition) {
      throw new Error('Selecciona un reporte.');
    }
    const filters = reportsForm.filters || {};
    const result = await requestDownload(`/api/v1/reports/export${buildQuery({ format })}`, {
      method: 'POST',
      body: {
        reportCode: reportsForm.reportCode,
        chartType: reportsForm.chartType || 'TABLE',
        from: filters.from || null,
        to: filters.to || null,
        filters,
      },
      ...context,
    });
    downloadBlob(result.blob, result.filename);
    return { filename: result.filename };
  }

  async function createReportExportJob() {
    requireCompany();
    const definition = reportDefinitions.find((report) => report.code === reportsForm.reportCode);
    if (!definition) {
      throw new Error('Selecciona un reporte.');
    }
    const filters = reportsForm.filters || {};
    for (const filter of definition.filters || []) {
      if (filter.required && !filters[filter.code]) {
        throw new Error(`El filtro ${filter.label || filter.code} es obligatorio.`);
      }
    }
    const created = await requestJson('/api/v1/reports/export-jobs', {
      method: 'POST',
      body: {
        reportCode: reportsForm.reportCode,
        chartType: reportsForm.chartType || 'TABLE',
        format: reportsForm.exportFormat || 'XLS',
        from: filters.from || null,
        to: filters.to || null,
        filters,
        notifyByEmail: Boolean(reportsForm.notifyByEmail),
      },
      ...context,
      idempotencyKey: createIdempotencyKey('report-export-job'),
    });
    await loadReportJobs();
    return created;
  }

  async function loadReportJobs() {
    requireCompany();
    const jobs = await requestJson('/api/v1/reports/export-jobs', context);
    setReportJobs(jobs || []);
    return jobs;
  }

  async function openReportExportDownload(jobId) {
    requireCompany();
    const result = await requestJson(`/api/v1/reports/export-jobs/${jobId}/download-link`, {
      method: 'POST',
      ...context,
      idempotencyKey: createIdempotencyKey('report-download-link'),
    });
    if (result?.downloadLink) {
      window.open(result.downloadLink, '_blank', 'noopener');
    }
    await loadReportJobs();
    return result;
  }

  async function loadReportDefinitions() {
    requireCompany();
    const definitions = await requestJson('/api/v1/report-definitions', context);
    const safeDefinitions = definitions || [];
    setReportDefinitions(safeDefinitions);
    if (safeDefinitions.length === 0) {
      setReportOptions({});
      return safeDefinitions;
    }
    const currentDefinition = safeDefinitions.find((report) => report.code === reportsForm.reportCode);
    const selectedDefinition = currentDefinition || safeDefinitions[0];
    if (!currentDefinition) {
      await selectReportDefinition(selectedDefinition.code, safeDefinitions);
    } else {
      await loadReportOptions(selectedDefinition.code, safeDefinitions);
    }
    return safeDefinitions;
  }

  async function selectReportDefinition(reportCode, definitions = reportDefinitions) {
    const definition = definitions.find((report) => report.code === reportCode);
    if (!definition) {
      setReportsForm(createReportsForm());
      setReportOptions({});
      setReportsData(null);
      return null;
    }
    setReportsForm((current) => ({
      reportCode: definition.code,
      chartType: definition.chartTypes?.[0] || 'TABLE',
      exportFormat: current.exportFormat || 'XLS',
      notifyByEmail: Boolean(current.notifyByEmail),
      filters: defaultReportFilters(definition, current.filters),
    }));
    setReportsData(null);
    await loadReportOptions(definition.code, definitions);
    return null;
  }

  async function loadReportOptions(reportCode, definitions = reportDefinitions) {
    requireCompany();
    const definition = definitions.find((report) => report.code === reportCode);
    if (!definition || !(definition.filters || []).some((filter) => filter.type === 'SELECT')) {
      setReportOptions({});
      return {};
    }
    const result = await requestJson(`/api/v1/reports/${reportCode}/options`, context);
    setReportOptions(result?.options || {});
    return result?.options || {};
  }

  async function saveAccountingConfiguration(payload) {
    requireCompany();
    const setup = await requestJson('/api/v1/accounting-configuration/batch', {
      method: 'POST',
      ...context,
      body: payload,
      idempotencyKey: createIdempotencyKey('accounting-configuration'),
    });
    setAccountingAccounts(setup?.accounts || []);
    setAccountingRules(setup?.rules || []);
    return setup;
  }

  async function loadAccountingConfiguration() {
    requireCompany();
    const [accounts, rules] = await Promise.all([
      requestJson('/api/v1/accounts', context),
      requestJson('/api/v1/accounting-rules', context),
    ]);
    setAccountingAccounts(accounts || []);
    setAccountingRules(rules || []);
    return { accounts, rules };
  }

  async function updateAccountingAccount(accountId, payload) {
    requireCompany();
    const account = await requestJson(`/api/v1/accounts/${accountId}`, {
      method: 'PUT',
      ...context,
      body: payload,
    });
    setAccountingAccounts((current) => current.map((item) => item.id === account.id ? account : item));
    return account;
  }

  async function deactivateAccountingAccount(accountId) {
    requireCompany();
    const account = await requestJson(`/api/v1/accounts/${accountId}/deactivate`, {
      method: 'POST',
      ...context,
      idempotencyKey: createIdempotencyKey('account-deactivate'),
    });
    setAccountingAccounts((current) => current.map((item) => item.id === account.id ? account : item));
    return account;
  }

  async function updateAccountingRule(ruleId, payload) {
    requireCompany();
    const rule = await requestJson(`/api/v1/accounting-rules/${ruleId}`, {
      method: 'PUT',
      ...context,
      body: payload,
    });
    setAccountingRules((current) => current.map((item) => item.id === rule.id ? rule : item));
    return rule;
  }

  async function deactivateAccountingRule(ruleId) {
    requireCompany();
    const rule = await requestJson(`/api/v1/accounting-rules/${ruleId}/deactivate`, {
      method: 'POST',
      ...context,
      idempotencyKey: createIdempotencyKey('accounting-rule-deactivate'),
    });
    setAccountingRules((current) => current.map((item) => item.id === rule.id ? rule : item));
    return rule;
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
      items: [...current.items, { productId: '', productName: '', itemType: '', quantity: '1', unitPrice: '0', discountAmount: '0', taxCode: '', taxRate: '' }],
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
    setIssuerProfiles([]);
    setNumberingResolutions([]);
    setAccountingAccounts([]);
    setAccountingRules([]);
    autoAccountingLoadKeyRef.current = '';
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
            {companyBranding?.loginLogoUrl && <img alt={activeBrandName} className="login-logo" src={companyBranding.loginLogoUrl} />}
            <span>{PRODUCT_NAME}</span>
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
        <div className="brand">
          {activeBrandLogo && <img alt={activeBrandName} src={activeBrandLogo} />}
          <span>{activeBrandName}</span>
        </div>
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
          <CompanySessionPanel accesses={companyAccesses} companies={rootCompanies} activeCompanyId={activeCompanyId} activeCompany={activeCompany} activeAccess={activeAccess} license={license} session={session} isRoot={isRoot} onCompanyChange={changeCompany} onLogout={logout} busy={busy} branding={companyBranding} productName={PRODUCT_NAME} />
        </header>

        <section className="panel-grid">
          {currentStep === 'Empresa' && (
            <>
              <CompanyForm
                form={companyForm}
                setForm={setCompanyForm}
                companies={rootCompanies}
                activeCompanyId={activeCompanyId}
                activeCompany={activeCompany}
                editingCompanyId={editingCompanyId}
                isRoot={isRoot}
                onSubmit={() => execute(isRoot && editingCompanyId ? updateCompany : isRoot ? createCompany : updateCompany)}
                onEditCompany={editCompanyFromTable}
                onToggleCompanyActive={(company) => execute(() => toggleCompanyActiveFromTable(company))}
                onOpenAdminModal={openAdminModalForCompany}
                onOpenBrandingModal={(company) => execute(() => openBrandingModalForCompany(company), { silentNullSuccess: true })}
                onNew={startNewCompany}
                busy={busy}
                documentTypeOptions={runtimeCatalogs.dianDocumentTypes}
              />
              {!isRoot && <CompanyBrandingPanel form={companyBrandingForm} setForm={setCompanyBrandingForm} branding={companyBranding} onSave={() => execute(saveCompanyBranding)} onUploadAsset={(purpose, file) => execute(() => uploadCompanyBrandingAsset(purpose, file))} busy={busy} disabled={!activeCompanyId || !canUse(['COMPANY_SETTINGS_MANAGE'])} />}
            </>
          )}
          {currentStep === 'Licencias' && (
            <LicenseAdminPanel form={licenseForm} setForm={setLicenseForm} companies={rootCompanies} license={managedLicense} usage={licenseUsage} onCompanyChange={selectLicenseCompany} onLoad={() => execute(loadManagedLicense)} onSave={() => execute(saveManagedLicense)} onActivate={() => execute(activateManagedLicense)} onSuspend={() => execute(suspendManagedLicense)} busy={busy || !isRoot} />
          )}
          {currentStep === 'Terceros' && (
            <ThirdPartyForm form={thirdPartyForm} setForm={setThirdPartyForm} companyMunicipalityCode={companyMunicipalityCode} onSubmit={() => execute(createThirdParty)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Terceros)} documentTypeOptionsSource={runtimeCatalogs.dianDocumentTypes} taxResponsibilityOptionsSource={runtimeCatalogs.taxResponsibilityOptions} taxRegimeOptionsSource={runtimeCatalogs.taxRegimeOptions} thirdPartyRoleCatalog={runtimeCatalogs.thirdPartyRoleCatalog} personTypeCatalog={runtimeCatalogs.personTypeCatalog} locations={runtimeCatalogs.locations} listFilters={operationalListFilters} setListFilters={setOperationalListFilters} thirdParties={thirdPartyList} onLoadThirdParties={() => execute(loadThirdPartyList)} />
          )}
          {currentStep === 'Inventario' && (
            <ProductForm form={productForm} setForm={setProductForm} onSubmit={() => execute(createProduct)} busy={busy || !activeCompanyId || !canUse(['INVENTORY_MANAGE'])} taxOptions={runtimeCatalogs.salesTaxOptions} itemTypeCatalog={runtimeCatalogs.itemTypeCatalog} listFilters={operationalListFilters} setListFilters={setOperationalListFilters} products={productList} purchases={purchaseList} onLoadProducts={() => execute(loadProductList)} onLoadPurchases={() => execute(loadPurchaseList)} />
          )}
          {currentStep === 'Fiscal' && (
            <>
              <FiscalPolicyForm form={fiscalPolicyForm} setForm={setFiscalPolicyForm} policy={fiscalPolicy} onSubmit={() => execute(configureFiscalPolicy)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Fiscal)} fiscalDocumentTypeOptions={runtimeCatalogs.fiscalDocumentTypeOptions} />
              <div className="split">
                <IssuerForm form={issuerForm} setForm={setIssuerForm} activeCompany={activeCompany} onSubmit={() => execute(configureIssuer)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Fiscal)} taxResponsibilityOptionsSource={runtimeCatalogs.taxResponsibilityOptions} locations={runtimeCatalogs.locations} />
                <ResolutionForm form={resolutionForm} setForm={setResolutionForm} onSubmit={() => execute(configureResolution)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Fiscal)} fiscalDocumentTypeOptions={runtimeCatalogs.fiscalDocumentTypeOptions} environmentOptions={runtimeCatalogs.fiscalEnvironmentOptions} />
              </div>
              <div className="split">
                <DataTable title="Emisores fiscales registrados" description="Solo un emisor fiscal puede estar activo por empresa." columns={['Razon social', 'NIT', 'Municipio', 'Estado', 'Acciones']} rows={issuerProfiles.map((issuer) => [
                  issuer.legalName,
                  `${issuer.nit}-${issuer.verificationDigit}`,
                  issuer.municipalityCode || 'Sin municipio',
                  issuer.active ? 'Activo' : 'Inactivo',
                  { searchText: issuer.active ? 'activo' : 'inactivo', content: <button className="secondary" disabled={busy} onClick={() => execute(() => toggleIssuerActive(issuer))} type="button">{issuer.active ? 'Inactivar' : 'Activar'}</button> },
                ])} rowKey={(row) => row[1]} pageSize={5} />
                <DataTable title="Resoluciones registradas" description="La resolucion de numeracion autoriza prefijo, rango, vigencia y tipo de documento fiscal ante la DIAN." columns={['Tipo', 'Resolucion', 'Rango', 'Vigencia', 'Estado', 'Acciones']} rows={numberingResolutions.map((resolution) => [
                  resolution.documentType,
                  `${resolution.prefix || 'Sin prefijo'} ${resolution.resolutionNumber}`,
                  `${resolution.fromNumber} - ${resolution.toNumber} actual ${resolution.currentNumber}`,
                  `${resolution.validFrom} / ${resolution.validTo}`,
                  resolution.active ? 'Activa' : 'Inactiva',
                  { searchText: resolution.active ? 'activa' : 'inactiva', content: <button className="secondary" disabled={busy} onClick={() => execute(() => toggleResolutionActive(resolution))} type="button">{resolution.active ? 'Inactivar' : 'Activar'}</button> },
                ])} rowKey={(row) => row[1]} pageSize={5} />
              </div>
            </>
          )}
          {currentStep === 'DIAN' && (
            <DianConfigurationPanel form={dianConfigurationForm} setForm={setDianConfigurationForm} configuration={dianConfiguration} onLoad={() => execute(loadDianConfiguration, { silentNullSuccess: true })} onSave={() => execute(saveDianConfiguration, { successMessage: 'Configuracion DIAN guardada correctamente.' })} onTest={() => execute(testDianConfiguration, { successMessage: 'Prueba de conexion DIAN finalizada.' })} onActivate={() => execute(activateDianConfiguration, { successMessage: 'Configuracion DIAN activada.' })} onDeactivate={() => execute(deactivateDianConfiguration, { successMessage: 'Configuracion DIAN inactivada.' })} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.DIAN)} />
          )}
          {currentStep === 'Documentos fiscales' && (
            <FiscalNotesPanel forms={fiscalNoteForms} setForms={setFiscalNoteForms} results={fiscalNoteResults} onSubmit={(noteType) => execute(() => createFiscalNote(noteType), { successMessage: 'Documento fiscal creado correctamente.' })} busy={busy || !activeCompanyId || !canUse(stepPermissionRules['Documentos fiscales'])} />
          )}
          {currentStep === 'Configuracion contable' && (
            <AccountingConfigurationPanel accounts={accountingAccounts} rules={accountingRules} onLoad={() => execute(loadAccountingConfiguration, { successMessage: 'Estado contable actualizado.' })} onConfigure={(payload) => execute(() => saveAccountingConfiguration(payload), { successMessage: 'Configuracion contable guardada correctamente.' })} onUpdateAccount={(accountId, payload) => execute(() => updateAccountingAccount(accountId, payload), { successMessage: 'Cuenta contable actualizada correctamente.' })} onDeactivateAccount={(accountId) => execute(() => deactivateAccountingAccount(accountId), { successMessage: 'Cuenta contable inactivada correctamente.' })} onUpdateRule={(ruleId, payload) => execute(() => updateAccountingRule(ruleId, payload), { successMessage: 'Regla contable actualizada correctamente.' })} onDeactivateRule={(ruleId) => execute(() => deactivateAccountingRule(ruleId), { successMessage: 'Regla contable inactivada correctamente.' })} busy={busy || !activeCompanyId || !canUse(stepPermissionRules['Configuracion contable'])} />
          )}
          {currentStep === 'Ventas' && (
            <SaleForm form={saleForm} setForm={setSaleForm} saleId={saleId} customerSearch={customerSearch} setCustomerSearch={setCustomerSearch} customerOptions={customerOptions} selectedCustomer={selectedCustomer} onSearchCustomers={searchCustomers} onSelectCustomer={selectCustomer} updateItem={updateSaleItem} addItem={addSaleItem} removeItem={removeSaleItem} onScanBarcode={(barcode) => execute(() => scanSaleBarcode(barcode))} onClose={() => execute(closeSale, { successMessage: 'Venta cerrada correctamente.' })} onPrintReceipt={(targetSaleId) => execute(() => openSaleReceipt(targetSaleId))} serviceConsumption={serviceConsumption} onLoadServiceConsumption={(serviceProductId) => execute(() => loadServiceConsumptionSuggestions(serviceProductId))} onUpdateServiceConsumptionQuantity={updateServiceConsumptionQuantity} onUpdateServiceConsumptionReason={updateServiceConsumptionReason} onConfirmServiceConsumption={() => execute(confirmServiceSupplyConsumption)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Ventas)} paymentOptions={runtimeCatalogs.paymentMethodOptions} walletOptions={runtimeCatalogs.virtualWalletOptions} />
          )}
          {currentStep === 'Registro de Ventas' && (
            <SalesRegistryPanel sales={salesList} selectedSale={selectedSaleDetail} listFilters={operationalListFilters} setListFilters={setOperationalListFilters} onLoadSales={() => execute(loadSalesList)} onViewDetail={(sale) => execute(() => openSaleDetail(sale), { silentNullSuccess: true })} onCloseDetail={() => setSelectedSaleDetail(null)} busy={busy || !activeCompanyId || !canUse(stepPermissionRules['Registro de Ventas'])} paymentOptions={runtimeCatalogs.paymentMethodOptions} />
          )}
          {currentStep === 'Nomina' && (
            <PayrollPanel settingsForm={payrollSettingsForm} setSettingsForm={setPayrollSettingsForm} workerForm={payrollWorkerForm} setWorkerForm={setPayrollWorkerForm} paymentForm={dailyLaborPaymentForm} setPaymentForm={setDailyLaborPaymentForm} workers={payrollWorkers} payments={dailyLaborPayments} electronicDocuments={electronicPayrollDocuments} documentTypeOptions={runtimeCatalogs.dianDocumentTypes} workerClassificationOptions={runtimeCatalogs.payrollWorkerClassificationOptions} paymentMethodOptions={runtimeCatalogs.paymentMethodOptions} onLoad={() => execute(loadPayrollData)} onSaveSettings={() => execute(savePayrollSettings)} onCreateWorker={() => execute(createPayrollWorker)} onCreateDailyPayment={() => execute(createDailyLaborPayment)} onIssueElectronicDocument={(paymentId) => execute(() => issueElectronicPayrollDocument(paymentId))} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Nomina)} />
          )}
          {currentStep === 'Reportes' && (
            <ReportsForm definitions={reportDefinitions} options={reportOptions} form={reportsForm} setForm={setReportsForm} data={reportsData} jobs={reportJobs} onReportChange={(reportCode) => execute(() => selectReportDefinition(reportCode), { silentNullSuccess: true })} onLoadDefinitions={() => execute(loadReportDefinitions)} onSubmit={() => execute(loadReports)} onExport={(format) => execute(() => exportReport(format), { successMessage: 'Reporte descargado correctamente.' })} onCreateExportJob={() => execute(createReportExportJob, { successMessage: 'Reporte en segundo plano creado correctamente.' })} onLoadExportJobs={() => execute(loadReportJobs, { silentNullSuccess: true })} onDownloadExportJob={(jobId) => execute(() => openReportExportDownload(jobId), { successMessage: 'Enlace de descarga generado correctamente.' })} busy={busy || !activeCompanyId || !canUse(stepPermissionRules.Reportes)} />
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
      {adminModalOpen && <AdminModal form={companyAdminForm} setForm={setCompanyAdminForm} activeCompany={adminTargetCompany || activeCompany} activeCompanyId={adminTargetCompanyId || activeCompanyId} onSubmit={() => execute(createInitialCompanyAdmin)} onClose={() => {
        setAdminModalOpen(false);
        setAdminTargetCompanyId('');
      }} busy={busy || !(adminTargetCompanyId || activeCompanyId)} />}
      {brandingModalOpen && <CompanyBrandingModal
        form={brandingEditorForm}
        setForm={setBrandingEditorForm}
        branding={brandingEditor}
        company={brandingTargetCompany}
        companyId={brandingTargetCompanyId}
        onSave={() => execute(() => saveCompanyBranding(brandingTargetCompanyId, brandingEditorForm, false), { successMessage: 'Marca empresarial guardada correctamente.' })}
        onUploadAsset={(purpose, file) => execute(() => uploadCompanyBrandingAsset(purpose, file, brandingTargetCompanyId, false))}
        onClose={() => {
          setBrandingModalOpen(false);
          setBrandingTargetCompanyId('');
          setBrandingEditor(null);
          setBrandingEditorForm(createCompanyBrandingForm());
        }}
        busy={busy || !brandingTargetCompanyId}
      />}
    </main>
  );
}

function toInstantQuery(value) {
  if (!value) {
    return '';
  }
  return new Date(value).toISOString();
}

function defaultReportFilters(definition, currentFilters = {}) {
  const today = new Date();
  const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
  const defaults = {
    from: currentFilters?.from || toDateInputValue(firstDay),
    to: currentFilters?.to || toDateInputValue(today),
  };
  return (definition.filters || []).reduce((filters, filter) => ({
    ...filters,
    [filter.code]: currentFilters?.[filter.code] || defaults[filter.code] || '',
  }), {});
}

function toDateInputValue(date) {
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}

function downloadBlob(blob, filename) {
  const url = URL.createObjectURL(blob);
  const anchor = document.createElement('a');
  anchor.href = url;
  anchor.download = filename;
  anchor.click();
  URL.revokeObjectURL(url);
}

function openBlob(blob) {
  const url = URL.createObjectURL(blob);
  window.open(url, '_blank', 'noopener,noreferrer');
  window.setTimeout(() => URL.revokeObjectURL(url), 60000);
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

function createOperationalListFilters() {
  return {
    thirdPartyType: 'CUSTOMER',
    thirdPartyActive: 'true',
    productActive: 'true',
    purchaseStatus: '',
    purchaseFrom: '',
    purchaseTo: '',
    saleStatus: '',
    saleFrom: '',
    saleTo: '',
    salePaymentMethodCode: '',
    saleDocumentStatus: '',
  };
}

function optionalBoolean(value) {
  if (value === 'true') {
    return true;
  }
  if (value === 'false') {
    return false;
  }
  return '';
}

function toDateTimeLocalValue(date) {
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(date.getHours())}:${pad(date.getMinutes())}`;
}
