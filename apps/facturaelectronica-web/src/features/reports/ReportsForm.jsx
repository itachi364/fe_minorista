import { DataTable } from '../../components/DataTable.jsx';
import { Field, SelectField } from '../../components/forms.jsx';

const statusOptions = [
  { value: 'PENDING', label: 'Pendiente' },
  { value: 'CONFIRMED', label: 'Confirmado' },
  { value: 'PAID', label: 'Pagado' },
  { value: 'PARTIALLY_PAID', label: 'Parcialmente pagado' },
  { value: 'OPEN', label: 'Abierto' },
  { value: 'OVERDUE', label: 'Vencido' },
  { value: 'VOIDED', label: 'Anulado' },
];

export function ReportsForm({ form, setForm, data, onSubmit, onInitializeAccounting, busy }) {
  return <section className="tool-panel">
    <header className="panel-header">
      <div>
        <h1>Reportes</h1>
        <p>Consulta ventas, inventario y contabilidad de la empresa activa.</p>
      </div>
      <div className="toolbar-actions">
        <button className="secondary" disabled={busy} onClick={onInitializeAccounting} type="button">Configurar contabilidad basica</button>
        <button className="primary" disabled={busy} onClick={onSubmit} type="button">Consultar</button>
      </div>
    </header>
    <div className="form-grid">
      <SelectField label="Estado" value={form.status} onChange={(value) => setForm({ ...form, status: value })} options={statusOptions} placeholder="Todos" />
      <Field label="Desde" value={form.from} onChange={(value) => setForm({ ...form, from: value })} type="date" />
      <Field label="Hasta" value={form.to} onChange={(value) => setForm({ ...form, to: value })} type="date" />
      <Field label="Codigo cuenta PUC" value={form.accountCode} onChange={(value) => setForm({ ...form, accountCode: value })} placeholder="Opcional" />
    </div>
    {data && <ReportResults data={data} />}
  </section>;
}

function ReportResults({ data }) {
  return <div className="reports-grid">
    <ReportCard title="Ventas" rows={toSalesRows(data.sales)} columns={['Fecha', 'Estado', 'Subtotal', 'IVA', 'Total']} />
    <ReportCard title="Inventario" rows={toStockRows(data.stock)} columns={['Item', 'SKU', 'Stock', 'Costo', 'Precio']} />
    <ReportCard title="Gastos" rows={toExpenseRows(data.expenses)} columns={['Fecha', 'Estado', 'Concepto', 'Subtotal', 'Total']} />
    <ReportCard title="Cuentas por cobrar" rows={toReceivableRows(data.accountsReceivable)} columns={['Fecha', 'Estado', 'Cliente', 'Total', 'Saldo']} />
    <ReportCard title="Cuentas por pagar" rows={toPayableRows(data.accountsPayable)} columns={['Fecha', 'Estado', 'Proveedor', 'Total', 'Saldo']} />
    <ReportCard title="Estado de resultados" rows={toStatementRows(data.incomeStatement)} columns={['Grupo', 'Concepto', 'Total']} />
    <ReportCard title="Balance general basico" rows={toStatementRows(data.balanceSheet)} columns={['Grupo', 'Concepto', 'Total']} />
    <ReportCard title="Libro mayor" rows={toLedgerRows(data.ledger)} columns={['Cuenta', 'Nombre', 'Debitos', 'Creditos', 'Saldo']} />
    <ReportCard title="Libro diario" rows={toJournalRows(data.journal)} columns={['Fecha', 'Origen', 'Descripcion', 'Debitos', 'Creditos']} />
  </div>;
}

function ReportCard({ title, rows, columns }) {
  return <DataTable
    title={title}
    titleLevel={2}
    rows={rows}
    columns={columns}
    emptyMessage="Sin datos para el periodo."
    sectionClassName="report-card"
    rowKey={(_row, index) => `${title}-${index}`}
  />;
}

function toSalesRows(sales = []) {
  return sales.map((sale) => [
    shortDate(sale.createdAt || sale.saleDate || sale.issuedAt),
    sale.status || '',
    money(sale.subtotal),
    money(sale.taxTotal),
    money(sale.total),
  ]);
}

function toStockRows(stock = []) {
  return stock.map((item) => [
    item.name || item.productName || item.productId || '',
    item.sku || '',
    quantity(item.currentStock ?? item.stock ?? item.quantityOnHand),
    money(item.cost),
    money(item.salePrice),
  ]);
}

function toExpenseRows(expenses = []) {
  return expenses.map((expense) => [
    shortDate(expense.expenseDate),
    expense.status || '',
    expense.concept || '',
    money(expense.subtotal),
    money(expense.total),
  ]);
}

function toReceivableRows(receivables = []) {
  return receivables.map((item) => [
    shortDate(item.issueDate || item.dueDate),
    item.status || '',
    item.customerId || '',
    money(item.total),
    money(item.balance),
  ]);
}

function toPayableRows(payables = []) {
  return payables.map((item) => [
    shortDate(item.issueDate || item.dueDate),
    item.status || '',
    item.supplierId || '',
    money(item.total),
    money(item.balance),
  ]);
}

function toLedgerRows(ledger = {}) {
  return (ledger.accounts || []).map((account) => [
    account.accountCode || '',
    account.accountName || '',
    money(account.debitTotal),
    money(account.creditTotal),
    money(account.balance),
  ]);
}

function toStatementRows(statement = {}) {
  const rows = (statement.groups || []).map((group) => [
    group.code || '',
    group.label || '',
    money(group.total),
  ]);
  if (statement.statementType) {
    rows.push(['', 'Resultado', money(statement.total)]);
  }
  return rows;
}

function toJournalRows(journal = {}) {
  return (journal.entries || []).map((entry) => [
    shortDate(entry.entryDate),
    entry.sourceType || '',
    entry.description || '',
    money(entry.debitTotal),
    money(entry.creditTotal),
  ]);
}

function shortDate(value) {
  if (!value) {
    return '';
  }
  return String(value).slice(0, 10);
}

function money(value) {
  if (value === null || value === undefined || value === '') {
    return '';
  }
  return Number(value).toLocaleString('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 2 });
}

function quantity(value) {
  if (value === null || value === undefined || value === '') {
    return '';
  }
  return Number(value).toLocaleString('es-CO');
}
