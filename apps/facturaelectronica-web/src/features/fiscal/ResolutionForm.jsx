import { Field, FormPanel, SelectField } from '../../components/forms.jsx';

export function ResolutionForm({ form, setForm, onSubmit, busy, fiscalDocumentTypeOptions = [], environmentOptions = [] }) {
  const errors = validateResolutionForm(form);
  const hasErrors = Object.values(errors).some(Boolean);

  function submit() {
    if (hasErrors) {
      return;
    }
    onSubmit();
  }

  return <FormPanel title="Resolucion" submitLabel="Crear resolucion" onSubmit={submit} busy={busy || hasErrors}>
    <p className="hint">La resolucion de numeracion es la autorizacion de la DIAN para emitir documentos fiscales con un tipo, prefijo, rango de consecutivos, ambiente y vigencia especificos.</p>
    <div className="form-grid compact">
      <SelectField label="Tipo de documento fiscal" value={form.documentType} onChange={(value) => setForm({ ...form, documentType: value })} options={fiscalDocumentTypeOptions} />
      <Field label="Numero resolucion" value={form.resolutionNumber} onChange={(value) => setForm({ ...form, resolutionNumber: value })} />
      <Field label="Prefijo" value={form.prefix} onChange={(value) => setForm({ ...form, prefix: value })} />
      <Field label="Desde" value={form.fromNumber} onChange={(value) => setForm({ ...form, fromNumber: value })} type="number" min="1" step="1" error={errors.fromNumber} />
      <Field label="Hasta" value={form.toNumber} onChange={(value) => setForm({ ...form, toNumber: value })} type="number" min="1" step="1" error={errors.toNumber} />
      <Field label="Vigencia desde" value={form.validFrom} onChange={(value) => setForm({ ...form, validFrom: value })} type="date" />
      <Field label="Vigencia hasta" value={form.validTo} onChange={(value) => setForm({ ...form, validTo: value })} type="date" error={errors.validTo} />
      <SelectField label="Ambiente" value={form.environment} onChange={(value) => setForm({ ...form, environment: value })} options={environmentOptions} />
    </div>
  </FormPanel>;
}

function validateResolutionForm(form) {
  const fromNumber = integerValue(form.fromNumber);
  const toNumber = integerValue(form.toNumber);
  return {
    fromNumber: form.fromNumber !== '' && (!Number.isInteger(fromNumber) || fromNumber <= 0)
      ? 'Desde debe ser un entero mayor que cero.'
      : '',
    toNumber: form.toNumber !== '' && (!Number.isInteger(toNumber) || toNumber <= 0)
      ? 'Hasta debe ser un entero mayor que cero.'
      : form.fromNumber !== '' && form.toNumber !== '' && Number.isInteger(fromNumber) && Number.isInteger(toNumber) && toNumber < fromNumber
      ? 'Hasta debe ser mayor o igual a Desde.'
      : '',
    validTo: form.validFrom && form.validTo && form.validTo < form.validFrom
      ? 'Vigencia hasta debe ser mayor o igual a Vigencia desde.'
      : '',
  };
}

function integerValue(value) {
  if (value === '' || value === null || value === undefined) {
    return null;
  }
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : Number.NaN;
}
