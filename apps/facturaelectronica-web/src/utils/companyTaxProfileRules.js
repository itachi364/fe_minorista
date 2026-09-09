export function deriveNationalTaxFlags(responsibilities = [], taxRegime = '') {
  const codes = new Set((responsibilities || []).map((value) => String(value).trim().toUpperCase()));
  const has = (code) => codes.has(code) || codes.has(`O-${code}`);
  return {
    vatResponsible: has('48'),
    withholdingAgent: has('07'),
    vatWithholdingAgent: has('23'),
    largeTaxpayer: has('13'),
    selfWithholding: has('15') || has('59'),
    simpleRegime: String(taxRegime).trim().toUpperCase() === 'SIMPLE' || has('47'),
  };
}
