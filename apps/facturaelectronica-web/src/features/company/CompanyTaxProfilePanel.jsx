import { CheckField, DualListField, FormPanel, SelectField } from '../../components/forms.jsx';
import { MunicipalityFields } from '../../components/MunicipalityFields.jsx';
import { deriveNationalTaxFlags } from '../../utils/companyTaxProfileRules.js';

const companySizeOptions = [
  { value: 'MICRO', label: 'Microempresa' },
  { value: 'PEQUENA', label: 'Pequena empresa' },
  { value: 'MEDIANA', label: 'Mediana empresa' },
  { value: 'GRANDE', label: 'Gran empresa' },
];

const reportingGroupOptions = [
  { value: 'GRUPO_1', label: 'Grupo 1' },
  { value: 'GRUPO_2', label: 'Grupo 2' },
  { value: 'GRUPO_3', label: 'Grupo 3' },
];

export function CompanyTaxProfilePanel({ form, setForm, onSave, busy, disabled, taxRegimeOptions = [],
  responsibilityOptions = [], ciiuOptions = [], locations = [] }) {
  return (
    <FormPanel title="Perfil fiscal de la empresa" submitLabel="Guardar perfil fiscal" onSubmit={onSave} busy={busy || disabled}>
      <CompanyTaxProfileFields form={form} setForm={setForm} taxRegimeOptions={taxRegimeOptions}
        responsibilityOptions={responsibilityOptions} ciiuOptions={ciiuOptions} locations={locations} disabled={disabled} />
    </FormPanel>
  );
}

export function CompanyTaxProfileFields({ form, setForm, taxRegimeOptions = [], responsibilityOptions = [],
  ciiuOptions = [], locations = [], disabled = false }) {
  const change = (name, value) => setForm({ ...form, [name]: value });
  const changeRegime = (value) => setForm({ ...form, taxRegime: value, ...deriveNationalTaxFlags(form.rutResponsibilities, value) });
  const changeResponsibilities = (value) => setForm({
    ...form,
    rutResponsibilities: value,
    ...deriveNationalTaxFlags(value, form.taxRegime),
  });

  return (
    <>
      <div className="form-grid">
        <SelectField label="Tamano empresarial" value={form.companySize} onChange={(value) => change('companySize', value)} options={companySizeOptions} disabled={disabled} />
        <SelectField label="Grupo de informacion financiera" value={form.financialReportingGroup} onChange={(value) => change('financialReportingGroup', value)} options={reportingGroupOptions} disabled={disabled} />
        <SelectField label="Regimen tributario" value={form.taxRegime} onChange={changeRegime} options={taxRegimeOptions} disabled={disabled} />
        <MunicipalityFields municipalityCode={form.icaMunicipalityCode} onChange={(value) => change('icaMunicipalityCode', value)} locations={locations} disabled={disabled} />
        <DualListField label="Responsabilidades RUT" value={form.rutResponsibilities} onChange={changeResponsibilities} options={responsibilityOptions} disabled={disabled} />
        <DualListField label="Actividades economicas CIIU" value={form.ciiuCodes} onChange={(value) => change('ciiuCodes', value)} options={ciiuOptions} searchable disabled={disabled} />
      </div>
      <div className="check-grid">
        <CheckField label="Detectado del RUT: responsable de IVA" checked={form.vatResponsible} onChange={() => {}} disabled />
        <CheckField label="Detectado del RUT: agente de retencion" checked={form.withholdingAgent} onChange={() => {}} disabled />
        <CheckField label="Detectado del RUT: agente de ReteIVA" checked={form.vatWithholdingAgent} onChange={() => {}} disabled />
        <CheckField label="Designada como agente de ReteICA por el municipio" checked={form.icaWithholdingAgent} onChange={(value) => change('icaWithholdingAgent', value)} disabled={disabled} />
        <CheckField label="Detectado del RUT: gran contribuyente" checked={form.largeTaxpayer} onChange={() => {}} disabled />
        <CheckField label="Detectado del RUT: autorretenedor" checked={form.selfWithholding} onChange={() => {}} disabled />
        <CheckField label="Detectado del regimen: SIMPLE" checked={form.simpleRegime} onChange={() => {}} disabled />
      </div>
    </>
  );
}
