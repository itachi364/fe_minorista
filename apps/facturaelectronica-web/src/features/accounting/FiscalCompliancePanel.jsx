import { useState } from 'react';
import { DataTable } from '../../components/DataTable.jsx';
import { Field, FormPanel, SearchableSelectField, SelectField, StatusBadge } from '../../components/forms.jsx';

const option = (value, label = value) => ({ value, label });
const withholdingTypes = ['RETEFUENTE', 'RETEIVA', 'RETEICA', 'AUTORETENCION'].map((value) => option(value));

export function FiscalCompliancePanel({ mappings = [], presentationMappings = [], form350Auxiliary = [], accounts = [], suppliers = [], periodSummary = null,
  reconciliation = null, certificates = [], onSaveMapping, onLoadPeriod, onClosePeriod,
  onSavePresentationMapping, onLoadCertificates, onGenerateCertificate, onDownloadCertificate, busy = false }) {
  const now = new Date();
  const [mapping, setMapping] = useState(() => ({
    withholdingType: 'RETEFUENTE', payableAccountCode: '', receivableAccountCode: '',
    validFrom: `${now.getFullYear()}-01-01`, validTo: '',
  }));
  const [period, setPeriod] = useState(() => ({ year: String(now.getFullYear()), month: String(now.getMonth() + 1) }));
  const [presentation, setPresentation] = useState(() => ({ accountId: '', financialReportingGroup: 'GRUPO_3',
    statementSection: '', presentationConcept: '', validFrom: `${now.getFullYear()}-01-01`, validTo: '', evidenceReference: '' }));
  const [certificate, setCertificate] = useState(() => ({ thirdPartyId: '', year: String(now.getFullYear()),
    certificateCity: '', issuerIdentification: '', issuerName: '', issuerAddress: '',
    beneficiaryIdentification: '', beneficiaryName: '' }));
  const activeAccounts = accounts.filter((item) => item.active !== false).map((item) => option(item.code, `${item.code} - ${item.name}`));
  const presentationAccountOptions = accounts.filter((item) => item.active !== false).map((item) => option(item.id, `${item.code} - ${item.name}`));
  const supplierOptions = suppliers.filter((item) => item.active !== false).map((item) => option(item.id,
    [item.businessName || item.fullName || item.tradeName || item.legalName, item.identificationNumber]
      .filter(Boolean).join(' - ')));
  const changeMapping = (name, value) => setMapping((current) => ({ ...current, [name]: value }));
  const changePeriod = (name, value) => setPeriod((current) => ({ ...current, [name]: value }));
  const changeCertificate = (name, value) => setCertificate((current) => ({ ...current, [name]: value }));
  const changePresentation = (name, value) => setPresentation((current) => ({ ...current, [name]: value }));
  const changeSupplier = (value) => {
    const supplier = suppliers.find((item) => item.id === value);
    setCertificate((current) => ({ ...current, thirdPartyId: value,
      beneficiaryIdentification: supplier?.identificationNumber || '',
      beneficiaryName: supplier?.businessName || supplier?.fullName || supplier?.tradeName || '' }));
  };
  const visiblePeriodSummary = periodSummary && Number(period.year) === periodSummary.year
    && Number(period.month) === periodSummary.month ? periodSummary : null;
  const visibleReconciliation = reconciliation && Number(period.year) === reconciliation.year
    && Number(period.month) === reconciliation.month ? reconciliation : null;

  return <section className="fiscal-compliance-section">
    <FormPanel title="Cuentas para retenciones" submitLabel="Guardar mapeo" busy={busy || !mapping.payableAccountCode || !mapping.validFrom}
      onSubmit={() => onSaveMapping({ ...mapping, validTo: mapping.validTo || null })}>
      <div className="form-grid three">
        <SelectField label="Tipo de retencion" value={mapping.withholdingType} onChange={(value) => changeMapping('withholdingType', value)} options={withholdingTypes} />
        <SearchableSelectField label="Cuenta por pagar" value={mapping.payableAccountCode} onChange={(value) => changeMapping('payableAccountCode', value)} options={activeAccounts} placeholder="Selecciona una cuenta" searchPlaceholder="Buscar por codigo o nombre" />
        <SearchableSelectField label="Cuenta por cobrar" value={mapping.receivableAccountCode} onChange={(value) => changeMapping('receivableAccountCode', value)} options={activeAccounts} placeholder="Sin cuenta por cobrar" searchPlaceholder="Buscar por codigo o nombre" />
        <Field label="Vigente desde" value={mapping.validFrom} onChange={(value) => changeMapping('validFrom', value)} type="date" />
        <Field label="Vigente hasta" value={mapping.validTo} onChange={(value) => changeMapping('validTo', value)} type="date" />
      </div>
    </FormPanel>

    <DataTable title="Mapeos contables fiscales" description="Cuentas vigentes usadas para contabilizar y conciliar las retenciones."
      columns={['Tipo', 'Cuenta por pagar', 'Cuenta por cobrar', 'Vigencia', 'Estado']}
      rows={mappings.map((item) => [item.withholdingType, item.payableAccountCode,
        item.receivableAccountCode || 'No aplica', `${item.validFrom} / ${item.validTo || 'abierta'}`,
        item.active ? 'Activo' : 'Inactivo'])} rowKey={(row) => `${row[0]}-${row[3]}`} pageSize={8} />

    <FormPanel title="Presentacion de estados financieros" submitLabel="Guardar presentacion"
      busy={busy || !presentation.accountId || !presentation.statementSection || !presentation.presentationConcept}
      onSubmit={() => onSavePresentationMapping({ ...presentation, validTo: presentation.validTo || null,
        evidenceReference: presentation.evidenceReference || null })}>
      <div className="form-grid three">
        <SearchableSelectField label="Cuenta contable" value={presentation.accountId} onChange={(value) => changePresentation('accountId', value)} options={presentationAccountOptions} placeholder="Selecciona una cuenta" searchPlaceholder="Buscar por codigo o nombre" />
        <SelectField label="Grupo de informacion financiera" value={presentation.financialReportingGroup} onChange={(value) => changePresentation('financialReportingGroup', value)} options={[option('GRUPO_1', 'Grupo 1'), option('GRUPO_2', 'Grupo 2'), option('GRUPO_3', 'Grupo 3')]} />
        <Field label="Seccion del estado financiero" value={presentation.statementSection} onChange={(value) => changePresentation('statementSection', value)} />
        <Field label="Concepto de presentacion" value={presentation.presentationConcept} onChange={(value) => changePresentation('presentationConcept', value)} />
        <Field label="Vigente desde" value={presentation.validFrom} onChange={(value) => changePresentation('validFrom', value)} type="date" />
        <Field label="Vigente hasta" value={presentation.validTo} onChange={(value) => changePresentation('validTo', value)} type="date" />
      </div>
    </FormPanel>
    <DataTable title="Mapeos de presentacion" columns={['Cuenta', 'Grupo', 'Seccion', 'Concepto', 'Vigencia']}
      rows={presentationMappings.map((item) => [item.accountCode, item.financialReportingGroup, item.statementSection,
        item.presentationConcept, `${item.validFrom} / ${item.validTo || 'abierta'}`])}
      rowKey={(row) => `${row[0]}-${row[1]}-${row[4]}`} pageSize={8} />

    <section className="tool-panel">
      <header className="panel-header">
        <div><h1>Cierre y conciliacion fiscal</h1><p>Consolida el periodo y compara retenciones calculadas con sus movimientos contables.</p></div>
        <div className="row-actions">
          <button className="secondary" disabled={busy} onClick={() => onLoadPeriod(Number(period.year), Number(period.month))} type="button">Consultar</button>
          <button className="primary" disabled={busy || visiblePeriodSummary?.closed} onClick={() => onClosePeriod(Number(period.year), Number(period.month))} type="button">Cerrar periodo</button>
        </div>
      </header>
      <div className="form-grid three">
        <Field label="Ano" value={period.year} onChange={(value) => changePeriod('year', value)} type="number" min="2000" max="2100" />
        <Field label="Mes" value={period.month} onChange={(value) => changePeriod('month', value)} type="number" min="1" max="12" />
        {visiblePeriodSummary && <div className="status-field"><span>Estado del periodo</span><StatusBadge label="Estado" value={visiblePeriodSummary.closed ? 'CERRADO' : 'ABIERTO'} tone={visiblePeriodSummary.closed ? 'warn' : 'ok'} /></div>}
      </div>
      {visiblePeriodSummary && <DataTable title="Resumen de retenciones" columns={['Tipo', 'Base', 'Retencion']}
        rows={summaryRows(visiblePeriodSummary)} rowKey={(row) => row[0]} pageSize={6} />}
      {visibleReconciliation && <div className="summary-strip">
        <span><b>Fiscal</b> {money(visibleReconciliation.fiscalTotal)}</span>
        <span><b>Contable</b> {money(visibleReconciliation.accountingTotal)}</span>
        <span><b>Diferencia</b> {money(visibleReconciliation.difference)}</span>
        <StatusBadge label="Conciliacion" value={visibleReconciliation.status} tone={Number(visibleReconciliation.difference) === 0 ? 'ok' : 'warn'} />
      </div>}
      <DataTable title="Auxiliar para Formulario 350" columns={['Seccion', 'Concepto', 'Tipo', 'Base', 'Retenido', 'Documentos']}
        rows={form350Auxiliary.map((item) => [item.form350Section || 'Pendiente de homologar', item.conceptCode,
          item.withholdingType, money(item.baseAmount), money(item.withheldAmount), item.documentCount])}
        rowKey={(row) => `${row[0]}-${row[1]}-${row[2]}`} pageSize={8} />
    </section>

    <section className="tool-panel">
      <header className="panel-header"><div><h1>Certificados de retencion</h1><p>Genera versiones inmutables por proveedor y ano gravable.</p></div></header>
      <div className="form-grid three">
        <SearchableSelectField label="Proveedor" value={certificate.thirdPartyId} onChange={changeSupplier} options={supplierOptions} placeholder="Selecciona un proveedor" searchPlaceholder="Buscar por nombre o documento" />
        <Field label="Ano gravable" value={certificate.year} onChange={(value) => changeCertificate('year', value)} type="number" min="2000" max="2100" />
        <Field label="Ciudad de expedicion" value={certificate.certificateCity} onChange={(value) => changeCertificate('certificateCity', value)} />
        <Field label="NIT del agente retenedor" value={certificate.issuerIdentification} onChange={(value) => changeCertificate('issuerIdentification', value)} />
        <Field label="Nombre del agente retenedor" value={certificate.issuerName} onChange={(value) => changeCertificate('issuerName', value)} />
        <Field label="Direccion del agente retenedor" value={certificate.issuerAddress} onChange={(value) => changeCertificate('issuerAddress', value)} />
        <Field label="Identificacion del beneficiario" value={certificate.beneficiaryIdentification} onChange={(value) => changeCertificate('beneficiaryIdentification', value)} />
        <Field label="Nombre del beneficiario" value={certificate.beneficiaryName} onChange={(value) => changeCertificate('beneficiaryName', value)} />
        <div className="field-actions">
          <button className="secondary" disabled={busy || !certificate.thirdPartyId} onClick={() => onLoadCertificates(certificate.thirdPartyId, Number(certificate.year))} type="button">Consultar</button>
          <button className="primary" disabled={busy || Object.values(certificate).some((value) => !value)} onClick={() => onGenerateCertificate({ ...certificate, year: Number(certificate.year) })} type="button">Generar</button>
        </div>
      </div>
      <DataTable title="Versiones generadas" columns={['Ano', 'Version', 'Base', 'Retenido', 'Generado', 'Acciones']}
        rows={certificates.map((item) => [item.year, item.version, money(item.taxableBaseTotal), money(item.withheldTotal),
          item.generatedAt, { searchText: 'descargar', content: <button className="secondary" disabled={busy} onClick={() => onDownloadCertificate(item.id)} type="button">Descargar CSV</button> }])}
        rowKey={(row) => `${row[0]}-${row[1]}`} pageSize={8} />
    </section>
  </section>;
}

function summaryRows(summary) {
  const bases = summary.taxableBaseByType || {};
  const withholdings = summary.withheldByType || {};
  return [...new Set([...Object.keys(bases), ...Object.keys(withholdings)])]
    .map((type) => [type, money(bases[type]), money(withholdings[type])]);
}

function money(value) {
  return Number(value || 0).toLocaleString('es-CO', { style: 'currency', currency: 'COP' });
}
