import { Field, FormPanel, SelectField, StatusBadge } from '../../components/forms.jsx';

const noteTypes = [
  {
    key: 'credit',
    title: 'Nota credito',
    description: 'Disminuye o corrige valores de una factura electronica emitida.',
    submitLabel: 'Crear nota credito',
  },
  {
    key: 'debit',
    title: 'Nota debito',
    description: 'Aumenta o corrige valores de una factura electronica emitida.',
    submitLabel: 'Crear nota debito',
  },
  {
    key: 'posAdjustment',
    title: 'Nota de ajuste POS',
    description: 'Corrige o anula un documento equivalente electronico POS.',
    submitLabel: 'Crear ajuste POS',
  },
];

const adjustmentOptions = [
  { value: 'CORRECTION', label: 'Correccion' },
  { value: 'CANCELLATION', label: 'Anulacion' },
];

export function FiscalNotesPanel({ forms, setForms, results, onSubmit, busy }) {
  return <section className="stack">
    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>Documentos fiscales</h1>
          <p className="hint">Gestiona notas fiscales independientes de ventas. Cada documento usa su propia resolucion activa y queda auditado.</p>
        </div>
        <StatusBadge label="Permiso" value="Documentos fiscales" tone="ok" />
      </header>
    </section>
    <div className="split">
      {noteTypes.map((noteType) => (
        <FiscalNoteForm
          key={noteType.key}
          noteType={noteType}
          form={forms[noteType.key]}
          setForm={(nextForm) => setForms((current) => ({ ...current, [noteType.key]: nextForm }))}
          result={results[noteType.key]}
          onSubmit={() => onSubmit(noteType.key)}
          busy={busy}
        />
      ))}
    </div>
  </section>;
}

function FiscalNoteForm({ noteType, form, setForm, result, onSubmit, busy }) {
  return <FormPanel title={noteType.title} submitLabel={noteType.submitLabel} onSubmit={onSubmit} busy={busy}>
    <p className="hint">{noteType.description}</p>
    <div className="form-grid compact">
      <Field label="Documento original" value={form.originalDocumentId} onChange={(value) => setForm({ ...form, originalDocumentId: value })} placeholder="UUID del documento fiscal emitido" />
      {noteType.key === 'posAdjustment' && (
        <SelectField label="Tipo de ajuste" value={form.adjustmentKind} onChange={(value) => setForm({ ...form, adjustmentKind: value })} options={adjustmentOptions} />
      )}
      <Field label="Motivo" value={form.reason} onChange={(value) => setForm({ ...form, reason: value })} placeholder="Describe la causa fiscal del documento" />
      <Field label="Subtotal" value={form.subtotal} onChange={(value) => setForm({ ...form, subtotal: value })} type="number" />
      <Field label="Impuesto" value={form.taxTotal} onChange={(value) => setForm({ ...form, taxTotal: value })} type="number" />
      <Field label="Total" value={form.total} onChange={(value) => setForm({ ...form, total: value })} type="number" />
    </div>
    {result && (
      <div className="badge-row">
        <StatusBadge label="Estado" value={result.status || 'Creada'} tone="ok" />
        <StatusBadge label="Consecutivo" value={`${result.prefix || 'Sin prefijo'} ${result.documentNumber || ''}`.trim()} />
      </div>
    )}
  </FormPanel>;
}
