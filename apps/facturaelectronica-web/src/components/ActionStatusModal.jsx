export function ActionStatusModal({ state, onClose }) {
  if (!state || state.status === 'idle') {
    return null;
  }
  const running = state.status === 'running';
  const title = running ? 'Procesando accion' : state.status === 'success' ? 'Proceso completado' : 'No se pudo completar';
  const message = running
    ? state.message || 'Estamos ejecutando el proceso solicitado.'
    : state.status === 'success'
      ? state.message || 'La accion se realizo correctamente.'
      : state.message || 'Hay un error al realizar la accion. Revisa Logs/Auditoria para mas detalle.';

  return (
    <div className="modal-backdrop" role="presentation">
      <section className="modal-card action-modal" role="dialog" aria-modal="true" aria-label={title}>
        <h2>{title}</h2>
        <p>{message}</p>
        <div className={running ? 'progress-track active' : 'progress-track'}>
          <span />
        </div>
        {state.correlationId && <p className="hint">Correlacion: <code>{state.correlationId}</code></p>}
        {!running && <button className="primary" type="button" onClick={onClose}>Cerrar</button>}
      </section>
    </div>
  );
}
