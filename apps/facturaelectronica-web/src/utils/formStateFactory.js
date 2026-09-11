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
    primaryColor: '#1f78a8',
    accentColor: '#2a7c61',
  };
}

export function createCompanyTaxProfileForm() {
  return {
    companySize: 'MICRO',
    financialReportingGroup: 'GRUPO_3',
    taxRegime: '',
    rutResponsibilities: [],
    vatResponsible: false,
    withholdingAgent: false,
    vatWithholdingAgent: false,
    icaWithholdingAgent: false,
    largeTaxpayer: false,
    selfWithholding: false,
    simpleRegime: false,
    icaMunicipalityCode: '',
    ciiuCodes: [],
    taxResidency: 'COLOMBIA',
    incomeTaxStatus: 'UNKNOWN',
    selfWithholdingScopes: [],
    fiscalEvidenceReference: '',
  };
}

export function createInvoicingObligationForm() {
  return {
    personType: '',
    rutGeneratedAt: '',
    economicOperationTypes: ['TAXED_GOODS_SALE'],
    customsUser: false,
    establishmentCount: '1',
    exploitsIntangibles: false,
    onlyExcludedOrUntaxedOperations: false,
    previousYearGrossActivityIncome: '',
    currentYearGrossActivityIncome: '',
    previousYearTaxedActivityFinancialOperations: '',
    currentYearTaxedActivityFinancialOperations: '',
    largestPreviousYearTaxedContract: '',
    largestCurrentYearTaxedContract: '',
    largestSameCustomerAggregate: '',
    voluntaryElectronicInvoicer: false,
    specialExceptionType: '',
    specialExceptionScope: '',
    rutAssetId: '',
    rutFile: null,
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
    enabledFeatures: [],
  };
}

export function createManagedUserForm() {
  return { fullName: '', email: '', password: '', roleId: '' };
}

export function createCompanyRoleForm() {
  return { name: '', description: '', permissionCodes: [] };
}

export function createOperationalPinForm() {
  return { pin: '' };
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
    ciiuCodes: [],
    taxResponsibilities: [],
    taxRegime: '',
    taxResidency: 'COLOMBIA',
    incomeTaxStatus: 'UNKNOWN',
    selfWithholdingScopes: [],
    fiscalEvidenceReference: '',
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

export function createPurchaseForm() {
  return {
    supplierId: '',
    paymentCondition: 'CASH',
    dueDate: '',
    evidenceType: '',
    evidenceUrl: '',
    evidenceFile: null,
    fiscalConceptCode: 'ANY',
    lines: [{ description: '', subtotal: '', tax: '0', total: '' }],
  };
}

export function createExpenseForm() {
  return {
    supplierId: '',
    expenseType: 'OPERATING_EXPENSE',
    expenseDate: toDateInputValue(new Date()),
    concept: '',
    subtotal: '',
    taxTotal: '0',
    total: '',
    fiscalConceptCode: 'ANY',
    paymentCondition: 'CASH',
    dueDate: '',
    evidenceType: '',
    evidenceUrl: '',
    evidenceFile: null,
  };
}

export function createAccountsReceivableForm() {
  const today = new Date();
  const dueDate = new Date(today);
  dueDate.setDate(today.getDate() + 30);
  return {
    customerId: '',
    issueDate: toDateInputValue(today),
    dueDate: toDateInputValue(dueDate),
    totalAmount: '',
  };
}

export function createReceivablePaymentForm() {
  return {
    receivableId: '',
    paymentDate: toDateInputValue(new Date()),
    amount: '',
    paymentMethod: '',
    reference: '',
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
    defaultSaleDocumentType: 'NON_FISCAL_SALE',
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
    certificateFile: null,
    certificatePassword: '',
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

export function createSaleDocumentOverrideForm() {
  return {
    documentType: 'ELECTRONIC_POS',
    authorizedBy: '',
    authorizedBySearch: '',
    pin: '',
    reason: '',
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
