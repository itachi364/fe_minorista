import { DataTable } from '../../components/DataTable.jsx';
import { ActionModal } from '../../components/Modal.jsx';
import { Field, SelectField } from '../../components/forms.jsx';

export function SalesRegistryPanel({
  sales = [],
  selectedSale,
  listFilters,
  setListFilters,
  onViewDetail,
  onCloseDetail,
  busy,
  paymentOptions = [],
}) {
  return (
    <div className="stack">
      <section className="tool-panel">
        <header className="panel-header">
          <div>
            <h1>Registro de Ventas</h1>
            <p className="hint">Historico cargado automaticamente por rango, estado y metodo de pago. Las ventas emitidas solo se visualizan.</p>
          </div>
        </header>
        <div className="form-grid compact">
          <SelectField label="Estado" value={listFilters.saleStatus} onChange={(value) => setListFilters({ ...listFilters, saleStatus: value })} options={[
            { value: 'DRAFT', label: 'Borrador' },
            { value: 'CONFIRMED', label: 'Confirmada' },
            { value: 'VOIDED', label: 'Anulada' },
          ]} placeholder="Todos" />
          <Field label="Desde" value={listFilters.saleFrom} onChange={(value) => setListFilters({ ...listFilters, saleFrom: value })} type="date" />
          <Field label="Hasta" value={listFilters.saleTo} onChange={(value) => setListFilters({ ...listFilters, saleTo: value })} type="date" />
          <SelectField label="Metodo de pago" value={listFilters.salePaymentMethodCode} onChange={(value) => setListFilters({ ...listFilters, salePaymentMethodCode: value })} options={paymentOptions} placeholder="Todos" />
          <SelectField label="Estado fiscal" value={listFilters.saleDocumentStatus} onChange={(value) => setListFilters({ ...listFilters, saleDocumentStatus: value })} options={[
            { value: 'VALIDATED', label: 'Validado' },
            { value: 'REJECTED', label: 'Rechazado' },
            { value: 'PENDING', label: 'Pendiente' },
          ]} placeholder="Todos" />
        </div>
      </section>
      <DataTable
        title="Ventas registradas"
        description="Historico inmutable de ventas por empresa."
        columns={['Fecha', 'Estado', 'Documento', 'Estado fiscal', 'Cliente', 'Metodo pago', 'Subtotal', 'IVA', 'Total', 'Detalle']}
        rows={sales.map((sale) => saleRow(sale, onViewDetail, busy))}
        rowKey={(_row, index) => sales[index]?.id || index}
        emptyMessage="Sin ventas consultadas."
      />
      {selectedSale && (
        <SaleDetailModal sale={selectedSale} onClose={onCloseDetail} />
      )}
    </div>
  );
}

function SaleDetailModal({ sale, onClose }) {
  const document = sale.electronicDocument;
  return (
    <ActionModal title="Detalle de venta" onClose={onClose} size="wide">
      <div className="detail-grid">
        <DetailCard title="Venta">
          <DetailRow label="ID venta" value={sale.id} code />
          <DetailRow label="Fecha creacion" value={dateTime(sale.createdAt)} />
          <DetailRow label="Fecha confirmacion" value={dateTime(sale.confirmedAt)} />
          <DetailRow label="Estado" value={labelSaleStatus(sale.status)} />
          <DetailRow label="Comprador" value={sale.customerId || labelBuyerMode(sale.buyerIdentificationMode)} code={Boolean(sale.customerId)} />
          <DetailRow label="Metodo de pago" value={labelPaymentMethod(sale.paymentMethodCode)} />
        </DetailCard>
        <DetailCard title="Documento electronico">
          <DetailRow label="Documento" value={document ? `${document.prefix || ''}${document.documentNumber || ''}` : 'Sin documento'} />
          <DetailRow label="Tipo" value={document?.documentType || ''} />
          <DetailRow label="Estado fiscal" value={labelDocumentStatus(document?.status)} />
          <DetailRow label="Proveedor" value={document?.providerStatus || ''} />
          <DetailRow label="Tracking" value={document?.providerTrackingId || ''} code />
          <DetailRow label="CUFE/CUDE" value={document?.cufeCude || 'No disponible'} code />
        </DetailCard>
        <DetailCard title="Totales">
          <DetailRow label="Subtotal" value={money(sale.subtotal)} />
          <DetailRow label="Descuento" value={money(sale.discountTotal)} />
          <DetailRow label="IVA / impuestos" value={money(sale.taxTotal)} />
          <DetailRow label="Total" value={money(sale.total)} />
        </DetailCard>
      </div>
      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Producto</th>
              <th>SKU</th>
              <th>Tipo</th>
              <th>Cantidad</th>
              <th>Precio</th>
              <th>Impuesto</th>
              <th>Total</th>
            </tr>
          </thead>
          <tbody>
            {(sale.lines || []).length === 0 && <tr><td colSpan="7">Sin lineas registradas.</td></tr>}
            {(sale.lines || []).map((line) => (
              <tr key={line.id || line.productId}>
                <td>{line.productName || line.productId || ''}</td>
                <td>{line.productSku || ''}</td>
                <td>{labelItemType(line.itemType)}</td>
                <td>{number(line.quantity)}</td>
                <td>{money(line.unitPrice)}</td>
                <td>{line.taxCode ? `${line.taxCode} ${number(line.taxRate)}%` : ''}</td>
                <td>{money(line.total)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </ActionModal>
  );
}

function DetailCard({ title, children }) {
  return (
    <section className="detail-card">
      <h2>{title}</h2>
      <dl>{children}</dl>
    </section>
  );
}

function DetailRow({ label, value, code = false }) {
  return (
    <div>
      <dt>{label}</dt>
      <dd>{code ? <code>{value || ''}</code> : value || ''}</dd>
    </div>
  );
}

function saleRow(sale, onViewDetail, busy) {
  const document = sale.electronicDocument;
  return [
    shortDate(sale.confirmedAt || sale.createdAt),
    labelSaleStatus(sale.status),
    document ? `${document.prefix || ''}${document.documentNumber || ''}` : 'Sin documento',
    labelDocumentStatus(document?.status),
    sale.customerId || labelBuyerMode(sale.buyerIdentificationMode),
    labelPaymentMethod(sale.paymentMethodCode),
    money(sale.subtotal),
    money(sale.taxTotal),
    money(sale.total),
    {
      searchText: 'ver detalle cufe cude documento fiscal',
      content: <button className="secondary compact" disabled={busy} onClick={() => onViewDetail(sale)} type="button">Ver detalle</button>,
    },
  ];
}

function labelSaleStatus(value) {
  return {
    DRAFT: 'Borrador',
    CONFIRMED: 'Confirmada',
    VOIDED: 'Anulada',
  }[value] || value || '';
}

function labelDocumentStatus(value) {
  return {
    VALIDATED: 'Validado',
    REJECTED: 'Rechazado',
    PENDING: 'Pendiente',
  }[value] || value || '';
}

function labelBuyerMode(value) {
  return {
    FINAL_CONSUMER: 'Consumidor final',
    IDENTIFIED_CUSTOMER: 'Cliente identificado',
  }[value] || value || 'Consumidor final';
}

function labelPaymentMethod(value) {
  return {
    CASH: 'Efectivo',
    DEBIT_CARD: 'Tarjeta debito',
    CREDIT_CARD: 'Tarjeta credito',
    BREB_KEY: 'Llave Bre-B',
    BANK_TRANSFER: 'Transferencia bancaria',
    VIRTUAL_WALLET: 'Billetera virtual',
  }[value] || value || '';
}

function labelItemType(value) {
  return {
    PHYSICAL_GOOD: 'Bien fisico',
    SERVICE: 'Servicio',
    SUPPLY: 'Insumo',
  }[value] || value || '';
}

function shortDate(value) {
  return value ? String(value).slice(0, 10) : '';
}

function dateTime(value) {
  return value ? new Date(value).toLocaleString('es-CO') : '';
}

function number(value) {
  if (value === null || value === undefined || value === '') {
    return '';
  }
  return Number(value).toLocaleString('es-CO');
}

function money(value) {
  if (value === null || value === undefined || value === '') {
    return '';
  }
  return Number(value).toLocaleString('es-CO', { style: 'currency', currency: 'COP', maximumFractionDigits: 2 });
}
