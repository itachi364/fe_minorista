import { DataTable } from '../../components/DataTable.jsx';
import { Field, FormPanel, SelectField } from '../../components/forms.jsx';

export function ExpensesPanel({
  form,
  setForm,
  suppliers = [],
  expenses = [],
  filters,
  setFilters,
  onCreate,
  onConfirm,
  onLoad,
  busy,
}) {
  const supplierOptions = suppliers.map((thirdParty) => ({
    value: thirdParty.id,
    label: thirdParty.businessName || thirdParty.fullName || thirdParty.tradeName || thirdParty.identificationNumber,
  }));

  return <div className="stack">
    <FormPanel title="Gasto o activo del negocio" submitLabel="Crear egreso" onSubmit={onCreate} busy={busy}>
      <p className="hint">Registra egresos que no aumentan inventario: gastos operativos o compras de activos como equipos, muebles y maquinaria.</p>
      <div className="form-grid">
        <SelectField label="Tipo de egreso" value={form.expenseType} onChange={(value) => setForm({ ...form, expenseType: value })} options={expenseTypeOptions} />
        <SelectField label="Proveedor" value={form.supplierId} onChange={(value) => setForm({ ...form, supplierId: value })} options={supplierOptions} placeholder="Proveedor opcional" />
        <Field label="Fecha del gasto" value={form.expenseDate} onChange={(value) => setForm({ ...form, expenseDate: value })} type="date" />
        <Field label="Concepto" value={form.concept} onChange={(value) => setForm({ ...form, concept: value })} />
        <Field label="Subtotal" value={form.subtotal} onChange={(value) => setForm({ ...form, subtotal: value })} type="number" min="0" step="0.01" />
        <Field label="IVA" value={form.taxTotal} onChange={(value) => setForm({ ...form, taxTotal: value })} type="number" min="0" step="0.01" />
        <Field label="Total" value={form.total} onChange={(value) => setForm({ ...form, total: value })} type="number" min="0" step="0.01" />
        <SelectField label="Condicion de pago" value={form.paymentCondition} onChange={(value) => setForm({ ...form, paymentCondition: value })} options={paymentConditionOptions} />
        <Field label="Fecha de vencimiento" value={form.dueDate} onChange={(value) => setForm({ ...form, dueDate: value })} type="date" disabled={form.paymentCondition !== 'CREDIT'} />
        <Field label="Soporte o evidencia" value={form.evidenceUrl} onChange={(value) => setForm({ ...form, evidenceUrl: value })} placeholder="URL opcional" />
      </div>
    </FormPanel>

    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>Gastos registrados</h1>
          <p className="hint">Consulta egresos y confirma los que deban afectar contabilidad.</p>
        </div>
        <button className="secondary" disabled={busy} onClick={onLoad} type="button">Consultar gastos</button>
      </header>
      <div className="form-grid compact">
        <SelectField label="Estado" value={filters.expenseStatus} onChange={(value) => setFilters({ ...filters, expenseStatus: value })} options={[
          { value: 'PENDING', label: 'Pendiente' },
          { value: 'CONFIRMED', label: 'Confirmado' },
        ]} placeholder="Todos" />
        <Field label="Desde" value={filters.expenseFrom} onChange={(value) => setFilters({ ...filters, expenseFrom: value })} type="date" />
        <Field label="Hasta" value={filters.expenseTo} onChange={(value) => setFilters({ ...filters, expenseTo: value })} type="date" />
      </div>
      <DataTable
        columns={['Fecha', 'Tipo', 'Concepto', 'Estado', 'Subtotal', 'IVA', 'Total', 'Vence', 'Acciones']}
        rows={expenses.map((expense) => expenseRow(expense, onConfirm, busy))}
        rowKey={(_row, index) => expenses[index]?.id || index}
        emptyMessage="Sin gastos consultados."
        sectionClassName="embedded-table"
      />
    </section>
  </div>;
}

const paymentConditionOptions = [
  { value: 'CASH', label: 'Contado' },
  { value: 'CREDIT', label: 'Credito' },
];

const expenseTypeOptions = [
  { value: 'OPERATING_EXPENSE', label: 'Gasto operativo' },
  { value: 'ASSET_PURCHASE', label: 'Compra de activo' },
];

function expenseRow(expense, onConfirm, busy) {
  return [
    shortDate(expense.expenseDate || expense.createdAt),
    expense.expenseType === 'ASSET_PURCHASE' ? 'Compra de activo' : 'Gasto operativo',
    expense.concept || '',
    expense.status === 'CONFIRMED' ? 'Confirmado' : 'Pendiente',
    money(expense.subtotal),
    money(expense.taxTotal),
    money(expense.total),
    shortDate(expense.dueDate),
    {
      searchText: `${expense.status || ''} ${expense.concept || ''}`,
      content: expense.status === 'CONFIRMED'
        ? 'Sin acciones'
        : <button className="secondary" disabled={busy} onClick={() => onConfirm(expense.id)} type="button">Confirmar</button>,
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
