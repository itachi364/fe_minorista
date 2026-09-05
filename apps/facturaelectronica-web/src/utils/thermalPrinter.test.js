import { describe, expect, test } from 'vitest';
import { canUseWebSerial, printReceiptWithThermalPrinter } from './thermalPrinter.js';

describe('thermalPrinter', () => {
  test('reports unavailable when browser does not expose WebSerial', async () => {
    expect(canUseWebSerial()).toBe(false);

    const printed = await printReceiptWithThermalPrinter(new Blob(['<html><body>Venta</body></html>']));

    expect(printed).toBe(false);
  });
});
