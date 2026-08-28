export function createLoginForm() {
  return { email: '', password: '' };
}

export function createCompanyForm() {
  return {
    legalName: '',
    tradeName: '',
    identificationTypeCode: '',
    identificationNumber: '',
    verificationDigit: '',
    email: '',
  };
}

export function createCompanyBrandingForm() {
  return {
    displayName: '',
    primaryColor: '',
    accentColor: '',
  };
}

export function createCompanyAdminForm() {
  return { fullName: '', email: '', password: '', role: 'OWNER' };
}

export function createLicenseForm() {
  const today = new Date();
  const nextYear = new Date(today);
  nextYear.setFullYear(today.getFullYear() + 1);
  return {
    companyId: '',
    planCode: 'CUSTOM',
    validFrom: toDateInputValue(today),
    validTo: toDateInputValue(nextYear),
    maxUsers: '',
    maxMonthlyDocuments: '',
    enabledModules: [],
  };
}

export function createManagedUserForm() {
  return { fullName: '', email: '', password: '', roleId: '' };
}

export function createCompanyRoleForm() {
  return { name: '', description: '', permissionCodes: [] };
}

export function createCatalogItemForm() {
  return {
    editingCode: '',
    code: '',
    label: '',
    description: '',
    regulatory: false,
    source: 'APP',
    sourceVersion: '',
    validFrom: '',
    validTo: '',
    sortOrder: '10',
  };
}

export function createThirdPartyForm() {
  return {
    thirdPartyType: '',
    personType: '',
    identificationTypeCode: '',
    identificationNumber: '',
    fullName: '',
    businessName: '',
    tradeName: '',
    email: '',
    phone: '',
    address: '',
    municipalityCode: '',
    taxResponsibilities: [],
    taxRegime: '',
  };
}

export function createProductForm() {
  return {
    sku: '',
    barcode: '',
    name: '',
    description: '',
    itemType: '',
    saleEnabled: false,
    purchaseEnabled: false,
    stockTracked: false,
    salePrice: '',
    finalSalePrice: '',
    cost: '',
    initialStock: '',
    taxCategoryCode: '',
    taxCode: '',
    taxLabel: '',
    taxRate: '',
  };
}

export function createIssuerForm() {
  return {
    legalName: '',
    nit: '',
    verificationDigit: '',
    taxResponsibilities: [],
    municipalityCode: '',
    address: '',
  };
}

export function createResolutionForm() {
  return {
    documentType: 'ELECTRONIC_INVOICE',
    resolutionNumber: '',
    prefix: '',
    fromNumber: '',
    toNumber: '',
    validFrom: '',
    validTo: '',
    environment: '',
  };
}

export function createFiscalPolicyForm() {
  return {
    defaultSaleDocumentType: 'ELECTRONIC_INVOICE',
    allowDocumentTypeOverride: true,
    requirePinForOverride: true,
  };
}

export function createFiscalNoteForm() {
  return {
    originalDocumentId: '',
    adjustmentKind: 'CORRECTION',
    reason: '',
    subtotal: '',
    taxTotal: '',
    total: '',
  };
}

export function createDianConfigurationForm() {
  return {
    mode: 'MOCK',
    environment: 'TEST',
    softwareId: '',
    softwarePin: '',
    technicalKey: '',
    certificatePayload: '',
    certificatePassword: '',
    certificateAlias: '',
    certificateFingerprint: '',
    certificateExpiresAt: '',
    serviceBaseUrl: '',
    testSetId: '',
    acceptedResponsibility: false,
  };
}

export function createSaleForm() {
  return {
    buyerIdentificationMode: 'FINAL_CONSUMER',
    customerId: '',
    paymentMethodCode: '',
    virtualWalletCode: '',
    items: [{ productId: '', productName: '', itemType: '', quantity: '1', unitPrice: '0', discountAmount: '0', taxCode: '', taxRate: '' }],
  };
}

export function createServiceConsumptionState() {
  return {
    serviceProductId: '',
    sourceDocumentId: '',
    reason: 'Consumo real de insumos por servicio facturado',
    suggestions: [],
    quantities: {},
  };
}

export function createReportsForm() {
  const today = new Date();
  const firstDay = new Date(today.getFullYear(), today.getMonth(), 1);
  return {
    reportCode: '',
    chartType: 'TABLE',
    exportFormat: 'XLS',
    notifyByEmail: false,
    filters: {
      from: toDateInputValue(firstDay),
      to: toDateInputValue(today),
    },
  };
}

export function createPayrollSettingsForm() {
  return { electronicPayrollEnabled: false, providerMode: 'MOCK' };
}

export function createPayrollWorkerForm() {
  return {
    identificationTypeCode: '',
    identificationNumber: '',
    verificationDigit: '',
    fullName: '',
    workerClassification: '',
    active: true,
  };
}

export function createDailyLaborPaymentForm() {
  return {
    workerId: '',
    workDate: toDateInputValue(new Date()),
    activityDescription: '',
    agreedAmount: '',
    paidAmount: '',
    paymentMethodCode: '',
    legalNoticeAccepted: false,
    notes: '',
  };
}

function toDateInputValue(date) {
  const pad = (value) => String(value).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
}
