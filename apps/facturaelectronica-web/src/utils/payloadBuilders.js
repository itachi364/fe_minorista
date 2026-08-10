import { calculateNitVerificationDigit, isNit, onlyDigits } from './nit.js';
import { isSimpleNaturalCustomer, normalizeThirdPartyForm } from './thirdPartyRules.js';

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

export function buildCompanyPayload(form) {
  const nitDocument = isNit(form.identificationTypeCode);
  return compactObject({
    ...form,
    identificationNumber: nitDocument ? onlyDigits(form.identificationNumber) : form.identificationNumber,
    verificationDigit: nitDocument ? calculateNitVerificationDigit(form.identificationNumber) : '',
  });
}

export function buildCompanyAdminPayload(form) {
  return compactObject({
    email: form.email,
    fullName: form.fullName,
    password: form.password,
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
    taxResponsibilities: commaList(normalizedForm.taxResponsibilities),
    taxRegime: normalizedForm.taxRegime,
    roles,
  });
}

export function buildProductPayload(form) {
  return compactObject({
    ...form,
    salePrice: toNumber(form.salePrice),
    cost: toNumber(form.cost),
    initialStock: toNumber(form.initialStock),
    taxRate: toNumber(form.taxRate),
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
