import { describe, expect, test } from 'vitest';
import {
  buildExpensePayload,
  buildPurchasePayload,
  buildThirdPartyPayload,
} from './payloadBuilders.js';

describe('fiscal form payloads', () => {
  test('keeps multiple CIIU activities for a juridical supplier', () => {
    const payload = buildThirdPartyPayload({
      thirdPartyType: 'SUPPLIER',
      personType: 'JURIDICA',
      identificationTypeCode: 31,
      identificationNumber: '900123456',
      businessName: 'Proveedor SAS',
      ciiuCodes: ['6201', '4711'],
      taxResponsibilities: ['O-13'],
      taxRegime: 'ORDINARIO',
    });

    expect(payload.ciiuCodes).toEqual(['6201', '4711']);
    expect(payload).not.toHaveProperty('ciiuCode');
  });

  test('removes CIIU activities from a simple natural customer', () => {
    const payload = buildThirdPartyPayload({
      thirdPartyType: 'CUSTOMER',
      personType: 'NATURAL',
      identificationTypeCode: 13,
      identificationNumber: '1234567890',
      fullName: 'Cliente Natural',
      ciiuCodes: ['6201'],
    }, '11001');

    expect(payload.ciiuCodes).toEqual([]);
  });

  test('only sends payment deadline for credit purchases and expenses', () => {
    const purchase = buildPurchasePayload({
      paymentCondition: 'CASH',
      dueDate: '2026-10-01',
      lines: [],
    });
    const expense = buildExpensePayload({
      expenseType: 'OPERATING_EXPENSE',
      paymentCondition: 'CASH',
      dueDate: '2026-10-01',
      total: '1000',
    });

    expect(purchase).not.toHaveProperty('dueDate');
    expect(expense).not.toHaveProperty('dueDate');
    expect(buildExpensePayload({ ...expense, paymentCondition: 'CREDIT', dueDate: '2026-10-01' }).dueDate)
      .toBe('2026-10-01');
  });
});
