import { CheckField, Field, FormPanel, SelectField, StatusBadge } from '../../components/forms.jsx';
import { companyLabel } from '../../utils/company.js';
import { licenseModuleLabel, licenseModuleOptions } from '../../data/licenseModules.js';

const planOptions = [
  { value: 'BASIC', label: 'Basico' },
  { value: 'POS', label: 'POS y facturacion' },
  { value: 'FULL', label: 'Completo' },
  { value: 'CUSTOM', label: 'Personalizado' },
];

export function LicenseAdminPanel({
  form,
  setForm,
  companies,
  license,
  onCompanyChange,
  onLoad,
  onSave,
  onActivate,
  onSuspend,
  busy,
}) {
  const selectedCompany = companies.find((company) => company.id === form.companyId);
  const selectedModules = new Set(form.enabledModules || []);

  function toggleModule(moduleCode, checked) {
    const nextModules = checked
      ? [...selectedModules, moduleCode]
      : [...selectedModules].filter((current) => current !== moduleCode);
    setForm({ ...form, enabledModules: nextModules });
  }

  return (
    <div className="stack">
      <FormPanel title="Licencia empresarial" submitLabel="Guardar licencia" onSubmit={onSave} busy={busy || !form.companyId}>
        <div className="form-grid">
          <label>
            Empresa contratante
            <select value={form.companyId} onChange={(event) => onCompanyChange(event.target.value)} disabled={busy || companies.length === 0}>
              <option value="">Seleccione una empresa</option>
              {companies.map((company) => <option key={company.id} value={company.id}>{companyLabel(company)}</option>)}
            </select>
          </label>
          <SelectField label="Tipo de licencia" value={form.planCode} onChange={(value) => setForm({ ...form, planCode: value })} options={planOptions} disabled={busy} />
          <Field label="Fecha inicio" value={form.validFrom} onChange={(value) => setForm({ ...form, validFrom: value })} type="date" />
          <Field label="Fecha vencimiento" value={form.validTo} onChange={(value) => setForm({ ...form, validTo: value })} type="date" />
          <Field label="Maximo usuarios" value={form.maxUsers} onChange={(value) => setForm({ ...form, maxUsers: value })} type="number" />
          <Field label="Maximo documentos mensuales" value={form.maxMonthlyDocuments} onChange={(value) => setForm({ ...form, maxMonthlyDocuments: value })} type="number" />
        </div>
        <section className="module-license-grid" aria-label="Modulos contratados">
          {licenseModuleOptions.map((module) => (
            <CheckField
              key={module.value}
              label={module.label}
              checked={selectedModules.has(module.value)}
              onChange={(checked) => toggleModule(module.value, checked)}
              disabled={busy}
            />
          ))}
        </section>
        <footer className="panel-actions">
          <button className="secondary" disabled={busy || !form.companyId} onClick={onLoad} type="button">Cargar licencia</button>
          <button className="secondary" disabled={busy || !license?.id} onClick={onActivate} type="button">Activar</button>
          <button className="danger-button" disabled={busy || !license?.id} onClick={onSuspend} type="button">Suspender</button>
        </footer>
      </FormPanel>

      <section className="tool-panel">
        <header className="panel-header">
          <div>
            <h1>Resumen de licencia</h1>
            <p>{selectedCompany ? companyLabel(selectedCompany) : 'Selecciona una empresa para administrar su licencia.'}</p>
          </div>
          {license?.status && <StatusBadge label="Estado" value={license.status} tone={license.status === 'ACTIVE' ? 'ok' : 'warn'} />}
        </header>
        <div className="license-summary">
          <p><b>Vigencia:</b> {license?.validFrom || 'Sin configurar'} - {license?.validTo || 'Sin configurar'}</p>
          <p><b>Plan:</b> {license?.planCode || 'Sin configurar'}</p>
          <p><b>Modulos:</b> {(license?.enabledModules || []).map(licenseModuleLabel).join(', ') || 'Sin modulos contratados'}</p>
        </div>
      </section>
    </div>
  );
}
