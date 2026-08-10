import { Field } from '../../components/forms.jsx';

export function AuditLogPanel({ events, filters, setFilters, onLoad, busy, canViewGlobal, activeCompanyId }) {
  return (
    <section className="audit-panel stack">
      <section className="tool-panel">
        <header className="panel-header">
          <div>
            <h1>Logs / Auditoria</h1>
            <p className="hint">{canViewGlobal ? 'Consulta auditoria de la empresa activa seleccionada por ROOT.' : 'Consulta auditoria de tu empresa.'}</p>
          </div>
          <button className="primary" type="button" onClick={onLoad} disabled={busy || !activeCompanyId}>Consultar logs</button>
        </header>
        <div className="form-grid compact">
          <Field label="Tipo de recurso" value={filters.resourceType} onChange={(value) => setFilters({ ...filters, resourceType: value })} placeholder="SALE, CATALOG_ITEM..." />
          <Field label="ID recurso" value={filters.resourceId} onChange={(value) => setFilters({ ...filters, resourceId: value })} placeholder="Identificador del recurso" />
          <Field label="Desde" value={filters.from} onChange={(value) => setFilters({ ...filters, from: value })} type="datetime-local" />
          <Field label="Hasta" value={filters.to} onChange={(value) => setFilters({ ...filters, to: value })} type="datetime-local" />
        </div>
      </section>

      <section className="tool-panel">
        <header className="panel-header">
          <div>
            <h1>Eventos registrados</h1>
            <p className="hint">Las acciones muestran detalle seguro. Para trazas tecnicas completas usa el correlationId en logs del servicio.</p>
          </div>
        </header>
        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>Fecha</th>
                <th>Usuario</th>
                <th>Accion</th>
                <th>Recurso</th>
                <th>Resultado</th>
                <th>Detalle</th>
              </tr>
            </thead>
            <tbody>
              {events.length === 0 && <tr><td colSpan="6">No hay eventos cargados.</td></tr>}
              {events.map((event) => (
                <tr key={event.id}>
                  <td>{formatDate(event.occurredAt)}</td>
                  <td><code>{event.userId || 'Sistema'}</code></td>
                  <td>{event.action}</td>
                  <td>{event.resourceType}<br /><code>{event.resourceId || 'N/A'}</code></td>
                  <td>{event.result}</td>
                  <td className="audit-detail">{event.detail || 'Sin detalle'}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </section>
    </section>
  );
}

function formatDate(value) {
  if (!value) {
    return 'N/A';
  }
  return new Date(value).toLocaleString('es-CO');
}
