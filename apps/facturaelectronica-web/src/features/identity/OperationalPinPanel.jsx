import { Field, FormPanel, StatusBadge } from '../../components/forms.jsx';

export function OperationalPinPanel({
  form,
  setForm,
  status,
  onSave,
  onUnlock,
  busy,
}) {
  const pinError = form.pin && form.pin.length !== 6
    ? 'El PIN debe tener exactamente 6 digitos numericos.'
    : '';
  const canSubmit = /^\d{6}$/.test(form.pin);
  const state = operationalPinState(status);
  const submitLabel = status?.configured ? 'Cambiar PIN' : 'Crear PIN';

  function updatePin(value) {
    setForm({ ...form, pin: onlyDigits(value).slice(0, 6) });
  }

  return (
    <div className="stack">
      <section className="tool-panel">
        <header className="panel-header">
          <div>
            <h1>PIN operacional</h1>
            <p>Estado cargado automaticamente. Administra el PIN de 6 digitos usado para autorizar cambios excepcionales en ventas fiscales.</p>
          </div>
        </header>
        <div className="summary-strip">
          <StatusBadge label="Configuracion" value={status?.configured ? 'Configurado' : 'Sin configurar'} tone={status?.configured ? 'ok' : 'warn'} />
          <StatusBadge label="Estado" value={state.label} tone={state.tone} />
          <StatusBadge label="Intentos" value={status?.remainingAttempts ?? 3} tone={status?.remainingAttempts === 0 ? 'warn' : 'ok'} />
          <StatusBadge label="Actualizado" value={formatDateTime(status?.updatedAt)} />
        </div>
      </section>

      <FormPanel title={submitLabel} submitLabel={submitLabel} onSubmit={onSave} busy={busy || !canSubmit}>
        <div className="form-grid compact">
          <Field
            label="PIN de 6 digitos"
            value={form.pin}
            onChange={updatePin}
            type="password"
            inputMode="numeric"
            pattern="[0-9]*"
            maxLength={6}
            autoComplete="new-password"
            error={pinError}
          />
        </div>
        <p className="hint">El PIN no reemplaza la contrasena de inicio de sesion y nunca se muestra de vuelta despues de guardarse.</p>
      </FormPanel>

      <section className="tool-panel">
        <header className="panel-header">
          <div>
            <h1>Desbloqueo</h1>
            <p>Cuando el PIN se bloquea por 3 intentos fallidos, un administrador autorizado puede desbloquearlo y exigir cambio.</p>
          </div>
          <button className="secondary" disabled={busy || !status?.locked} onClick={onUnlock} type="button">Desbloquear PIN</button>
        </header>
      </section>
    </div>
  );
}

function onlyDigits(value) {
  return String(value || '').replace(/\D/g, '');
}

function operationalPinState(status) {
  if (!status?.configured) {
    return { label: 'Pendiente', tone: 'warn' };
  }
  if (status.locked) {
    return { label: 'Bloqueado', tone: 'warn' };
  }
  if (status.mustChange) {
    return { label: 'Cambio requerido', tone: 'warn' };
  }
  return { label: 'Activo', tone: 'ok' };
}

function formatDateTime(value) {
  if (!value) {
    return 'Sin fecha';
  }
  return new Date(value).toLocaleString('es-CO');
}
