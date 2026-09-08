import { calculateNitVerificationDigit, isNit, onlyDigits } from './nit.js';
import { isSimpleNaturalCustomer, normalizeThirdPartyForm } from './thirdPartyRules.js';
import { calculateTaxIncludedAmounts } from './taxCalculations.js';

function toNumber(value) {
  if (value === '' || value === null || value === undefined) {
    return undefined;
  }
  return Number(value);
}

export function compactObject(value) {
  return Object.fromEntries(Object.entries(value).filter(([, entry]) => entry !== '' && entry !== undefined));
}

function commaList(value) {
  if (Array.isArray(value)) {
    return value.filter(Boolean);
  }
  return value.split(',').map((item) => item.trim()).filter(Boolean);
}

export function asPretty(value) {
  return JSON.stringify(value, null, 2);
}

export function buildCompanyPayload(form, taxProfile) {
  if (!taxProfile?.taxRegime) {
    throw new Error('Selecciona el regimen tributario de la empresa.');
  }
  const nitDocument = isNit(form.identificationTypeCode);
  return compactObject({
    ...form,
    identificationNumber: nitDocument ? onlyDigits(form.identificationNumber) : form.identificationNumber,
    verificationDigit: nitDocument ? calculateNitVerificationDigit(form.identificationNumber) : '',
    taxProfile: {
      ...taxProfile,
      rutResponsibilities: Array.isArray(taxProfile?.rutResponsibilities) ? taxProfile.rutResponsibilities : [],
      ciiuCodes: Array.isArray(taxProfile?.ciiuCodes) ? taxProfile.ciiuCodes : [],
    },
  });
}

export function buildCompanyAdminPayload(form) {
  return compactObject({
    email: form.email,
    fullName: form.fullName,
    password: form.password,
  });
}

export function buildLicensePayload(form) {
  return compactObject({
    planCode: form.planCode,
    validFrom: form.validFrom,
    validTo: form.validTo,
    maxUsers: toNumber(form.maxUsers),
    maxMonthlyDocuments: toNumber(form.maxMonthlyDocuments),
    enabledModules: Array.isArray(form.enabledModules) ? form.enabledModules : [],
  });
}

export function buildThirdPartyPayload(form, companyMunicipalityCode) {
  const normalizedForm = normalizeThirdPartyForm(form, companyMunicipalityCode);
  const simpleNaturalCustomer = isSimpleNaturalCustomer(normalizedForm);
  const roles = normalizedForm.thirdPartyType === 'BOTH' ? ['CUSTOMER', 'SUPPLIER'] : [normalizedForm.thirdPartyType];
  const nitDocument = isNit(normalizedForm.identificationTypeCode);
  const verificationDigit = nitDocument ? calculateNitVerificationDigit(normalizedForm.identificationNumber) : '';
  return compactObject({
    personType: normalizedForm.personType,
    identificationTypeCode: normalizedForm.identificationTypeCode,
    identificationNumber: nitDocument ? onlyDigits(normalizedForm.identificationNumber) : normalizedForm.identificationNumber,
    verificationDigit: nitDocument && verificationDigit !== '' ? Number(verificationDigit) : null,
    fullName: normalizedForm.fullName || null,
    businessName: simpleNaturalCustomer ? null : normalizedForm.businessName || null,
    tradeName: simpleNaturalCustomer ? null : normalizedForm.tradeName,
    email: normalizedForm.email,
    phone: normalizedForm.phone,
    address: normalizedForm.address,
    municipalityCode: normalizedForm.municipalityCode,
    ciiuCodes: simpleNaturalCustomer ? [] : commaList(normalizedForm.ciiuCodes),
    taxResponsibilities: commaList(normalizedForm.taxResponsibilities),
    taxRegime: normalizedForm.taxRegime,
    roles,
  });
}

export function buildProductPayload(form) {
  const calculatedPrice = form.finalSalePrice
    ? calculateTaxIncludedAmounts(form.finalSalePrice, form.taxRate)
    : null;
  return compactObject({
    ...form,
    finalSalePrice: undefined,
    salePrice: calculatedPrice?.base || toNumber(form.salePrice),
    cost: toNumber(form.cost),
    initialStock: toNumber(form.initialStock),
    taxRate: toNumber(form.taxRate),
  });
}

export function buildPurchasePayload(form) {
  const lines = (form.lines || []).map((line) => {
    const subtotal = toNumber(line.subtotal) ?? 0;
    const tax = toNumber(line.tax) ?? 0;
    const total = Number((subtotal + tax).toFixed(2));
    return compactObject({
      description: line.description,
      quantity: 1,
      unitCost: subtotal,
      subtotal,
      tax,
      total,
    });
  });
  const subtotal = Number(lines.reduce((current, line) => current + Number(line.subtotal || 0), 0).toFixed(2));
  const taxTotal = Number(lines.reduce((current, line) => current + Number(line.tax || 0), 0).toFixed(2));
  const total = Number((subtotal + taxTotal).toFixed(2));
  return compactObject({
    supplierId: form.supplierId || null,
    subtotal,
    taxTotal,
    total,
    fiscalConceptCode: form.fiscalConceptCode || 'ANY',
    paymentCondition: form.paymentCondition,
    dueDate: form.paymentCondition === 'CREDIT' ? form.dueDate : undefined,
    evidenceUrl: form.evidenceType ? form.evidenceUrl : undefined,
    lines,
  });
}

export function buildExpensePayload(form) {
  const subtotal = toNumber(form.subtotal) ?? 0;
  const taxTotal = toNumber(form.taxTotal) ?? 0;
  const total = Number((subtotal + taxTotal).toFixed(2));
  return compactObject({
    supplierId: form.supplierId || null,
    expenseType: form.expenseType || 'OPERATING_EXPENSE',
    expenseDate: form.expenseDate,
    concept: form.concept,
    subtotal,
    taxTotal,
    total,
    fiscalConceptCode: form.fiscalConceptCode || 'ANY',
    paymentCondition: form.paymentCondition,
    dueDate: form.paymentCondition === 'CREDIT' ? form.dueDate : undefined,
    evidenceUrl: form.evidenceType ? form.evidenceUrl : undefined,
  });
}

export function buildAccountsReceivablePayload(form, sourceId) {
  return compactObject({
    customerId: form.customerId,
    sourceType: 'ADJUSTMENT',
    sourceId,
    issueDate: form.issueDate,
    dueDate: form.dueDate,
    totalAmount: toNumber(form.totalAmount),
    idempotencyKey: sourceId,
  });
}

export function buildReceivablePaymentPayload(form) {
  return compactObject({
    paymentDate: form.paymentDate,
    amount: toNumber(form.amount),
    paymentMethod: form.paymentMethod,
    reference: form.reference,
  });
}

export function buildIssuerPayload(form) {
  return compactObject({
    ...form,
    taxResponsibilities: commaList(form.taxResponsibilities),
  });
}

export function buildResolutionPayload(form) {
  return compactObject({
    ...form,
    fromNumber: toNumber(form.fromNumber),
    toNumber: toNumber(form.toNumber),
  });
}

export function buildSalePayload(form) {
  return compactObject({
    buyerIdentificationMode: form.buyerIdentificationMode,
    customerId: form.customerId || null,
    paymentMethodCode: form.paymentMethodCode,
    virtualWalletCode: form.paymentMethodCode === 'VIRTUAL_WALLET' ? form.virtualWalletCode || null : null,
    items: form.items.map((item) => compactObject({
      productId: item.productId,
      quantity: toNumber(item.quantity),
      discountAmount: toNumber(item.discountAmount) ?? 0,
    })),
  });
}

export function buildFiscalNotePayload(form) {
  return compactObject({
    originalDocumentId: form.originalDocumentId,
    adjustmentKind: form.adjustmentKind || undefined,
    reason: form.reason,
    subtotal: toNumber(form.subtotal),
    taxTotal: toNumber(form.taxTotal),
    total: toNumber(form.total),
  });
}
