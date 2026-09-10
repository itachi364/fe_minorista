import { DataTable } from '../../components/DataTable.jsx';

export function FiscalCalculationResult({ result, title = 'Resultado fiscal' }) {
  if (!result) return null;
  const items = result.items || [];
  return (
    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>{title}</h1>
          <p className="hint">Bruto {money(result.grossAmount)} | Retenciones {money(result.withholdingTotal)} | Neto {money(result.netPayable)}</p>
        </div>
      </header>
      <DataTable
        columns={['Retencion', 'Decision', 'Base linea', 'Acumulado anterior', 'Base acumulada', 'Tarifa', 'Valor', 'Regla', 'Razon']}
        rows={items.map((item) => [
          item.withholdingType,
          item.decision,
          money(item.baseAmount),
          money(item.previousAccumulatedBase),
          money(item.cumulativeBase ?? item.baseAmount),
          `${(Number(item.rate || 0) * 100).toLocaleString('es-CO')}%`,
          money(item.amount),
          item.ruleVersion || item.parameterVersion || 'Sin regla',
          item.sourceUrl ? { searchText: `${item.reason} ${item.legalReference}`, content: <a href={item.sourceUrl} target="_blank" rel="noreferrer">{item.reason}</a> } : item.reason,
        ])}
        rowKey={(_row, index) => items[index]?.id || `${items[index]?.withholdingType}-${index}`}
        emptyMessage="No hay decisiones fiscales para esta operacion."
        sectionClassName="embedded-table"
      />
    </section>
  );
}

function money(value) {
  return Number(value || 0).toLocaleString('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 2 });
}
