import { Field, FormPanel, SelectField } from '../../components/forms.jsx';

export function ResolutionForm({ form, setForm, onSubmit, busy, fiscalDocumentTypeOptions = [], environmentOptions = [] }) {
  return <FormPanel title="Resolucion" submitLabel="Crear resolucion" onSubmit={onSubmit} busy={busy}>
    <div className="form-grid compact">
      <SelectField label="Tipo de documento fiscal" value={form.documentType} onChange={(value) => setForm({ ...form, documentType: value })} options={fiscalDocumentTypeOptions} />
      <Field label="Numero resolucion" value={form.resolutionNumber} onChange={(value) => setForm({ ...form, resolutionNumber: value })} />
      <Field label="Prefijo" value={form.prefix} onChange={(value) => setForm({ ...form, prefix: value })} />
      <Field label="Desde" value={form.fromNumber} onChange={(value) => setForm({ ...form, fromNumber: value })} type="number" />
      <Field label="Hasta" value={form.toNumber} onChange={(value) => setForm({ ...form, toNumber: value })} type="number" />
      <Field label="Vigencia desde" value={form.validFrom} onChange={(value) => setForm({ ...form, validFrom: value })} type="date" />
      <Field label="Vigencia hasta" value={form.validTo} onChange={(value) => setForm({ ...form, validTo: value })} type="date" />
      <SelectField label="Ambiente" value={form.environment} onChange={(value) => setForm({ ...form, environment: value })} options={environmentOptions} />
    </div>
  </FormPanel>;
}
