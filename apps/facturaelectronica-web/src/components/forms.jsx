import { useId, useState } from 'react';
import { asPretty } from '../utils/payloadBuilders.js';

export function FormPanel({ title, submitLabel, onSubmit, busy, children }) {
  return <form className="tool-panel" onSubmit={(event) => { event.preventDefault(); onSubmit(); }}>
    <header className="panel-header">
      <h1>{title}</h1>
      <button className="primary" disabled={busy} type="submit">{submitLabel}</button>
    </header>
    {children}
  </form>;
}

export function Field({ label, value, onChange, type = 'text', readOnly = false, disabled = false, placeholder = '', autoComplete, inputRef, onKeyDown, onBlur, error = '', min, max, step, maxLength, inputMode, pattern, list, accept }) {
  const inputId = useId();
  const errorId = `${inputId}-error`;
  return <label className={error ? 'field invalid' : 'field'}>
    {label}
    <input
      ref={inputRef}
      value={value}
      onChange={(event) => onChange(event.target.value)}
      onKeyDown={onKeyDown}
      onBlur={onBlur}
      type={type}
      readOnly={readOnly}
      disabled={disabled}
      placeholder={placeholder}
      autoComplete={autoComplete}
      min={min}
      max={max}
      step={step}
      maxLength={maxLength}
      inputMode={inputMode}
      pattern={pattern}
      list={list}
      accept={accept}
      aria-invalid={Boolean(error)}
      aria-describedby={error ? errorId : undefined}
    />
    {error && <span className="field-error-message" id={errorId}>{error}</span>}
  </label>;
}

export function SelectField({ label, value, onChange, options, disabled = false, placeholder = 'Selecciona una opcion' }) {
  return <label>
    {label}
    <select value={value} onChange={(event) => onChange(event.target.value)} disabled={disabled}>
      <option value="">{placeholder}</option>
      {options.map((option) => {
        const normalized = typeof option === 'object' ? option : { value: option, label: option };
        return <option key={normalized.value} value={normalized.value}>{normalized.label}</option>;
      })}
    </select>
  </label>;
}

export function MultiSelectField({ label, value, onChange, options, disabledValues = [] }) {
  const selectedValues = Array.isArray(value) ? value : [];
  return <label>
    {label}
    <select multiple value={selectedValues} onChange={(event) => {
      const nextValues = Array.from(event.target.selectedOptions).map((option) => option.value);
      onChange(nextValues);
    }}>
      {options.map((option) => (
        <option key={option.value} value={option.value} disabled={disabledValues.includes(option.value)}>
          {option.label}
        </option>
      ))}
    </select>
  </label>;
}

export function DualListField({ label, value, onChange, options, exclusiveValues = [], disabled = false }) {
  const selectedValues = Array.isArray(value) ? value : [];
  const selectedSet = new Set(selectedValues);
  const exclusiveSet = new Set(exclusiveValues);
  const availableOptions = options.filter((option) => !selectedSet.has(option.value));
  const selectedOptions = options.filter((option) => selectedSet.has(option.value));
  const [availableSelection, setAvailableSelection] = useSelectState();
  const [selectedSelection, setSelectedSelection] = useSelectState();

  function addSelected() {
    if (availableSelection.length === 0) {
      return;
    }
    if (availableSelection.some((item) => exclusiveSet.has(item))) {
      onChange(availableSelection.filter((item) => exclusiveSet.has(item)).slice(0, 1));
      setAvailableSelection([]);
      return;
    }
    const nextValues = [...selectedValues.filter((item) => !exclusiveSet.has(item)), ...availableSelection]
      .filter((item, index, all) => all.indexOf(item) === index);
    onChange(nextValues);
    setAvailableSelection([]);
  }

  function removeSelected() {
    if (selectedSelection.length === 0) {
      return;
    }
    onChange(selectedValues.filter((item) => !selectedSelection.includes(item)));
    setSelectedSelection([]);
  }

  return <fieldset className="dual-list-field">
    <legend>{label}</legend>
    <div className="dual-list-grid">
      <label>
        Disponibles
        <select multiple size={6} value={availableSelection} onChange={(event) => setAvailableSelection(valuesFromSelect(event))} disabled={disabled}>
          {availableOptions.map((option) => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </select>
      </label>
      <div className="dual-list-actions">
        <button className="secondary" disabled={disabled || availableSelection.length === 0} onClick={addSelected} type="button">Agregar &gt;</button>
        <button className="secondary" disabled={disabled || selectedSelection.length === 0} onClick={removeSelected} type="button">&lt; Quitar</button>
      </div>
      <label>
        Seleccionadas
        <select multiple size={6} value={selectedSelection} onChange={(event) => setSelectedSelection(valuesFromSelect(event))} disabled={disabled}>
          {selectedOptions.map((option) => (
            <option key={option.value} value={option.value}>{option.label}</option>
          ))}
        </select>
      </label>
    </div>
  </fieldset>;
}

function valuesFromSelect(event) {
  return Array.from(event.target.selectedOptions).map((option) => option.value);
}

function useSelectState() {
  return useState([]);
}

export function CheckField({ label, checked, onChange, disabled = false }) {
  return <label className="check-field">
    <input checked={checked} onChange={(event) => onChange(event.target.checked)} type="checkbox" disabled={disabled} />
    {label}
  </label>;
}

export function StatusBadge({ label, value, tone }) {
  return <span className={tone === 'ok' ? 'status-badge ok' : tone === 'warn' ? 'status-badge warn' : 'status-badge'}>
    <b>{label}</b> {value}
  </span>;
}

export function Result({ title, value, tone }) {
  return (
    <section className={tone === 'danger' ? 'result-panel danger' : 'result-panel'}>
      <h2>{title}</h2>
      <pre>{value ? asPretty(value) : ''}</pre>
    </section>
  );
}
