import { describe, expect, test } from 'vitest';
import { buildProductPayload } from './payloadBuilders.js';
import { calculateSaleTotals, calculateTaxAddedAmounts, calculateTaxIncludedAmounts } from './taxCalculations.js';

describe('tax calculations', () => {
  test('extracts base and tax from a final price with VAT included', () => {
    expect(calculateTaxIncludedAmounts('6000', '19')).toEqual({
      base: 5042.02,
      tax: 957.98,
      total: 6000,
    });
  });

  test('calculates totals from sale lines using base price and tax rate', () => {
    expect(calculateSaleTotals([
      { quantity: '2', unitPrice: '5042.02', discountAmount: '0', taxRate: '19' },
      { quantity: '1', unitPrice: '1000', discountAmount: '100', taxRate: '0' },
    ])).toEqual({
      subtotal: 10984.04,
      tax: 1915.97,
      total: 12900.01,
    });
  });

  test('adds tax to a base amount for inventory listings', () => {
    expect(calculateTaxAddedAmounts('5042.02', '19')).toEqual({
      base: 5042.02,
      tax: 957.98,
      total: 6000,
    });
  });
});

describe('product payload', () => {
  test('sends salePrice as base price when user captures final price', () => {
    expect(buildProductPayload({
      sku: 'SKU-IVA',
      barcode: '123456789',
      name: 'Cafe',
      itemType: 'PHYSICAL_GOOD',
      saleEnabled: true,
      purchaseEnabled: true,
      stockTracked: true,
      finalSalePrice: '6000',
      salePrice: '',
      cost: '3000',
      initialStock: '5',
      taxCategoryCode: 'IVA',
      taxCode: 'IVA_19',
      taxLabel: 'IVA 19%',
      taxRate: '19',
    })).toMatchObject({
      salePrice: 5042.02,
      cost: 3000,
      initialStock: 5,
      taxRate: 19,
    });
  });
});
