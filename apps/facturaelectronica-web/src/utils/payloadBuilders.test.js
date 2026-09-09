import { describe, expect, test } from 'vitest';
import {
  buildCompanyPayload,
  buildInvoicingObligationPayload,
  buildExpensePayload,
  buildPurchasePayload,
  buildThirdPartyPayload,
} from './payloadBuilders.js';

test('builds invoicing obligation only from source data', () => {
  const result = buildInvoicingObligationPayload({
    personType: 'NATURAL',
    rutGeneratedAt: '2026-09-01',
    economicOperationTypes: ['TAXED_GOODS_SALE'],
    customsUser: false,
    establishmentCount: '1',
    exploitsIntangibles: false,
    onlyExcludedOrUntaxedOperations: false,
    previousYearGrossActivityIncome: '90000000',
    currentYearGrossActivityIncome: '45000000',
    previousYearTaxedActivityFinancialOperations: '80000000',
    currentYearTaxedActivityFinancialOperations: '40000000',
    largestPreviousYearTaxedContract: '30000000',
    largestCurrentYearTaxedContract: '25000000',
    largestSameCustomerAggregate: '25000000',
    voluntaryElectronicInvoicer: false,
    specialExceptionType: '',
    specialExceptionScope: '',
    rutAssetId: '',
  }, { taxRegime: 'ORDINARIO', rutResponsibilities: ['49'], ciiuCodes: ['4711'] }, 'asset-id');

  expect(result).toMatchObject({
    personType: 'NATURAL',
    taxRegime: 'ORDINARIO',
    establishmentCount: 1,
    previousYearGrossActivityIncome: 90000000,
    rutResponsibilityCodes: ['49'],
    ciiuCodes: ['4711'],
    rutAssetId: 'asset-id',
  });
  expect(result.status).toBeUndefined();
});

describe('fiscal form payloads', () => {
  test('requires and nests the company tax profile', () => {
    const company = {
      legalName: 'Empresa Demo SAS',
      identificationTypeCode: 31,
      identificationNumber: '900123456',
      email: 'admin@example.com',
    };
    const taxProfile = {
      companySize: 'MICRO',
      financialReportingGroup: 'GRUPO_3',
      taxRegime: 'RESPONSABLE_IVA',
      rutResponsibilities: ['O-13'],
      ciiuCodes: ['4711'],
      withholdingAgent: true,
    };

    expect(buildCompanyPayload(company, taxProfile)).toMatchObject({
      identificationNumber: '900123456',
      taxProfile,
    });
    expect(() => buildCompanyPayload(company, { ...taxProfile, taxRegime: '' }))
      .toThrow('Selecciona el regimen tributario de la empresa.');
  });

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
