import { itemTypeOptions } from '../../data/catalogs.js';
import { CheckField, Field, FormPanel, SelectField } from '../../components/forms.jsx';

export function ProductForm({ form, setForm, onSubmit, busy, taxOptions = [] }) {
  function updateTax(taxCode) {
    const selected = taxOptions.find((option) => option.value === taxCode);
    setForm({
      ...form,
      taxCode,
      taxCategoryCode: selected?.taxCategoryCode || form.taxCategoryCode,
      taxLabel: selected?.taxLabel || selected?.label || form.taxLabel,
      taxRate: String(selected?.taxRate ?? form.taxRate),
    });
  }

  return <FormPanel title="Producto / servicio / insumo" submitLabel="Crear item" onSubmit={onSubmit} busy={busy}>
    <div className="form-grid">
      <Field label="SKU" value={form.sku} onChange={(value) => setForm({ ...form, sku: value })} />
      <Field label="Codigo de barras" value={form.barcode} onChange={(value) => setForm({ ...form, barcode: value })} placeholder="Escanea o digita el codigo" autoComplete="off" />
      <Field label="Nombre" value={form.name} onChange={(value) => setForm({ ...form, name: value })} />
      <Field label="Descripcion" value={form.description} onChange={(value) => setForm({ ...form, description: value })} />
      <SelectField label="Tipo de item" value={form.itemType} onChange={(value) => setForm({ ...form, itemType: value })} options={itemTypeOptions} />
      <SelectField label="Impuesto de venta" value={form.taxCode} onChange={updateTax} options={taxOptions} />
      <Field label="Categoria impuesto" value={form.taxCategoryCode} onChange={() => {}} readOnly />
      <Field label="Tarifa impuesto" value={form.taxRate} onChange={() => {}} readOnly />
      <Field label="Precio venta" value={form.salePrice} onChange={(value) => setForm({ ...form, salePrice: value })} type="number" />
      <Field label="Costo" value={form.cost} onChange={(value) => setForm({ ...form, cost: value })} type="number" />
      <Field label="Stock inicial" value={form.initialStock} onChange={(value) => setForm({ ...form, initialStock: value })} type="number" />
      <CheckField label="Vendido" checked={form.saleEnabled} onChange={(value) => setForm({ ...form, saleEnabled: value })} />
      <CheckField label="Comprado" checked={form.purchaseEnabled} onChange={(value) => setForm({ ...form, purchaseEnabled: value })} />
      <CheckField label="Controla stock" checked={form.stockTracked} onChange={(value) => setForm({ ...form, stockTracked: value })} />
    </div>
  </FormPanel>;
}
