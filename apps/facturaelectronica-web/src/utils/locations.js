import { colombiaLocations } from '../data/divipola.js';

export function findLocationByMunicipality(municipalityCode, locations = colombiaLocations) {
  const availableLocations = locations.length > 0 ? locations : colombiaLocations;
  for (const department of availableLocations) {
    const municipality = department.municipalities.find((item) => item.code === municipalityCode);
    if (municipality) {
      return { department, municipality };
    }
  }
  return { department: availableLocations[0], municipality: availableLocations[0].municipalities[0] };
}
