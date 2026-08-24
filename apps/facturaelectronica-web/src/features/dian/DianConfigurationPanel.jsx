import { CheckField, Field, FormPanel, SelectField, StatusBadge } from '../../components/forms.jsx';

const modeOptions = [
  { value: 'MOCK', label: 'Mock interno de pruebas' },
  { value: 'REAL', label: 'Conexion real DIAN por empresa' },
];

const environmentOptions = [
  { value: 'TEST', label: 'Pruebas / habilitacion' },
  { value: 'PRODUCTION', label: 'Produccion' },
];

export function DianConfigurationPanel({
  form,
  setForm,
  configuration,
  onLoad,
  onSave,
  onTest,
  onActivate,
  onDeactivate,
  busy,
}) {
  const isReal = form.mode === 'REAL';
  return <section className="stacked">
    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>Configuracion DIAN</h1>
          <p>Parametros propios de la empresa facturadora. NexoFiscal no actua como proveedor tecnologico DIAN.</p>
        </div>
        <button className="secondary" type="button" onClick={onLoad} disabled={busy}>Consultar estado</button>
      </header>
      <div className="status-row">
        <StatusBadge label="Estado" value={configuration?.status || 'Sin configurar'} tone={configuration?.status === 'ACTIVE' ? 'ok' : 'warn'} />
        <StatusBadge label="Prueba" value={configuration?.lastTestStatus || 'Sin prueba'} tone={configuration?.lastTestStatus === 'SUCCESS' ? 'ok' : 'warn'} />
        <StatusBadge label="Certificado" value={configuration?.certificateConfigured ? 'Configurado' : 'Pendiente'} tone={configuration?.certificateConfigured ? 'ok' : 'warn'} />
      </div>
    </section>

    <FormPanel title="Conector DIAN por empresa" submitLabel="Guardar configuracion" onSubmit={onSave} busy={busy}>
      <div className="form-grid compact">
        <SelectField label="Modo" value={form.mode} onChange={(value) => setForm({ ...form, mode: value })} options={modeOptions} />
        <SelectField label="Ambiente" value={form.environment} onChange={(value) => setForm({ ...form, environment: value })} options={environmentOptions} />
        <Field label="Software ID" value={form.softwareId} onChange={(value) => setForm({ ...form, softwareId: value })} disabled={!isReal} />
        <Field label="Software PIN" value={form.softwarePin} onChange={(value) => setForm({ ...form, softwarePin: value })} type="password" disabled={!isReal} autoComplete="new-password" />
        <Field label="Clave tecnica" value={form.technicalKey} onChange={(value) => setForm({ ...form, technicalKey: value })} type="password" disabled={!isReal} autoComplete="new-password" />
        <Field label="Alias certificado" value={form.certificateAlias} onChange={(value) => setForm({ ...form, certificateAlias: value })} disabled={!isReal} />
        <Field label="Huella certificado" value={form.certificateFingerprint} onChange={(value) => setForm({ ...form, certificateFingerprint: value })} disabled={!isReal} />
        <Field label="Vencimiento certificado" value={form.certificateExpiresAt} onChange={(value) => setForm({ ...form, certificateExpiresAt: value })} type="datetime-local" disabled={!isReal} />
        <Field label="Password certificado" value={form.certificatePassword} onChange={(value) => setForm({ ...form, certificatePassword: value })} type="password" disabled={!isReal} autoComplete="new-password" />
        <Field label="URL servicio DIAN" value={form.serviceBaseUrl} onChange={(value) => setForm({ ...form, serviceBaseUrl: value })} disabled={!isReal} />
        <Field label="Set de pruebas" value={form.testSetId} onChange={(value) => setForm({ ...form, testSetId: value })} disabled={!isReal} />
      </div>
      <label>
        Certificado en base64 / PEM
        <textarea value={form.certificatePayload} onChange={(event) => setForm({ ...form, certificatePayload: event.target.value })} disabled={!isReal} rows={5} />
      </label>
      <CheckField label="La empresa acepta que es responsable de su habilitacion, certificado y credenciales ante la DIAN." checked={form.acceptedResponsibility} onChange={(value) => setForm({ ...form, acceptedResponsibility: value })} disabled={!isReal} />
    </FormPanel>

    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>Pruebas y activacion</h1>
          <p>Activa la conexion solo despues de una prueba exitosa y configuracion completa.</p>
        </div>
      </header>
      <div className="button-row">
        <button className="secondary" type="button" onClick={onTest} disabled={busy || !configuration}>Probar conexion</button>
        <button className="primary" type="button" onClick={onActivate} disabled={busy || !configuration}>Activar</button>
        <button className="secondary danger-soft" type="button" onClick={onDeactivate} disabled={busy || !configuration}>Inactivar</button>
      </div>
      {configuration?.lastTestMessage && <p className="muted">{configuration.lastTestMessage}</p>}
    </section>
  </section>;
}
