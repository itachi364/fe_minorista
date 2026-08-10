package com.msvanegasg.facturaelectronica.billing.domain.model;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public final class TaxResponsibilityCatalog {

    private static final String NO_APLICA = "R-99-PN";
    private static final Set<String> ALLOWED_CODES = Set.of("O-13", "O-15", "O-23", "O-47", NO_APLICA);

    private TaxResponsibilityCatalog() {
    }

    public static List<String> normalize(List<String> codes) {
        if (codes == null || codes.isEmpty()) {
            return List.of();
        }
        LinkedHashSet<String> normalized = new LinkedHashSet<>();
        for (String code : codes) {
            if (code == null || code.isBlank()) {
                continue;
            }
            String normalizedCode = code.trim().toUpperCase();
            if (!ALLOWED_CODES.contains(normalizedCode)) {
                throw new IllegalArgumentException("taxResponsibilities contains unsupported DIAN code");
            }
            normalized.add(normalizedCode);
        }
        if (normalized.contains(NO_APLICA) && normalized.size() > 1) {
            throw new IllegalArgumentException("R-99-PN cannot be combined with other tax responsibilities");
        }
        return List.copyOf(normalized);
    }
}
