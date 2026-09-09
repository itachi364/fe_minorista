import { describe, expect, it } from 'vitest';
import { deriveNationalTaxFlags } from './companyTaxProfileRules.js';

describe('deriveNationalTaxFlags', () => {
  it('derives national fiscal statuses from RUT responsibilities', () => {
    expect(deriveNationalTaxFlags(['O-07', 'O-13', 'O-23', 'O-48', 'O-59'], 'ORDINARIO')).toEqual({
      vatResponsible: true,
      withholdingAgent: true,
      vatWithholdingAgent: true,
      largeTaxpayer: true,
      selfWithholding: true,
      simpleRegime: false,
    });
  });

  it('derives SIMPLE from either the regime or responsibility 47', () => {
    expect(deriveNationalTaxFlags([], 'SIMPLE').simpleRegime).toBe(true);
    expect(deriveNationalTaxFlags(['47'], 'ORDINARIO').simpleRegime).toBe(true);
  });
});
