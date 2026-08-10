import { personTypeOptions, thirdPartyTypeOptions } from '../../data/catalogs.js';
import { DualListField, Field, FormPanel, SelectField } from '../../components/forms.jsx';
import { MunicipalityFields } from '../../components/MunicipalityFields.jsx';
import { calculateNitVerificationDigit, isNit, onlyDigits } from '../../utils/nit.js';
import {
  SIMPLE_NATURAL_CUSTOMER_REGIME,
  SIMPLE_NATURAL_CUSTOMER_RESPONSIBILITY,
  isSimpleNaturalCustomer,
  normalizeThirdPartyForm,
} from '../../utils/thirdPartyRules.js';

export function ThirdPartyForm({
  form,
  setForm,
  onSubmit,
  busy,
  companyMunicipalityCode,
  documentTypeOptionsSource = [],
  taxResponsibilityOptionsSource = [],
  taxRegimeOptionsSource = [],
  locations,
}) {
  const normalizedForm = normalizeThirdPartyForm(form, companyMunicipalityCode);
  const simpleNaturalCustomer = isSimpleNaturalCustomer(normalizedForm);
  const nitDocument = isNit(normalizedForm.identificationTypeCode);
  const verificationDigit = nitDocument ? calculateNitVerificationDigit(normalizedForm.identificationNumber) : '';
  const documentTypeOptions = simpleNaturalCustomer
    ? documentTypeOptionsSource.filter((option) => !isNit(option.value))
    : documentTypeOptionsSource;
  const naturalCustomerHasAddress = Boolean((normalizedForm.address || '').trim());
  const fiscalResponsibilityLabel = optionLabel(taxResponsibilityOptionsSource, SIMPLE_NATURAL_CUSTOMER_RESPONSIBILITY);
  const taxRegimeLabel = optionLabel(taxRegimeOptionsSource, SIMPLE_NATURAL_CUSTOMER_REGIME);

  function update(nextForm) {
    setForm(normalizeThirdPartyForm(nextForm, companyMunicipalityCode));
  }

  function updateIdentificationType(value) {
    const identificationTypeCode = Number(value);
    const safeTypeCode = simpleNaturalCustomer && isNit(identificationTypeCode) ? 13 : identificationTypeCode;
    const nextNumber = isNit(safeTypeCode) ? onlyDigits(normalizedForm.identificationNumber) : normalizedForm.identificationNumber;
    update({ ...normalizedForm, identificationTypeCode: safeTypeCode, identificationNumber: nextNumber });
  }

  function updateIdentificationNumber(value) {
    update({ ...normalizedForm, identificationNumber: nitDocument ? onlyDigits(value) : value });
  }

  function updateAddress(value) {
    const nextForm = { ...normalizedForm, address: value };
    update(value.trim() ? nextForm : { ...nextForm, municipalityCode: companyMunicipalityCode || normalizedForm.municipalityCode });
  }

  function updateTaxResponsibilities(values) {
    const nextValues = values.includes('R-99-PN') ? ['R-99-PN'] : values.filter((value) => value !== 'R-99-PN');
    update({ ...normalizedForm, taxResponsibilities: nextValues });
  }

  return <FormPanel title="Cliente / proveedor" submitLabel="Guardar tercero" onSubmit={onSubmit} busy={busy}>
    <div className="form-grid">
      <SelectField label="Tipo de tercero" value={normalizedForm.thirdPartyType} onChange={(value) => update({ ...normalizedForm, thirdPartyType: value })} options={thirdPartyTypeOptions} />
      <SelectField label="Tipo de persona" value={normalizedForm.personType} onChange={(value) => update({ ...normalizedForm, personType: value })} options={personTypeOptions} />
      <SelectField label="Tipo de documento" value={normalizedForm.identificationTypeCode} onChange={updateIdentificationType} options={documentTypeOptions} />
      <Field label="Numero de documento" value={normalizedForm.identificationNumber} onChange={updateIdentificationNumber} />
      <Field label="Digito de verificacion" value={verificationDigit} onChange={() => {}} readOnly />
      <Field label="Nombre completo" value={normalizedForm.fullName} onChange={(value) => update({ ...normalizedForm, fullName: value })} />
      {!simpleNaturalCustomer && <Field label="Razon social" value={normalizedForm.businessName} onChange={(value) => update({ ...normalizedForm, businessName: value })} />}
      {!simpleNaturalCustomer && <Field label="Nombre comercial" value={normalizedForm.tradeName} onChange={(value) => update({ ...normalizedForm, tradeName: value })} />}
      <Field label="Correo electronico" value={normalizedForm.email} onChange={(value) => update({ ...normalizedForm, email: value })} type="email" />
      <Field label="Telefono" value={normalizedForm.phone} onChange={(value) => update({ ...normalizedForm, phone: value })} />
      <Field label="Direccion" value={normalizedForm.address} onChange={updateAddress} />
      <MunicipalityFields municipalityCode={normalizedForm.municipalityCode} onChange={(value) => update({ ...normalizedForm, municipalityCode: value })} disabled={simpleNaturalCustomer && !naturalCustomerHasAddress} locations={locations} />
      {simpleNaturalCustomer
        ? <Field label="Responsabilidades fiscales" value={fiscalResponsibilityLabel} onChange={() => {}} readOnly />
        : <DualListField label="Responsabilidades fiscales" value={normalizedForm.taxResponsibilities} onChange={updateTaxResponsibilities} options={taxResponsibilityOptionsSource} exclusiveValues={['R-99-PN']} />}
      {simpleNaturalCustomer
        ? <Field label="Regimen tributario" value={taxRegimeLabel} onChange={() => {}} readOnly />
        : <SelectField label="Regimen tributario" value={normalizedForm.taxRegime} onChange={(value) => update({ ...normalizedForm, taxRegime: value })} options={taxRegimeOptionsSource} />}
    </div>
  </FormPanel>;
}

function optionLabel(options, value) {
  return options.find((option) => option.value === value)?.label || value;
}
