import { cleanup, fireEvent, render, screen, within } from '@testing-library/react';
import { afterEach, describe, expect, test, vi } from 'vitest';
import { FiscalCompliancePanel } from './FiscalCompliancePanel.jsx';

const ACCOUNT = { id: 'account-1', code: '236540', name: 'Retencion en la fuente por pagar', active: true };
const SUPPLIER = { id: 'supplier-1', businessName: 'Proveedor SAS', identificationNumber: '900123456', active: true };

afterEach(cleanup);

describe('FiscalCompliancePanel', () => {
  test('saves an account mapping using catalog account codes', () => {
    const onSaveMapping = vi.fn();
    renderPanel({ onSaveMapping });

    fireEvent.click(screen.getByRole('button', { name: 'Cuenta por pagar' }));
    fireEvent.click(screen.getByRole('option', { name: '236540 - Retencion en la fuente por pagar' }));
    fireEvent.click(screen.getByRole('button', { name: 'Guardar mapeo' }));

    expect(onSaveMapping).toHaveBeenCalledWith(expect.objectContaining({
      withholdingType: 'RETEFUENTE', payableAccountCode: '236540', validTo: null,
    }));
  });

  test('loads and closes the selected fiscal period', () => {
    const onLoadPeriod = vi.fn();
    const onClosePeriod = vi.fn();
    renderPanel({ onLoadPeriod, onClosePeriod });
    const section = screen.getByText('Cierre y conciliacion fiscal').closest('section');

    fireEvent.change(within(section).getByLabelText('Ano'), { target: { value: '2025' } });
    fireEvent.change(within(section).getByLabelText('Mes'), { target: { value: '12' } });
    fireEvent.click(within(section).getByRole('button', { name: 'Consultar' }));
    fireEvent.click(within(section).getByRole('button', { name: 'Cerrar periodo' }));

    expect(onLoadPeriod).toHaveBeenCalledWith(2025, 12);
    expect(onClosePeriod).toHaveBeenCalledWith(2025, 12);
  });

  test('generates and downloads an immutable supplier certificate version', () => {
    const onGenerateCertificate = vi.fn();
    const onDownloadCertificate = vi.fn();
    renderPanel({
      onGenerateCertificate,
      onDownloadCertificate,
      certificates: [{ id: 'certificate-1', year: 2025, version: 2, taxableBaseTotal: 100000,
        withheldTotal: 4000, generatedAt: '2026-01-10T12:00:00Z' }],
    });

    fireEvent.click(screen.getByRole('button', { name: 'Proveedor' }));
    fireEvent.click(screen.getByRole('option', { name: 'Proveedor SAS - 900123456' }));
    fireEvent.change(screen.getByLabelText('Ano gravable'), { target: { value: '2025' } });
    fireEvent.click(screen.getByRole('button', { name: 'Generar' }));
    fireEvent.click(screen.getByRole('button', { name: 'Descargar CSV' }));

    expect(onGenerateCertificate).toHaveBeenCalledWith('supplier-1', 2025);
    expect(onDownloadCertificate).toHaveBeenCalledWith('certificate-1');
  });
});

function renderPanel(overrides = {}) {
  return render(<FiscalCompliancePanel
    mappings={[]}
    accounts={[ACCOUNT]}
    suppliers={[SUPPLIER]}
    certificates={[]}
    onSaveMapping={vi.fn()}
    onLoadPeriod={vi.fn()}
    onClosePeriod={vi.fn()}
    onLoadCertificates={vi.fn()}
    onGenerateCertificate={vi.fn()}
    onDownloadCertificate={vi.fn()}
    busy={false}
    {...overrides}
  />);
}
