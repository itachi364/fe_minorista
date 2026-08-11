export function findLocationByMunicipality(municipalityCode, locations = []) {
  const availableLocations = Array.isArray(locations) ? locations : [];
  if (availableLocations.length === 0) {
    return null;
  }
  for (const department of availableLocations) {
    const municipality = department.municipalities.find((item) => item.code === municipalityCode);
    if (municipality) {
      return { department, municipality };
    }
  }
  return { department: availableLocations[0], municipality: availableLocations[0].municipalities[0] };
}
