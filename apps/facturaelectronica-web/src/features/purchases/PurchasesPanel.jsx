import { DataTable } from '../../components/DataTable.jsx';
import { Field, FormPanel, SelectField } from '../../components/forms.jsx';
import { FiscalCalculationResult } from '../accounting/FiscalCalculationResult.jsx';

export function PurchasesPanel({
  form,
  setForm,
  suppliers = [],
  purchases = [],
  filters,
  setFilters,
  onCreate,
  onConfirm,
  onCalculate,
  onViewFiscal,
  fiscalResult,
  busy,
}) {
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
    setForm({ ...form, lines: [...form.lines, { description: '', subtotal: '', tax: '0', total: '' }] });
  }

  function removeLine(index) {
    setForm({ ...form, lines: form.lines.filter((_line, currentIndex) => currentIndex !== index) });
  }

  return <div className="stack">
    <FormPanel title="Factura de compra" submitLabel="Crear compra" onSubmit={onCreate} busy={busy}>
      <p className="hint">Registra facturas de proveedores para control financiero y reinversion. El stock se ajusta desde Inventario.</p>
      <div className="form-grid">
        <SelectField label="Proveedor" value={form.supplierId} onChange={(value) => setForm({ ...form, supplierId: value })} options={supplierOptions} placeholder="Proveedor opcional" />
        <SelectField label="Concepto fiscal" value={form.fiscalConceptCode} onChange={(value) => setForm({ ...form, fiscalConceptCode: value })} options={purchaseConceptOptions} />
        <SelectField label="Condicion de pago" value={form.paymentCondition} onChange={(value) => setForm({ ...form, paymentCondition: value, dueDate: value === 'CREDIT' ? form.dueDate : '' })} options={paymentConditionOptions} />
        {form.paymentCondition === 'CREDIT' && <Field label="Fecha limite de pago" value={form.dueDate} onChange={(value) => setForm({ ...form, dueDate: value })} type="date" />}
        <SelectField label="Soporte o evidencia" value={form.evidenceType} onChange={(value) => setForm({ ...form, evidenceType: value, evidenceUrl: '', evidenceFile: null })} options={evidenceOptions} placeholder="Sin evidencia" />
        {form.evidenceType === 'URL' && <Field label="URL de evidencia" value={form.evidenceUrl} onChange={(value) => setForm({ ...form, evidenceUrl: value })} placeholder="https://..." />}
        {form.evidenceType === 'PDF' && (
          <label className="field">
            Archivo PDF
            <input accept="application/pdf,.pdf" disabled={busy} key={form.evidenceFile?.name || 'purchase-evidence-empty'} onChange={(event) => setForm({ ...form, evidenceFile: event.target.files?.[0] || null })} type="file" />
            {form.evidenceFile && <span className="field-note">{form.evidenceFile.name}</span>}
          </label>
        )}
      </div>
      <div className="line-list">
        {form.lines.map((line, index) => (
          <div className="line-card" key={`purchase-line-${index}`}>
            <Field label="Concepto" value={line.description} onChange={(value) => updateLine(index, { description: value })} placeholder="Ej. Factura proveedor, mercancia, insumos o reinversion" />
            <Field label="Subtotal" value={line.subtotal} onChange={(value) => updateLine(index, withTotal(line, { subtotal: value }))} type="number" min="0" step="0.01" />
            <Field label="IVA" value={line.tax} onChange={(value) => updateLine(index, withTotal(line, { tax: value }))} type="number" min="0" step="0.01" />
            <Field label="Total" value={line.total} onChange={() => {}} readOnly />
            <button className="secondary danger-soft" disabled={busy || form.lines.length === 1} onClick={() => removeLine(index)} type="button">Quitar</button>
          </div>
        ))}
      </div>
      <button className="secondary" disabled={busy} onClick={addLine} type="button">Agregar concepto</button>
    </FormPanel>

    <FiscalCalculationResult result={fiscalResult} title="Calculo fiscal de compra" />

    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>Compras registradas</h1>
          <p className="hint">Facturas de proveedor cargadas automaticamente por rango y estado.</p>
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
        columns={['Fecha', 'Estado', 'Proveedor', 'Total', 'Vence', 'Acciones']}
        rows={purchases.map((purchase) => purchaseRow(purchase, onConfirm, onCalculate, onViewFiscal, busy))}
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

const evidenceOptions = [
  { value: 'PDF', label: 'Archivo PDF' },
  { value: 'URL', label: 'URL' },
];

const purchaseConceptOptions = [
  { value: 'ANY', label: 'Compra general' },
  { value: 'AGRICULTURAL_UNPROCESSED', label: 'Productos agricolas sin procesamiento' },
  { value: 'COFFEE_PARCHMENT_CHERRY', label: 'Cafe pergamino o cereza' },
  { value: 'GOLD_INTERNATIONAL_TRADING', label: 'Oro por sociedad de comercializacion internacional' },
];

function purchaseRow(purchase, onConfirm, onCalculate, onViewFiscal, busy) {
  return [
    shortDate(purchase.createdAt),
    purchase.status === 'CONFIRMED' ? 'Confirmada' : 'Pendiente',
    purchase.supplierId || 'Sin proveedor',
    money(purchase.total),
    shortDate(purchase.dueDate),
    {
      searchText: purchase.status || '',
      content: purchase.status === 'CONFIRMED'
        ? <button className="secondary" disabled={busy} onClick={() => onViewFiscal(purchase)} type="button">Detalle fiscal</button>
        : <div className="row-actions">
          <button className="secondary" disabled={busy} onClick={() => onCalculate(purchase)} type="button">Calcular</button>
          <button className="secondary" disabled={busy} onClick={() => onConfirm(purchase.id)} type="button">Confirmar</button>
        </div>,
    },
  ];
}

function withTotal(line, patch) {
  const next = { ...line, ...patch };
  next.total = String(Number(next.subtotal || 0) + Number(next.tax || 0));
  return next;
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
