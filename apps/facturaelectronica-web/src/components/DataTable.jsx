import { useEffect, useMemo, useState } from 'react';

export function DataTable({
  title,
  description,
  columns,
  rows,
  emptyMessage = 'Sin registros.',
  sectionClassName = 'tool-panel',
  titleLevel = 1,
  rowKey,
  searchable = true,
  pageSize = 10,
}) {
  const [searchText, setSearchText] = useState('');
  const [page, setPage] = useState(0);
  const Heading = titleLevel === 2 ? 'h2' : 'h1';
  const normalizedSearch = searchText.trim().toLowerCase();
  const indexedRows = useMemo(() => rows.map((row, index) => ({ row, index })), [rows]);
  const filteredRows = useMemo(() => {
    if (!normalizedSearch) {
      return indexedRows;
    }
    return indexedRows.filter(({ row }) => row.some((cell) => cellText(cell).includes(normalizedSearch)));
  }, [indexedRows, normalizedSearch]);
  const pageCount = Math.max(1, Math.ceil(filteredRows.length / pageSize));
  const safePage = Math.min(page, pageCount - 1);
  const visibleRows = filteredRows.slice(safePage * pageSize, safePage * pageSize + pageSize);

  useEffect(() => {
    setPage(0);
  }, [normalizedSearch, rows.length]);

  return (
    <section className={sectionClassName}>
      {title && (
        <header className="panel-header">
          <div>
            <Heading>{title}</Heading>
            {description && <p className="hint">{description}</p>}
          </div>
        </header>
      )}
      {searchable && rows.length > 0 && (
        <div className="table-toolbar">
          <label>
            Buscar
            <input value={searchText} onChange={(event) => setSearchText(event.target.value)} placeholder="Filtra la tabla" />
          </label>
        </div>
      )}
      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>{columns.map((column) => <th key={column}>{column}</th>)}</tr>
          </thead>
          <tbody>
            {filteredRows.length === 0 && <tr><td colSpan={columns.length}>{rows.length === 0 ? emptyMessage : 'Sin coincidencias.'}</td></tr>}
            {visibleRows.map(({ row, index }) => (
              <tr key={rowKey ? rowKey(row, index) : index}>
                {row.map((cell, cellIndex) => <td key={cellIndex}>{cell}</td>)}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
      {filteredRows.length > pageSize && (
        <footer className="table-pagination">
          <button className="secondary" disabled={safePage === 0} onClick={() => setPage((current) => Math.max(0, current - 1))} type="button">Anterior</button>
          <span>Pagina {safePage + 1} de {pageCount}</span>
          <button className="secondary" disabled={safePage >= pageCount - 1} onClick={() => setPage((current) => Math.min(pageCount - 1, current + 1))} type="button">Siguiente</button>
        </footer>
      )}
    </section>
  );
}

function cellText(cell) {
  if (cell === null || cell === undefined) {
    return '';
  }
  if (typeof cell === 'string' || typeof cell === 'number' || typeof cell === 'boolean') {
    return String(cell).toLowerCase();
  }
  return '';
}
