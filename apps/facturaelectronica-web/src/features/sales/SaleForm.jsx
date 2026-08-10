import { useEffect } from 'react';
import { Field, SelectField } from '../../components/forms.jsx';

export function SaleForm({ form, setForm, saleId, customerSearch, setCustomerSearch, customerOptions, selectedCustomer, onSearchCustomers, onSelectCustomer, updateItem, addItem, removeItem, onCreate, onConfirm, busy, paymentOptions = [], walletOptions = [] }) {
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

  return <section className="tool-panel">
    <header className="panel-header">
      <h1>Venta POS</h1>
      <div className="button-row">
        <button className="secondary" onClick={addItem} type="button">Agregar linea</button>
        <button className="primary" disabled={busy} onClick={onCreate} type="button">Crear venta</button>
      </div>
    </header>
    <div className="form-grid compact">
      <div className="customer-search-field">
        <Field label="Cliente por numero de documento" value={customerSearch} onChange={updateCustomerSearch} placeholder="Escribe el documento del cliente" />
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
      <SelectField label="Canal" value={form.saleChannel} onChange={(value) => setForm({ ...form, saleChannel: value })} options={['POS', 'ELECTRONIC_INVOICE']} />
      <SelectField label="Metodo de pago" value={form.paymentMethodCode} onChange={updatePaymentMethod} options={paymentOptions} />
      {form.paymentMethodCode === 'VIRTUAL_WALLET' && (
        <SelectField label="Billetera virtual" value={form.virtualWalletCode || 'NEQUI'} onChange={(value) => setForm({ ...form, virtualWalletCode: value })} options={walletOptions} />
      )}
      <div className={saleId ? 'sale-state ready' : 'sale-state'}>
        <span>{saleId ? 'Venta pendiente de confirmacion' : 'Crea la venta para habilitar la confirmacion POS'}</span>
        {saleId && <code>{saleId}</code>}
      </div>
    </div>
    <div className="line-list">
      {form.items.map((item, index) => (
        <div className="line-row" key={`${index}-${item.productId}`}>
          <Field label="Producto" value={item.productId} onChange={(value) => updateItem(index, 'productId', value)} />
          <Field label="Cantidad" value={item.quantity} onChange={(value) => updateItem(index, 'quantity', value)} type="number" />
          <Field label="Precio" value={item.unitPrice} onChange={(value) => updateItem(index, 'unitPrice', value)} type="number" />
          <Field label="Descuento" value={item.discountAmount} onChange={(value) => updateItem(index, 'discountAmount', value)} type="number" />
          <Field label="Impuesto" value={item.taxCode} onChange={(value) => updateItem(index, 'taxCode', value)} />
          <Field label="Tasa" value={item.taxRate} onChange={(value) => updateItem(index, 'taxRate', value)} type="number" />
          <button className="icon-button" onClick={() => removeItem(index)} type="button" aria-label="Eliminar linea">X</button>
        </div>
      ))}
    </div>
    <button className="primary" disabled={busy || !saleId} onClick={onConfirm} type="button">Confirmar POS</button>
  </section>;
}
