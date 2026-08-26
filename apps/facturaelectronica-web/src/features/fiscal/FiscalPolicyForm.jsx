import { CheckField, FormPanel, SelectField, StatusBadge } from '../../components/forms.jsx';

export function FiscalPolicyForm({ form, setForm, policy, onSubmit, busy, fiscalDocumentTypeOptions = [] }) {
  const saleDocumentOptions = fiscalDocumentTypeOptions.filter((option) => (
    ['ELECTRONIC_INVOICE', 'ELECTRONIC_POS'].includes(option.value)
  ));

  return <FormPanel title="Politica fiscal de ventas" submitLabel="Guardar politica" onSubmit={onSubmit} busy={busy}>
    <p className="hint">Define que documento fiscal se emite por defecto al confirmar una venta y si se permiten cambios operativos autorizados.</p>
    <div className="form-grid compact">
      <SelectField label="Documento fiscal por defecto" value={form.defaultSaleDocumentType} onChange={(value) => setForm({ ...form, defaultSaleDocumentType: value })} options={saleDocumentOptions} />
      <CheckField label="Permitir cambio excepcional del tipo de documento en venta" checked={form.allowDocumentTypeOverride} onChange={(value) => setForm({ ...form, allowDocumentTypeOverride: value })} />
      <CheckField label="Exigir PIN operacional para cambios excepcionales" checked={form.requirePinForOverride} onChange={(value) => setForm({ ...form, requirePinForOverride: value })} disabled={!form.allowDocumentTypeOverride} />
    </div>
    <div className="badge-row">
      <StatusBadge label="Actual" value={policy?.defaultSaleDocumentType || 'Factura electronica de venta'} tone="ok" />
      <StatusBadge label="PIN" value={policy?.requirePinForOverride ? 'Requerido' : 'No requerido'} tone={policy?.requirePinForOverride ? 'warn' : 'ok'} />
    </div>
  </FormPanel>;
}
