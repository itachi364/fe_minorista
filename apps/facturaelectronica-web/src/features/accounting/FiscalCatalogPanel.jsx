import { useState } from 'react';
import { DataTable } from '../../components/DataTable.jsx';
import { MunicipalityFields } from '../../components/MunicipalityFields.jsx';
import { CheckField, Field, FormPanel, SearchableSelectField, SelectField } from '../../components/forms.jsx';

const option = (value, label = value) => ({ value, label });
const operationOptions = [option('PURCHASE', 'Compra'), option('EXPENSE', 'Gasto'), option('PAYMENT', 'Pago'), option('PAYROLL', 'Nomina'), option('RECEIPT', 'Ingreso / recaudo')];
const typeOptions = ['RETEFUENTE', 'RETEIVA', 'RETEICA', 'AUTORETENCION'].map((value) => option(value));
const decisionOptions = [option('APPLIED', 'Aplicar'), option('EXEMPT', 'Exento'), option('NOT_APPLIED', 'Excluir'), option('BLOCKED', 'Bloquear')];
const baseOptions = [option('TAXABLE_BASE', 'Base gravable'), option('VAT_AMOUNT', 'Valor del IVA'), option('COMPANY_INCOME', 'Ingreso propio')];

export function FiscalCatalogPanel({ parameters, rules, isRoot, activeCompanyId, locations = [], ciiuOptions = [],
  taxRegimeOptions = [], responsibilityOptions = [], fiscalConceptOptions = [], thirdParties = [], onLoad, onSave,
  onDeactivate, onOpenEvidence = () => {}, busy }) {
  const [form, setForm] = useState(emptyForm);
  const change = (name, value) => setForm((current) => ({ ...current, [name]: value }));
  const thirdPartyOptions = thirdParties.filter((item) => item.active !== false).map((item) => ({
    value: item.id,
    label: [item.businessName || item.fullName || item.tradeName, item.identificationNumber]
      .filter(Boolean).join(' - '),
  }));

  function changeDecision(value) {
    setForm((current) => value === 'EXEMPT'
      ? { ...current, decision: value }
      : { ...current, decision: value, targetThirdPartyId: '', evidenceFile: null });
  }

  function changeTargetThirdParty(value) {
    setForm((current) => ({ ...current, targetThirdPartyId: value, evidenceFile: value ? current.evidenceFile : null }));
  }

  function changeGlobalRule(value) {
    setForm((current) => value
      ? { ...current, globalRule: true, targetThirdPartyId: '', evidenceFile: null }
      : { ...current, globalRule: false });
  }

  async function submit() {
    const result = await onSave({
      ...form,
      rate: Number(form.rate),
      thresholdValue: Number(form.thresholdValue),
      priority: Number(form.priority),
      specificity: Number(form.specificity),
      validTo: form.validTo || null,
      conceptCode: form.conceptCode || 'ANY',
      municipalityCode: form.municipalityCode || null,
      ciiuCode: form.ciiuCode || null,
      requiredThirdPartyTaxRegime: form.requiredThirdPartyTaxRegime || null,
      requiredThirdPartyResponsibility: form.requiredThirdPartyResponsibility || null,
      targetThirdPartyId: form.targetThirdPartyId || null,
      evidenceReference: null,
    }, isRoot && form.globalRule, form.evidenceFile);
    if (result) setForm(emptyForm());
  }

  return <>
    <DataTable title="Parametros fiscales" description="Valores publicados usados segun la fecha de cada operacion." columns={['Parametro', 'Valor', 'Version', 'Vigencia', 'Fuente']} rows={parameters.map((item) => [
      item.code,
      Number(item.value).toLocaleString('es-CO'),
      item.version,
      `${item.validFrom} / ${item.validTo || 'abierta'}`,
      { searchText: item.legalReference, content: <a href={item.sourceUrl} target="_blank" rel="noreferrer">{item.legalReference}</a> },
    ])} rowKey={(row) => row[2]} />

    <FormPanel title="Nueva version de regla fiscal" submitLabel="Publicar regla" onSubmit={submit} busy={busy || (!activeCompanyId && !form.globalRule)}>
      <div className="form-grid three">
        <Field label="Version" value={form.ruleSetVersion} onChange={(value) => change('ruleSetVersion', value)} />
        <SelectField label="Operacion" value={form.operationType} onChange={(value) => change('operationType', value)} options={operationOptions} />
        <SelectField label="Concepto" value={form.conceptCode} onChange={(value) => change('conceptCode', value)} options={fiscalConceptOptions} placeholder="Cualquier concepto" />
        <SelectField label="Tipo de retencion" value={form.withholdingType} onChange={(value) => change('withholdingType', value)} options={typeOptions} />
        <SelectField label="Decision" value={form.decision} onChange={changeDecision} options={decisionOptions} />
        <Field label="Tarifa decimal" value={form.rate} onChange={(value) => change('rate', value)} type="number" min="0" step="0.000001" />
        <SelectField label="Unidad del umbral" value={form.thresholdUnit} onChange={(value) => change('thresholdUnit', value)} options={[option('UVT'), option('COP')]} />
        <Field label="Valor del umbral" value={form.thresholdValue} onChange={(value) => change('thresholdValue', value)} type="number" min="0" step="0.000001" />
        <SelectField label="Comparacion" value={form.thresholdOperator} onChange={(value) => change('thresholdOperator', value)} options={[option('GTE', 'Mayor o igual'), option('GT', 'Mayor que')]} />
        <SelectField label="Base de calculo" value={form.calculationBase} onChange={(value) => change('calculationBase', value)} options={baseOptions} />
        <SelectField label="Tratamiento" value={form.thresholdTreatment} onChange={(value) => change('thresholdTreatment', value)} options={[option('FULL_AMOUNT', 'Valor total'), option('EXCESS', 'Solo excedente')]} />
        <MunicipalityFields municipalityCode={form.municipalityCode} onChange={(value) => change('municipalityCode', value)} locations={locations} optional municipalityLabel="Municipio DIVIPOLA" />
        <SearchableSelectField label="Codigo CIIU" value={form.ciiuCode} onChange={(value) => change('ciiuCode', value)} options={ciiuOptions} placeholder="Cualquier CIIU" searchPlaceholder="Buscar codigo o actividad" />
        <SelectField label="Regimen requerido" value={form.requiredThirdPartyTaxRegime} onChange={(value) => change('requiredThirdPartyTaxRegime', value)} options={taxRegimeOptions} placeholder="Cualquier regimen" />
        <SelectField label="Responsabilidad requerida" value={form.requiredThirdPartyResponsibility} onChange={(value) => change('requiredThirdPartyResponsibility', value)} options={responsibilityOptions} placeholder="Cualquier responsabilidad" />
        <SearchableSelectField label="Tercero exento" value={form.targetThirdPartyId} onChange={changeTargetThirdParty} options={thirdPartyOptions} disabled={form.globalRule || form.decision !== 'EXEMPT' || !activeCompanyId} placeholder="Sin tercero especifico" searchPlaceholder="Buscar por nombre o documento" />
        {form.decision === 'EXEMPT' && form.targetThirdPartyId && !form.globalRule && <label>
          Soporte de exencion (PDF)
          <input accept="application/pdf,.pdf" key={form.evidenceFile?.name || 'fiscal-evidence-empty'} onChange={(event) => change('evidenceFile', event.target.files?.[0] || null)} required type="file" />
          {form.evidenceFile && <span className="field-note">{form.evidenceFile.name}</span>}
        </label>}
        <Field label="Vigente desde" value={form.validFrom} onChange={(value) => change('validFrom', value)} type="date" />
        <Field label="Vigente hasta" value={form.validTo} onChange={(value) => change('validTo', value)} type="date" />
        <Field label="Prioridad" value={form.priority} onChange={(value) => change('priority', value)} type="number" />
        <Field label="Especificidad" value={form.specificity} onChange={(value) => change('specificity', value)} type="number" min="0" />
        <Field label="Referencia normativa" value={form.legalReference} onChange={(value) => change('legalReference', value)} />
        <Field label="URL oficial HTTPS" value={form.sourceUrl} onChange={(value) => change('sourceUrl', value)} type="url" />
      </div>
      <div className="check-grid">
        <CheckField label="Empresa es agente retenedor" checked={form.requiresCompanyWithholdingAgent} onChange={(value) => change('requiresCompanyWithholdingAgent', value)} />
        <CheckField label="Empresa es responsable de IVA" checked={form.requiresCompanyVatResponsible} onChange={(value) => change('requiresCompanyVatResponsible', value)} />
        <CheckField label="Empresa es agente de ReteIVA" checked={form.requiresCompanyVatWithholdingAgent} onChange={(value) => change('requiresCompanyVatWithholdingAgent', value)} />
        <CheckField label="Empresa es agente de ReteICA" checked={form.requiresCompanyIcaWithholdingAgent} onChange={(value) => change('requiresCompanyIcaWithholdingAgent', value)} />
        {isRoot && <CheckField label="Regla nacional global" checked={form.globalRule} onChange={changeGlobalRule} />}
      </div>
    </FormPanel>

    <DataTable title="Reglas fiscales" description="Las versiones publicadas se conservan; una correccion crea otra version o inactiva la anterior." columns={['Tipo', 'Concepto', 'Tarifa', 'Umbral', 'Alcance', 'Vigencia', 'Decision', 'Soporte', 'Acciones']} rows={rules.map((rule) => [
      rule.withholdingType,
      rule.conceptCode || 'ANY',
      `${(Number(rule.rate) * 100).toLocaleString('es-CO')}%`,
      `${rule.thresholdValue} ${rule.thresholdUnit}`,
      rule.companyId ? (rule.municipalityCode ? `Municipio ${rule.municipalityCode}` : 'Empresa') : 'Nacional',
      `${rule.validFrom} / ${rule.validTo || 'abierta'}`,
      rule.active ? rule.decision : 'INACTIVA',
      rule.evidenceReference
        ? { searchText: 'PDF', content: <button className="secondary" disabled={busy} onClick={() => onOpenEvidence(rule.evidenceReference)} type="button">Ver PDF</button> }
        : '',
      { searchText: rule.active ? 'activa' : 'inactiva', content: rule.active && (rule.companyId || isRoot)
        ? <button className="secondary danger-soft" disabled={busy} onClick={() => onDeactivate(rule)} type="button">Inactivar</button>
        : '' },
    ])} rowKey={(row, index) => `${row[0]}-${row[1]}-${row[5]}-${index}`} pageSize={15} />
    <button className="secondary" disabled={busy} onClick={onLoad} type="button">Actualizar catalogo</button>
  </>;
}

function emptyForm() {
  return {
    ruleSetVersion: 'EMPRESA-2026-01', operationType: 'PURCHASE', conceptCode: 'ANY', withholdingType: 'RETEICA',
    rate: '0', thresholdUnit: 'COP', thresholdValue: '0', thresholdOperator: 'GTE', calculationBase: 'TAXABLE_BASE',
    thresholdTreatment: 'FULL_AMOUNT', decision: 'APPLIED', requiresCompanyWithholdingAgent: false,
    requiresCompanyVatResponsible: false, requiresCompanyVatWithholdingAgent: false,
    requiresCompanyIcaWithholdingAgent: true, requiredThirdPartyTaxRegime: '', requiredThirdPartyResponsibility: '',
    targetThirdPartyId: '', evidenceFile: null,
    municipalityCode: '', ciiuCode: '', validFrom: '2026-01-01', validTo: '', priority: '100', specificity: '20',
    legalReference: '', sourceUrl: '', published: true, globalRule: false,
  };
}
