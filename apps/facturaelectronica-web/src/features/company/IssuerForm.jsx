import { DualListField, Field, FormPanel } from '../../components/forms.jsx';
import { MunicipalityFields } from '../../components/MunicipalityFields.jsx';

export function IssuerForm({ form, setForm, activeCompany, onSubmit, busy, taxResponsibilityOptionsSource = [], locations }) {
  function updateTaxResponsibilities(values) {
    const nextValues = values.includes('R-99-PN') ? ['R-99-PN'] : values.filter((value) => value !== 'R-99-PN');
    setForm({ ...form, taxResponsibilities: nextValues });
  }

  return <FormPanel title="Emisor fiscal" submitLabel="Guardar emisor" onSubmit={onSubmit} busy={busy}>
    <p className="hint">El emisor fiscal corresponde a la empresa activa. Los datos principales se precargan desde la empresa contratante.</p>
    <div className="form-grid compact">
      <Field label="Razon social" value={activeCompany?.legalName || form.legalName} onChange={(value) => setForm({ ...form, legalName: value })} readOnly={Boolean(activeCompany)} />
      <Field label="NIT" value={activeCompany?.identificationNumber || form.nit} onChange={(value) => setForm({ ...form, nit: value })} readOnly={Boolean(activeCompany)} />
      <Field label="DV" value={activeCompany?.verificationDigit || form.verificationDigit} onChange={(value) => setForm({ ...form, verificationDigit: value })} readOnly={Boolean(activeCompany)} />
      <DualListField label="Responsabilidades fiscales" value={form.taxResponsibilities} onChange={updateTaxResponsibilities} options={taxResponsibilityOptionsSource} exclusiveValues={['R-99-PN']} />
      <MunicipalityFields municipalityCode={form.municipalityCode} onChange={(value) => setForm({ ...form, municipalityCode: value })} locations={locations} />
      <Field label="Direccion fiscal" value={form.address} onChange={(value) => setForm({ ...form, address: value })} />
    </div>
  </FormPanel>;
}
