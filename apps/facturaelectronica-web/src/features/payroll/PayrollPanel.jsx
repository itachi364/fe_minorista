import { DataTable } from '../../components/DataTable.jsx';
import { CheckField, Field, FormPanel, SelectField } from '../../components/forms.jsx';

export function PayrollPanel({
  settingsForm,
  setSettingsForm,
  workerForm,
  setWorkerForm,
  paymentForm,
  setPaymentForm,
  workers,
  payments = [],
  electronicDocuments = [],
  documentTypeOptions,
  workerClassificationOptions,
  paymentMethodOptions,
  onSaveSettings,
  onCreateWorker,
  onCreateDailyPayment,
  onIssueElectronicDocument,
  busy,
}) {
  const workerOptions = workers.map((worker) => ({
    value: worker.id,
    label: `${worker.fullName} - ${worker.identificationNumber}`,
  }));

  return (
    <section className="stack">
      <section className="tool-panel">
        <header className="panel-header">
          <div>
            <h1>Nomina</h1>
            <p className="hint">Trabajadores, pagos diarios verbales y soportes se cargan automaticamente al entrar al modulo.</p>
          </div>
        </header>
      </section>

      <div className="split">
        <FormPanel title="Configuracion de nomina" submitLabel="Guardar configuracion" onSubmit={onSaveSettings} busy={busy}>
          <div className="form-grid compact">
            <CheckField label="Usar nomina electronica" checked={settingsForm.electronicPayrollEnabled} onChange={(value) => setSettingsForm({ ...settingsForm, electronicPayrollEnabled: value })} />
            <Field label="Modo conector" value={settingsForm.providerMode} onChange={(value) => setSettingsForm({ ...settingsForm, providerMode: value })} readOnly />
          </div>
        </FormPanel>

        <FormPanel title="Trabajador" submitLabel="Crear trabajador" onSubmit={onCreateWorker} busy={busy}>
          <div className="form-grid compact">
            <SelectField label="Tipo de documento" value={workerForm.identificationTypeCode} onChange={(value) => setWorkerForm({ ...workerForm, identificationTypeCode: value })} options={documentTypeOptions} />
            <Field label="Numero de documento" value={workerForm.identificationNumber} onChange={(value) => setWorkerForm({ ...workerForm, identificationNumber: value })} />
            <Field label="Digito de verificacion" value={workerForm.verificationDigit} onChange={(value) => setWorkerForm({ ...workerForm, verificationDigit: value })} />
            <Field label="Nombre completo" value={workerForm.fullName} onChange={(value) => setWorkerForm({ ...workerForm, fullName: value })} />
            <SelectField label="Clasificacion laboral" value={workerForm.workerClassification} onChange={(value) => setWorkerForm({ ...workerForm, workerClassification: value })} options={workerClassificationOptions} />
            <CheckField label="Activo" checked={workerForm.active} onChange={(value) => setWorkerForm({ ...workerForm, active: value })} />
          </div>
        </FormPanel>
      </div>

      <FormPanel title="Pago diario verbal" submitLabel="Registrar pago" onSubmit={onCreateDailyPayment} busy={busy}>
        <div className="form-grid">
          <SelectField label="Trabajador" value={paymentForm.workerId} onChange={(value) => setPaymentForm({ ...paymentForm, workerId: value })} options={workerOptions} />
          <Field label="Fecha de trabajo" value={paymentForm.workDate} onChange={(value) => setPaymentForm({ ...paymentForm, workDate: value })} type="date" />
          <Field label="Actividad realizada" value={paymentForm.activityDescription} onChange={(value) => setPaymentForm({ ...paymentForm, activityDescription: value })} />
          <Field label="Valor acordado" value={paymentForm.agreedAmount} onChange={(value) => setPaymentForm({ ...paymentForm, agreedAmount: value })} type="number" />
          <Field label="Valor pagado" value={paymentForm.paidAmount} onChange={(value) => setPaymentForm({ ...paymentForm, paidAmount: value })} type="number" />
          <SelectField label="Metodo de pago" value={paymentForm.paymentMethodCode} onChange={(value) => setPaymentForm({ ...paymentForm, paymentMethodCode: value })} options={paymentMethodOptions} />
          <Field label="Notas" value={paymentForm.notes} onChange={(value) => setPaymentForm({ ...paymentForm, notes: value })} />
          <CheckField label="Acepta advertencia legal" checked={paymentForm.legalNoticeAccepted} onChange={(value) => setPaymentForm({ ...paymentForm, legalNoticeAccepted: value })} />
          <div className="field-note wide">El pago diario verbal registra el acuerdo operativo. La empresa debe validar con su contador/asesor laboral si debe reportar seguridad social, nomina electronica o soporte adicional.</div>
        </div>
      </FormPanel>

      <div className="split">
        <PayrollTable
          title="Pagos diarios registrados"
          rows={payments.map((payment) => [
            workerName(workers, payment.workerId),
            payment.workDate,
            money(payment.paidAmount),
            payment.paymentMethodCode,
            <button className="secondary" disabled={busy || !settingsForm.electronicPayrollEnabled || hasDocument(electronicDocuments, payment.id)} onClick={() => onIssueElectronicDocument(payment.id)} type="button">
              Emitir soporte
            </button>,
          ])}
          columns={['Trabajador', 'Fecha', 'Pagado', 'Metodo', 'Nomina electronica']}
        />
        <PayrollTable
          title="Soportes electronicos mock"
          rows={electronicDocuments.map((document) => [
            document.issuedAt ? String(document.issuedAt).slice(0, 10) : '',
            document.dailyLaborPaymentId,
            document.cune,
            document.status,
          ])}
          columns={['Fecha', 'Pago diario', 'CUNE', 'Estado']}
        />
      </div>
    </section>
  );
}

function PayrollTable({ title, columns, rows }) {
  return <DataTable
    title={title}
    columns={columns}
    rows={rows}
    emptyMessage="Sin registros."
    sectionClassName="tool-panel compact-panel"
  />;
}

function workerName(workers, workerId) {
  const worker = workers.find((item) => item.id === workerId);
  return worker ? worker.fullName : workerId;
}

function hasDocument(documents, paymentId) {
  return documents.some((document) => document.dailyLaborPaymentId === paymentId);
}

function money(value) {
  if (value === null || value === undefined || value === '') {
    return '';
  }
  return Number(value).toLocaleString('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 2 });
}
