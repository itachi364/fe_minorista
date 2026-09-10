import { useState } from 'react';
import { DataTable } from '../../components/DataTable.jsx';
import { MunicipalityFields } from '../../components/MunicipalityFields.jsx';
import { Field, FormPanel, SearchableSelectField, SelectField, StatusBadge } from '../../components/forms.jsx';

const option = (value, label = value) => ({ value, label });

export function FiscalGovernancePanel({ sources = [], warnings = [], packages = [], locations = [],
  ciiuOptions = [], onCreateSource, onAddEvent, onCreatePackage, onValidateCsv, onImportCsv, onPublish, busy }) {
  const [sourceForm, setSourceForm] = useState(emptySourceForm);
  const [eventForm, setEventForm] = useState(emptyEventForm);
  const [packageForm, setPackageForm] = useState(emptyPackageForm);
  const [csvFile, setCsvFile] = useState(null);
  const [csvValidation, setCsvValidation] = useState(null);
  const sourceOptions = sources.map((item) => option(item.source.id, `${item.source.code} - ${item.source.title}`));

  async function createSource() {
    const result = await onCreateSource({
      ...sourceForm,
      issuedOn: sourceForm.issuedOn || null,
      reviewDueOn: sourceForm.reviewDueOn || null,
    });
    if (result) setSourceForm(emptySourceForm());
  }

  async function addEvent() {
    const result = await onAddEvent(eventForm.sourceId, {
      ...eventForm,
      sourceId: undefined,
      effectiveTo: eventForm.effectiveTo || null,
      notes: eventForm.notes || null,
    });
    if (result) setEventForm(emptyEventForm());
  }

  async function createPackage() {
    const result = await onCreatePackage({
      municipalityCode: packageForm.municipalityCode,
      packageCode: packageForm.packageCode,
      version: packageForm.version,
      validFrom: packageForm.validFrom,
      validTo: packageForm.validTo || null,
      legalReference: packageForm.legalReference,
      officialSourceUrl: packageForm.officialSourceUrl,
      rules: [{
        operationType: packageForm.operationType,
        conceptCode: packageForm.conceptCode || 'ANY',
        ciiuCode: packageForm.ciiuCode || null,
        rate: Number(packageForm.rate),
        thresholdUnit: packageForm.thresholdUnit,
        thresholdValue: Number(packageForm.thresholdValue),
        thresholdOperator: packageForm.thresholdOperator,
        calculationBase: 'TAXABLE_BASE',
      }],
    });
    if (result) setPackageForm(emptyPackageForm());
  }

  async function validateCsv() {
    if (!csvFile) return;
    setCsvValidation(await onValidateCsv(csvFile));
  }

  async function importCsv() {
    if (!csvFile || !csvValidation?.valid) return;
    const result = await onImportCsv(csvFile);
    if (result) {
      setCsvFile(null);
      setCsvValidation(null);
    }
  }

  return <section className="fiscal-governance">
    <div className="panel-header section-heading">
      <div>
        <h1>Gobierno del catalogo fiscal</h1>
        <p className="hint">Fuentes, vigencias y paquetes territoriales administrados exclusivamente por ROOT.</p>
      </div>
    </div>

    {warnings.length > 0 && <DataTable title="Alertas normativas" description="Fuentes suspendidas o pendientes de revision." columns={['Severidad', 'Fuente', 'Alerta', 'Fecha']} rows={warnings.map((item) => [
      item.severity,
      item.sourceCode,
      item.message,
      item.dueOn || 'Revision inmediata',
    ])} rowKey={(row, index) => `${row[1]}-${row[2]}-${index}`} pageSize={5} />}

    <DataTable title="Fuentes normativas" description="El estado se resuelve segun la fecha fiscal consultada." columns={['Codigo', 'Titulo', 'Autoridad', 'Estado', 'Revision', 'Eventos']} rows={sources.map((item) => [
      item.source.code,
      item.source.title,
      item.source.authority,
      item.status || 'SIN EVENTO',
      item.source.reviewDueOn || 'Sin fecha',
      item.events.length,
    ])} rowKey={(row) => row[0]} pageSize={8} />

    <div className="split">
      <FormPanel title="Nueva fuente normativa" submitLabel="Registrar fuente" onSubmit={createSource} busy={busy}>
        <div className="form-grid two">
          <Field label="Codigo" value={sourceForm.code} onChange={(value) => setSourceForm({ ...sourceForm, code: value })} />
          <Field label="Titulo" value={sourceForm.title} onChange={(value) => setSourceForm({ ...sourceForm, title: value })} />
          <Field label="Autoridad" value={sourceForm.authority} onChange={(value) => setSourceForm({ ...sourceForm, authority: value })} />
          <Field label="URL oficial HTTPS" value={sourceForm.officialUrl} onChange={(value) => setSourceForm({ ...sourceForm, officialUrl: value })} type="url" />
          <Field label="Fecha de expedicion" value={sourceForm.issuedOn} onChange={(value) => setSourceForm({ ...sourceForm, issuedOn: value })} type="date" />
          <Field label="Proxima revision" value={sourceForm.reviewDueOn} onChange={(value) => setSourceForm({ ...sourceForm, reviewDueOn: value })} type="date" />
        </div>
      </FormPanel>

      <FormPanel title="Nuevo evento juridico" submitLabel="Registrar evento" onSubmit={addEvent} busy={busy || !eventForm.sourceId}>
        <div className="form-grid two">
          <SearchableSelectField label="Fuente normativa" value={eventForm.sourceId} onChange={(value) => setEventForm({ ...eventForm, sourceId: value })} options={sourceOptions} searchPlaceholder="Buscar fuente" />
          <SelectField label="Tipo de evento" value={eventForm.eventType} onChange={(value) => setEventForm({ ...eventForm, eventType: value })} options={['PUBLISHED', 'EFFECTIVE', 'MODIFIED', 'SUSPENDED', 'REACTIVATED', 'REPEALED'].map((value) => option(value))} />
          <Field label="Vigente desde" value={eventForm.effectiveFrom} onChange={(value) => setEventForm({ ...eventForm, effectiveFrom: value })} type="date" />
          <Field label="Vigente hasta" value={eventForm.effectiveTo} onChange={(value) => setEventForm({ ...eventForm, effectiveTo: value })} type="date" />
          <Field label="Referencia" value={eventForm.reference} onChange={(value) => setEventForm({ ...eventForm, reference: value })} />
          <Field label="URL oficial HTTPS" value={eventForm.officialUrl} onChange={(value) => setEventForm({ ...eventForm, officialUrl: value })} type="url" />
          <Field label="Notas" value={eventForm.notes} onChange={(value) => setEventForm({ ...eventForm, notes: value })} />
        </div>
      </FormPanel>
    </div>

    <FormPanel title="Nuevo paquete municipal ReteICA" submitLabel="Crear borrador" onSubmit={createPackage} busy={busy || !packageForm.municipalityCode}>
      <div className="form-grid three">
        <MunicipalityFields municipalityCode={packageForm.municipalityCode} onChange={(value) => setPackageForm({ ...packageForm, municipalityCode: value })} locations={locations} optional municipalityLabel="Municipio DIVIPOLA" />
        <Field label="Codigo del paquete" value={packageForm.packageCode} onChange={(value) => setPackageForm({ ...packageForm, packageCode: value })} />
        <Field label="Version" value={packageForm.version} onChange={(value) => setPackageForm({ ...packageForm, version: value })} />
        <SelectField label="Operacion" value={packageForm.operationType} onChange={(value) => setPackageForm({ ...packageForm, operationType: value })} options={[option('PURCHASE', 'Compra'), option('EXPENSE', 'Gasto'), option('PAYMENT', 'Pago')]} />
        <Field label="Concepto" value={packageForm.conceptCode} onChange={(value) => setPackageForm({ ...packageForm, conceptCode: value })} />
        <SearchableSelectField label="Codigo CIIU" value={packageForm.ciiuCode} onChange={(value) => setPackageForm({ ...packageForm, ciiuCode: value })} options={ciiuOptions} placeholder="Cualquier CIIU" searchPlaceholder="Buscar codigo o actividad" />
        <Field label="Tarifa decimal" value={packageForm.rate} onChange={(value) => setPackageForm({ ...packageForm, rate: value })} type="number" min="0" max="1" step="0.000001" />
        <SelectField label="Unidad del umbral" value={packageForm.thresholdUnit} onChange={(value) => setPackageForm({ ...packageForm, thresholdUnit: value })} options={[option('COP'), option('UVT')]} />
        <Field label="Valor del umbral" value={packageForm.thresholdValue} onChange={(value) => setPackageForm({ ...packageForm, thresholdValue: value })} type="number" min="0" step="0.000001" />
        <SelectField label="Comparacion" value={packageForm.thresholdOperator} onChange={(value) => setPackageForm({ ...packageForm, thresholdOperator: value })} options={[option('GTE', 'Mayor o igual'), option('GT', 'Mayor que')]} />
        <Field label="Vigente desde" value={packageForm.validFrom} onChange={(value) => setPackageForm({ ...packageForm, validFrom: value })} type="date" />
        <Field label="Vigente hasta" value={packageForm.validTo} onChange={(value) => setPackageForm({ ...packageForm, validTo: value })} type="date" />
        <Field label="Referencia normativa" value={packageForm.legalReference} onChange={(value) => setPackageForm({ ...packageForm, legalReference: value })} />
        <Field label="URL oficial HTTPS" value={packageForm.officialSourceUrl} onChange={(value) => setPackageForm({ ...packageForm, officialSourceUrl: value })} type="url" />
      </div>
    </FormPanel>

    <form className="tool-panel" onSubmit={(event) => event.preventDefault()}>
      <header className="panel-header"><h1>Cargar paquetes ReteICA por CSV</h1></header>
      <div className="form-grid two">
        <label>Archivo CSV delimitado por comas
          <input accept="text/csv,.csv" key={csvFile?.name || 'reteica-csv-empty'} onChange={(event) => {
            setCsvFile(event.target.files?.[0] || null);
            setCsvValidation(null);
          }} type="file" />
        </label>
        <div className="row-actions fiscal-csv-actions">
          <button className="secondary" disabled={busy || !csvFile} onClick={validateCsv} type="button">Validar CSV</button>
          <button className="primary" disabled={busy || !csvValidation?.valid} onClick={importCsv} type="button">Importar borradores</button>
        </div>
      </div>
      {csvValidation && <div className="inline-status">
        <StatusBadge label="Validacion" value={csvValidation.valid ? 'VALIDO' : 'INVALIDO'} tone={csvValidation.valid ? 'ok' : 'warn'} />
        <span>{csvValidation.rowCount} filas, {csvValidation.packageCount} paquetes</span>
        {csvValidation.errors.map((error) => <span className="field-error-message" key={error}>{error}</span>)}
      </div>}
    </form>

    <DataTable title="Paquetes municipales ReteICA" description="DIVIPOLA identifica el municipio, pero no crea paquetes automaticamente." columns={['Municipio', 'Paquete', 'Version', 'Vigencia', 'Reglas', 'Estado', 'Acciones']} rows={packages.map((item) => [
      `${item.municipalityCode} - ${item.municipalityName}`,
      item.code,
      item.version,
      `${item.validFrom} / ${item.validTo || 'abierta'}`,
      item.ruleCount,
      item.status,
      { searchText: item.status, content: item.status === 'DRAFT' ? <button className="primary" disabled={busy} onClick={() => onPublish(item.id)} type="button">Publicar</button> : '' },
    ])} rowKey={(row, index) => `${row[0]}-${row[1]}-${row[2]}-${index}`} pageSize={10} />
  </section>;
}

function emptySourceForm() {
  return { code: '', title: '', authority: '', officialUrl: '', issuedOn: '', reviewDueOn: '' };
}

function emptyEventForm() {
  return { sourceId: '', eventType: 'EFFECTIVE', effectiveFrom: '', effectiveTo: '', reference: '', officialUrl: '', notes: '' };
}

function emptyPackageForm() {
  return { municipalityCode: '', packageCode: '', version: '', operationType: 'PURCHASE', conceptCode: 'ANY',
    ciiuCode: '', rate: '0', thresholdUnit: 'COP', thresholdValue: '0', thresholdOperator: 'GTE',
    validFrom: '2026-01-01', validTo: '', legalReference: '', officialSourceUrl: '' };
}
