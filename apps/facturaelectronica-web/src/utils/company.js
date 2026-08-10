export function companyLabel(company) {
  if (!company) return 'Sin empresa seleccionada';
  const id = company.id || company.companyId;
  return `${company.legalName || company.tradeName || id} (${company.identificationNumber || id})`;
}

export function buildIssuerFromCompany(company, currentIssuer) {
  if (!company) return currentIssuer;
  return {
    ...currentIssuer,
    legalName: company.legalName || currentIssuer.legalName,
    nit: company.identificationNumber || currentIssuer.nit,
    verificationDigit: company.verificationDigit || currentIssuer.verificationDigit,
  };
}
