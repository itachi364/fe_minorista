import { DataTable } from '../../components/DataTable.jsx';
import { Field, SelectField } from '../../components/forms.jsx';

const chartLabels = {
  TABLE: 'Tabla',
  BAR: 'Barras',
  LINE: 'Historico',
  PIE: 'Circular',
};

export function ReportsForm({
  definitions,
  options,
  form,
  setForm,
  data,
  onReportChange,
  onLoadDefinitions,
  onSubmit,
  onExport,
  busy,
}) {
  const selectedReport = definitions.find((report) => report.code === form.reportCode);
  const rows = extractRows(data?.data);

  return <section className="tool-panel report-workspace">
    <header className="panel-header">
      <div>
        <h1>Reportes</h1>
        <p>Genera reportes operativos, financieros y administrativos desde catalogo backend.</p>
      </div>
      <div className="toolbar-actions">
        <button className="secondary" disabled={busy} onClick={onLoadDefinitions} type="button">Actualizar catalogo</button>
        <button className="secondary" disabled={busy || !selectedReport} onClick={() => onExport('CSV')} type="button">Descargar CSV</button>
        <button className="secondary" disabled={busy || !selectedReport} onClick={() => onExport('XLS')} type="button">Descargar Excel</button>
        <button className="primary" disabled={busy || !selectedReport} onClick={onSubmit} type="button">Generar reporte</button>
      </div>
    </header>

    <div className="report-layout">
      <section className="report-controls">
        <div className="form-grid">
          <SelectField
            label="Reporte"
            value={form.reportCode}
            onChange={onReportChange}
            options={definitions.map((report) => ({ value: report.code, label: `${report.category} - ${report.label}` }))}
            disabled={busy}
          />
          <SelectField
            label="Visualizacion"
            value={form.chartType}
            onChange={(chartType) => setForm({ ...form, chartType })}
            options={(selectedReport?.chartTypes || ['TABLE']).map((chartType) => ({ value: chartType, label: chartLabels[chartType] || chartType }))}
            disabled={busy || !selectedReport}
          />
        </div>
        {selectedReport && <p className="hint">{selectedReport.description}</p>}
        <div className="form-grid compact">
          {(selectedReport?.filters || []).map((filter) => (
            <ReportFilterField
              key={filter.code}
              filter={filter}
              value={form.filters?.[filter.code] || ''}
              options={options[filter.code] || []}
              disabled={busy}
              onChange={(value) => setForm({
                ...form,
                filters: { ...(form.filters || {}), [filter.code]: value },
              })}
            />
          ))}
        </div>
      </section>

      <section className="report-visual">
        <ReportSummary data={data} report={selectedReport} rowCount={rows.values.length} />
        {data && form.chartType !== 'TABLE' && <SimpleChart rows={rows.values} chartType={form.chartType} />}
      </section>
    </div>

    {data && <DataTable
      title="Resultado"
      titleLevel={2}
      rows={rows.values}
      columns={rows.columns}
      emptyMessage="Sin datos para los filtros seleccionados."
      sectionClassName="report-table-panel"
      rowKey={(_row, index) => `report-row-${index}`}
    />}
  </section>;
}

function ReportFilterField({ filter, value, options, disabled, onChange }) {
  const label = filter.required ? `${filter.label} *` : filter.label;
  if (filter.type === 'DATE') {
    return <Field label={label} type="date" value={value} onChange={onChange} disabled={disabled} />;
  }
  if (filter.type === 'SELECT') {
    return <SelectField
      label={label}
      value={value}
      onChange={onChange}
      options={options.map((option) => ({ value: option.value, label: option.label }))}
      disabled={disabled}
      placeholder="Todos"
    />;
  }
  return <Field label={label} value={value} onChange={onChange} disabled={disabled} placeholder="Opcional" />;
}

function ReportSummary({ data, report, rowCount }) {
  return <div className="report-summary">
    <span>
      <b>Reporte</b>
      {report?.label || 'Sin seleccion'}
    </span>
    <span>
      <b>Registros</b>
      {data ? rowCount : 'Pendiente'}
    </span>
    <span>
      <b>Generado</b>
      {data?.generatedAt ? shortDateTime(data.generatedAt) : 'Sin ejecutar'}
    </span>
  </div>;
}

function SimpleChart({ rows, chartType }) {
  const points = chartPoints(rows);
  if (points.length === 0) {
    return <div className="report-chart empty">No hay datos numericos para graficar.</div>;
  }
  const max = Math.max(...points.map((point) => point.value), 1);
  return <div className={`report-chart ${chartType.toLowerCase()}`}>
    {points.slice(0, 12).map((point) => (
      <div className="chart-row" key={point.label}>
        <span>{point.label}</span>
        <div><i style={{ width: `${Math.max(4, (point.value / max) * 100)}%` }} /></div>
        <strong>{formatNumber(point.value)}</strong>
      </div>
    ))}
  </div>;
}

function extractRows(payload) {
  const source = selectTabularSource(payload);
  if (!Array.isArray(source) || source.length === 0) {
    if (payload && typeof payload === 'object' && !Array.isArray(payload)) {
      const flat = flattenObject(payload);
      return { columns: Object.keys(flat).map(formatColumn), values: [Object.values(flat).map(formatCell)] };
    }
    return { columns: ['Resultado'], values: [] };
  }
  const flatRows = source.map(flattenObject);
  const columns = Array.from(new Set(flatRows.flatMap((row) => Object.keys(row))));
  return {
    columns: columns.map(formatColumn),
    values: flatRows.map((row) => columns.map((column) => formatCell(row[column]))),
  };
}

function selectTabularSource(payload) {
  if (Array.isArray(payload)) {
    return payload;
  }
  if (!payload || typeof payload !== 'object') {
    return [];
  }
  const entries = Object.entries(payload).filter(([, value]) => Array.isArray(value));
  if (entries.length === 0) {
    return [];
  }
  return entries.sort((left, right) => right[1].length - left[1].length)[0][1];
}

function flattenObject(value, prefix = '') {
  if (!value || typeof value !== 'object' || Array.isArray(value)) {
    return { [prefix || 'value']: value };
  }
  return Object.entries(value).reduce((result, [key, nested]) => {
    const nextKey = prefix ? `${prefix}.${key}` : key;
    if (nested && typeof nested === 'object' && !Array.isArray(nested)) {
      return { ...result, ...flattenObject(nested, nextKey) };
    }
    return { ...result, [nextKey]: nested };
  }, {});
}

function chartPoints(rows) {
  return rows.map((row, index) => {
    const numericCell = row.find((cell) => typeof rawNumber(cell) === 'number');
    return {
      label: String(row.find((cell) => cell && Number.isNaN(Number(cell))) || `Dato ${index + 1}`).slice(0, 34),
      value: rawNumber(numericCell) || 0,
    };
  }).filter((point) => point.value > 0);
}

function rawNumber(value) {
  if (typeof value === 'number') {
    return value;
  }
  if (typeof value !== 'string') {
    return null;
  }
  const normalized = value.replace(/[^\d.,-]/g, '').replace(/\./g, '').replace(',', '.');
  const parsed = Number(normalized);
  return Number.isFinite(parsed) ? parsed : null;
}

function formatColumn(value) {
  return String(value)
    .replace(/\./g, ' / ')
    .replace(/([a-z])([A-Z])/g, '$1 $2')
    .replace(/_/g, ' ')
    .toLowerCase()
    .replace(/^\w|\s\w/g, (letter) => letter.toUpperCase());
}

function formatCell(value) {
  if (value === null || value === undefined) {
    return '';
  }
  if (typeof value === 'number') {
    return Number(value).toLocaleString('es-CO');
  }
  if (typeof value === 'boolean') {
    return value ? 'Si' : 'No';
  }
  if (Array.isArray(value)) {
    return value.length;
  }
  return String(value);
}

function formatNumber(value) {
  return Number(value).toLocaleString('es-CO', { maximumFractionDigits: 2 });
}

function shortDateTime(value) {
  return new Date(value).toLocaleString('es-CO', { dateStyle: 'short', timeStyle: 'short' });
}
