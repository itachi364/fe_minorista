import { DataTable } from '../../components/DataTable.jsx';
import { StatusBadge } from '../../components/forms.jsx';

const eventLabels = {
  SALE_CONFIRMED: 'Venta facturada',
  PURCHASE_CONFIRMED: 'Compra confirmada',
  EXPENSE_CONFIRMED: 'Egreso confirmado',
  PAYROLL_PAYMENT_CONFIRMED: 'Pago de nomina',
};

const sourceLabels = {
  SALE: 'Venta',
  PURCHASE: 'Compra',
  EXPENSE: 'Egreso',
  PAYROLL: 'Nomina',
};

export function AccountingConfigurationPanel({ accounts, rules, onLoad, onInitialize, busy }) {
  return <section className="accounting-configuration">
    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>Configuracion contable</h1>
          <p className="hint">Inicializa y valida las cuentas PUC y reglas minimas usadas por ventas, compras, egresos y reportes contables.</p>
        </div>
        <div className="toolbar-actions">
          <button className="secondary" disabled={busy} onClick={onLoad} type="button">Actualizar estado</button>
          <button className="primary" disabled={busy} onClick={onInitialize} type="button">Inicializar contabilidad basica</button>
        </div>
      </header>
      <div className="status-row">
        <StatusBadge label="Cuentas" value={accounts.length} tone={accounts.length > 0 ? 'ok' : 'warn'} />
        <StatusBadge label="Reglas activas" value={rules.filter((rule) => rule.active).length} tone={rules.some((rule) => rule.active) ? 'ok' : 'warn'} />
        <StatusBadge label="Ventas POS" value={hasActiveSaleRule(rules) ? 'Lista' : 'Pendiente'} tone={hasActiveSaleRule(rules) ? 'ok' : 'warn'} />
      </div>
    </section>

    <DataTable
      title="Reglas contables"
      description="Estas reglas determinan los asientos automaticos que se crean al cerrar operaciones."
      columns={['Evento', 'Origen', 'Nombre', 'Lineas', 'Estado']}
      rows={rules.map((rule) => [
        eventLabels[rule.eventType] || rule.eventType,
        sourceLabels[rule.sourceType] || rule.sourceType,
        rule.name,
        rule.lines?.length || 0,
        rule.active ? 'Activa' : 'Inactiva',
      ])}
      emptyMessage="Sin reglas configuradas. Inicializa la contabilidad basica antes de cerrar ventas."
      rowKey={(row) => `${row[0]}-${row[2]}`}
      pageSize={5}
    />

    <DataTable
      title="Plan de cuentas"
      description="Cuentas PUC disponibles para registrar asientos y generar reportes financieros."
      columns={['Codigo', 'Cuenta', 'Categoria', 'Naturaleza', 'Estado']}
      rows={accounts.map((account) => [
        account.code,
        account.name,
        account.category,
        account.nature,
        account.active ? 'Activa' : 'Inactiva',
      ])}
      emptyMessage="Sin cuentas configuradas."
      rowKey={(row) => row[0]}
      pageSize={8}
    />
  </section>;
}

function hasActiveSaleRule(rules) {
  return rules.some((rule) => rule.eventType === 'SALE_CONFIRMED' && rule.active);
}
