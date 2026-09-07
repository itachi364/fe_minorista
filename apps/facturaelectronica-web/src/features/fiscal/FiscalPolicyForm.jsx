import { CheckField, FormPanel, SelectField, StatusBadge } from '../../components/forms.jsx';

export function FiscalPolicyForm({ form, setForm, policy, onSubmit, busy, fiscalDocumentTypeOptions = [] }) {
  const saleDocumentOptions = fiscalDocumentTypeOptions.filter((option) => (
    ['ELECTRONIC_INVOICE', 'ELECTRONIC_POS', 'NON_FISCAL_SALE'].includes(option.value)
  ));
  const currentDocumentType = policy?.defaultSaleDocumentType || form.defaultSaleDocumentType || 'NON_FISCAL_SALE';

  return <FormPanel title="Politica fiscal de ventas" submitLabel="Guardar politica" onSubmit={onSubmit} busy={busy}>
    <p className="hint">Define si la venta transmite documento electronico a DIAN o queda como venta interna con impuestos liquidados segun la politica contable de la empresa.</p>
    <div className="form-grid compact">
      <SelectField label="Modo de cierre por defecto" value={form.defaultSaleDocumentType} onChange={(value) => setForm({ ...form, defaultSaleDocumentType: value })} options={saleDocumentOptions} />
      <CheckField label="Permitir cambio excepcional del tipo de documento en venta" checked={form.allowDocumentTypeOverride} onChange={(value) => setForm({ ...form, allowDocumentTypeOverride: value })} />
      <CheckField label="Exigir PIN operacional para cambios excepcionales" checked={form.requirePinForOverride} onChange={(value) => setForm({ ...form, requirePinForOverride: value })} disabled={!form.allowDocumentTypeOverride} />
    </div>
    <div className="badge-row">
      <StatusBadge label="Actual" value={labelFiscalDocument(currentDocumentType, saleDocumentOptions)} tone="ok" />
      <StatusBadge label="PIN" value={policy?.requirePinForOverride ? 'Requerido' : 'No requerido'} tone={policy?.requirePinForOverride ? 'warn' : 'ok'} />
    </div>
  </FormPanel>;
}

function labelFiscalDocument(value, options) {
  const option = options.find((item) => item.value === value);
  if (option) {
    return option.label;
  }
  const labels = {
    ELECTRONIC_INVOICE: 'Factura electronica de venta',
    ELECTRONIC_POS: 'POS electronico',
    NON_FISCAL_SALE: 'Venta interna no fiscal',
  };
  return labels[value] || value || 'Sin definir';
}
