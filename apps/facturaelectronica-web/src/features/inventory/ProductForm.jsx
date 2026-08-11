import { Field, FormPanel, SelectField } from '../../components/forms.jsx';

const itemUsageProfiles = [
  {
    value: 'SELL_STOCK',
    label: 'Vender y controlar inventario',
    description: 'Aparece en POS, se puede comprar y descuenta unidades del stock.',
    saleEnabled: true,
    purchaseEnabled: true,
    stockTracked: true,
  },
  {
    value: 'SELL_NO_STOCK',
    label: 'Vender sin stock propio',
    description: 'Aparece en POS, no descuenta unidades directas. Util para servicios facturables.',
    saleEnabled: true,
    purchaseEnabled: false,
    stockTracked: false,
  },
  {
    value: 'SUPPLY_STOCK',
    label: 'Insumo controlado',
    description: 'No aparece en POS, se compra y se controla en inventario.',
    saleEnabled: false,
    purchaseEnabled: true,
    stockTracked: true,
  },
  {
    value: 'PURCHASE_NO_STOCK',
    label: 'Compra sin inventario',
    description: 'No aparece en POS y no controla unidades. Util para gastos o servicios comprados.',
    saleEnabled: false,
    purchaseEnabled: true,
    stockTracked: false,
  },
];

export function ProductForm({ form, setForm, onSubmit, busy, taxOptions = [], itemTypeCatalog = [] }) {
  const selectedUsage = findUsageProfile(form);

  function updateUsage(value) {
    const profile = itemUsageProfiles.find((item) => item.value === value);
    if (!profile) {
      return;
    }
    setForm({
      ...form,
      saleEnabled: profile.saleEnabled,
      purchaseEnabled: profile.purchaseEnabled,
      stockTracked: profile.stockTracked,
    });
  }

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
      <SelectField label="Tipo de item" value={form.itemType} onChange={(value) => setForm({ ...form, itemType: value })} options={itemTypeCatalog} />
      <SelectField label="Impuesto de venta" value={form.taxCode} onChange={updateTax} options={taxOptions} />
      <Field label="Categoria impuesto" value={form.taxCategoryCode} onChange={() => {}} readOnly />
      <Field label="Tarifa impuesto" value={form.taxRate} onChange={() => {}} readOnly />
      <Field label="Precio venta" value={form.salePrice} onChange={(value) => setForm({ ...form, salePrice: value })} type="number" />
      <Field label="Costo" value={form.cost} onChange={(value) => setForm({ ...form, cost: value })} type="number" />
      <Field label="Stock inicial" value={form.initialStock} onChange={(value) => setForm({ ...form, initialStock: value })} type="number" />
      <SelectField label="Uso del item" value={selectedUsage?.value || ''} onChange={updateUsage} options={itemUsageProfiles} />
      <div className="field-note wide">
        {selectedUsage?.description || 'Selecciona como se usara el item en ventas, compras e inventario.'}
      </div>
    </div>
  </FormPanel>;
}

function findUsageProfile(form) {
  return itemUsageProfiles.find((profile) => profile.saleEnabled === form.saleEnabled
    && profile.purchaseEnabled === form.purchaseEnabled
    && profile.stockTracked === form.stockTracked);
}
