import { useMemo, useState } from 'react';
import { DataTable } from '../../components/DataTable.jsx';
import { Field, SelectField, StatusBadge } from '../../components/forms.jsx';

const eventOptions = [
  { value: 'SALE_CONFIRMED', label: 'Venta facturada' },
  { value: 'PURCHASE_CONFIRMED', label: 'Compra confirmada' },
  { value: 'EXPENSE_CONFIRMED', label: 'Egreso confirmado' },
  { value: 'ACCOUNTS_PAYABLE_PAYMENT_REGISTERED', label: 'Pago de cuenta por pagar' },
  { value: 'ACCOUNTS_RECEIVABLE_PAYMENT_REGISTERED', label: 'Recaudo de cuenta por cobrar' },
  { value: 'PAYROLL_DAILY_PAYMENT_REGISTERED', label: 'Pago diario de nomina' },
];

const sourceOptions = [
  { value: 'SALE', label: 'Venta' },
  { value: 'PURCHASE', label: 'Compra' },
  { value: 'EXPENSE', label: 'Egreso' },
  { value: 'ACCOUNTS_PAYABLE_PAYMENT', label: 'Pago cuenta por pagar' },
  { value: 'ACCOUNTS_RECEIVABLE_PAYMENT', label: 'Recaudo cuenta por cobrar' },
  { value: 'PAYROLL_DAILY_PAYMENT', label: 'Pago diario' },
];

const sideOptions = [
  { value: 'DEBIT', label: 'Debito' },
  { value: 'CREDIT', label: 'Credito' },
];

const amountTypeOptions = [
  { value: 'TOTAL', label: 'Total' },
  { value: 'SUBTOTAL', label: 'Subtotal' },
  { value: 'TAX_TOTAL', label: 'Impuesto' },
];

const eventLabels = Object.fromEntries(eventOptions.map((option) => [option.value, option.label]));
const sourceLabels = Object.fromEntries(sourceOptions.map((option) => [option.value, option.label]));

const recommendedTemplate = {
  accounts: [
    { code: '1105', name: 'Caja', parentAccountId: null },
    { code: '1110', name: 'Bancos', parentAccountId: null },
    { code: '1305', name: 'Clientes', parentAccountId: null },
    { code: '1435', name: 'Inventarios', parentAccountId: null },
    { code: '2205', name: 'Proveedores', parentAccountId: null },
    { code: '2408', name: 'IVA generado', parentAccountId: null },
    { code: '4135', name: 'Ingresos operacionales', parentAccountId: null },
    { code: '5135', name: 'Gastos operacionales', parentAccountId: null },
  ],
  rules: [
    {
      eventType: 'SALE_CONFIRMED',
      sourceType: 'SALE',
      name: 'Venta facturada',
      lines: [
        { accountCode: '1105', side: 'DEBIT', amountType: 'TOTAL', description: 'Ingreso a caja' },
        { accountCode: '4135', side: 'CREDIT', amountType: 'SUBTOTAL', description: 'Ingreso por venta' },
        { accountCode: '2408', side: 'CREDIT', amountType: 'TAX_TOTAL', description: 'IVA generado' },
      ],
    },
    {
      eventType: 'EXPENSE_CONFIRMED',
      sourceType: 'EXPENSE',
      name: 'Egreso pagado de contado',
      lines: [
        { accountCode: '5135', side: 'DEBIT', amountType: 'TOTAL', description: 'Gasto operacional' },
        { accountCode: '1105', side: 'CREDIT', amountType: 'TOTAL', description: 'Salida de caja' },
      ],
    },
  ],
};

export function AccountingConfigurationPanel({
  accounts,
  rules,
  onLoad,
  onConfigure,
  onUpdateAccount = async () => null,
  onDeactivateAccount = async () => null,
  onUpdateRule = async () => null,
  onDeactivateRule = async () => null,
  busy,
}) {
  const [draft, setDraft] = useState(() => emptyDraft());
  const [editing, setEditing] = useState(null);
  const errors = useMemo(() => validateDraft(draft), [draft]);
  const isEditingAccount = editing?.type === 'account';
  const isEditingRule = editing?.type === 'rule';
  const canSubmit = !busy
    && (draft.accounts.length > 0 || draft.rules.length > 0)
    && errors.length === 0
    && (!isEditingAccount || (draft.accounts.length === 1 && draft.rules.length === 0))
    && (!isEditingRule || (draft.rules.length === 1 && draft.accounts.length === 0));

  async function submitDraft() {
    if (!canSubmit) {
      return null;
    }
    if (isEditingAccount) {
      const result = await onUpdateAccount(editing.id, accountPayload(draft.accounts[0]));
      if (result) {
        clearDraft();
      }
      return result;
    }
    if (isEditingRule) {
      const result = await onUpdateRule(editing.id, rulePayload(draft.rules[0]));
      if (result) {
        clearDraft();
      }
      return result;
    }
    const existingAccountCodes = new Set(accounts.map((account) => account.code));
    const result = await onConfigure({
      accounts: draft.accounts
        .filter(({ code }) => !existingAccountCodes.has(code.trim()))
        .map(accountPayload),
      rules: draft.rules.map(rulePayload),
    });
    if (result) {
      clearDraft();
    }
    return result;
  }

  function clearDraft() {
    setDraft(emptyDraft());
    setEditing(null);
  }

  function editAccount(account) {
    setEditing({ type: 'account', id: account.id });
    setDraft({
      accounts: [{
        code: account.code || '',
        name: account.name || '',
        parentAccountId: account.parentAccountId || '',
      }],
      rules: [],
    });
  }

  function editRule(rule) {
    setEditing({ type: 'rule', id: rule.id });
    setDraft({
      accounts: [],
      rules: [{
        eventType: rule.eventType || 'SALE_CONFIRMED',
        sourceType: rule.sourceType || 'SALE',
        name: rule.name || '',
        lines: rule.lines?.length ? rule.lines.map((line) => ({
          accountCode: line.accountCode || '',
          side: line.side || 'DEBIT',
          amountType: line.amountType || 'TOTAL',
          description: line.description || '',
        })) : [emptyMovement('DEBIT'), emptyMovement('CREDIT')],
      }],
    });
  }

  return <section className="accounting-configuration">
    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>Configuracion contable</h1>
          <p className="hint">Configura cuentas PUC y reglas de asientos por empresa antes de cerrar ventas, compras o egresos.</p>
        </div>
        <div className="toolbar-actions">
          <button className="secondary" disabled={busy} onClick={onLoad} type="button">Actualizar estado</button>
          <button className="secondary" disabled={busy || Boolean(editing)} onClick={() => setDraft(cloneTemplate())} type="button">Usar plantilla recomendada</button>
          {editing && <button className="secondary" disabled={busy} onClick={clearDraft} type="button">Cancelar edicion</button>}
          <button className="primary" disabled={!canSubmit} onClick={submitDraft} type="button">{submitLabel(editing)}</button>
        </div>
      </header>
      <div className="status-row">
        <StatusBadge label="Cuentas" value={accounts.length} tone={accounts.length > 0 ? 'ok' : 'warn'} />
        <StatusBadge label="Reglas activas" value={rules.filter((rule) => rule.active).length} tone={rules.some((rule) => rule.active) ? 'ok' : 'warn'} />
        <StatusBadge label="Ventas" value={hasActiveSaleRule(rules) ? 'Lista' : 'Pendiente'} tone={hasActiveSaleRule(rules) ? 'ok' : 'warn'} />
      </div>
      {errors.length > 0 && <div className="inline-alert warn">
        {errors.slice(0, 3).map((error) => <p key={error}>{error}</p>)}
      </div>}
    </section>

    <section className="tool-panel accounting-builder">
      <header className="panel-header">
        <div>
          <h2>Plan de cuentas</h2>
          <p className="hint">Agrega una o varias cuentas PUC antes de guardar.</p>
        </div>
        <button className="secondary" disabled={busy || isEditingRule} onClick={() => setDraft(addAccountRow)} type="button">Agregar cuenta</button>
      </header>
      {draft.accounts.length === 0 && <p className="empty-state">Sin cuentas en preparacion.</p>}
      {draft.accounts.map((account, index) => (
        <div className="accounting-row" key={`account-${index}`}>
          <Field label="Codigo PUC" value={account.code} onChange={(value) => setDraft(updateAccount(index, 'code', value))} disabled={busy || isEditingAccount} placeholder="Ej. 1105" />
          <Field label="Nombre de cuenta" value={account.name} onChange={(value) => setDraft(updateAccount(index, 'name', value))} disabled={busy} placeholder="Ej. Caja" />
          <Field label="Cuenta padre" value={account.parentAccountId || ''} onChange={(value) => setDraft(updateAccount(index, 'parentAccountId', value))} disabled={busy} placeholder="UUID opcional" />
          <button className="danger" disabled={busy} onClick={() => setDraft(removeAccount(index))} type="button">Quitar</button>
        </div>
      ))}
    </section>

    <section className="tool-panel accounting-builder">
      <header className="panel-header">
        <div>
          <h2>Reglas contables</h2>
          <p className="hint">Cada regla puede tener varios movimientos contables. Debe existir al menos un debito y un credito.</p>
        </div>
        <button className="secondary" disabled={busy || isEditingAccount} onClick={() => setDraft(addRuleRow)} type="button">Agregar regla</button>
      </header>
      {draft.rules.length === 0 && <p className="empty-state">Sin reglas en preparacion.</p>}
      {draft.rules.map((rule, ruleIndex) => (
        <section className="accounting-rule-card" key={`rule-${ruleIndex}`}>
          <div className="form-grid compact">
            <SelectField label="Evento" value={rule.eventType} onChange={(value) => setDraft(updateRule(ruleIndex, 'eventType', value))} options={eventOptions} disabled={busy} />
            <SelectField label="Origen" value={rule.sourceType} onChange={(value) => setDraft(updateRule(ruleIndex, 'sourceType', value))} options={sourceOptions} disabled={busy} />
            <Field label="Nombre de la regla" value={rule.name} onChange={(value) => setDraft(updateRule(ruleIndex, 'name', value))} disabled={busy} />
          </div>
          <header className="subsection-header">
            <h3>Movimientos contables</h3>
            <div className="toolbar-actions">
              <button className="secondary" disabled={busy} onClick={() => setDraft(addMovement(ruleIndex))} type="button">Agregar movimiento</button>
              <button className="danger" disabled={busy} onClick={() => setDraft(removeRule(ruleIndex))} type="button">Quitar regla</button>
            </div>
          </header>
          {rule.lines.map((line, lineIndex) => (
            <div className="accounting-row movement-row" key={`rule-${ruleIndex}-line-${lineIndex}`}>
              <Field label="Cuenta PUC" value={line.accountCode} onChange={(value) => setDraft(updateMovement(ruleIndex, lineIndex, 'accountCode', value))} disabled={busy} placeholder="Ej. 1105" />
              <SelectField label="Naturaleza" value={line.side} onChange={(value) => setDraft(updateMovement(ruleIndex, lineIndex, 'side', value))} options={sideOptions} disabled={busy} />
              <SelectField label="Base de monto" value={line.amountType} onChange={(value) => setDraft(updateMovement(ruleIndex, lineIndex, 'amountType', value))} options={amountTypeOptions} disabled={busy} />
              <Field label="Descripcion" value={line.description} onChange={(value) => setDraft(updateMovement(ruleIndex, lineIndex, 'description', value))} disabled={busy} />
              <button className="danger" disabled={busy || rule.lines.length <= 1} onClick={() => setDraft(removeMovement(ruleIndex, lineIndex))} type="button">Quitar</button>
            </div>
          ))}
        </section>
      ))}
    </section>

    <DataTable
      title="Reglas contables"
      description="Estas reglas determinan los asientos automaticos que se crean al cerrar operaciones."
      columns={['Evento', 'Origen', 'Nombre', 'Movimientos contables', 'Uso', 'Estado', 'Acciones']}
      rows={rules.map((rule) => [
        eventLabels[rule.eventType] || rule.eventType,
        sourceLabels[rule.sourceType] || rule.sourceType,
        rule.name,
        rule.lines?.length || 0,
        usageLabel(rule),
        rule.active ? 'Activa' : 'Inactiva',
        {
          searchText: rule.used ? 'usada solo lectura' : 'sin uso editable',
          content: rule.used
            ? <span className="hint">Solo lectura</span>
            : <div className="row-actions">
                <button className="secondary" disabled={busy} onClick={() => editRule(rule)} type="button">Actualizar</button>
                <button className="secondary" disabled={busy || !rule.active} onClick={() => onDeactivateRule(rule.id)} type="button">Inactivar</button>
              </div>,
        },
      ])}
      emptyMessage="Sin reglas configuradas. Crea una configuracion contable antes de cerrar ventas."
      rowKey={(row) => `${row[0]}-${row[2]}-${row[4]}`}
      pageSize={5}
    />

    <DataTable
      title="Plan de cuentas"
      description="Cuentas PUC disponibles para registrar asientos y generar reportes financieros."
      columns={['Codigo', 'Cuenta', 'Categoria', 'Naturaleza', 'Uso', 'Estado', 'Acciones']}
      rows={accounts.map((account) => [
        account.code,
        account.name,
        account.category,
        account.nature,
        usageLabel(account),
        account.active ? 'Activa' : 'Inactiva',
        {
          searchText: account.used ? 'usada solo lectura' : 'sin uso editable',
          content: account.used
            ? <span className="hint">Solo lectura</span>
            : <div className="row-actions">
                <button className="secondary" disabled={busy} onClick={() => editAccount(account)} type="button">Actualizar</button>
                <button className="secondary" disabled={busy || !account.active} onClick={() => onDeactivateAccount(account.id)} type="button">Inactivar</button>
              </div>,
        },
      ])}
      emptyMessage="Sin cuentas configuradas."
      rowKey={(row) => row[0]}
      pageSize={8}
    />
  </section>;
}

function hasActiveSaleRule(rules) {
  return rules.some((rule) => rule.eventType === 'SALE_CONFIRMED' && rule.active);
}

function submitLabel(editing) {
  if (editing?.type === 'account') {
    return 'Actualizar cuenta';
  }
  if (editing?.type === 'rule') {
    return 'Actualizar regla';
  }
  return 'Guardar configuracion';
}

function usageLabel(item) {
  const count = Number(item.usageCount || 0);
  return item.used ? `Usada (${count})` : 'Sin uso';
}

function accountPayload({ code, name, parentAccountId }) {
  return {
    code: code.trim(),
    name: name.trim(),
    parentAccountId: parentAccountId || null,
  };
}

function rulePayload(rule) {
  return {
    eventType: rule.eventType,
    sourceType: rule.sourceType,
    name: rule.name.trim(),
    lines: rule.lines.map((line) => ({
      accountCode: line.accountCode.trim(),
      side: line.side,
      amountType: line.amountType,
      description: line.description.trim(),
    })),
  };
}

function emptyDraft() {
  return { accounts: [], rules: [] };
}

function cloneTemplate() {
  return JSON.parse(JSON.stringify(recommendedTemplate));
}

function addAccountRow(current) {
  return { ...current, accounts: [...current.accounts, { code: '', name: '', parentAccountId: '' }] };
}

function updateAccount(index, field, value) {
  return (current) => ({
    ...current,
    accounts: current.accounts.map((account, currentIndex) => currentIndex === index ? { ...account, [field]: value } : account),
  });
}

function removeAccount(index) {
  return (current) => ({ ...current, accounts: current.accounts.filter((_, currentIndex) => currentIndex !== index) });
}

function addRuleRow(current) {
  return {
    ...current,
    rules: [...current.rules, {
      eventType: 'SALE_CONFIRMED',
      sourceType: 'SALE',
      name: '',
      lines: [emptyMovement('DEBIT'), emptyMovement('CREDIT')],
    }],
  };
}

function updateRule(index, field, value) {
  return (current) => ({
    ...current,
    rules: current.rules.map((rule, currentIndex) => currentIndex === index ? { ...rule, [field]: value } : rule),
  });
}

function removeRule(index) {
  return (current) => ({ ...current, rules: current.rules.filter((_, currentIndex) => currentIndex !== index) });
}

function addMovement(ruleIndex) {
  return (current) => ({
    ...current,
    rules: current.rules.map((rule, currentIndex) => currentIndex === ruleIndex
      ? { ...rule, lines: [...rule.lines, emptyMovement('DEBIT')] }
      : rule),
  });
}

function updateMovement(ruleIndex, lineIndex, field, value) {
  return (current) => ({
    ...current,
    rules: current.rules.map((rule, currentIndex) => currentIndex === ruleIndex
      ? {
          ...rule,
          lines: rule.lines.map((line, currentLineIndex) => currentLineIndex === lineIndex
            ? { ...line, [field]: value }
            : line),
        }
      : rule),
  });
}

function removeMovement(ruleIndex, lineIndex) {
  return (current) => ({
    ...current,
    rules: current.rules.map((rule, currentIndex) => currentIndex === ruleIndex
      ? { ...rule, lines: rule.lines.filter((_, currentLineIndex) => currentLineIndex !== lineIndex) }
      : rule),
  });
}

function emptyMovement(side) {
  return { accountCode: '', side, amountType: 'TOTAL', description: '' };
}

function validateDraft(draft) {
  const errors = [];
  const accountCodes = draft.accounts.map((account) => account.code.trim()).filter(Boolean);
  const duplicateAccount = accountCodes.find((code, index) => accountCodes.indexOf(code) !== index);
  if (duplicateAccount) {
    errors.push(`La cuenta ${duplicateAccount} esta repetida en el lote.`);
  }
  draft.accounts.forEach((account, index) => {
    if (!account.code.trim() || !account.name.trim()) {
      errors.push(`La cuenta ${index + 1} requiere codigo PUC y nombre.`);
    }
  });
  draft.rules.forEach((rule, index) => {
    if (!rule.eventType || !rule.sourceType || !rule.name.trim()) {
      errors.push(`La regla ${index + 1} requiere evento, origen y nombre.`);
    }
    const hasDebit = rule.lines.some((line) => line.side === 'DEBIT');
    const hasCredit = rule.lines.some((line) => line.side === 'CREDIT');
    if (!hasDebit || !hasCredit) {
      errors.push(`La regla ${index + 1} requiere al menos un debito y un credito.`);
    }
    rule.lines.forEach((line, lineIndex) => {
      if (!line.accountCode.trim() || !line.side || !line.amountType) {
        errors.push(`El movimiento ${lineIndex + 1} de la regla ${index + 1} requiere cuenta, naturaleza y base de monto.`);
      }
    });
  });
  return errors;
}
