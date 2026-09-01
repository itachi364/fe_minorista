import { DataTable } from '../../components/DataTable.jsx';
import { Field, FormPanel, SelectField } from '../../components/forms.jsx';

export function ReceivablesPanel({
  form,
  setForm,
  paymentForm,
  setPaymentForm,
  customers = [],
  receivables = [],
  filters,
  setFilters,
  paymentOptions = [],
  onCreate,
  onRegisterPayment,
  onLoad,
  busy,
}) {
  const customerOptions = customers.map((thirdParty) => ({
    value: thirdParty.id,
    label: `${thirdParty.identificationNumber || 'Sin documento'} - ${thirdParty.businessName || thirdParty.fullName || thirdParty.tradeName || thirdParty.id}`,
  }));
  const receivableOptions = receivables
    .filter((receivable) => receivable.status !== 'PAID')
    .map((receivable) => ({
      value: receivable.id,
      label: `${shortId(receivable.id)} - ${money(receivable.balance)} pendientes`,
    }));

  return <div className="stack">
    <FormPanel title="Deudor / cuenta por cobrar" submitLabel="Crear deudor" onSubmit={onCreate} busy={busy}>
      <p className="hint">Registra dinero que un cliente o tercero le debe al negocio y controla sus abonos.</p>
      <div className="form-grid">
        <SelectField label="Cliente deudor" value={form.customerId} onChange={(value) => setForm({ ...form, customerId: value })} options={customerOptions} />
        <Field label="Fecha de registro" value={form.issueDate} onChange={(value) => setForm({ ...form, issueDate: value })} type="date" />
        <Field label="Fecha de vencimiento" value={form.dueDate} onChange={(value) => setForm({ ...form, dueDate: value })} type="date" />
        <Field label="Valor adeudado" value={form.totalAmount} onChange={(value) => setForm({ ...form, totalAmount: value })} type="number" min="0" step="0.01" />
      </div>
    </FormPanel>

    <FormPanel title="Abono de deudor" submitLabel="Registrar abono" onSubmit={onRegisterPayment} busy={busy}>
      <div className="form-grid">
        <SelectField label="Cuenta por cobrar" value={paymentForm.receivableId} onChange={(value) => setPaymentForm({ ...paymentForm, receivableId: value })} options={receivableOptions} />
        <Field label="Fecha de pago" value={paymentForm.paymentDate} onChange={(value) => setPaymentForm({ ...paymentForm, paymentDate: value })} type="date" />
        <Field label="Valor abonado" value={paymentForm.amount} onChange={(value) => setPaymentForm({ ...paymentForm, amount: value })} type="number" min="0" step="0.01" />
        <SelectField label="Metodo de pago" value={paymentForm.paymentMethod} onChange={(value) => setPaymentForm({ ...paymentForm, paymentMethod: value })} options={paymentOptions} />
        <Field label="Referencia" value={paymentForm.reference} onChange={(value) => setPaymentForm({ ...paymentForm, reference: value })} />
      </div>
    </FormPanel>

    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>Deudores registrados</h1>
          <p className="hint">Consulta saldos, vencimientos y estado de cuentas por cobrar.</p>
        </div>
        <button className="secondary" disabled={busy} onClick={onLoad} type="button">Consultar deudores</button>
      </header>
      <div className="form-grid compact">
        <SelectField label="Estado" value={filters.receivableStatus} onChange={(value) => setFilters({ ...filters, receivableStatus: value })} options={[
          { value: 'OPEN', label: 'Abierto' },
          { value: 'PARTIALLY_PAID', label: 'Con abonos' },
          { value: 'PAID', label: 'Pagado' },
          { value: 'OVERDUE', label: 'Vencido' },
        ]} placeholder="Todos" />
        <Field label="Desde" value={filters.receivableFrom} onChange={(value) => setFilters({ ...filters, receivableFrom: value })} type="date" />
        <Field label="Hasta" value={filters.receivableTo} onChange={(value) => setFilters({ ...filters, receivableTo: value })} type="date" />
      </div>
      <DataTable
        columns={['Cliente', 'Emision', 'Vence', 'Valor', 'Abonado', 'Saldo', 'Estado']}
        rows={receivables.map((receivable) => receivableRow(receivable, customers))}
        rowKey={(_row, index) => receivables[index]?.id || index}
        emptyMessage="Sin deudores consultados."
        sectionClassName="embedded-table"
      />
    </section>
  </div>;
}

function receivableRow(receivable, customers) {
  const customer = customers.find((item) => item.id === receivable.customerId);
  return [
    customer?.businessName || customer?.fullName || customer?.tradeName || receivable.customerId || '',
    shortDate(receivable.issueDate),
    shortDate(receivable.dueDate),
    money(receivable.totalAmount),
    money(receivable.paidAmount),
    money(receivable.balance),
    statusLabel(receivable.status),
  ];
}

function statusLabel(status) {
  return {
    OPEN: 'Abierto',
    PARTIALLY_PAID: 'Con abonos',
    PAID: 'Pagado',
    OVERDUE: 'Vencido',
    CANCELLED: 'Cancelado',
  }[status] || status || '';
}

function shortId(value) {
  return value ? String(value).slice(0, 8) : '';
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
