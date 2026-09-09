import { CheckField, Field, FormPanel, SelectField, StatusBadge } from '../../components/forms.jsx';
import { companyLabel } from '../../utils/company.js';
import {
  canEditLicenseModules,
  commercialLicenseFeatureOptions,
  featuresForLicensePlan,
  licenseModuleLabel,
  licenseModuleOptions,
  licensePlanOptions,
  modulesForLicensePlan,
} from '../../data/licenseModules.js';
import { DataTable } from '../../components/DataTable.jsx';

export function LicenseAdminPanel({
  form,
  setForm,
  companies,
  license,
  usage,
  onCompanyChange,
  onLoad,
  onSave,
  onActivate,
  onSuspend,
  busy,
}) {
  const selectedCompany = companies.find((company) => company.id === form.companyId);
  const selectedModules = new Set(form.enabledModules || []);
  const selectedFeatures = new Set(form.enabledFeatures || []);
  const licenseLoadedForSelectedCompany = Boolean(form.companyId && license?.companyId === form.companyId);
  const manualModuleSelection = canEditLicenseModules(form.planCode);

  function changePlan(planCode) {
    setForm({
      ...form,
      planCode,
      enabledModules: modulesForLicensePlan(planCode, form.enabledModules),
      enabledFeatures: featuresForLicensePlan(planCode, form.enabledFeatures, form.enabledModules),
    });
  }

  function toggleModule(moduleCode, checked) {
    if (!manualModuleSelection) {
      return;
    }
    const module = licenseModuleOptions.find((item) => item.value === moduleCode);
    const moduleFeatures = new Set((module?.features || []).map((item) => item.value));
    const nextModules = checked
      ? [...selectedModules, moduleCode]
      : [...selectedModules].filter((current) => current !== moduleCode);
    const nextFeatures = checked
      ? [...new Set([...selectedFeatures, ...moduleFeatures])]
      : [...selectedFeatures].filter((current) => !moduleFeatures.has(current));
    setForm({ ...form, enabledModules: nextModules, enabledFeatures: nextFeatures });
  }

  function toggleFeature(featureCode, checked) {
    if (!manualModuleSelection) return;
    const nextFeatures = checked
      ? [...selectedFeatures, featureCode]
      : [...selectedFeatures].filter((current) => current !== featureCode);
    setForm({ ...form, enabledFeatures: [...new Set(nextFeatures)] });
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
          <SelectField label="Tipo de licencia" value={form.planCode} onChange={changePlan} options={licensePlanOptions} disabled={busy} />
          <Field label="Fecha inicio" value={form.validFrom} onChange={(value) => setForm({ ...form, validFrom: value })} type="date" />
          <Field label="Fecha vencimiento" value={form.validTo} onChange={(value) => setForm({ ...form, validTo: value })} type="date" />
          <Field label="Maximo usuarios" value={form.maxUsers} onChange={(value) => setForm({ ...form, maxUsers: value })} type="number" />
          <Field label="Maximo documentos mensuales" value={form.maxMonthlyDocuments} onChange={(value) => setForm({ ...form, maxMonthlyDocuments: value })} type="number" />
        </div>
        <section className="license-entitlements" aria-label="Modulos y funcionalidades contratadas">
          {licenseModuleOptions.map((module) => (
            <fieldset className="license-module-group" key={module.value}>
              <legend>
                <CheckField label={module.label} checked={selectedModules.has(module.value)}
                  onChange={(checked) => toggleModule(module.value, checked)} disabled={busy || !manualModuleSelection} />
              </legend>
              <div className="license-feature-list">
                {module.features.map((item) => <CheckField key={item.value} label={item.label}
                  checked={selectedFeatures.has(item.value)} onChange={(checked) => toggleFeature(item.value, checked)}
                  disabled={busy || !manualModuleSelection || !selectedModules.has(module.value)} />)}
              </div>
            </fieldset>
          ))}
        </section>
        {manualModuleSelection && <fieldset className="license-commercial-options">
          <legend>Servicios y personalizaciones</legend>
          <div className="license-feature-list">
            {commercialLicenseFeatureOptions.map((item) => <CheckField key={item.value} label={item.label}
              checked={selectedFeatures.has(item.value)} onChange={(checked) => toggleFeature(item.value, checked)}
              disabled={busy || (item.value === 'ACCOUNTANT_PORTAL' && !selectedModules.has('ACCOUNTING'))} />)}
          </div>
        </fieldset>}
        <footer className="panel-actions">
          <button className="secondary" disabled={busy || !form.companyId || licenseLoadedForSelectedCompany} onClick={onLoad} type="button">
            {licenseLoadedForSelectedCompany ? 'Licencia cargada' : 'Cargar licencia'}
          </button>
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
          <p><b>Funcionalidades:</b> {license?.enabledFeatures?.length ?? 0}</p>
          <p><b>Usuarios activos:</b> {quota(usage?.activeUsers, usage?.maxUsers)}</p>
          <p><b>Documentos del mes:</b> {quota(usage?.monthlyDocuments, usage?.maxMonthlyDocuments)}</p>
        </div>
      </section>
      <DataTable
        title="Uso comercial"
        description="Consumo actual de la licencia seleccionada."
        columns={['Indicador', 'Uso', 'Limite']}
        rows={[
          ['Usuarios activos', usage?.activeUsers ?? 'Sin consultar', usage?.maxUsers ?? 'Ilimitado'],
          ['Documentos emitidos del mes', usage?.monthlyDocuments ?? 'Sin consultar', usage?.maxMonthlyDocuments ?? 'Ilimitado'],
        ]}
      />
    </div>
  );
}

function quota(value, max) {
  if (value === undefined || value === null) {
    return 'Sin consultar';
  }
  return `${value} / ${max ?? 'Ilimitado'}`;
}
