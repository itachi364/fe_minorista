import { useEffect, useRef, useState } from 'react';
import { DataTable } from '../../components/DataTable.jsx';
import { Field, SelectField } from '../../components/forms.jsx';

export function SaleForm({ form, setForm, saleId, customerSearch, setCustomerSearch, customerOptions, selectedCustomer, onSearchCustomers, onSelectCustomer, updateItem, addItem, removeItem, onCreate, onConfirm, onScanBarcode, serviceConsumption, onLoadServiceConsumption, onUpdateServiceConsumptionQuantity, onUpdateServiceConsumptionReason, onConfirmServiceConsumption, busy, paymentOptions = [], walletOptions = [], listFilters, setListFilters, sales = [], onLoadSales }) {
  const [barcodeScan, setBarcodeScan] = useState('');
  const barcodeRef = useRef(null);
  const serviceLines = form.items.filter((item) => item.productId && item.itemType === 'SERVICE');

  useEffect(() => {
    let ignore = false;
    const searchText = String(customerSearch || '').trim();
    if (searchText.length < 2 || selectedCustomer?.identificationNumber === searchText) {
      return undefined;
    }
    const timeoutId = window.setTimeout(() => {
      onSearchCustomers(searchText).then((customers) => {
        if (!ignore && customers.length === 1 && customers[0].identificationNumber === searchText) {
          onSelectCustomer(customers[0]);
        }
      });
    }, 250);
    return () => {
      ignore = true;
      window.clearTimeout(timeoutId);
    };
  }, [customerSearch, selectedCustomer, onSearchCustomers, onSelectCustomer]);

  useEffect(() => {
    barcodeRef.current?.focus();
  }, []);

  useEffect(() => {
    const scanText = String(barcodeScan || '').trim();
    if (scanText.length < 4) {
      return undefined;
    }
    const timeoutId = window.setTimeout(() => submitScan(scanText), 180);
    return () => window.clearTimeout(timeoutId);
  }, [barcodeScan]);

  async function submitScan(scanText = barcodeScan) {
    const normalized = String(scanText || '').trim();
    if (!normalized) {
      return;
    }
    await onScanBarcode(normalized);
    setBarcodeScan('');
    window.setTimeout(() => barcodeRef.current?.focus(), 0);
  }

  function updatePaymentMethod(paymentMethodCode) {
    setForm({
      ...form,
      paymentMethodCode,
      virtualWalletCode: paymentMethodCode === 'VIRTUAL_WALLET' ? form.virtualWalletCode || 'NEQUI' : '',
    });
  }

  function updateCustomerSearch(value) {
    setCustomerSearch(value);
    if (form.customerId) {
      setForm({ ...form, customerId: '' });
      onSelectCustomer(null);
    }
  }

  function updateBuyerMode(buyerIdentificationMode) {
    const finalConsumer = buyerIdentificationMode === 'FINAL_CONSUMER';
    setForm({
      ...form,
      buyerIdentificationMode,
      customerId: finalConsumer ? '' : form.customerId,
    });
    if (finalConsumer) {
      setCustomerSearch('');
      onSelectCustomer(null);
    }
  }

  return <div className="stack">
    <section className="tool-panel">
      <header className="panel-header">
        <h1>Ventas</h1>
        <div className="button-row">
          <button className="secondary" onClick={addItem} type="button">Agregar linea</button>
          <button className="primary" disabled={busy} onClick={onCreate} type="button">Crear venta</button>
        </div>
      </header>
      <div className="form-grid compact">
        <SelectField label="Comprador" value={form.buyerIdentificationMode} onChange={updateBuyerMode} options={[
          { value: 'IDENTIFIED_CUSTOMER', label: 'Cliente identificado' },
          { value: 'FINAL_CONSUMER', label: 'Consumidor final' },
        ]} />
        <div className="customer-search-field">
          <Field label="Cliente por numero de documento" value={customerSearch} onChange={updateCustomerSearch} placeholder="Escribe el documento del cliente" disabled={form.buyerIdentificationMode === 'FINAL_CONSUMER'} />
          {customerOptions.length > 0 && (
            <div className="customer-options" role="listbox" aria-label="Coincidencias de clientes">
              {customerOptions.map((customer) => (
                <button key={customer.id} className="customer-option" onClick={() => onSelectCustomer(customer)} type="button">
                  <b>{customer.identificationNumber}{customer.verificationDigit !== null && customer.verificationDigit !== undefined ? `-${customer.verificationDigit}` : ''}</b>
                  <span>{customer.businessName || customer.fullName || customer.tradeName || 'Cliente sin nombre'}</span>
                </button>
              ))}
            </div>
          )}
          {selectedCustomer && (
            <p className="selected-customer">Cliente seleccionado: {selectedCustomer.businessName || selectedCustomer.fullName || selectedCustomer.tradeName} ({selectedCustomer.identificationNumber})</p>
          )}
        </div>
        <SelectField label="Metodo de pago" value={form.paymentMethodCode} onChange={updatePaymentMethod} options={paymentOptions} />
        {form.paymentMethodCode === 'VIRTUAL_WALLET' && (
          <SelectField label="Billetera virtual" value={form.virtualWalletCode || 'NEQUI'} onChange={(value) => setForm({ ...form, virtualWalletCode: value })} options={walletOptions} />
        )}
        <Field label="Scanner codigo de barras" value={barcodeScan} onChange={setBarcodeScan} inputRef={barcodeRef} placeholder="Escanea aqui" autoComplete="off" onKeyDown={(event) => {
          if (event.key === 'Enter') {
            event.preventDefault();
            submitScan();
          }
        }} />
        <div className={saleId ? 'sale-state ready' : 'sale-state'}>
          <span>{saleId ? 'Venta pendiente de confirmacion' : 'Crea la venta para habilitar la confirmacion POS'}</span>
          {saleId && <code>{saleId}</code>}
        </div>
      </div>
      <div className="line-list">
        {form.items.map((item, index) => (
          <div className="line-row" key={`${index}-${item.productId}`}>
            <Field label="Producto" value={item.productId} onChange={(value) => updateItem(index, 'productId', value)} />
            <Field label="Nombre" value={item.productName || ''} onChange={() => {}} readOnly />
            <Field label="Cantidad" value={item.quantity} onChange={(value) => updateItem(index, 'quantity', value)} type="number" />
            <Field label="Precio" value={item.unitPrice} onChange={() => {}} type="number" readOnly />
            <Field label="Descuento" value={item.discountAmount} onChange={(value) => updateItem(index, 'discountAmount', value)} type="number" />
            <button className="icon-button" onClick={() => removeItem(index)} type="button" aria-label="Eliminar linea">X</button>
          </div>
        ))}
      </div>
      <button className="primary" disabled={busy || !saleId} onClick={onConfirm} type="button">Confirmar POS</button>
      {saleId && serviceLines.length > 0 && (
      <section className="service-consumption-panel">
        <header className="panel-header">
          <div>
            <h2>Consumo de insumos por servicio</h2>
            <p className="hint">Carga los insumos asociados y confirma solo las cantidades reales usadas.</p>
          </div>
        </header>
        <div className="button-row">
          {serviceLines.map((item) => (
            <button className="secondary" disabled={busy} key={item.productId} onClick={() => onLoadServiceConsumption(item.productId)} type="button">
              Cargar insumos de {item.productName || item.productId}
            </button>
          ))}
        </div>
        {serviceConsumption?.suggestions?.length > 0 && (
          <div className="service-consumption-editor">
            <Field label="Motivo del consumo" value={serviceConsumption.reason} onChange={onUpdateServiceConsumptionReason} />
            <div className="table-wrap">
              <table className="data-table">
                <thead>
                  <tr>
                    <th>Insumo</th>
                    <th>Stock actual</th>
                    <th>Costo</th>
                    <th>Cantidad usada</th>
                  </tr>
                </thead>
                <tbody>
                  {serviceConsumption.suggestions.map((suggestion) => (
                    <tr key={suggestion.supplyProductId}>
                      <td>
                        <b>{suggestion.supplyName}</b>
                        <span className="muted-block">{suggestion.supplySku}</span>
                        {suggestion.notes && <span className="muted-block">{suggestion.notes}</span>}
                      </td>
                      <td>{number(suggestion.currentStock)}</td>
                      <td>{money(suggestion.unitCost)}</td>
                      <td>
                        <input
                          min="0"
                          step="0.01"
                          type="number"
                          value={serviceConsumption.quantities[suggestion.supplyProductId] || ''}
                          onChange={(event) => onUpdateServiceConsumptionQuantity(suggestion.supplyProductId, event.target.value)}
                        />
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
            <button className="primary" disabled={busy} onClick={onConfirmServiceConsumption} type="button">Confirmar consumo de insumos</button>
          </div>
        )}
      </section>
      )}
    </section>
    <section className="tool-panel">
      <header className="panel-header">
        <div>
          <h1>Ventas registradas</h1>
          <p className="hint">Consulta ventas recientes y su estado operativo.</p>
        </div>
        <button className="secondary" disabled={busy} onClick={onLoadSales} type="button">Consultar ventas</button>
      </header>
      <div className="form-grid compact">
        <SelectField label="Estado" value={listFilters.saleStatus} onChange={(value) => setListFilters({ ...listFilters, saleStatus: value })} options={[
          { value: 'DRAFT', label: 'Borrador' },
          { value: 'CONFIRMED', label: 'Confirmada' },
          { value: 'VOIDED', label: 'Anulada' },
        ]} placeholder="Todos" />
        <Field label="Desde" value={listFilters.saleFrom} onChange={(value) => setListFilters({ ...listFilters, saleFrom: value })} type="date" />
        <Field label="Hasta" value={listFilters.saleTo} onChange={(value) => setListFilters({ ...listFilters, saleTo: value })} type="date" />
      </div>
      <DataTable
        columns={['Fecha', 'Estado', 'Cliente', 'Metodo pago', 'Subtotal', 'IVA', 'Total']}
        rows={sales.map(saleRow)}
        rowKey={(_row, index) => sales[index]?.id || index}
        emptyMessage="Sin ventas consultadas."
        sectionClassName="embedded-table"
      />
    </section>
  </div>;
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

function saleRow(sale) {
  return [
    shortDate(sale.createdAt || sale.saleDate),
    labelSaleStatus(sale.status),
    sale.customerId || sale.buyerIdentificationMode || 'Consumidor final',
    sale.paymentMethodCode || '',
    money(sale.subtotal),
    money(sale.taxTotal),
    money(sale.total),
  ];
}

function labelSaleStatus(value) {
  return {
    DRAFT: 'Borrador',
    CONFIRMED: 'Confirmada',
    VOIDED: 'Anulada',
  }[value] || value || '';
}

function shortDate(value) {
  return value ? String(value).slice(0, 10) : '';
}
