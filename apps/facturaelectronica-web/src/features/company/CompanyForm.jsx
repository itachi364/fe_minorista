import { DataTable } from '../../components/DataTable.jsx';
import { CheckField, Field, FormPanel, SelectField, StatusBadge } from '../../components/forms.jsx';
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
  obligationForm,
  setObligationForm,
  obligationResult,
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
      {isRoot && <section className="company-tax-section">
        <header className="subsection-header">
          <div>
            <h2>Obligacion de facturar</h2>
            <p className="hint">El resultado lo calcula el sistema a partir del RUT y las condiciones declaradas.</p>
          </div>
          {obligationResult?.status && <StatusBadge label="Estado" value={obligationResult.status}
            tone={obligationResult.status === 'NOT_OBLIGATED_VERIFIED' ? 'ok' : 'warn'} />}
        </header>
        <div className="form-grid">
          <SelectField label="Tipo de persona" value={obligationForm.personType}
            onChange={(value) => setObligationForm({ ...obligationForm, personType: value })}
            options={[{ value: 'NATURAL', label: 'Persona natural' }, { value: 'JURIDICAL', label: 'Persona juridica' }]} />
          <Field label="Fecha de generacion del RUT" type="date" value={obligationForm.rutGeneratedAt}
            onChange={(value) => setObligationForm({ ...obligationForm, rutGeneratedAt: value })} />
          <Field label="Numero de establecimientos" type="number" min="0" step="1"
            value={obligationForm.establishmentCount}
            onChange={(value) => setObligationForm({ ...obligationForm, establishmentCount: value })} />
          <label className="field">RUT en PDF
            <input type="file" accept="application/pdf,.pdf" onChange={(event) => setObligationForm({
              ...obligationForm, rutFile: event.target.files?.[0] || null,
            })} />
            {obligationForm.rutAssetId && !obligationForm.rutFile && <span className="hint">RUT cargado previamente.</span>}
          </label>
        </div>
        <div className="check-grid">
          <CheckField label="Usuario aduanero" checked={obligationForm.customsUser}
            onChange={(value) => setObligationForm({ ...obligationForm, customsUser: value })} />
          <CheckField label="Explota intangibles, franquicias o concesiones" checked={obligationForm.exploitsIntangibles}
            onChange={(value) => setObligationForm({ ...obligationForm, exploitsIntangibles: value })} />
          <CheckField label="Solo vende bienes excluidos o servicios no gravados" checked={obligationForm.onlyExcludedOrUntaxedOperations}
            onChange={(value) => setObligationForm({ ...obligationForm, onlyExcludedOrUntaxedOperations: value })} />
          <CheckField label="Facturador electronico voluntario" checked={obligationForm.voluntaryElectronicInvoicer}
            onChange={(value) => setObligationForm({ ...obligationForm, voluntaryElectronicInvoicer: value })} />
        </div>
        <div className="form-grid">
          <MoneyField label="Ingresos actividad ano anterior" name="previousYearGrossActivityIncome" form={obligationForm} setForm={setObligationForm} />
          <MoneyField label="Ingresos actividad ano actual" name="currentYearGrossActivityIncome" form={obligationForm} setForm={setObligationForm} />
          <MoneyField label="Operaciones financieras gravadas ano anterior" name="previousYearTaxedActivityFinancialOperations" form={obligationForm} setForm={setObligationForm} />
          <MoneyField label="Operaciones financieras gravadas ano actual" name="currentYearTaxedActivityFinancialOperations" form={obligationForm} setForm={setObligationForm} />
          <MoneyField label="Mayor contrato gravado ano anterior" name="largestPreviousYearTaxedContract" form={obligationForm} setForm={setObligationForm} />
          <MoneyField label="Mayor contrato gravado ano actual" name="largestCurrentYearTaxedContract" form={obligationForm} setForm={setObligationForm} />
          <MoneyField label="Acumulado mayor con un mismo cliente" name="largestSameCustomerAggregate" form={obligationForm} setForm={setObligationForm} />
          <SelectField label="Tipo principal de operacion" value={obligationForm.economicOperationTypes?.[0] || ''}
            onChange={(value) => setObligationForm({ ...obligationForm, economicOperationTypes: value ? [value] : [] })}
            options={[
              { value: 'TAXED_GOODS_SALE', label: 'Venta de bienes gravados' },
              { value: 'TAXED_SERVICE', label: 'Prestacion de servicios gravados' },
              { value: 'EXCLUDED_OR_UNTAXED', label: 'Bienes excluidos o servicios no gravados' },
              { value: 'RESTAURANT_OR_BAR', label: 'Restaurante o bar' },
            ]} />
          <SelectField label="Excepcion especial" value={obligationForm.specialExceptionType}
            onChange={(value) => setObligationForm({ ...obligationForm, specialExceptionType: value,
              specialExceptionScope: value ? obligationForm.specialExceptionScope : '' })}
            options={[
              { value: 'PUBLIC_URBAN_TRANSPORT', label: 'Transporte publico urbano' },
              { value: 'FINANCIAL_ENTITY_OPERATION', label: 'Operacion de entidad financiera' },
              { value: 'EMPLOYMENT_OR_PENSION_INCOME', label: 'Ingreso laboral o pensional' },
              { value: 'FOREIGN_DIGITAL_SERVICE', label: 'Servicio digital desde el exterior' },
            ]} />
          <SelectField label="Alcance de la excepcion" value={obligationForm.specialExceptionScope}
            onChange={(value) => setObligationForm({ ...obligationForm, specialExceptionScope: value })}
            disabled={!obligationForm.specialExceptionType}
            options={[
              { value: 'ONLY_DECLARED_OPERATION', label: 'Solo la operacion declarada' },
              { value: 'ALL_OPERATIONS_BY_SUBJECT', label: 'Todas las operaciones del sujeto' },
            ]} />
        </div>
        {obligationResult?.decisionReasons?.length > 0 && <p className="hint">
          Motivos: {obligationResult.decisionReasons.join(', ')}. Version: {obligationResult.normativeRuleSetVersion}.
        </p>}
      </section>}
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

function MoneyField({ label, name, form, setForm }) {
  return <Field label={label} type="number" min="0" step="0.01" value={form[name]}
    onChange={(value) => setForm({ ...form, [name]: value })} />;
}
