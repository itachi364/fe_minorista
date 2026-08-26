export function Modal({ title, message, onClose }) {
  return (
    <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="license-modal-title">
      <section className="modal-card">
        <h1 id="license-modal-title">{title}</h1>
        <p>{message}</p>
        <button className="primary" onClick={onClose} type="button">Aceptar</button>
      </section>
    </div>
  );
}

export function ActionModal({ title, children, onClose, size = 'default' }) {
  return <div className="modal-backdrop" role="dialog" aria-modal="true" aria-labelledby="action-modal-title">
    <section className={`modal-card action-modal-card modal-${size}`}>
      <header className="modal-header">
        <h1 id="action-modal-title">{title}</h1>
        <button className="secondary" onClick={onClose} type="button">Cerrar</button>
      </header>
      {children}
    </section>
  </div>;
}
