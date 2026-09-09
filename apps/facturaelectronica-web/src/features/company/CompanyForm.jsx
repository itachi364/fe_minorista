import { DataTable } from '../../components/DataTable.jsx';
import { Field, FormPanel, SelectField } from '../../components/forms.jsx';
import { companyLabel } from '../../utils/company.js';
import { calculateNitVerificationDigit, isNit, onlyDigits } from '../../utils/nit.js';
import { CompanyTaxProfileFields } from './CompanyTaxProfilePanel.jsx';

export function CompanyForm({
  form,
  setForm,
  companies,
  activeCompanyId,
  activeCompany,
  editingCompanyId,
  isRoot,
  onSubmit,
  onEditCompany,
  onToggleCompanyActive,
  onOpenAdminModal,
  onOpenBrandingModal,
  onNew,
  busy,
  documentTypeOptions = [],
  taxProfileForm,
  setTaxProfileForm,
  taxRegimeOptions = [],
  responsibilityOptions = [],
  ciiuOptions = [],
  locations = [],
  canManageCompanySettings = false,
  canManageFiscalSettings = false,
}) {
  const nitDocument = isNit(form.identificationTypeCode);
  const verificationDigit = nitDocument ? calculateNitVerificationDigit(form.identificationNumber) : '';

  function updateIdentificationType(value) {
    const identificationTypeCode = Number(value);
    const nextNumber = isNit(identificationTypeCode) ? onlyDigits(form.identificationNumber) : form.identificationNumber;
    setForm({ ...form, identificationTypeCode, identificationNumber: nextNumber, verificationDigit: isNit(identificationTypeCode) ? calculateNitVerificationDigit(nextNumber) : '' });
  }

  function updateIdentificationNumber(value) {
    const identificationNumber = nitDocument ? onlyDigits(value) : value;
    setForm({ ...form, identificationNumber, verificationDigit: nitDocument ? calculateNitVerificationDigit(identificationNumber) : '' });
  }

  const submitLabel = isRoot && editingCompanyId ? 'Actualizar empresa' : isRoot ? 'Crear empresa' : 'Actualizar empresa';
  const canUpdateActiveCompany = Boolean(activeCompanyId);

  return <div className="stack">
    <FormPanel title="Empresa contratante" submitLabel={submitLabel} onSubmit={onSubmit} busy={busy || (!isRoot && (!canUpdateActiveCompany || (!canManageCompanySettings && !canManageFiscalSettings)))}>
      {isRoot && (
        <div className="button-row company-actions">
          <button className="secondary" disabled={busy} onClick={onNew} type="button">Nueva empresa</button>
          {editingCompanyId && <span className="hint">Editando una empresa seleccionada desde la tabla.</span>}
        </div>
      )}
      <div className="form-grid">
        <Field label="Razon social" value={form.legalName} onChange={(value) => setForm({ ...form, legalName: value })} disabled={!isRoot && !canManageCompanySettings} />
        <Field label="Nombre comercial" value={form.tradeName} onChange={(value) => setForm({ ...form, tradeName: value })} disabled={!isRoot && !canManageCompanySettings} />
        <SelectField label="Tipo de identificacion" value={form.identificationTypeCode} onChange={updateIdentificationType} options={documentTypeOptions} disabled={!isRoot && !canManageCompanySettings} />
        <Field label="Numero de identificacion" value={form.identificationNumber} onChange={updateIdentificationNumber} disabled={!isRoot && !canManageCompanySettings} />
        <Field label="Digito de verificacion" value={verificationDigit} onChange={() => {}} readOnly />
        <Field label="Correo administrativo" value={form.email} onChange={(value) => setForm({ ...form, email: value })} type="email" disabled={!isRoot && !canManageCompanySettings} />
      </div>
      <section className="company-tax-section">
        <header className="subsection-header">
          <div>
            <h2>Perfil fiscal y retenciones</h2>
            <p className="hint">La clasificacion registrada debe coincidir con el RUT y la orientacion contable de la empresa.</p>
          </div>
        </header>
        <CompanyTaxProfileFields form={taxProfileForm} setForm={setTaxProfileForm}
          taxRegimeOptions={taxRegimeOptions} responsibilityOptions={responsibilityOptions}
          ciiuOptions={ciiuOptions} locations={locations} disabled={!isRoot && !canManageFiscalSettings && !canManageCompanySettings} />
      </section>
    </FormPanel>
    {isRoot && (
      <DataTable
        title="Empresas registradas"
        description="Administra empresas contratantes desde acciones explicitas por fila. La empresa activa solo define contexto operativo."
        columns={['Empresa', 'Identificacion', 'Correo', 'Estado', 'Acciones']}
        emptyMessage="Aun no hay empresas registradas."
        rows={companies.map((company) => {
          const companyId = company.id || company.companyId;
          const suspended = company.status === 'SUSPENDED';
          return [
            companyLabel(company),
            `${company.identificationTypeCode || ''} ${company.identificationNumber || ''}`.trim() || 'Sin identificacion',
            company.email || 'Sin correo',
            suspended ? 'Inactiva' : 'Activa',
            {
              searchText: `${companyLabel(company)} ${company.email || ''} ${company.status || ''}`,
              content: (
                <div className="table-actions">
                  <button className="secondary" disabled={busy} onClick={() => onEditCompany(company)} type="button">Actualizar</button>
                  <button className="secondary" disabled={busy} onClick={() => onToggleCompanyActive(company)} type="button">{suspended ? 'Activar' : 'Inactivar'}</button>
                  <button className="secondary" disabled={busy} onClick={() => onOpenAdminModal(company)} type="button">Crear administrador</button>
                  <button className="secondary" disabled={busy} onClick={() => onOpenBrandingModal(company)} type="button">Crear marca empresarial</button>
                </div>
              ),
            },
          ];
        })}
        rowKey={(row, index) => companies[index]?.id || companies[index]?.companyId || row[0]}
      />
    )}
  </div>;
}
