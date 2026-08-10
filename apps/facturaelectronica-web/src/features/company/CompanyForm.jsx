import { Field, FormPanel, SelectField } from '../../components/forms.jsx';
import { companyLabel } from '../../utils/company.js';
import { calculateNitVerificationDigit, isNit, onlyDigits } from '../../utils/nit.js';

export function CompanyForm({ form, setForm, companies, activeCompanyId, activeCompany, isRoot, onCompanyChange, onSubmit, onOpenAdminModal, busy, documentTypeOptions = [] }) {
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

  return <div className="stack">
    <FormPanel title="Empresa contratante" submitLabel="Crear empresa" onSubmit={onSubmit} busy={busy}>
      <div className="form-grid">
        <Field label="Razon social" value={form.legalName} onChange={(value) => setForm({ ...form, legalName: value })} />
        <Field label="Nombre comercial" value={form.tradeName} onChange={(value) => setForm({ ...form, tradeName: value })} />
        <SelectField label="Tipo de identificacion" value={form.identificationTypeCode} onChange={updateIdentificationType} options={documentTypeOptions} />
        <Field label="Numero de identificacion" value={form.identificationNumber} onChange={updateIdentificationNumber} />
        <Field label="Digito de verificacion" value={verificationDigit} onChange={() => {}} readOnly />
        <Field label="Correo administrativo" value={form.email} onChange={(value) => setForm({ ...form, email: value })} type="email" />
      </div>
    </FormPanel>
    {isRoot && (
      <section className="tool-panel">
        <header className="panel-header">
          <div>
            <h1>Administrador inicial</h1>
            <p>Selecciona una empresa y crea el usuario administrador con rol OWNER empresarial.</p>
          </div>
          <button className="primary" disabled={busy || !activeCompanyId} onClick={onOpenAdminModal} type="button">Crear administrador</button>
        </header>
        <div className="form-grid compact">
          <label>
            Empresa activa
            <select value={activeCompanyId} onChange={(event) => onCompanyChange(event.target.value)} disabled={busy || companies.length === 0}>
              <option value="">Seleccione una empresa</option>
              {companies.map((company) => <option key={company.id} value={company.id}>{companyLabel(company)}</option>)}
            </select>
          </label>
          <Field label="Empresa seleccionada" value={companyLabel(activeCompany)} onChange={() => {}} readOnly />
        </div>
      </section>
    )}
  </div>;
}
