import { useEffect, useRef, useState } from 'react';
import { ActionModal } from '../../components/Modal.jsx';
import { Field, SelectField } from '../../components/forms.jsx';
import { calculateSaleTotals, calculateTaxAddedAmounts } from '../../utils/taxCalculations.js';

export function SaleForm({ form, setForm, saleId, customerSearch, setCustomerSearch, customerOptions, selectedCustomer, onSearchCustomers, onSelectCustomer, updateItem, removeItem, onClose, onPrintReceipt, onScanBarcode, serviceConsumption, onLoadServiceConsumption, onUpdateServiceConsumptionQuantity, onUpdateServiceConsumptionReason, onConfirmServiceConsumption, fiscalPolicy, documentOverride, overrideForm = {}, setOverrideForm = () => {}, fiscalDocumentTypeOptions = [], authorizerOptions = [], onLoadAuthorizers, onRequestDocumentOverride, busy, paymentOptions = [], walletOptions = [] }) {
  const [barcodeScan, setBarcodeScan] = useState('');
  const [overrideModalOpen, setOverrideModalOpen] = useState(false);
  const barcodeRef = useRef(null);
  const lastSubmittedScanRef = useRef({ value: '', at: 0 });
  const serviceLines = form.items.filter((item) => item.productId && item.itemType === 'SERVICE');
  const totals = calculateSaleTotals(form.items);
  const saleDocumentOptions = fiscalDocumentTypeOptions.filter((option) => ['ELECTRONIC_INVOICE', 'ELECTRONIC_POS'].includes(option.value));
  const defaultDocumentType = fiscalPolicy?.defaultSaleDocumentType || 'ELECTRONIC_INVOICE';
  const effectiveDocumentType = documentOverride?.documentType || defaultDocumentType;
  const canRequestOverride = Boolean(onRequestDocumentOverride) && fiscalPolicy?.allowDocumentTypeOverride !== false && saleDocumentOptions.length > 1;

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
    const now = Date.now();
    if (lastSubmittedScanRef.current.value === normalized && now - lastSubmittedScanRef.current.at < 350) {
      return;
    }
    lastSubmittedScanRef.current = { value: normalized, at: now };
    await onScanBarcode(normalized);
    setBarcodeScan('');
    window.setTimeout(() => barcodeRef.current?.focus(), 0);
  }

  function submitClose(event) {
    event.preventDefault();
    onClose();
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

  return <>
    <form className="stack" onSubmit={submitClose}>
      <section className="tool-panel">
      <header className="panel-header">
        <h1>Ventas</h1>
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
        <div className={saleId || documentOverride ? 'sale-state ready' : 'sale-state'}>
          <span>Documento fiscal: {labelFiscalDocument(effectiveDocumentType, saleDocumentOptions)}</span>
          <small>{documentOverride ? 'Cambio autorizado solo para esta venta.' : 'Agrega productos y cierra la venta en un solo paso.'}</small>
          {saleId && <code>{saleId}</code>}
        </div>
      </div>
      <div className="line-list">
        {form.items.map((item, index) => (
          <SaleLineRow
            key={`${index}-${item.productId}`}
            item={item}
            index={index}
            updateItem={updateItem}
            removeItem={removeItem}
          />
        ))}
      </div>
      <div className="sale-summary">
        <span><b>Subtotal</b>{money(totals.subtotal)}</span>
        <span><b>IVA</b>{money(totals.tax)}</span>
        <span><b>Total</b>{money(totals.total)}</span>
      </div>
      <div className="button-row">
        {canRequestOverride && (
          <button className="secondary" disabled={busy} onClick={() => {
            Promise.resolve(onLoadAuthorizers?.()).catch(() => undefined);
            setOverrideModalOpen(true);
          }} type="button">
            Solicitar cambio de documento fiscal
          </button>
        )}
        <button className="primary" disabled={busy} type="submit">Cerrar venta</button>
        {saleId && <button className="secondary" disabled={busy} onClick={() => onPrintReceipt(saleId)} type="button">Imprimir comprobante</button>}
      </div>
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
    </form>
    {overrideModalOpen && (
      <SaleDocumentOverrideModal
        form={overrideForm}
        setForm={setOverrideForm}
        documentOptions={saleDocumentOptions}
        authorizerOptions={authorizerOptions}
        currentDocumentType={effectiveDocumentType}
        onSubmit={async (payload) => {
          await onRequestDocumentOverride(payload);
          setOverrideModalOpen(false);
        }}
        onClose={() => setOverrideModalOpen(false)}
        busy={busy}
      />
    )}
  </>;
}

function SaleDocumentOverrideModal({ form, setForm, documentOptions, authorizerOptions = [], currentDocumentType, onSubmit, onClose, busy }) {
  const availableDocumentOptions = documentOptions.filter((option) => option.value !== currentDocumentType);
  const selectedDocumentType = availableDocumentOptions.some((option) => option.value === form.documentType)
    ? form.documentType
    : availableDocumentOptions[0]?.value || '';
  const authorizers = authorizerOptions
    .filter((user) => user?.id)
    .map((user) => ({
      id: user.id,
      label: `${user.fullName || 'Usuario'} - ${user.email || user.id}`,
      email: user.email || '',
    }));
  const pinIsValid = /^\d{6}$/.test(form.pin || '');
  const reasonIsValid = String(form.reason || '').trim().length >= 8;
  const disabled = busy || !selectedDocumentType || !pinIsValid || !reasonIsValid;

  function updatePin(value) {
    setForm({ ...form, pin: String(value || '').replace(/\D/g, '').slice(0, 6) });
  }

  function updateAuthorizer(value) {
    const selected = authorizers.find((authorizer) => [authorizer.id, authorizer.email, authorizer.label]
      .filter(Boolean)
      .some((candidate) => candidate.toLowerCase() === String(value || '').toLowerCase()));
    setForm({
      ...form,
      authorizedBy: selected?.id || '',
      authorizedBySearch: value,
    });
  }

  async function submit(event) {
    event.preventDefault();
    if (disabled) {
      return;
    }
    await onSubmit({
      documentType: selectedDocumentType,
      authorizedBy: form.authorizedBy || null,
      pin: form.pin,
      reason: String(form.reason || '').trim(),
    });
  }

  return <ActionModal title="Solicitar cambio de documento fiscal" onClose={onClose} size="medium">
    <form className="modal-section" onSubmit={submit}>
      <p className="hint">El cambio aplica solo a esta venta y requiere autorizacion operacional con PIN de administrador.</p>
      <div className="modal-form-grid">
        <SelectField label="Documento fiscal destino" value={selectedDocumentType} onChange={(value) => setForm({ ...form, documentType: value })} options={availableDocumentOptions} />
        <Field label="Usuario autorizador" value={form.authorizedBySearch || ''} onChange={updateAuthorizer} list="sale-authorizer-options" placeholder="Opcional si autoriza el usuario actual" autoComplete="off" />
        <datalist id="sale-authorizer-options">
          {authorizers.map((authorizer) => (
            <option key={authorizer.id} value={authorizer.email || authorizer.id}>{authorizer.label}</option>
          ))}
        </datalist>
        <Field label="PIN operacional" value={form.pin || ''} onChange={updatePin} type="password" maxLength={6} inputMode="numeric" pattern="[0-9]*" error={form.pin && !pinIsValid ? 'El PIN debe tener exactamente 6 digitos.' : ''} />
        <Field label="Motivo del cambio" value={form.reason || ''} onChange={(value) => setForm({ ...form, reason: value })} error={form.reason && !reasonIsValid ? 'Indica un motivo de minimo 8 caracteres.' : ''} />
      </div>
      <div className="modal-actions">
        <button className="secondary" disabled={busy} onClick={onClose} type="button">Cancelar</button>
        <button className="primary" disabled={disabled} type="submit">Autorizar cambio</button>
      </div>
    </form>
  </ActionModal>;
}

function SaleLineRow({ item, index, updateItem, removeItem }) {
  const quantity = Number(item.quantity || 0);
  const amounts = calculateTaxAddedAmounts(Number(item.unitPrice || 0) * quantity - Number(item.discountAmount || 0), item.taxRate);
  return (
          <div className="line-row" key={`${index}-${item.productId}`}>
            <Field label="Producto" value={item.productId} onChange={(value) => updateItem(index, 'productId', value)} />
            <Field label="Nombre" value={item.productName || ''} onChange={() => {}} readOnly />
            <Field label="Cantidad" value={item.quantity} onChange={(value) => updateItem(index, 'quantity', value)} type="number" />
            <Field label="Precio sin IVA" value={item.unitPrice} onChange={() => {}} type="number" readOnly />
            <Field label="IVA" value={amounts.tax ? String(amounts.tax) : ''} onChange={() => {}} type="number" readOnly />
            <Field label="Total" value={amounts.total ? String(amounts.total) : ''} onChange={() => {}} type="number" readOnly />
            <Field label="Descuento" value={item.discountAmount} onChange={(value) => updateItem(index, 'discountAmount', value)} type="number" />
            <button className="icon-button" onClick={() => removeItem(index)} type="button" aria-label="Eliminar linea">X</button>
          </div>
  );
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

function labelFiscalDocument(value, options) {
  const option = options.find((item) => item.value === value);
  if (option) {
    return option.label;
  }
  const labels = {
    ELECTRONIC_INVOICE: 'Factura electronica de venta',
    ELECTRONIC_POS: 'POS electronico',
  };
  return labels[value] || value || 'Sin definir';
}
