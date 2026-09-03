import { DataTable } from '../../components/DataTable.jsx';

export function FinanceDashboard({ summary, onRefresh, busy }) {
  const groups = summary?.incomeStatement?.groups || [];
  const receivables = summary?.receivables || [];
  const payables = summary?.payables || [];
  const net = Number(summary?.incomeStatement?.total || 0);
  const receivableBalance = receivables.reduce((total, item) => total + number(item.balance), 0);
  const payableBalance = payables.reduce((total, item) => total + number(item.balance), 0);
  const liquidity = receivableBalance - payableBalance;

  return <div className="stack">
    <section className="tool-panel finance-dashboard">
      <header className="panel-header">
        <div>
          <h1>Finanzas</h1>
          <p className="hint">Resumen diario para revisar utilidad, deudas por cobrar y obligaciones por pagar.</p>
        </div>
        <button className="secondary" disabled={busy} onClick={onRefresh} type="button">Actualizar</button>
      </header>
      <div className="finance-kpis">
        <FinanceKpi label="Resultado del periodo" value={money(net)} tone={net >= 0 ? 'positive' : 'negative'} />
        <FinanceKpi label="Por cobrar" value={money(receivableBalance)} />
        <FinanceKpi label="Por pagar" value={money(payableBalance)} tone={payableBalance > receivableBalance ? 'warning' : 'neutral'} />
        <FinanceKpi label="Liquidez proyectada" value={money(liquidity)} tone={liquidity >= 0 ? 'positive' : 'negative'} />
      </div>
    </section>

    <div className="split">
      <DataTable
        title="Resultado financiero"
        titleLevel={2}
        columns={['Concepto', 'Valor']}
        rows={groups.map((group) => [group.label || group.group || '', money(group.total)])}
        emptyMessage="Sin movimientos contables en el rango automatico."
      />
      <DataTable
        title="Alertas"
        titleLevel={2}
        columns={['Indicador', 'Estado']}
        rows={[
          ['Cuentas por cobrar abiertas', receivables.length],
          ['Cuentas por pagar abiertas', payables.length],
          ['Balance operativo', net >= 0 ? 'Utilidad' : 'Perdida'],
        ]}
        searchable={false}
      />
    </div>

    <div className="split">
      <DataTable
        title="Cuentas por cobrar"
        titleLevel={2}
        columns={['Cliente', 'Vence', 'Saldo', 'Estado']}
        rows={receivables.map((item) => [shortId(item.customerId), shortDate(item.dueDate), money(item.balance), statusLabel(item.status)])}
        emptyMessage="Sin deudores abiertos para el rango automatico."
      />
      <DataTable
        title="Cuentas por pagar"
        titleLevel={2}
        columns={['Proveedor', 'Vence', 'Saldo', 'Estado']}
        rows={payables.map((item) => [shortId(item.supplierId), shortDate(item.dueDate), money(item.balance), statusLabel(item.status)])}
        emptyMessage="Sin obligaciones por pagar para el rango automatico."
      />
    </div>
  </div>;
}

function FinanceKpi({ label, value, tone = 'neutral' }) {
  return <span className={`finance-kpi ${tone}`}>
    <b>{label}</b>
    <strong>{value}</strong>
  </span>;
}

function number(value) {
  const parsed = Number(value || 0);
  return Number.isFinite(parsed) ? parsed : 0;
}

function money(value) {
  return number(value).toLocaleString('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 2 });
}

function shortDate(value) {
  return value ? String(value).slice(0, 10) : '';
}

function shortId(value) {
  return value ? String(value).slice(0, 8) : 'Sin tercero';
}

function statusLabel(status) {
  return {
    OPEN: 'Abierta',
    PARTIALLY_PAID: 'Con abonos',
    PAID: 'Pagada',
    OVERDUE: 'Vencida',
    PENDING: 'Pendiente',
    CONFIRMED: 'Confirmada',
  }[status] || status || '';
}
