import { isNit } from './nit.js';

export const SIMPLE_NATURAL_CUSTOMER_RESPONSIBILITY = 'R-99-PN';
export const SIMPLE_NATURAL_CUSTOMER_REGIME = 'NO_RESPONSABLE_IVA';
export const DEFAULT_NATURAL_DOCUMENT_TYPE = 13;

export function isSimpleNaturalCustomer(form) {
  return form.thirdPartyType === 'CUSTOMER' && form.personType === 'NATURAL';
}

export function normalizeThirdPartyForm(form, companyMunicipalityCode) {
  if (!isSimpleNaturalCustomer(form)) {
    return form;
  }
  const hasAddress = Boolean((form.address || '').trim());
  const fallbackMunicipalityCode = companyMunicipalityCode || form.municipalityCode || '';
  return {
    ...form,
    identificationTypeCode: isNit(form.identificationTypeCode) ? DEFAULT_NATURAL_DOCUMENT_TYPE : Number(form.identificationTypeCode),
    verificationDigit: '',
    businessName: '',
    tradeName: '',
    taxResponsibilities: [SIMPLE_NATURAL_CUSTOMER_RESPONSIBILITY],
    taxRegime: SIMPLE_NATURAL_CUSTOMER_REGIME,
    municipalityCode: hasAddress ? form.municipalityCode || fallbackMunicipalityCode : fallbackMunicipalityCode,
  };
}
