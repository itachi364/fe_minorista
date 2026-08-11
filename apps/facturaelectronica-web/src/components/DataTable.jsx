export function DataTable({
  title,
  description,
  columns,
  rows,
  emptyMessage = 'Sin registros.',
  sectionClassName = 'tool-panel',
  titleLevel = 1,
  rowKey,
}) {
  const Heading = titleLevel === 2 ? 'h2' : 'h1';
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
      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>{columns.map((column) => <th key={column}>{column}</th>)}</tr>
          </thead>
          <tbody>
            {rows.length === 0 && <tr><td colSpan={columns.length}>{emptyMessage}</td></tr>}
            {rows.map((row, rowIndex) => (
              <tr key={rowKey ? rowKey(row, rowIndex) : rowIndex}>
                {row.map((cell, cellIndex) => <td key={cellIndex}>{cell}</td>)}
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </section>
  );
}
