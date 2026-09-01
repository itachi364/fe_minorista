import { DataTable } from '../../components/DataTable.jsx';
import { Field, SelectField } from '../../components/forms.jsx';

export function AuditLogPanel({ events, filters, setFilters, busy, canViewGlobal, activeCompanyId, resourceTypes = [] }) {
  const resourceTypeOptions = resourceTypes.map((resourceType) => ({ value: resourceType, label: resourceType }));
  return (
    <section className="audit-panel stack">
      <section className="tool-panel">
        <header className="panel-header">
          <div>
            <h1>Logs / Auditoria</h1>
            <p className="hint">{canViewGlobal ? 'Auditoria cargada automaticamente para la empresa activa seleccionada por ROOT.' : 'Auditoria cargada automaticamente para tu empresa.'}</p>
          </div>
        </header>
        <div className="form-grid compact">
          <SelectField label="Tipo de recurso" value={filters.resourceType} onChange={(value) => setFilters({ ...filters, resourceType: value })} options={resourceTypeOptions} placeholder="Todos los recursos" />
          <Field label="Desde" value={filters.from} onChange={(value) => setFilters({ ...filters, from: value })} type="datetime-local" />
          <Field label="Hasta" value={filters.to} onChange={(value) => setFilters({ ...filters, to: value })} type="datetime-local" />
        </div>
      </section>

      <DataTable
        title="Eventos registrados"
        description="Las acciones muestran detalle seguro. Para trazas tecnicas completas usa el correlationId en logs del servicio."
        columns={['Fecha', 'Usuario', 'Accion', 'Recurso', 'Resultado', 'Detalle']}
        rows={events.map((event) => [
          formatDate(event.occurredAt),
          <code>{event.userId || 'Sistema'}</code>,
          event.action,
          event.resourceType,
          event.result,
          <span className="audit-detail">{event.detail || 'Sin detalle'}</span>,
        ])}
        emptyMessage="No hay eventos para el filtro actual."
        rowKey={(_row, index) => events[index]?.id || index}
      />
    </section>
  );
}

function formatDate(value) {
  if (!value) {
    return 'N/A';
  }
  return new Date(value).toLocaleString('es-CO');
}
