const demoStamp = Date.now().toString().slice(-8);

export const initialLogin = { email: '', password: '' };

export const initialCompany = {
  legalName: `Empresa Demo ${demoStamp} SAS`,
  tradeName: `Tienda Demo ${demoStamp}`,
  identificationTypeCode: 31,
  identificationNumber: `90${demoStamp}`,
  verificationDigit: '7',
  email: `admin-${demoStamp}@example.com`,
};

export const initialCompanyAdmin = {
  fullName: 'Administrador Empresa',
  email: 'admin.empresa@example.com',
  password: 'AdminDemo#2026!',
  role: 'OWNER',
};

export const initialManagedUser = {
  fullName: 'Usuario Vendedor',
  email: 'vendedor@example.com',
  password: 'VendedorDemo#2026!',
};

export const initialCompanyRole = {
  name: 'VENDEDOR',
  description: 'Puede registrar ventas POS y consultar inventario.',
  permissionCodes: ['SALES_CREATE', 'FISCAL_DOCUMENTS_ISSUE', 'INVENTORY_VIEW'],
};

export const initialRoleAssignment = {
  userId: '',
  roleIds: [],
};

export const initialCatalogItem = {
  editingCode: '',
  code: '',
  label: '',
  description: '',
  regulatory: false,
  source: 'APP',
  sourceVersion: '2026-08',
  validFrom: '',
  validTo: '',
  sortOrder: '10',
};

export const initialThirdParty = {
  thirdPartyType: 'CUSTOMER',
  personType: 'NATURAL',
  identificationTypeCode: 13,
  identificationNumber: '1234567890',
  fullName: 'Cliente Demo',
  businessName: '',
  tradeName: '',
  email: 'cliente@example.com',
  phone: '3000000000',
  address: '',
  municipalityCode: '11001',
  taxResponsibilities: ['R-99-PN'],
  taxRegime: 'NO_RESPONSABLE_IVA',
};

export const initialProduct = {
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

export const initialIssuer = {
  legalName: 'Empresa Demo SAS',
  nit: '900123456',
  verificationDigit: '7',
  taxResponsibilities: ['O-13'],
  municipalityCode: '11001',
  address: 'Calle 1 # 2-3',
};

export const initialResolution = {
  documentType: 'ELECTRONIC_POS',
  resolutionNumber: '18760000001',
  prefix: 'POS',
  fromNumber: '100',
  toNumber: '999',
  validFrom: '2026-01-01',
  validTo: '2026-12-31',
  environment: 'TEST',
};

export const initialSale = {
  customerId: '',
  saleChannel: 'POS',
  paymentMethodCode: 'CASH',
  virtualWalletCode: '',
  items: [{ productId: '', quantity: '1', unitPrice: '15000', discountAmount: '0', taxCode: 'IVA_19', taxRate: '19' }],
};

export const initialReports = {
  status: '',
  from: '',
  to: '',
  productId: '',
  accountCode: '',
};
