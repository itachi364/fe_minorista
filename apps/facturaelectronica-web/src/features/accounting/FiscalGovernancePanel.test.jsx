import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import { afterEach, expect, test, vi } from 'vitest';
import { FiscalGovernancePanel } from './FiscalGovernancePanel.jsx';

afterEach(cleanup);

test('validates a CSV before enabling the synchronous import', async () => {
  const onValidateCsv = vi.fn().mockResolvedValue({ valid: true, rowCount: 1, packageCount: 1, errors: [] });
  const onImportCsv = vi.fn().mockResolvedValue([{ id: 'package-1' }]);
  render(<FiscalGovernancePanel
    sources={[]}
    warnings={[]}
    packages={[]}
    locations={[{ departmentCode: '11', departmentName: 'Bogota', municipalities: [{ code: '11001', name: 'Bogota, D.C.' }] }]}
    onCreateSource={vi.fn()}
    onAddEvent={vi.fn()}
    onCreatePackage={vi.fn()}
    onValidateCsv={onValidateCsv}
    onImportCsv={onImportCsv}
    onPublish={vi.fn()}
    busy={false}
  />);
  const file = new File(['municipalityDivipolaCode'], 'reteica.csv', { type: 'text/csv' });

  fireEvent.change(screen.getByLabelText('Archivo CSV delimitado por comas'), { target: { files: [file] } });
  expect(screen.getByRole('button', { name: 'Importar borradores' })).toBeDisabled();
  fireEvent.click(screen.getByRole('button', { name: 'Validar CSV' }));

  await waitFor(() => expect(screen.getByText('VALIDO')).toBeInTheDocument());
  fireEvent.click(screen.getByRole('button', { name: 'Importar borradores' }));
  await waitFor(() => expect(onImportCsv).toHaveBeenCalledWith(file));
});

test('publishes only draft municipal packages', () => {
  const onPublish = vi.fn();
  render(<FiscalGovernancePanel
    sources={[]}
    warnings={[]}
    packages={[{
      id: 'package-1', municipalityCode: '11001', municipalityName: 'Bogota, D.C.', code: 'BOG-RETEICA',
      version: '2026-1', validFrom: '2026-01-01', validTo: null, ruleCount: 1, status: 'DRAFT',
    }]}
    locations={[]}
    onCreateSource={vi.fn()}
    onAddEvent={vi.fn()}
    onCreatePackage={vi.fn()}
    onValidateCsv={vi.fn()}
    onImportCsv={vi.fn()}
    onPublish={onPublish}
    busy={false}
  />);

  fireEvent.click(screen.getByRole('button', { name: 'Publicar' }));
  expect(onPublish).toHaveBeenCalledWith('package-1');
});
