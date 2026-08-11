import { findLocationByMunicipality } from '../utils/locations.js';

export function MunicipalityFields({ municipalityCode, onChange, disabled = false, locations = [] }) {
  const availableLocations = Array.isArray(locations) ? locations : [];
  const current = findLocationByMunicipality(municipalityCode, availableLocations);
  if (!current) {
    return <>
      <label>
        Departamento
        <select value="" disabled>
          <option value="">Catalogo no disponible</option>
        </select>
      </label>
      <label>
        Municipio / ciudad
        <select value="" disabled>
          <option value="">Catalogo no disponible</option>
        </select>
      </label>
    </>;
  }
  const departmentCode = current.department.departmentCode;
  const municipalities = current.department.municipalities;
  return <>
    <label>
      Departamento
      <select value={departmentCode} onChange={(event) => onChange(availableLocations.find((department) => department.departmentCode === event.target.value)?.municipalities[0]?.code || municipalityCode)} disabled={disabled}>
        {availableLocations.map((department) => <option key={department.departmentCode} value={department.departmentCode}>{department.departmentName}</option>)}
      </select>
    </label>
    <label>
      Municipio / ciudad
      <select value={current.municipality.code} onChange={(event) => onChange(event.target.value)} disabled={disabled}>
        {municipalities.map((municipality) => <option key={municipality.code} value={municipality.code}>{municipality.name}</option>)}
      </select>
    </label>
  </>;
}
