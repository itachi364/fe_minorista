import { requestJson } from '../api/client.js';

export const emptyRuntimeCatalogs = {
  dianDocumentTypes: [],
  taxResponsibilityOptions: [],
  taxRegimeOptions: [],
  salesTaxOptions: [],
  paymentMethodOptions: [],
  virtualWalletOptions: [],
  fiscalDocumentTypeOptions: [],
  fiscalEnvironmentOptions: [],
  locations: [],
};

const catalogMap = {
  DIAN_DOCUMENT_TYPE: 'dianDocumentTypes',
  TAX_RESPONSIBILITY: 'taxResponsibilityOptions',
  TAX_REGIME: 'taxRegimeOptions',
  SALES_TAX: 'salesTaxOptions',
  PAYMENT_METHOD: 'paymentMethodOptions',
  VIRTUAL_WALLET: 'virtualWalletOptions',
  FISCAL_DOCUMENT_TYPE: 'fiscalDocumentTypeOptions',
  FISCAL_ENVIRONMENT: 'fiscalEnvironmentOptions',
};

export async function loadRuntimeCatalogs({ token, companyId }) {
  const entries = await Promise.all(Object.entries(catalogMap).map(async ([catalogCode, target]) => {
    const path = companyId
      ? `/api/v1/company-catalogs/${catalogCode}/items`
      : `/api/v1/catalogs/${catalogCode}/items`;
    const items = await requestJson(path, { token, companyId });
    return [target, items.filter((item) => item.active && item.enabledForCompany !== false).map(toOption)];
  }));
  const departments = await requestJson('/api/v1/catalogs/departments', { token, companyId });
  const locations = await Promise.all(departments.map(async (department) => {
    const municipalities = await requestJson(`/api/v1/catalogs/departments/${department.code}/municipalities`, {
      token,
      companyId,
    });
    return {
      departmentCode: department.code,
      departmentName: department.name,
      municipalities: municipalities.map((municipality) => ({ code: municipality.code, name: municipality.name })),
    };
  }));
  return { ...emptyRuntimeCatalogs, ...Object.fromEntries(entries), locations };
}

function toOption(item) {
  const value = normalizeValue(item);
  const prefixedCatalogs = new Set(['DIAN_DOCUMENT_TYPE', 'TAX_RESPONSIBILITY']);
  const label = prefixedCatalogs.has(item.catalogCode) && !item.label?.startsWith(`${item.code} -`)
    ? `${item.code} - ${item.label}`
    : item.label;
  return {
    value,
    label,
    description: item.description || '',
    ...taxMetadata(item),
  };
}

function normalizeValue(item) {
  return item.catalogCode === 'DIAN_DOCUMENT_TYPE' ? Number(item.code) : item.code;
}

function taxMetadata(item) {
  if (item.catalogCode !== 'SALES_TAX') {
    return {};
  }
  const description = item.description || '';
  const categoryMatch = description.match(/category=([^;]+)/);
  const rateMatch = description.match(/rate=([0-9]+(?:\.[0-9]+)?)/);
  return {
    taxCategoryCode: categoryMatch ? categoryMatch[1] : item.code,
    taxCode: item.code,
    taxLabel: item.label,
    taxRate: rateMatch ? Number(rateMatch[1]) : 0,
  };
}
