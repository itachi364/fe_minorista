import { DataTable } from '../../components/DataTable.jsx';
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
  thirdPartyRoleCatalog = [],
  personTypeCatalog = [],
  locations,
  listFilters,
  setListFilters,
  thirdParties = [],
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

  const listTypeOptions = thirdPartyRoleCatalog.filter((option) => ['CUSTOMER', 'SUPPLIER'].includes(option.value));

  return <div className="stack">
    <FormPanel title="Cliente / proveedor" submitLabel="Guardar tercero" onSubmit={onSubmit} busy={busy}>
      <div className="form-grid">
        <SelectField label="Tipo de tercero" value={normalizedForm.thirdPartyType} onChange={(value) => update({ ...normalizedForm, thirdPartyType: value })} options={thirdPartyRoleCatalog} />
        <SelectField label="Tipo de persona" value={normalizedForm.personType} onChange={(value) => update({ ...normalizedForm, personType: value })} options={personTypeCatalog} />
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
    </FormPanel>
    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>Terceros registrados</h1>
          <p className="hint">Clientes y proveedores cargados automaticamente para la empresa activa.</p>
        </div>
      </header>
      <div className="form-grid compact">
        <SelectField label="Tipo de tercero" value={listFilters.thirdPartyType} onChange={(value) => setListFilters({ ...listFilters, thirdPartyType: value || 'CUSTOMER' })} options={listTypeOptions} />
        <SelectField label="Estado" value={listFilters.thirdPartyActive} onChange={(value) => setListFilters({ ...listFilters, thirdPartyActive: value })} options={[
          { value: 'true', label: 'Activos' },
          { value: 'false', label: 'Inactivos' },
        ]} placeholder="Todos" />
      </div>
      <DataTable
        columns={['Documento', 'Nombre', 'Tipo persona', 'Correo', 'Telefono', 'Estado']}
        rows={thirdParties.map(thirdPartyRow)}
        rowKey={(_row, index) => thirdParties[index]?.id || index}
        emptyMessage="Sin terceros registrados para el filtro actual."
        sectionClassName="embedded-table"
      />
    </section>
  </div>;
}

function optionLabel(options, value) {
  return options.find((option) => option.value === value)?.label || value;
}

function thirdPartyRow(thirdParty) {
  return [
    `${thirdParty.identificationNumber || ''}${thirdParty.verificationDigit !== null && thirdParty.verificationDigit !== undefined ? `-${thirdParty.verificationDigit}` : ''}`,
    thirdParty.businessName || thirdParty.fullName || thirdParty.tradeName || '',
    thirdParty.personType === 'JURIDICA' ? 'Juridica' : 'Natural',
    thirdParty.email || '',
    thirdParty.phone || '',
    thirdParty.active === false ? 'Inactivo' : 'Activo',
  ];
}
