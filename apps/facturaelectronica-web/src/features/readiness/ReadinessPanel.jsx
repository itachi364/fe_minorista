import { DataTable } from '../../components/DataTable.jsx';
import { StatusBadge } from '../../components/forms.jsx';

const statusLabels = {
  READY: 'Listo',
  WARNING: 'Atencion',
  BLOCKED: 'Bloqueado',
};

export function ReadinessPanel({ readiness, onRefresh, onOpenStep, busy }) {
  const items = readiness?.items || [];
  const readyCount = items.filter((item) => item.status === 'READY').length;
  const blockedCount = items.filter((item) => item.status === 'BLOCKED').length;
  const warningCount = items.filter((item) => item.status === 'WARNING').length;

  return <section className="readiness-workspace">
    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>Puesta en marcha</h1>
          <p className="hint">Revisa si la empresa esta lista para vender, contabilizar y operar sin bloqueos.</p>
        </div>
        <button className="primary" disabled={busy} onClick={onRefresh} type="button">Actualizar estado</button>
      </header>
      <div className="status-row">
        <StatusBadge label="Estado" value={statusLabels[readiness?.status] || 'Sin consultar'} tone={tone(readiness?.status)} />
        <StatusBadge label="Listos" value={readyCount} tone="ok" />
        <StatusBadge label="Atencion" value={warningCount} tone={warningCount > 0 ? 'warn' : 'ok'} />
        <StatusBadge label="Bloqueos" value={blockedCount} tone={blockedCount > 0 ? 'warn' : 'ok'} />
      </div>
    </section>

    <DataTable
      title="Configuraciones requeridas"
      description="Cada fila indica que falta y a que modulo ir para corregirlo."
      columns={['Proceso', 'Estado', 'Mensaje', 'Accion']}
      rows={items.map((item) => [
        item.label,
        statusLabels[item.status] || item.status,
        item.message,
        item.actionStep
          ? {
              searchText: item.actionStep,
              content: <button className="secondary" disabled={busy} onClick={() => onOpenStep(item.actionStep)} type="button">Abrir {item.actionStep}</button>,
            }
          : 'Sin accion',
      ])}
      emptyMessage="Actualiza el estado para ver los faltantes de la empresa."
      rowKey={(row) => row[0]}
      pageSize={10}
    />
  </section>;
}

function tone(status) {
  if (status === 'READY') {
    return 'ok';
  }
  if (status === 'WARNING' || status === 'BLOCKED') {
    return 'warn';
  }
  return '';
}
