import { Field, FormPanel } from '../../components/forms.jsx';

export function ReportsForm({ form, setForm, onSubmit, busy }) {
  return <FormPanel title="Reportes" submitLabel="Consultar" onSubmit={onSubmit} busy={busy}>
    <div className="form-grid">
      <Field label="Estado" value={form.status} onChange={(value) => setForm({ ...form, status: value })} />
      <Field label="Desde" value={form.from} onChange={(value) => setForm({ ...form, from: value })} type="date" />
      <Field label="Hasta" value={form.to} onChange={(value) => setForm({ ...form, to: value })} type="date" />
      <Field label="Producto" value={form.productId} onChange={(value) => setForm({ ...form, productId: value })} />
      <Field label="Cuenta" value={form.accountCode} onChange={(value) => setForm({ ...form, accountCode: value })} />
    </div>
  </FormPanel>;
}
