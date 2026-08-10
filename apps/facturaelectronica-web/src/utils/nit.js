const WEIGHTS_RIGHT_TO_LEFT = [3, 7, 13, 17, 19, 23, 29, 37, 41, 43, 47, 53, 59, 67, 71];

export function onlyDigits(value) {
  return String(value || '').replace(/\D/g, '');
}

export function calculateNitVerificationDigit(documentNumber) {
  const normalized = onlyDigits(documentNumber);
  if (!normalized || normalized.length > WEIGHTS_RIGHT_TO_LEFT.length) {
    return '';
  }
  let sum = 0;
  for (let index = normalized.length - 1, weightIndex = 0; index >= 0; index -= 1, weightIndex += 1) {
    sum += Number(normalized[index]) * WEIGHTS_RIGHT_TO_LEFT[weightIndex];
  }
  const residue = sum % 11;
  if (residue === 0) {
    return '0';
  }
  if (residue === 1) {
    return '1';
  }
  return String(11 - residue);
}

export function isNit(identificationTypeCode) {
  return Number(identificationTypeCode) === 31;
}
