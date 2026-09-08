import { CheckField, DualListField, FormPanel, SelectField } from '../../components/forms.jsx';
import { MunicipalityFields } from '../../components/MunicipalityFields.jsx';

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
        responsibilityOptions={responsibilityOptions} ciiuOptions={ciiuOptions} locations={locations} />
    </FormPanel>
  );
}

export function CompanyTaxProfileFields({ form, setForm, taxRegimeOptions = [], responsibilityOptions = [],
  ciiuOptions = [], locations = [] }) {
  const change = (name, value) => setForm({ ...form, [name]: value });
  const changeRegime = (value) => setForm({ ...form, taxRegime: value, simpleRegime: value === 'SIMPLE' });

  return (
    <>
      <div className="form-grid">
        <SelectField label="Tamano empresarial" value={form.companySize} onChange={(value) => change('companySize', value)} options={companySizeOptions} />
        <SelectField label="Grupo de informacion financiera" value={form.financialReportingGroup} onChange={(value) => change('financialReportingGroup', value)} options={reportingGroupOptions} />
        <SelectField label="Regimen tributario" value={form.taxRegime} onChange={changeRegime} options={taxRegimeOptions} />
        <MunicipalityFields municipalityCode={form.icaMunicipalityCode} onChange={(value) => change('icaMunicipalityCode', value)} locations={locations} />
        <DualListField label="Responsabilidades RUT" value={form.rutResponsibilities} onChange={(value) => change('rutResponsibilities', value)} options={responsibilityOptions} />
        <DualListField label="Actividades economicas CIIU" value={form.ciiuCodes} onChange={(value) => change('ciiuCodes', value)} options={ciiuOptions} searchable />
      </div>
      <div className="check-grid">
        <CheckField label="Responsable de IVA" checked={form.vatResponsible} onChange={(value) => change('vatResponsible', value)} />
        <CheckField label="Agente de retencion" checked={form.withholdingAgent} onChange={(value) => change('withholdingAgent', value)} />
        <CheckField label="Agente de ReteIVA" checked={form.vatWithholdingAgent} onChange={(value) => change('vatWithholdingAgent', value)} />
        <CheckField label="Agente de ReteICA" checked={form.icaWithholdingAgent} onChange={(value) => change('icaWithholdingAgent', value)} />
        <CheckField label="Gran contribuyente" checked={form.largeTaxpayer} onChange={(value) => change('largeTaxpayer', value)} />
        <CheckField label="Autorretenedor" checked={form.selfWithholding} onChange={(value) => change('selfWithholding', value)} />
        <CheckField label="Regimen SIMPLE" checked={form.simpleRegime} onChange={(value) => setForm({ ...form, simpleRegime: value, taxRegime: value ? 'SIMPLE' : form.taxRegime })} />
      </div>
    </>
  );
}
