import { DataTable } from '../../components/DataTable.jsx';
import { Field, FormPanel, SelectField } from '../../components/forms.jsx';

export function PurchasesPanel({
  form,
  setForm,
  products = [],
  suppliers = [],
  purchases = [],
  filters,
  setFilters,
  onCreate,
  onConfirm,
  busy,
}) {
  const productOptions = products.map((product) => ({
    value: product.id,
    label: `${product.sku || 'Sin SKU'} - ${product.name || product.id}`,
  }));
  const supplierOptions = suppliers.map((thirdParty) => ({
    value: thirdParty.id,
    label: thirdParty.businessName || thirdParty.fullName || thirdParty.tradeName || thirdParty.identificationNumber,
  }));

  function updateLine(index, patch) {
    setForm({
      ...form,
      lines: form.lines.map((line, currentIndex) => (currentIndex === index ? { ...line, ...patch } : line)),
    });
  }

  function addLine() {
    setForm({ ...form, lines: [...form.lines, { productId: '', quantity: '1', unitCost: '', tax: '0' }] });
  }

  function removeLine(index) {
    setForm({ ...form, lines: form.lines.filter((_line, currentIndex) => currentIndex !== index) });
  }

  return <div className="stack">
    <FormPanel title="Reabastecimiento de inventario" submitLabel="Crear compra" onSubmit={onCreate} busy={busy}>
      <p className="hint">Usa este modulo solo para compras de mercancia o insumos que aumentan inventario.</p>
      <div className="form-grid">
        <SelectField label="Proveedor" value={form.supplierId} onChange={(value) => setForm({ ...form, supplierId: value })} options={supplierOptions} placeholder="Proveedor opcional" />
        <SelectField label="Condicion de pago" value={form.paymentCondition} onChange={(value) => setForm({ ...form, paymentCondition: value })} options={paymentConditionOptions} />
        <Field label="Fecha de vencimiento" value={form.dueDate} onChange={(value) => setForm({ ...form, dueDate: value })} type="date" disabled={form.paymentCondition !== 'CREDIT'} />
        <Field label="Soporte o evidencia" value={form.evidenceUrl} onChange={(value) => setForm({ ...form, evidenceUrl: value })} placeholder="URL opcional" />
      </div>
      <div className="line-list">
        {form.lines.map((line, index) => (
          <div className="line-card" key={`purchase-line-${index}`}>
            <SelectField label="Producto / insumo" value={line.productId} onChange={(value) => updateLine(index, { productId: value })} options={productOptions} />
            <Field label="Cantidad" value={line.quantity} onChange={(value) => updateLine(index, { quantity: value })} type="number" min="0" step="0.01" />
            <Field label="Costo unitario" value={line.unitCost} onChange={(value) => updateLine(index, { unitCost: value })} type="number" min="0" step="0.01" />
            <Field label="IVA compra" value={line.tax} onChange={(value) => updateLine(index, { tax: value })} type="number" min="0" step="0.01" />
            <button className="secondary danger-soft" disabled={busy || form.lines.length === 1} onClick={() => removeLine(index)} type="button">Quitar</button>
          </div>
        ))}
      </div>
      <button className="secondary" disabled={busy} onClick={addLine} type="button">Agregar producto</button>
    </FormPanel>

    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>Compras registradas</h1>
          <p className="hint">Entradas reales de inventario cargadas automaticamente por rango y estado.</p>
        </div>
      </header>
      <div className="form-grid compact">
        <SelectField label="Estado" value={filters.purchaseStatus} onChange={(value) => setFilters({ ...filters, purchaseStatus: value })} options={[
          { value: 'PENDING', label: 'Pendiente' },
          { value: 'CONFIRMED', label: 'Confirmada' },
        ]} placeholder="Todos" />
        <Field label="Desde" value={filters.purchaseFrom} onChange={(value) => setFilters({ ...filters, purchaseFrom: value })} type="date" />
        <Field label="Hasta" value={filters.purchaseTo} onChange={(value) => setFilters({ ...filters, purchaseTo: value })} type="date" />
      </div>
      <DataTable
        columns={['Fecha', 'Estado', 'Proveedor', 'Subtotal', 'IVA', 'Total', 'Vence', 'Acciones']}
        rows={purchases.map((purchase) => purchaseRow(purchase, onConfirm, busy))}
        rowKey={(_row, index) => purchases[index]?.id || index}
        emptyMessage="Sin compras registradas para el filtro actual."
        sectionClassName="embedded-table"
      />
    </section>
  </div>;
}

const paymentConditionOptions = [
  { value: 'CASH', label: 'Contado' },
  { value: 'CREDIT', label: 'Credito' },
];

function purchaseRow(purchase, onConfirm, busy) {
  return [
    shortDate(purchase.createdAt),
    purchase.status === 'CONFIRMED' ? 'Confirmada' : 'Pendiente',
    purchase.supplierId || 'Sin proveedor',
    money(purchase.subtotal),
    money(purchase.taxTotal),
    money(purchase.total),
    shortDate(purchase.dueDate),
    {
      searchText: purchase.status || '',
      content: purchase.status === 'CONFIRMED'
        ? 'Sin acciones'
        : <button className="secondary" disabled={busy} onClick={() => onConfirm(purchase.id)} type="button">Confirmar</button>,
    },
  ];
}

function shortDate(value) {
  return value ? String(value).slice(0, 10) : '';
}

function money(value) {
  if (value === null || value === undefined || value === '') {
    return '';
  }
  return Number(value).toLocaleString('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 2 });
}
