export function toNumber(value, fallback = 0) {
  if (value === '' || value === null || value === undefined) {
    return fallback;
  }
  const parsed = Number(value);
  return Number.isFinite(parsed) ? parsed : fallback;
}

export function roundMoney(value) {
  return Math.round((toNumber(value) + Number.EPSILON) * 100) / 100;
}

export function calculateTaxIncludedAmounts(finalPrice, taxRate) {
  const total = roundMoney(finalPrice);
  const rate = toNumber(taxRate);
  if (total <= 0) {
    return { base: 0, tax: 0, total: 0 };
  }
  if (rate <= 0) {
    return { base: total, tax: 0, total };
  }
  const base = roundMoney(total / (1 + rate / 100));
  return { base, tax: roundMoney(total - base), total };
}

export function calculateTaxAddedAmounts(baseAmount, taxRate) {
  const base = roundMoney(baseAmount);
  const rate = toNumber(taxRate);
  const tax = rate <= 0 ? 0 : roundMoney(base * (rate / 100));
  return { base, tax, total: roundMoney(base + tax) };
}

export function calculateSaleTotals(items = []) {
  return items.reduce((summary, item) => {
    const quantity = toNumber(item.quantity);
    const unitBase = toNumber(item.unitPrice);
    const discount = toNumber(item.discountAmount);
    const rate = toNumber(item.taxRate);
    const grossBase = roundMoney(quantity * unitBase);
    const taxableBase = Math.max(0, roundMoney(grossBase - discount));
    const tax = rate <= 0 ? 0 : roundMoney(taxableBase * (rate / 100));
    return {
      subtotal: roundMoney(summary.subtotal + taxableBase),
      tax: roundMoney(summary.tax + tax),
      total: roundMoney(summary.total + taxableBase + tax),
    };
  }, { subtotal: 0, tax: 0, total: 0 });
}
